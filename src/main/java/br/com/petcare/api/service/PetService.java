package br.com.petcare.api.service;

import br.com.petcare.api.dto.PetDto;
import br.com.petcare.api.entity.Pet;
import br.com.petcare.api.entity.Usuario;
import br.com.petcare.api.enums.Perfil;
import br.com.petcare.api.exception.AcessoNegadoException;
import br.com.petcare.api.exception.ResourceNotFoundException;
import br.com.petcare.api.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;

    public List<Pet> listar(Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() == Perfil.CLINICA) {
            return petRepository.findAll();
        }
        return petRepository.findByTutorId(usuarioLogado.getId());
    }

    public Pet buscarPorId(Usuario usuarioLogado, Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado: " + id));
        validarPosse(usuarioLogado, pet);
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

    public void excluir(Usuario usuarioLogado, Long id) {
        Pet pet = buscarPorId(usuarioLogado, id);
        petRepository.delete(pet);
    }

    /**
     * Regra de segurança: um TUTOR só pode acessar/alterar os próprios pets.
     * A CLINICA tem acesso a todos os pets do sistema.
     */
    private void validarPosse(Usuario usuarioLogado, Pet pet) {
        boolean ehTutorDono = pet.getTutor().getId().equals(usuarioLogado.getId());
        boolean ehClinica = usuarioLogado.getPerfil() == Perfil.CLINICA;

        if (!ehTutorDono && !ehClinica) {
            throw new AcessoNegadoException("Você não tem permissão para acessar este pet");
        }
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
