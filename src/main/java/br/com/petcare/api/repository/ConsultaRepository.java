package br.com.petcare.api.repository;

import br.com.petcare.api.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByPetId(Long petId);
    List<Consulta> findByPetTutorId(Long tutorId);
}
