package br.com.petcare.api.controller;

import br.com.petcare.api.dto.ConsultaDto;
import br.com.petcare.api.dto.PetDto;
import br.com.petcare.api.dto.VacinaDto;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.entity.Vacina;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.repository.UsuarioRepository;
import br.com.petcare.api.service.ConsultaService;
import br.com.petcare.api.service.DashboardService;
import br.com.petcare.api.service.PetService;
import br.com.petcare.api.service.VacinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de visualização (frontend) exigida pela Sprint 3 (30 pts).
 * Páginas server-side renderizadas com Thymeleaf, autenticadas via sessão
 * (cadeia "webFilterChain" do SecurityConfig) — separada da API JWT usada pelo Mobile.
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PetService petService;
    private final DashboardService dashboardService;
    private final ConsultaService consultaService;
    private final VacinaService vacinaService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registrar")
    public String formularioRegistro() {
        return "registrar";
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam String nome,
                             @RequestParam String email,
                             @RequestParam String senha,
                             Model model) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            model.addAttribute("erro", "Já existe uma conta com esse e-mail");
            return "registrar";
        }

        Usuario tutor = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .perfil(Perfil.TUTOR)
                .build();

        usuarioRepository.save(tutor);
        return "redirect:/login?registrado";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("dados", dashboardService.montar(usuario));
        return "dashboard";
    }

    @GetMapping("/pets")
    public String listarPets(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("pets", petService.listar(usuario));
        return "pets";
    }

    @PostMapping("/pets")
    public String criarPet(@AuthenticationPrincipal Usuario usuario,
                            @RequestParam String nome,
                            @RequestParam String raca,
                            @RequestParam(required = false) String dataNascimento,
                            @RequestParam(required = false) Double peso) {
        LocalDate nascimento = (dataNascimento != null && !dataNascimento.isBlank())
                ? LocalDate.parse(dataNascimento) : null;

        petService.criar(usuario, new PetDto.Request(
                nome, raca, nascimento, peso != null ? BigDecimal.valueOf(peso) : null, null));

        return "redirect:/pets";
    }

    @PostMapping("/pets/{id}/excluir")
    public String excluirPet(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        petService.excluir(usuario, id);
        return "redirect:/pets";
    }

    @GetMapping("/consultas")
    public String listarConsultas(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("pets", petService.listar(usuario));
        model.addAttribute("consultas", consultaService.listar(usuario));
        return "consultas";
    }

    @PostMapping("/consultas/agendar")
    public String agendarConsulta(@AuthenticationPrincipal Usuario usuario,
                                   @RequestParam Long petId,
                                   @RequestParam String dataHora,
                                   @RequestParam String motivo,
                                   @RequestParam(required = false) String observacoes,
                                   RedirectAttributes redirect) {
        try {
            String valor = dataHora.length() == 16 ? dataHora + ":00" : dataHora;
            consultaService.agendar(usuario, new ConsultaDto.Request(
                    petId, LocalDateTime.parse(valor), motivo, observacoes));
            redirect.addFlashAttribute("sucesso", "Consulta agendada com sucesso!");
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        } catch (Exception ex) {
            redirect.addFlashAttribute("erro", "Não foi possível agendar: " + ex.getMessage());
        }
        return "redirect:/consultas";
    }

    @GetMapping("/vacinas")
    public String listarVacinas(@AuthenticationPrincipal Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("pets", petService.listar(usuario));

        List<Vacina> vacinas = new ArrayList<>();
        petService.listar(usuario).forEach(pet ->
                vacinas.addAll(vacinaService.listarPorPet(usuario, pet.getId())));
        model.addAttribute("vacinas", vacinas);
        return "vacinas";
    }

    @PostMapping("/vacinas/aplicar")
    public String aplicarVacina(@AuthenticationPrincipal Usuario usuario,
                                 @RequestParam Long petId,
                                 @RequestParam String nome,
                                 @RequestParam String dataAplicacao,
                                 RedirectAttributes redirect) {
        try {
            Vacina vacina = vacinaService.aplicar(usuario, new VacinaDto.Request(
                    petId, nome, LocalDate.parse(dataAplicacao)));
            redirect.addFlashAttribute("sucesso",
                    "Vacina registrada! Próxima dose em "
                            + vacina.getDataProximaDose());
        } catch (Exception ex) {
            redirect.addFlashAttribute("erro", "Não foi possível aplicar: " + ex.getMessage());
        }
        return "redirect:/vacinas";
    }
}
