package negocio.service;

import negocio.dtos.UsuarioDTO;
import negocio.excepciones.NegocioException;

import java.util.List;

public interface IUsuarioService {

    UsuarioDTO registrarUsuario(UsuarioDTO dto) throws NegocioException;

    UsuarioDTO iniciarSesion(String nombreUsuario, String contrasena) throws NegocioException;

    List<UsuarioDTO> listarUsuarios() throws NegocioException;

    /** Login de la caja: solo con la contraseña de un administrador activo. */
    UsuarioDTO iniciarSesionAdministrador(String contrasena) throws NegocioException;

    /** Cambia nombre, usuario y rol. Si dto trae contraseña, también la restablece. */
    UsuarioDTO actualizarUsuario(UsuarioDTO dto) throws NegocioException;

    /** Desactivar en lugar de borrar: así se conserva quién registró cada pedido. */
    void cambiarEstado(Long idUsuario, boolean activo) throws NegocioException;

    void cambiarContrasena(Long idUsuario, String actual, String nueva) throws NegocioException;
}
