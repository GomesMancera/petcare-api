package br.com.petcare.api.config;

import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.repository.UsuarioRepository;
import br.com.petcare.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cria a conta da CLINICA (admin) na primeira inicialização, com e-mail e senha vindos de variáveis
 * de ambiente (ADMIN_EMAIL / ADMIN_SENHA). Nenhuma senha fica no código nem no repositório.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.admin.nome}")
    private String nome;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.senha}")
    private String senha;

    @Override
    public void run(String... args) {
        if (email.isBlank() || senha.isBlank()) {
            log.warn("ADMIN_EMAIL/ADMIN_SENHA não definidos: nenhuma conta de CLINICA foi criada automaticamente.");
            return;
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        usuarioService.registrarClinica(new RegistroRequest(nome, email, senha));
        log.info("Conta de CLINICA criada para {}", email);
    }
}
