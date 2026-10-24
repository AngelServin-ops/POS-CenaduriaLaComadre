package negocio.mappers;

import negocio.dtos.AdministradorDTO;
import persistencia.entidades.Administrador;

public class AdministradorMapper {

    public static AdministradorDTO aDTO(Administrador admin) {
        if (admin == null) {
            return null;
        }
        return new AdministradorDTO(admin.getId(), admin.getNombre());
    }

    public static Administrador aEntidad(AdministradorDTO dto) {
        Administrador admin = new Administrador(dto.getNombre());
        admin.setId(dto.getId());
        return admin;
    }
}
