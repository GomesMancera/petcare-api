package br.com.petcare.api.controller;

import br.com.petcare.api.dto.VacinaDto;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.entity.Vacina;
import br.com.petcare.api.service.VacinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacinas")
@RequiredArgsConstructor
public class VacinaController {

    private final VacinaService vacinaService;

    @GetMapping
    public List<VacinaDto.Response> listar(@AuthenticationPrincipal Usuario usuario) {
        return vacinaService.listar(usuario).stream().map(VacinaService::toResponse).toList();
    }

    @GetMapping("/pet/{petId}")
    public List<VacinaDto.Response> listarPorPet(@AuthenticationPrincipal Usuario usuario,
                                                  @PathVariable Long petId) {
        return vacinaService.listarPorPet(usuario, petId).stream().map(VacinaService::toResponse).toList();
    }

    @GetMapping("/{id}")
    public VacinaDto.Response buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return VacinaService.toResponse(vacinaService.buscarPorId(usuario, id));
    }

    /**
     * Fluxo completo de aplicação de vacina (não é CRUD simples): calcula
     * automaticamente a data da próxima dose de acordo com o tipo de vacina.
     */
    @PostMapping("/aplicar")
    public ResponseEntity<VacinaDto.Response> aplicar(@AuthenticationPrincipal Usuario usuario,
                                                       @Valid @RequestBody VacinaDto.Request request) {
        Vacina vacina = vacinaService.aplicar(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VacinaService.toResponse(vacina));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        vacinaService.excluir(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
