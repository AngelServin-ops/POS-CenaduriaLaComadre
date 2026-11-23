package negocio.dtos;

import java.math.BigDecimal;

/** Qué cubrió un pago: "2 Pozole rojo × $65.00 = $130.00". */
public record PartidaPagoDTO(String descripcion, int cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}
