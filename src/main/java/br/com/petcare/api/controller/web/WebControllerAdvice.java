package br.com.petcare.api.controller.web;

import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.exception.AcessoNegadoException;
import br.com.petcare.api.exception.ConflitoException;
import br.com.petcare.api.exception.RegraNegocioException;
import br.com.petcare.api.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Comportamento comum das páginas web: expõe o usuário logado para o menu e converte erros de negócio
 * em mensagem na própria tela (em vez de JSON), voltando para a página de origem.
 */
@ControllerAdvice(basePackages = "br.com.petcare.api.controller.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebControllerAdvice {

    @ModelAttribute("usuario")
    public Usuario usuarioLogado(@AuthenticationPrincipal Usuario usuario) {
        return usuario;
    }

    @ExceptionHandler({RegraNegocioException.class, ConflitoException.class,
            AcessoNegadoException.class, ResourceNotFoundException.class})
    public String tratarErroDeNegocio(RuntimeException ex, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("erro", ex.getMessage());
        return "redirect:" + paginaDeOrigem(request.getRequestURI());
    }

    /** Falha de validação dos formulários (@Valid): mostra a primeira mensagem na própria tela. */
    @ExceptionHandler(BindException.class)
    public String tratarFormularioInvalido(BindException ex, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("erro", ex.getAllErrors().get(0).getDefaultMessage());
        return "redirect:" + paginaDeOrigem(request.getRequestURI());
    }

    private String paginaDeOrigem(String caminho) {
        if (caminho.startsWith("/consultas")) {
            return "/consultas";
        }
        if (caminho.startsWith("/vacinas")) {
            return "/vacinas";
        }
        if (caminho.startsWith("/pets")) {
            return "/pets";
        }
        return "/dashboard";
    }
}
