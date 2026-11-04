package br.com.petcare.api.service;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.dto.DashboardResponse;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.entity.Vacina;
import br.com.petcare.api.enums.StatusConsulta;
import br.com.petcare.api.repository.ConsultaRepository;
import br.com.petcare.api.repository.PetRepository;
import br.com.petcare.api.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Agrega pets, consultas e vacinas do usuário logado para montar a Home/Dashboard (web e mobile). */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMITE_AGENDA = 7;

    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;
    private final VacinaRepository vacinaRepository;

    public DashboardResponse montar(Usuario usuarioLogado) {
        boolean ehClinica = usuarioLogado.isClinica();

        long quantidadePets = ehClinica
                ? petRepository.count()
                : petRepository.findByTutorId(usuarioLogado.getId()).size();

        List<Consulta> consultas = ehClinica
                ? consultaRepository.findAll()
                : consultaRepository.findByPetTutorId(usuarioLogado.getId());

        List<Vacina> vacinas = ehClinica
                ? vacinaRepository.findAll()
                : vacinaRepository.findByPetTutorId(usuarioLogado.getId());

        LocalDateTime agora = LocalDateTime.now();

        List<Consulta> agendaFutura = consultas.stream()
                .filter(c -> c.getStatus() == StatusConsulta.AGENDADA && c.getDataHora().isAfter(agora))
                .sorted(Comparator.comparing(Consulta::getDataHora))
                .toList();

        long vacinasPendentes = vacinas.stream().filter(VacinaService::estaPendente).count();

        LocalDateTime ultimaConsulta = consultas.stream()
                .filter(c -> c.getStatus() == StatusConsulta.REALIZADA)
                .map(Consulta::getDataHora)
                .max(Comparator.naturalOrder())
                .orElse(null);

        List<ConsultaDto.Response> proximas = agendaFutura.stream()
                .limit(LIMITE_AGENDA)
                .map(ConsultaService::toResponse)
                .toList();

        return new DashboardResponse(quantidadePets, agendaFutura.size(), vacinasPendentes, ultimaConsulta, proximas);
    }
}
