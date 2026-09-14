package br.com.petcare.api.dto;

import br.com.petcare.api.enums.StatusConsulta;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ConsultaDto {

    public record Request(
            @NotNull(message = "O pet é obrigatório") Long petId,
            @NotNull(message = "Data e hora são obrigatórias")
            @Future(message = "A consulta deve ser marcada para uma data futura")
            LocalDateTime dataHora,
            @NotBlank(message = "Motivo é obrigatório") String motivo,
            String observacoes
    ) {}

    public record Response(
            Long id,
            Long petId,
            String petNome,
            LocalDateTime dataHora,
            String motivo,
            String observacoes,
            StatusConsulta status
    ) {}
}
