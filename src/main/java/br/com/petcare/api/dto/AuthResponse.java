package br.com.petcare.api.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        String nome,
        String email,
        String perfil
) {}
