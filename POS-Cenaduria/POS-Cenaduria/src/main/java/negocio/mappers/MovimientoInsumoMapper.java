package negocio.mappers;

import negocio.dtos.MovimientoInsumoDTO;
import persistencia.entidades.MovimientoInsumo;

public class MovimientoInsumoMapper {

    public static MovimientoInsumoDTO aDTO(MovimientoInsumo mov) {
        MovimientoInsumoDTO dto = new MovimientoInsumoDTO(
                mov.getTipo().name(),
                mov.getMotivo().name(),
                mov.getCantidad(),
                mov.getObservacion(),
                mov.getInsumo().getId(),
                mov.getUsuario().getId()
        );
        dto.setId(mov.getId());
        dto.setFecha(mov.getFecha());
        dto.setNombreInsumo(mov.getInsumo().getNombre());
        dto.setNombreUsuario(mov.getUsuario().getNombreUsuario());
        return dto;
    }
}
