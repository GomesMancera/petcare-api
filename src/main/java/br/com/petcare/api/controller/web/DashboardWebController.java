package br.com.petcare.api.controller.web;

import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("dados", dashboardService.montar(usuario));
        return "dashboard";
    }
}
