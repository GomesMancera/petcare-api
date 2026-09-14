package br.com.petcare.api.service;

import br.com.petcare.api.dto.VacinaDto;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.entity.Vacina;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.exception.AcessoNegadoException;
import br.com.petcare.api.exception.ResourceNotFoundException;
import br.com.petcare.api.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VacinaService {

    /**
     * Intervalo (em dias) até a próxima dose, de acordo com o tipo de vacina.
     * Vacinas não mapeadas caem no intervalo padrão de 365 dias (reforço anual).
     */
    private static final Map<String, Integer> INTERVALO_PROXIMA_DOSE_DIAS = Map.of(
            "v10", 21,
            "v8", 21,
            "antirrabica", 365,
            "giardia", 365,
            "gripe canina", 365
    );
    private static final int INTERVALO_PADRAO_DIAS = 365;

    private final VacinaRepository vacinaRepository;
    private final PetService petService;

    public List<Vacina> listarPorPet(Usuario usuarioLogado, Long petId) {
        petService.buscarPorId(usuarioLogado, petId); // valida posse do pet
        return vacinaRepository.findByPetId(petId);
    }

    public Vacina buscarPorId(Usuario usuarioLogado, Long id) {
        Vacina vacina = vacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina não encontrada: " + id));
        validarPosse(usuarioLogado, vacina);
        return vacina;
    }

    /**
     * FLUXO COMPLETO (não é CRUD simples): registrar a aplicação de uma vacina
     * calculando automaticamente a data da próxima dose, de acordo com o tipo de vacina.
     */
    public Vacina aplicar(Usuario usuarioLogado, VacinaDto.Request request) {
        Pet pet = petService.buscarPorId(usuarioLogado, request.petId());

        int intervaloDias = INTERVALO_PROXIMA_DOSE_DIAS.getOrDefault(
                request.nome().trim().toLowerCase(Locale.ROOT), INTERVALO_PADRAO_DIAS);

        Vacina vacina = Vacina.builder()
                .pet(pet)
                .nome(request.nome())
                .dataAplicacao(request.dataAplicacao())
                .dataProximaDose(request.dataAplicacao().plusDays(intervaloDias))
                .build();

        return vacinaRepository.save(vacina);
    }

    public void excluir(Usuario usuarioLogado, Long id) {
        Vacina vacina = buscarPorId(usuarioLogado, id);
        vacinaRepository.delete(vacina);
    }

    private void validarPosse(Usuario usuarioLogado, Vacina vacina) {
        boolean ehTutorDono = vacina.getPet().getTutor().getId().equals(usuarioLogado.getId());
        boolean ehClinica = usuarioLogado.getPerfil() == Perfil.CLINICA;

        if (!ehTutorDono && !ehClinica) {
            throw new AcessoNegadoException("Você não tem permissão para acessar esta vacina");
        }
    }

    public static VacinaDto.Response toResponse(Vacina vacina) {
        boolean pendente = vacina.getDataProximaDose() != null
                && !vacina.getDataProximaDose().isAfter(LocalDate.now().plusDays(30));

        return new VacinaDto.Response(
                vacina.getId(),
                vacina.getPet().getId(),
                vacina.getPet().getNome(),
                vacina.getNome(),
                vacina.getDataAplicacao(),
                vacina.getDataProximaDose(),
                pendente
        );
    }
}
