package negocio.dtos;

public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String nombreUsuario;
    // Solo se usa para registrar o cambiar la contraseña; nunca se llena al consultar
    private String contrasena;
    private String rol;
    private boolean activo = true;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, String nombre, String nombreUsuario, String rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.activo = activo;
    }

    /** Constructor para registrar un usuario nuevo. */
    public UsuarioDTO(String nombre, String nombreUsuario, String contrasena, String rol) {
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
