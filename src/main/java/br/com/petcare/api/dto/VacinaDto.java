package br.com.petcare.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class VacinaDto {

    public record Request(
            @NotNull(message = "O pet é obrigatório") Long petId,
            @NotBlank(message = "Nome da vacina é obrigatório") String nome,
            @NotNull(message = "Data de aplicação é obrigatória")
            @PastOrPresent(message = "A data de aplicação não pode ser futura")
            LocalDate dataAplicacao
    ) {}

    public record Response(
            Long id,
            Long petId,
            String petNome,
            String nome,
            LocalDate dataAplicacao,
            LocalDate dataProximaDose,
            boolean pendente
    ) {}
}
