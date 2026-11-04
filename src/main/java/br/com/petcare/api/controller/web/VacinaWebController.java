package br.com.petcare.api.controller.web;

import br.com.petcare.api.dto.VacinaDto;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.TipoVacina;
import br.com.petcare.api.service.PetService;
import br.com.petcare.api.service.VacinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class VacinaWebController {

    private final VacinaService vacinaService;
    private final PetService petService;

    /** Carteira de vacinação do pet escolhido (por padrão, o primeiro da lista). */
    @GetMapping("/vacinas")
    public String listar(@AuthenticationPrincipal Usuario usuario,
                         @RequestParam(required = false) Long petId,
                         Model model) {
        List<Pet> pets = petService.listar(usuario);
        model.addAttribute("pets", pets);
        model.addAttribute("tiposVacina", TipoVacina.nomes());
        model.addAttribute("hoje", LocalDate.now());

        if (!pets.isEmpty()) {
            Long petSelecionado = petId != null ? petId : pets.get(0).getId();
            model.addAttribute("petSelecionado", petSelecionado);
            model.addAttribute("vacinas", vacinaService.listarPorPet(usuario, petSelecionado).stream()
                    .map(VacinaService::toResponse)
                    .toList());
        }
        return "vacinas";
    }

    @PostMapping("/vacinas/aplicar")
    public String aplicar(@AuthenticationPrincipal Usuario usuario,
                          @Valid @ModelAttribute VacinaDto.Request form,
                          RedirectAttributes redirect) {
        vacinaService.aplicar(usuario, form);
        redirect.addFlashAttribute("sucesso", "Vacina registrada! A próxima dose foi calculada automaticamente.");
        return "redirect:/vacinas?petId=" + form.petId();
    }

    @PostMapping("/vacinas/{id}/excluir")
    public String excluir(@AuthenticationPrincipal Usuario usuario,
                          @PathVariable Long id,
                          @RequestParam Long petId,
                          RedirectAttributes redirect) {
        vacinaService.excluir(usuario, id);
        redirect.addFlashAttribute("sucesso", "Registro de vacina removido.");
        return "redirect:/vacinas?petId=" + petId;
    }
}
