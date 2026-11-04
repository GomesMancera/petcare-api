package br.com.petcare.api.controller;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @GetMapping
    public List<ConsultaDto.Response> listar(@AuthenticationPrincipal Usuario usuario) {
        return consultaService.listar(usuario).stream().map(ConsultaService::toResponse).toList();
    }

    /** Horários livres (HH:mm) no dia informado: o app só deve oferecer estes ao usuário. */
    @GetMapping("/horarios-disponiveis")
    public List<String> horariosDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return consultaService.horariosDisponiveis(data);
    }

    @GetMapping("/{id}")
    public ConsultaDto.Response buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return ConsultaService.toResponse(consultaService.buscarPorId(usuario, id));
    }

    /**
     * Fluxo completo de agendamento (não é um simples POST de CRUD):
     * valida conflito de horário para o pet antes de criar a consulta.
     */
    @PostMapping("/agendar")
    public ResponseEntity<ConsultaDto.Response> agendar(@AuthenticationPrincipal Usuario usuario,
                                                         @Valid @RequestBody ConsultaDto.Request request) {
        Consulta consulta = consultaService.agendar(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaService.toResponse(consulta));
    }

    @PutMapping("/{id}")
    public ConsultaDto.Response atualizar(@AuthenticationPrincipal Usuario usuario,
                                           @PathVariable Long id,
                                           @Valid @RequestBody ConsultaDto.Request request) {
        return ConsultaService.toResponse(consultaService.atualizar(usuario, id, request));
    }

    @PatchMapping("/{id}/realizar")
    public ConsultaDto.Response marcarComoRealizada(@AuthenticationPrincipal Usuario usuario,
                                                     @PathVariable Long id) {
        return ConsultaService.toResponse(consultaService.marcarComoRealizada(usuario, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        consultaService.cancelar(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
