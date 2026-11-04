package br.com.petcare.api.controller;

import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.dto.UsuarioDto;
import br.com.petcare.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rotas administrativas: protegidas no SecurityConfig, só o perfil CLINICA acessa /api/admin/**. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @PostMapping("/clinicas")
    public ResponseEntity<UsuarioDto.Response> cadastrarClinica(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioDto.Response.de(usuarioService.registrarClinica(request)));
    }
}
