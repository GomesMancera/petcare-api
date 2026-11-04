package br.com.petcare.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetDto {

    public record Request(
            @NotBlank(message = "Nome do pet é obrigatório") @Size(max = 80, message = "Nome do pet deve ter no máximo 80 caracteres") String nome,
            @NotBlank(message = "Raça é obrigatória") @Size(max = 40, message = "Raça deve ter no máximo 40 caracteres") String raca,
            LocalDate dataNascimento,
            @Positive(message = "Peso deve ser maior que zero") @DecimalMax(value = "999.99", message = "Peso máximo: 999,99 kg") BigDecimal peso,
            @Size(max = 300, message = "URL da foto deve ter no máximo 300 caracteres") String fotoUrl
    ) {}

    public record Response(
            Long id,
            String nome,
            String raca,
            LocalDate dataNascimento,
            BigDecimal peso,
            String fotoUrl,
            Long tutorId,
            String tutorNome
    ) {}
}