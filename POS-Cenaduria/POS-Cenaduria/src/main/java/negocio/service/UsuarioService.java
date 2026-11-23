package negocio.service;

import negocio.dtos.UsuarioDTO;
import negocio.excepciones.NegocioException;
import negocio.mappers.UsuarioMapper;
import negocio.seguridad.Contrasenas;
import persistencia.entidades.Usuario;
import persistencia.enums.RolUsuario;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IUsuarioDAO;

import java.util.List;
import java.util.Optional;

public class UsuarioService implements IUsuarioService {

    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final IUsuarioDAO usuarioDAO;

    public UsuarioService(IUsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UsuarioDTO registrarUsuario(UsuarioDTO dto) throws NegocioException {
        validar(dto);
        try {
            if (usuarioDAO.buscarPorNombreUsuario(dto.getNombreUsuario()).isPresent()) {
                throw new NegocioException("El nombre de usuario ya está en uso");
            }
            RolUsuario rol = RolUsuario.valueOf(dto.getRol());
            if (rol == RolUsuario.ADMINISTRADOR) {
                validarContrasenaUnica(dto.getContrasena(), null);
            }
            Usuario usuario = new Usuario(
                    dto.getNombre().trim(),
                    dto.getNombreUsuario().trim(),
                    hashSegunRol(rol, dto.getContrasena()),
                    rol
            );
            usuarioDAO.guardar(usuario);
            return UsuarioMapper.aDTO(usuario);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al registrar el usuario", e);
        }
    }

    @Override
    public UsuarioDTO iniciarSesion(String nombreUsuario, String contrasena) throws NegocioException {
        try {
            Optional<Usuario> usuario = nombreUsuario == null
                    ? Optional.empty()
                    : usuarioDAO.buscarPorNombreUsuario(nombreUsuario);
            // Mismo mensaje en ambos casos para no revelar qué usuarios existen
            if (usuario.isEmpty() || !usuario.get().isActivo()
                    || !Contrasenas.verificar(contrasena, usuario.get().getContrasenaHash())) {
                throw new NegocioException("Usuario o contraseña incorrectos");
            }
            return UsuarioMapper.aDTO(usuario.get());
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al iniciar sesión", e);
        }
    }

    @Override
    public UsuarioDTO iniciarSesionAdministrador(String contrasena) throws NegocioException {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new NegocioException("Escribe la contraseña");
        }
        try {
            for (Usuario u : usuarioDAO.listarTodos()) {
                if (u.isActivo() && u.getRol() == RolUsuario.ADMINISTRADOR
                        && Contrasenas.verificar(contrasena, u.getContrasenaHash())) {
                    return UsuarioMapper.aDTO(u);
                }
            }
            throw new NegocioException("Contraseña incorrecta");
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al iniciar sesión", e);
        }
    }

    @Override
    public UsuarioDTO actualizarUsuario(UsuarioDTO dto) throws NegocioException {
        if (dto == null || dto.getId() == null) {
            throw new NegocioException("Debe indicar el usuario");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()
                || dto.getNombreUsuario() == null || dto.getNombreUsuario().isBlank()) {
            throw new NegocioException("El nombre y el nombre de usuario son obligatorios");
        }
        RolUsuario rol;
        try {
            rol = RolUsuario.valueOf(dto.getRol());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Rol no válido", e);
        }
        boolean cambiaContrasena = dto.getContrasena() != null && !dto.getContrasena().isEmpty();
        if (cambiaContrasena && dto.getContrasena().length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new NegocioException("La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
        try {
            Usuario u = buscar(dto.getId());
            Optional<Usuario> mismoNombre = usuarioDAO.buscarPorNombreUsuario(dto.getNombreUsuario().trim());
            if (mismoNombre.isPresent() && !mismoNombre.get().getId().equals(u.getId())) {
                throw new NegocioException("El nombre de usuario ya está en uso");
            }
            if (u.getRol() == RolUsuario.ADMINISTRADOR && rol != RolUsuario.ADMINISTRADOR) {
                validarQueQuedeOtroAdministrador(u);
            }
            if (rol == RolUsuario.ADMINISTRADOR && u.getRol() != RolUsuario.ADMINISTRADOR && !cambiaContrasena) {
                throw new NegocioException("Asigna una contraseña para que pueda entrar como administrador");
            }
            if (rol == RolUsuario.ADMINISTRADOR && cambiaContrasena) {
                validarContrasenaUnica(dto.getContrasena(), u.getId());
            }
            u.setNombre(dto.getNombre().trim());
            u.setNombreUsuario(dto.getNombreUsuario().trim());
            u.setRol(rol);
            if (cambiaContrasena) {
                u.setContrasenaHash(Contrasenas.hashear(dto.getContrasena()));
            }
            usuarioDAO.actualizar(u);
            return UsuarioMapper.aDTO(u);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al actualizar el usuario", e);
        }
    }

    @Override
    public void cambiarEstado(Long idUsuario, boolean activo) throws NegocioException {
        try {
            Usuario u = buscar(idUsuario);
            if (!activo && u.getRol() == RolUsuario.ADMINISTRADOR) {
                validarQueQuedeOtroAdministrador(u);
            }
            u.setActivo(activo);
            usuarioDAO.actualizar(u);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al cambiar el estado del usuario", e);
        }
    }

    @Override
    public void cambiarContrasena(Long idUsuario, String actual, String nueva) throws NegocioException {
        if (nueva == null || nueva.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new NegocioException("La contraseña nueva debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
        try {
            Usuario u = buscar(idUsuario);
            if (!Contrasenas.verificar(actual, u.getContrasenaHash())) {
                throw new NegocioException("La contraseña actual no es correcta");
            }
            if (u.getRol() == RolUsuario.ADMINISTRADOR) {
                validarContrasenaUnica(nueva, u.getId());
            }
            u.setContrasenaHash(Contrasenas.hashear(nueva));
            usuarioDAO.actualizar(u);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al cambiar la contraseña", e);
        }
    }

    // ---------------- Reglas ----------------

    /** Como el login es solo con contraseña, dos administradores no pueden compartirla. */
    private void validarContrasenaUnica(String contrasena, Long excepto) throws NegocioException {
        try {
            for (Usuario otro : usuarioDAO.listarTodos()) {
                if (otro.getRol() == RolUsuario.ADMINISTRADOR && !otro.getId().equals(excepto)
                        && Contrasenas.verificar(contrasena, otro.getContrasenaHash())) {
                    throw new NegocioException("Esa contraseña ya la usa otro administrador. Elige otra.");
                }
            }
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al validar la contraseña", e);
        }
    }

    /** Siempre debe quedar al menos un administrador activo, o nadie podría entrar. */
    private void validarQueQuedeOtroAdministrador(Usuario u) throws NegocioException, PersistenciaException {
        long otros = usuarioDAO.listarTodos().stream()
                .filter(x -> x.isActivo() && x.getRol() == RolUsuario.ADMINISTRADOR && !x.getId().equals(u.getId()))
                .count();
        if (otros == 0) {
            throw new NegocioException("Debe quedar al menos un administrador activo");
        }
    }

    private Usuario buscar(Long id) throws NegocioException, PersistenciaException {
        if (id == null) {
            throw new NegocioException("Debe indicar el usuario");
        }
        return usuarioDAO.buscarPorId(id).orElseThrow(() -> new NegocioException("Usuario no encontrado"));
    }

    /** Cajeros y meseros no inician sesión: se les guarda un hash aleatorio. */
    private static String hashSegunRol(RolUsuario rol, String contrasena) {
        if (rol != RolUsuario.ADMINISTRADOR && (contrasena == null || contrasena.isEmpty())) {
            return Contrasenas.hashear(java.util.UUID.randomUUID().toString());
        }
        return Contrasenas.hashear(contrasena);
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() throws NegocioException {
        try {
            return usuarioDAO.listarTodos().stream()
                    .map(UsuarioMapper::aDTO)
                    .toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al listar los usuarios", e);
        }
    }

    // ---------------- Validaciones ----------------

    private void validar(UsuarioDTO dto) throws NegocioException {
        if (dto == null) {
            throw new NegocioException("El usuario no puede ser nulo");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new NegocioException("El nombre es obligatorio");
        }
        if (dto.getNombreUsuario() == null || dto.getNombreUsuario().isBlank()) {
            throw new NegocioException("El nombre de usuario es obligatorio");
        }
        RolUsuario rol;
        try {
            rol = RolUsuario.valueOf(dto.getRol());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Rol no válido", e);
        }
        boolean conContrasena = dto.getContrasena() != null && !dto.getContrasena().isEmpty();
        if ((rol == RolUsuario.ADMINISTRADOR || conContrasena)
                && (!conContrasena || dto.getContrasena().length() < LONGITUD_MINIMA_CONTRASENA)) {
            throw new NegocioException("La contraseña debe tener al menos "
                    + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
    }
}
