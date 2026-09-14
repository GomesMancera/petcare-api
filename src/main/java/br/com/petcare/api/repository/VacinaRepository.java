package br.com.petcare.api.repository;

import br.com.petcare.api.entity.Vacina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    List<Vacina> findByPetId(Long petId);
    List<Vacina> findByPetTutorId(Long tutorId);
}
