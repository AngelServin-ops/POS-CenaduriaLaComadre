package negocio.mappers;

import negocio.dtos.PlatilloDTO;
import negocio.dtos.VariacionDTO;
import persistencia.entidades.Platillo;
import persistencia.entidades.VariacionPlatillo;

public class PlatilloMapper {

    public static PlatilloDTO aDTO(Platillo p) {
        return new PlatilloDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getCategoria().name(),
                p.isDisponible(),
                p.getVariaciones().stream().filter(VariacionPlatillo::isActiva).map(PlatilloMapper::aDTO).toList()
        );
    }

    public static VariacionDTO aDTO(VariacionPlatillo v) {
        return new VariacionDTO(v.getId(), v.getNombre(), v.getPrecioExtra());
    }
}
