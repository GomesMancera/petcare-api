package br.com.petcare.api.service;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.enums.StatusConsulta;
import br.com.petcare.api.exception.ConflitoException;
import br.com.petcare.api.exception.RegraNegocioException;
import br.com.petcare.api.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private PetService petService;
    @Mock
    private AutorizacaoService autorizacaoService;

    private ConsultaService service;
    private Usuario tutor;
    private Pet pet;

    @BeforeEach
    void preparar() {
        service = new ConsultaService(consultaRepository, petService, autorizacaoService);
        tutor = Usuario.builder().id(1L).perfil(Perfil.TUTOR).build();
        pet = Pet.builder().id(10L).nome("Rex").tutor(tutor).build();
    }

    private LocalDateTime horarioValido() {
        return LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    private ConsultaDto.Request pedido(LocalDateTime dataHora) {
        return new ConsultaDto.Request(10L, dataHora, "Check-up", null);
    }

    @Test
    void agendaConsultaEmHorarioLivre() {
        LocalDateTime horario = horarioValido();
        when(petService.buscarPorId(tutor, 10L)).thenReturn(pet);
        when(consultaRepository.existsByDataHoraAndStatusNot(horario, StatusConsulta.CANCELADA)).thenReturn(false);
        when(consultaRepository.saveAndFlush(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta consulta = service.agendar(tutor, pedido(horario));

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(consulta.getDataHora()).isEqualTo(horario);
    }

    @Test
    void recusaHorarioJaOcupado() {
        LocalDateTime horario = horarioValido();
        when(petService.buscarPorId(tutor, 10L)).thenReturn(pet);
        when(consultaRepository.existsByDataHoraAndStatusNot(horario, StatusConsulta.CANCELADA)).thenReturn(true);

        assertThatThrownBy(() -> service.agendar(tutor, pedido(horario)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void recusaQuandoOBancoRejeitaPorHorarioDuplicado() {
        // Simula a corrida: a checagem passou, mas outra requisição ocupou o horário antes do commit.
        LocalDateTime horario = horarioValido();
        when(petService.buscarPorId(tutor, 10L)).thenReturn(pet);
        when(consultaRepository.existsByDataHoraAndStatusNot(horario, StatusConsulta.CANCELADA)).thenReturn(false);
        when(consultaRepository.saveAndFlush(any(Consulta.class)))
                .thenThrow(new DataIntegrityViolationException("UX_PC_CONSULTA_HORARIO"));

        assertThatThrownBy(() -> service.agendar(tutor, pedido(horario)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void recusaHorarioForaDaGradeDeTrintaMinutos() {
        LocalDateTime foraDaGrade = horarioValido().withMinute(10);
        when(petService.buscarPorId(tutor, 10L)).thenReturn(pet);

        assertThatThrownBy(() -> service.agendar(tutor, pedido(foraDaGrade)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void recusaHorarioForaDoExpediente() {
        LocalDateTime madrugada = horarioValido().withHour(3);
        when(petService.buscarPorId(tutor, 10L)).thenReturn(pet);

        assertThatThrownBy(() -> service.agendar(tutor, pedido(madrugada)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoPermiteCancelarConsultaJaRealizada() {
        Consulta realizada = Consulta.builder()
                .id(5L).pet(pet).dataHora(horarioValido()).status(StatusConsulta.REALIZADA).build();
        when(consultaRepository.findById(5L)).thenReturn(Optional.of(realizada));

        assertThatThrownBy(() -> service.cancelar(tutor, 5L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelarLiberaOHorario() {
        Consulta agendada = Consulta.builder()
                .id(6L).pet(pet).dataHora(horarioValido()).status(StatusConsulta.AGENDADA).build();
        when(consultaRepository.findById(6L)).thenReturn(Optional.of(agendada));

        service.cancelar(tutor, 6L);

        assertThat(agendada.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        verify(consultaRepository).save(agendada);
    }

    @Test
    void horariosDisponiveisNaoIncluemOsOcupados() {
        LocalDateTime ocupado = horarioValido();
        when(consultaRepository.findByDataHoraBetweenAndStatusNot(any(), any(), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of(Consulta.builder().dataHora(ocupado).status(StatusConsulta.AGENDADA).build()));

        List<String> livres = service.horariosDisponiveis(ocupado.toLocalDate());

        assertThat(livres).doesNotContain("10:00").contains("10:30", "08:00");
    }
}
