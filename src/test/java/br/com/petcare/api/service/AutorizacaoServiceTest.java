package br.com.petcare.api.service;

import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.exception.AcessoNegadoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutorizacaoServiceTest {

    private final AutorizacaoService autorizacao = new AutorizacaoService();

    private Usuario usuario(Long id, Perfil perfil) {
        return Usuario.builder().id(id).perfil(perfil).build();
    }

    @Test
    void tutorAcessaRecursoDoProprioPet() {
        assertThatCode(() -> autorizacao.validarPosse(usuario(1L, Perfil.TUTOR), 1L, "este pet"))
                .doesNotThrowAnyException();
    }

    @Test
    void tutorNaoAcessaRecursoDeOutroTutor() {
        assertThatThrownBy(() -> autorizacao.validarPosse(usuario(1L, Perfil.TUTOR), 2L, "este pet"))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void clinicaAcessaRecursoDeQualquerTutor() {
        assertThatCode(() -> autorizacao.validarPosse(usuario(9L, Perfil.CLINICA), 2L, "este pet"))
                .doesNotThrowAnyException();
    }
}
