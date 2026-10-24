package negocio.mappers;

import negocio.dtos.InsumoDTO;
import persistencia.entidades.Administrador;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.UnidadInsumo;

public class InsumoMapper {

    public static InsumoDTO aDTO(Insumo insumo) {
        return new InsumoDTO(
                insumo.getId(),
                insumo.getNombreInsumo(),
                insumo.getStock(),
                insumo.getEstadoInsumo() != null ? insumo.getEstadoInsumo().name() : null,
                insumo.getUnidadInsumo().name(),
                insumo.getCategoria().name(),
                AdministradorMapper.aDTO(insumo.getAdministrador())
        );
    }

    // Lanza IllegalArgumentException o NullPointerException si unidad/categoría no son válidas
    public static Insumo aEntidad(InsumoDTO dto, Administrador admin) {
        return new Insumo(
                dto.getNombre(),
                dto.getStock(),
                UnidadInsumo.valueOf(dto.getUnidad()),
                CategoriaInsumo.valueOf(dto.getCategoria()),
                admin
        );
    }
}
