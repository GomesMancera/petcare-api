package br.com.petcare.api.controller;

import br.com.petcare.api.dto.AuthResponse;
import br.com.petcare.api.dto.LoginRequest;
import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.repository.UsuarioRepository;
import br.com.petcare.api.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token, usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.gerarToken(usuario);
        return ResponseEntity.ok(new AuthResponse(
                token, usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name()));
    }
}
