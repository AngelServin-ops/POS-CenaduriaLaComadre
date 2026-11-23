package negocio.dtos;

import java.math.BigDecimal;

/** Extra con costo de un platillo, p. ej. "Extra queso" (+$15.00). */
public record VariacionDTO(Long id, String nombre, BigDecimal precioExtra) {

    @Override
    public String toString() {
        return precioExtra.signum() > 0
                ? nombre + " (+" + Formatos.dinero(precioExtra) + ")"
                : nombre;
    }
}
