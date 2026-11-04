package br.com.petcare.api.service;

import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.exception.ConflitoException;
import br.com.petcare.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Único ponto de criação de usuários: o perfil é sempre decidido aqui, nunca pelo cliente. */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /** Cadastro público (API e web): sempre TUTOR. */
    public Usuario registrarTutor(RegistroRequest request) {
        return registrar(request, Perfil.TUTOR);
    }

    /** Uso restrito: admin inicial e rota /api/admin/clinicas (protegida para CLINICA). */
    public Usuario registrarClinica(RegistroRequest request) {
        return registrar(request, Perfil.CLINICA);
    }

    private Usuario registrar(RegistroRequest request, Perfil perfil) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflitoException("Já existe uma conta com esse e-mail");
        }

        return usuarioRepository.save(Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(perfil)
                .build());
    }
}
