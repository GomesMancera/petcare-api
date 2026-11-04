package br.com.petcare.api.controller;

import br.com.petcare.api.dto.AuthResponse;
import br.com.petcare.api.dto.LoginRequest;
import br.com.petcare.api.dto.RefreshRequest;
import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Cadastro público: cria sempre um TUTOR (o perfil não é aceito no corpo da requisição). */
    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.renovar(request.refreshToken());
    }
}
