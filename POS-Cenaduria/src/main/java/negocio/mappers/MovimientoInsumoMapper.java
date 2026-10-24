package negocio.mappers;

import negocio.dtos.MovimientoInsumoDTO;
import persistencia.entidades.MovimientoInsumo;

public class MovimientoInsumoMapper {

    public static MovimientoInsumoDTO aDTO(MovimientoInsumo mov) {
        return new MovimientoInsumoDTO(
                mov.getId(),
                mov.getTipo().name(),
                mov.getFecha(),
                mov.getCantidad(),
                InsumoMapper.aDTO(mov.getInsumo())
        );
    }
}
