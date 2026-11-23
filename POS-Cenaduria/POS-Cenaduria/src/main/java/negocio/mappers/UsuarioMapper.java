package negocio.mappers;

import negocio.dtos.UsuarioDTO;
import persistencia.entidades.Usuario;

public class UsuarioMapper {

    /** Nunca copia la contraseña ni su hash hacia el DTO. */
    public static UsuarioDTO aDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getNombreUsuario(),
                usuario.getRol() != null ? usuario.getRol().name() : null,
                usuario.isActivo()
        );
    }
}
