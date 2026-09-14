package br.com.petcare.api.service;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.enums.StatusConsulta;
import br.com.petcare.api.exception.AcessoNegadoException;
import br.com.petcare.api.exception.ResourceNotFoundException;
import br.com.petcare.api.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private static final int DURACAO_PADRAO_MINUTOS = 30;

    private final ConsultaRepository consultaRepository;
    private final PetService petService;

    public List<Consulta> listar(Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() == Perfil.CLINICA) {
            return consultaRepository.findAll();
        }
        return consultaRepository.findByPetTutorId(usuarioLogado.getId());
    }

    public Consulta buscarPorId(Usuario usuarioLogado, Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + id));
        validarPosse(usuarioLogado, consulta);
        return consulta;
    }

    /**
     * FLUXO COMPLETO (não é CRUD simples): agendamento de consulta com validação
     * de conflito de horário para o mesmo pet, considerando uma janela de 30 minutos.
     */
    public Consulta agendar(Usuario usuarioLogado, ConsultaDto.Request request) {
        Pet pet = petService.buscarPorId(usuarioLogado, request.petId());

        boolean existeConflito = consultaRepository.findByPetId(pet.getId()).stream()
                .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
                .anyMatch(c -> haConflitoDeHorario(c.getDataHora(), request.dataHora()));

        if (existeConflito) {
            throw new IllegalStateException(
                    "Já existe uma consulta agendada para este pet próxima desse horário");
        }

        Consulta consulta = Consulta.builder()
                .pet(pet)
                .dataHora(request.dataHora())
                .motivo(request.motivo())
                .observacoes(request.observacoes())
                .status(StatusConsulta.AGENDADA)
                .build();

        return consultaRepository.save(consulta);
    }

    public Consulta atualizar(Usuario usuarioLogado, Long id, ConsultaDto.Request request) {
        Consulta consulta = buscarPorId(usuarioLogado, id);
        consulta.setDataHora(request.dataHora());
        consulta.setMotivo(request.motivo());
        consulta.setObservacoes(request.observacoes());
        return consultaRepository.save(consulta);
    }

    public Consulta marcarComoRealizada(Usuario usuarioLogado, Long id) {
        Consulta consulta = buscarPorId(usuarioLogado, id);
        consulta.setStatus(StatusConsulta.REALIZADA);
        return consultaRepository.save(consulta);
    }

    public void cancelar(Usuario usuarioLogado, Long id) {
        Consulta consulta = buscarPorId(usuarioLogado, id);
        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);
    }

    private boolean haConflitoDeHorario(LocalDateTime existente, LocalDateTime novo) {
        long diferencaMinutos = Math.abs(Duration.between(existente, novo).toMinutes());
        return diferencaMinutos < DURACAO_PADRAO_MINUTOS;
    }

    private void validarPosse(Usuario usuarioLogado, Consulta consulta) {
        boolean ehTutorDono = consulta.getPet().getTutor().getId().equals(usuarioLogado.getId());
        boolean ehClinica = usuarioLogado.getPerfil() == Perfil.CLINICA;

        if (!ehTutorDono && !ehClinica) {
            throw new AcessoNegadoException("Você não tem permissão para acessar esta consulta");
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
