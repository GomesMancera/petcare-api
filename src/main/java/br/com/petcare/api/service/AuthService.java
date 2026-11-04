package br.com.petcare.api.service;

import br.com.petcare.api.dto.AuthResponse;
import br.com.petcare.api.dto.LoginRequest;
import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.exception.NaoAutenticadoException;
import br.com.petcare.api.repository.UsuarioRepository;
import br.com.petcare.api.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse registrar(RegistroRequest request) {
        return montarResposta(usuarioService.registrarTutor(request));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new NaoAutenticadoException("E-mail ou senha inválidos"));
        return montarResposta(usuario);
    }

    /** Troca um refresh token válido por um novo par de tokens (renovação de sessão do app). */
    public AuthResponse renovar(String refreshToken) {
        try {
            if (!jwtService.ehRefreshToken(refreshToken)) {
                throw new NaoAutenticadoException("Refresh token inválido");
            }
            Usuario usuario = usuarioRepository.findByEmail(jwtService.extrairEmail(refreshToken))
                    .orElseThrow(() -> new NaoAutenticadoException("Usuário do token não existe mais"));
            return montarResposta(usuario);
        } catch (JwtException | IllegalArgumentException e) {
            throw new NaoAutenticadoException("Sessão expirada. Faça login novamente.");
        }
    }

    private AuthResponse montarResposta(Usuario usuario) {
        return new AuthResponse(
                jwtService.gerarToken(usuario),
                jwtService.gerarRefreshToken(usuario),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name());
    }
}
