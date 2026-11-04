package br.com.petcare.api.service;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.StatusConsulta;
import br.com.petcare.api.exception.ConflitoException;
import br.com.petcare.api.exception.RegraNegocioException;
import br.com.petcare.api.exception.ResourceNotFoundException;
import br.com.petcare.api.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agenda da clínica: atendimentos de 30 minutos, das 08:00 às 18:00, um por horário.
 * A regra é garantida em duas camadas: checagem na aplicação (mensagem amigável) e índice único
 * parcial no banco (migration V2), que impede a condição de corrida entre requisições simultâneas.
 */
@Service
@RequiredArgsConstructor
public class ConsultaService {

    private static final int DURACAO_MINUTOS = 30;
    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(18, 0);
    private static final String MSG_HORARIO_OCUPADO =
            "Este horário já está ocupado. Escolha outro horário disponível.";

    private final ConsultaRepository consultaRepository;
    private final PetService petService;
    private final AutorizacaoService autorizacaoService;

    public List<Consulta> listar(Usuario usuarioLogado) {
        if (usuarioLogado.isClinica()) {
            return consultaRepository.findAll();
        }
        return consultaRepository.findByPetTutorId(usuarioLogado.getId());
    }

    public Consulta buscarPorId(Usuario usuarioLogado, Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + id));
        autorizacaoService.validarPosse(usuarioLogado, consulta.getPet().getTutor().getId(), "esta consulta");
        return consulta;
    }

    /**
     * FLUXO COMPLETO (não é CRUD simples): agendamento de consulta com validação de horário de
     * atendimento e de conflito com outras consultas ativas da clínica.
     */
    @Transactional
    public Consulta agendar(Usuario usuarioLogado, ConsultaDto.Request request) {
        Pet pet = petService.buscarPorId(usuarioLogado, request.petId());
        validarHorarioDeAtendimento(request.dataHora());
        exigirHorarioLivre(request.dataHora(), null);

        Consulta consulta = Consulta.builder()
                .pet(pet)
                .dataHora(request.dataHora())
                .motivo(request.motivo())
                .observacoes(request.observacoes())
                .status(StatusConsulta.AGENDADA)
                .build();

        return salvarGarantindoHorarioUnico(consulta);
    }

    @Transactional
    public Consulta atualizar(Usuario usuarioLogado, Long id, ConsultaDto.Request request) {
        Consulta consulta = buscarPorId(usuarioLogado, id);
        exigirAgendada(consulta, "alterada");
        validarHorarioDeAtendimento(request.dataHora());
        exigirHorarioLivre(request.dataHora(), consulta.getId());

        consulta.setDataHora(request.dataHora());
        consulta.setMotivo(request.motivo());
        consulta.setObservacoes(request.observacoes());
        return salvarGarantindoHorarioUnico(consulta);
    }

    public Consulta marcarComoRealizada(Usuario usuarioLogado, Long id) {
        Consulta consulta = buscarPorId(usuarioLogado, id);
        exigirAgendada(consulta, "marcada como realizada");
        consulta.setStatus(StatusConsulta.REALIZADA);
        return consultaRepository.save(consulta);
    }

    /** Cancelamento lógico: o registro fica no histórico e o horário volta a ficar livre. */
    public void cancelar(Usuario usuarioLogado, Long id) {
        Consulta consulta = buscarPorId(usuarioLogado, id);
        exigirAgendada(consulta, "cancelada");
        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);
    }

    /** Horários (HH:mm) ainda livres no dia informado, para o app e o site oferecerem só opções válidas. */
    public List<String> horariosDisponiveis(LocalDate data) {
        Set<LocalDateTime> ocupados = consultaRepository
                .findByDataHoraBetweenAndStatusNot(data.atTime(ABERTURA), data.atTime(FECHAMENTO), StatusConsulta.CANCELADA)
                .stream()
                .map(Consulta::getDataHora)
                .collect(Collectors.toSet());

        LocalDateTime agora = LocalDateTime.now();
        List<String> livres = new ArrayList<>();
        for (LocalTime hora = ABERTURA; hora.isBefore(FECHAMENTO); hora = hora.plusMinutes(DURACAO_MINUTOS)) {
            LocalDateTime horario = data.atTime(hora);
            if (horario.isAfter(agora) && !ocupados.contains(horario)) {
                livres.add(hora.toString());
            }
        }
        return livres;
    }

    private void validarHorarioDeAtendimento(LocalDateTime dataHora) {
        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("A consulta deve ser marcada para uma data e hora futuras");
        }
        LocalTime hora = dataHora.toLocalTime();
        boolean naGrade = dataHora.getMinute() % DURACAO_MINUTOS == 0
                && dataHora.getSecond() == 0 && dataHora.getNano() == 0;
        boolean naJornada = !hora.isBefore(ABERTURA) && hora.isBefore(FECHAMENTO);
        if (!naGrade || !naJornada) {
            throw new RegraNegocioException(
                    "Atendimento das 08:00 às 18:00, em intervalos de 30 minutos (ex.: 14:00 ou 14:30)");
        }
    }

    private void exigirHorarioLivre(LocalDateTime dataHora, Long consultaIgnoradaId) {
        boolean ocupado = consultaIgnoradaId == null
                ? consultaRepository.existsByDataHoraAndStatusNot(dataHora, StatusConsulta.CANCELADA)
                : consultaRepository.existsByDataHoraAndStatusNotAndIdNot(
                        dataHora, StatusConsulta.CANCELADA, consultaIgnoradaId);
        if (ocupado) {
            throw new ConflitoException(MSG_HORARIO_OCUPADO);
        }
    }

    private void exigirAgendada(Consulta consulta, String acao) {
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só uma consulta agendada pode ser " + acao
                    + " (status atual: " + consulta.getStatus() + ")");
        }
    }

    /** O flush força o índice único do banco a valer aqui dentro: se outra requisição ganhou o horário, vira 409. */
    private Consulta salvarGarantindoHorarioUnico(Consulta consulta) {
        try {
            return consultaRepository.saveAndFlush(consulta);
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException(MSG_HORARIO_OCUPADO);
        }
    }

    public static ConsultaDto.Response toResponse(Consulta consulta) {
        return new ConsultaDto.Response(
                consulta.getId(),
                consulta.getPet().getId(),
                consulta.getPet().getNome(),
                consulta.getDataHora(),
                consulta.getMotivo(),
                consulta.getObservacoes(),
                consulta.getStatus()
        );
    }
}
