package br.com.petcare.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cadastro de usuário. O perfil NÃO vem do cliente: o servidor decide
 * (TUTOR no cadastro público, CLINICA apenas por rota/admin protegidos).
 */
public record RegistroRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres") String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres") String email,
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senha
) {}
