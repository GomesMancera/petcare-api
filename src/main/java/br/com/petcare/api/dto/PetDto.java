package br.com.petcare.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetDto {

    public record Request(
            @NotBlank(message = "Nome do pet é obrigatório") String nome,
            @NotBlank(message = "Raça é obrigatória") String raca,
            LocalDate dataNascimento,
            BigDecimal peso,
            String fotoUrl
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