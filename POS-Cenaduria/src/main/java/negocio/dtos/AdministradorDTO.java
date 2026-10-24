package negocio.dtos;

public class AdministradorDTO extends UsuarioDTO {

    public AdministradorDTO() {
        super();
        setRol("ADMINISTRADOR");
    }

    public AdministradorDTO(Long id, String nombre) {
        super(id, nombre, "ADMINISTRADOR");
    }
}
