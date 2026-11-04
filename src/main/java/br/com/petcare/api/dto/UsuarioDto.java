package br.com.petcare.api.dto;

import br.com.petcare.api.entity.Usuario;

public class UsuarioDto {

    public record Response(Long id, String nome, String email, String perfil) {
        public static Response de(Usuario usuario) {
            return new Response(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                    usuario.getPerfil().name());
        }
    }
}
