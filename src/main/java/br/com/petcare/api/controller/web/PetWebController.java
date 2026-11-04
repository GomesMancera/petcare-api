package br.com.petcare.api.controller.web;

import br.com.petcare.api.dto.PetDto;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PetWebController {

    private final PetService petService;

    @GetMapping("/pets")
    public String listar(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("pets", petService.listar(usuario));
        return "pets";
    }

    @PostMapping("/pets")
    public String criar(@AuthenticationPrincipal Usuario usuario,
                        @Valid @ModelAttribute PetDto.Request form,
                        RedirectAttributes redirect) {
        petService.criar(usuario, form);
        redirect.addFlashAttribute("sucesso", "Pet cadastrado com sucesso!");
        return "redirect:/pets";
    }

    @PostMapping("/pets/{id}/excluir")
    public String excluir(@AuthenticationPrincipal Usuario usuario,
                          @PathVariable Long id,
                          RedirectAttributes redirect) {
        petService.excluir(usuario, id);
        redirect.addFlashAttribute("sucesso", "Pet excluído.");
        return "redirect:/pets";
    }
}
