package br.com.petcare.api.repository;

import br.com.petcare.api.entity.Consulta;
import br.com.petcare.api.enums.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByPetTutorId(Long tutorId);

    boolean existsByDataHoraAndStatusNot(LocalDateTime dataHora, StatusConsulta status);

    boolean existsByDataHoraAndStatusNotAndIdNot(LocalDateTime dataHora, StatusConsulta status, Long id);

    List<Consulta> findByDataHoraBetweenAndStatusNot(LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);

    void deleteByPetId(Long petId);
}
