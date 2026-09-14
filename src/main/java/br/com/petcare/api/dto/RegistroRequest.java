package br.com.petcare.api.dto;

import br.com.petcare.api.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank @Email(message = "E-mail inválido") String email,
        @NotBlank @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senha,
        @NotNull(message = "Perfil é obrigatório (TUTOR ou CLINICA)") Perfil perfil
) {}
