package negocio.mappers;

import negocio.dtos.InsumoDTO;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;

public class InsumoMapper {

    public static InsumoDTO aDTO(Insumo insumo, EstadoInsumo estado) {
        return new InsumoDTO(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getStock(),
                insumo.getStockMinimo(),
                estado != null ? estado.name() : null,
                insumo.getUnidad().name(),
                insumo.getCategoria().name(),
                insumo.isActivo()
        );
    }

    // Lanza IllegalArgumentException o NullPointerException si unidad/categoría no son válidas
    public static Insumo aEntidad(InsumoDTO dto) {
        return new Insumo(
                dto.getNombre(),
                dto.getStock(),
                dto.getStockMinimo(),
                UnidadInsumo.valueOf(dto.getUnidad()),
                CategoriaInsumo.valueOf(dto.getCategoria())
        );
    }
}
