package br.com.petcare.api.controller.web;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.service.ConsultaService;
import br.com.petcare.api.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ConsultaWebController {

    private final ConsultaService consultaService;
    private final PetService petService;

    @GetMapping("/consultas")
    public String listar(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("pets", petService.listar(usuario));
        model.addAttribute("consultas", consultaService.listar(usuario).stream()
                .sorted(Comparator.comparing(Consulta::getDataHora).reversed())
                .map(ConsultaService::toResponse)
                .toList());
        return "consultas";
    }

    /** Alimenta o seletor de horários da tela: só aparecem horários realmente livres. */
    @GetMapping("/consultas/horarios")
    @ResponseBody
    public List<String> horarios(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return consultaService.horariosDisponiveis(data);
    }

    @PostMapping("/consultas/agendar")
    public String agendar(@AuthenticationPrincipal Usuario usuario,
                          @Valid @ModelAttribute ConsultaDto.Request form,
                          RedirectAttributes redirect) {
        consultaService.agendar(usuario, form);
        redirect.addFlashAttribute("sucesso", "Consulta agendada com sucesso!");
        return "redirect:/consultas";
    }

    @PostMapping("/consultas/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal Usuario usuario,
                           @PathVariable Long id,
                           RedirectAttributes redirect) {
        consultaService.cancelar(usuario, id);
        redirect.addFlashAttribute("sucesso", "Consulta cancelada.");
        return "redirect:/consultas";
    }

    /** Rota restrita à CLINICA (ver SecurityConfig). */
    @PostMapping("/consultas/{id}/realizar")
    public String realizar(@AuthenticationPrincipal Usuario usuario,
                           @PathVariable Long id,
                           RedirectAttributes redirect) {
        consultaService.marcarComoRealizada(usuario, id);
        redirect.addFlashAttribute("sucesso", "Consulta marcada como realizada.");
        return "redirect:/consultas";
    }
}
