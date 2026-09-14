package br.com.petcare.api.service;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.dto.DashboardResponse;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.entity.Vacina;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.enums.StatusConsulta;
import br.com.petcare.api.repository.ConsultaRepository;
import br.com.petcare.api.repository.PetRepository;
import br.com.petcare.api.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * FLUXO COMPLETO 1 exigido pela Sprint 3 (não é CRUD): agrega dados de pets,
 * consultas e vacinas do tutor logado para montar a Home/Dashboard do app.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;
    private final VacinaRepository vacinaRepository;

    public DashboardResponse montar(Usuario usuarioLogado) {
        boolean ehClinica = usuarioLogado.getPerfil() == Perfil.CLINICA;

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
        LocalDate limitePendencia = LocalDate.now().plusDays(30);

        long proximasConsultas = consultas.stream()
                .filter(c -> c.getStatus() == StatusConsulta.AGENDADA && c.getDataHora().isAfter(agora))
                .count();

        long vacinasPendentes = vacinas.stream()
                .filter(v -> v.getDataProximaDose() != null && !v.getDataProximaDose().isAfter(limitePendencia))
                .count();

        LocalDateTime ultimaConsulta = consultas.stream()
                .filter(c -> c.getStatus() == StatusConsulta.REALIZADA)
                .map(Consulta::getDataHora)
                .max(Comparator.naturalOrder())
                .orElse(null);

        List<ConsultaDto.Response> agendaProximosSete = consultas.stream()
                .filter(c -> c.getStatus() == StatusConsulta.AGENDADA && c.getDataHora().isAfter(agora))
                .sorted(Comparator.comparing(Consulta::getDataHora))
                .limit(7)
                .map(ConsultaService::toResponse)
                .toList();

        return new DashboardResponse(
                quantidadePets, proximasConsultas, vacinasPendentes, ultimaConsulta, agendaProximosSete);
    }
}
