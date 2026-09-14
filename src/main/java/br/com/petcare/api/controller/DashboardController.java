package br.com.petcare.api.controller;

import br.com.petcare.api.dto.DashboardResponse;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal Usuario usuario) {
        return dashboardService.montar(usuario);
    }
}
