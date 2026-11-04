package br.com.petcare.api.service;

import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.exception.AcessoNegadoException;
import org.springframework.stereotype.Component;

/**
 * Regra única de posse (antes repetida em PetService, ConsultaService e VacinaService):
 * o TUTOR só acessa recursos dos próprios pets; a CLINICA acessa todos.
 */
@Component
public class AutorizacaoService {

    public void validarPosse(Usuario usuarioLogado, Long tutorDonoId, String recurso) {
        boolean ehDono = tutorDonoId.equals(usuarioLogado.getId());
        if (!ehDono && !usuarioLogado.isClinica()) {
            throw new AcessoNegadoException("Você não tem permissão para acessar " + recurso);
        }
    }
}
