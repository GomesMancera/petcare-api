package br.com.petcare.api.controller.web;

import br.com.petcare.api.dto.RegistroRequest;
import br.com.petcare.api.exception.ConflitoException;
import br.com.petcare.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registrar")
    public String formularioRegistro() {
        return "registrar";
    }

    /** Cadastro público: cria sempre um TUTOR. */
    @PostMapping("/registrar")
    public String registrar(@Valid @ModelAttribute RegistroRequest form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("erro", result.getAllErrors().get(0).getDefaultMessage());
            return "registrar";
        }
        try {
            usuarioService.registrarTutor(form);
        } catch (ConflitoException e) {
            model.addAttribute("erro", e.getMessage());
            return "registrar";
        }
        return "redirect:/login?registrado";
    }
}
