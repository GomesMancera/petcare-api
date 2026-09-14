package br.com.petcare.api.dto;

public record AuthResponse(
        String token,
        String nome,
        String email,
        String perfil
) {}
