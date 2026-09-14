package br.com.petcare.api.controller;

import br.com.petcare.api.dto.PetDto;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public List<PetDto.Response> listar(@AuthenticationPrincipal Usuario usuario) {
        return petService.listar(usuario).stream().map(PetService::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PetDto.Response buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return PetService.toResponse(petService.buscarPorId(usuario, id));
    }

    @PostMapping
    public ResponseEntity<PetDto.Response> criar(@AuthenticationPrincipal Usuario usuario,
                                                  @Valid @RequestBody PetDto.Request request) {
        Pet pet = petService.criar(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PetService.toResponse(pet));
    }

    @PutMapping("/{id}")
    public PetDto.Response atualizar(@AuthenticationPrincipal Usuario usuario,
                                      @PathVariable Long id,
                                      @Valid @RequestBody PetDto.Request request) {
        return PetService.toResponse(petService.atualizar(usuario, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        petService.excluir(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
