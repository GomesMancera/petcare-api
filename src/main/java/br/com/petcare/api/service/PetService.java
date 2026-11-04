package br.com.petcare.api.service;

import br.com.petcare.api.dto.PetDto;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.exception.ResourceNotFoundException;
import br.com.petcare.api.repository.ConsultaRepository;
import br.com.petcare.api.repository.PetRepository;
import br.com.petcare.api.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;
    private final VacinaRepository vacinaRepository;
    private final AutorizacaoService autorizacaoService;

    public List<Pet> listar(Usuario usuarioLogado) {
        if (usuarioLogado.isClinica()) {
            return petRepository.findAll();
        }
        return petRepository.findByTutorId(usuarioLogado.getId());
    }

    public Pet buscarPorId(Usuario usuarioLogado, Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado: " + id));
        autorizacaoService.validarPosse(usuarioLogado, pet.getTutor().getId(), "este pet");
        return pet;
    }

    public Pet criar(Usuario tutorLogado, PetDto.Request request) {
        Pet pet = Pet.builder()
                .nome(request.nome())
                .raca(request.raca())
                .dataNascimento(request.dataNascimento())
                .peso(request.peso())
                .fotoUrl(request.fotoUrl())
                .tutor(tutorLogado)
                .build();
        return petRepository.save(pet);
    }

    public Pet atualizar(Usuario usuarioLogado, Long id, PetDto.Request request) {
        Pet pet = buscarPorId(usuarioLogado, id);
        pet.setNome(request.nome());
        pet.setRaca(request.raca());
        pet.setDataNascimento(request.dataNascimento());
        pet.setPeso(request.peso());
        pet.setFotoUrl(request.fotoUrl());
        return petRepository.save(pet);
    }

    /** Remove também o histórico do pet (consultas e vacinas); sem isso a chave estrangeira bloqueia a exclusão. */
    @Transactional
    public void excluir(Usuario usuarioLogado, Long id) {
        Pet pet = buscarPorId(usuarioLogado, id);
        consultaRepository.deleteByPetId(pet.getId());
        vacinaRepository.deleteByPetId(pet.getId());
        petRepository.delete(pet);
    }

    public static PetDto.Response toResponse(Pet pet) {
        return new PetDto.Response(
                pet.getId(),
                pet.getNome(),
                pet.getRaca(),
                pet.getDataNascimento(),
                pet.getPeso(),
                pet.getFotoUrl(),
                pet.getTutor().getId(),
                pet.getTutor().getNome()
        );
    }
}
