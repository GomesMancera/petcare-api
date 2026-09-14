package br.com.petcare.api.enums;

/**
 * Os dois tipos de usuário exigidos pela Sprint 3 (Spring Security - 30 pts):
 * TUTOR: dono do pet, só acessa os próprios dados.
 * CLINICA: administra consultas/vacinas de todos os pets.
 */
public enum Perfil {
    TUTOR,
    CLINICA
}
