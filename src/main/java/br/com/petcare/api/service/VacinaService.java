package br.com.petcare.api.service;

import br.com.petcare.api.dto.VacinaDto;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.entity.Vacina;
import br.com.petcare.api.enums.TipoVacina;
import br.com.petcare.api.exception.ResourceNotFoundException;
import br.com.petcare.api.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacinaService {

    /** Quantos dias antes da próxima dose a vacina passa a contar como pendente. */
    public static final int DIAS_ALERTA_PENDENCIA = 30;

    private final VacinaRepository vacinaRepository;
    private final PetService petService;
    private final AutorizacaoService autorizacaoService;

    /** Todas as vacinas visíveis ao usuário (usada pelo app para agendar os lembretes de reforço). */
    public List<Vacina> listar(Usuario usuarioLogado) {
        if (usuarioLogado.isClinica()) {
            return vacinaRepository.findAll();
        }
        return vacinaRepository.findByPetTutorId(usuarioLogado.getId());
    }

    public List<Vacina> listarPorPet(Usuario usuarioLogado, Long petId) {
        petService.buscarPorId(usuarioLogado, petId); // valida posse do pet
        return vacinaRepository.findByPetId(petId);
    }

    public Vacina buscarPorId(Usuario usuarioLogado, Long id) {
        Vacina vacina = vacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina não encontrada: " + id));
        autorizacaoService.validarPosse(usuarioLogado, vacina.getPet().getTutor().getId(), "esta vacina");
        return vacina;
    }

    /**
     * FLUXO COMPLETO (não é CRUD simples): registrar a aplicação de uma vacina
     * calculando automaticamente a data da próxima dose, de acordo com o tipo de vacina.
     */
    public Vacina aplicar(Usuario usuarioLogado, VacinaDto.Request request) {
        Pet pet = petService.buscarPorId(usuarioLogado, request.petId());
        int intervaloDias = TipoVacina.intervaloEmDiasPara(request.nome());

        Vacina vacina = Vacina.builder()
                .pet(pet)
                .nome(request.nome().trim())
                .dataAplicacao(request.dataAplicacao())
                .dataProximaDose(request.dataAplicacao().plusDays(intervaloDias))
                .build();

        return vacinaRepository.save(vacina);
    }

    public void excluir(Usuario usuarioLogado, Long id) {
        Vacina vacina = buscarPorId(usuarioLogado, id);
        vacinaRepository.delete(vacina);
    }

    public static boolean estaPendente(Vacina vacina) {
        return vacina.getDataProximaDose() != null
                && !vacina.getDataProximaDose().isAfter(LocalDate.now().plusDays(DIAS_ALERTA_PENDENCIA));
    }

    public static VacinaDto.Response toResponse(Vacina vacina) {
        return new VacinaDto.Response(
                vacina.getId(),
                vacina.getPet().getId(),
                vacina.getPet().getNome(),
                vacina.getNome(),
                vacina.getDataAplicacao(),
                vacina.getDataProximaDose(),
                estaPendente(vacina)
        );
    }
}
