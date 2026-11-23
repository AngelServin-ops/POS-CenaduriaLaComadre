package negocio.dtos;

import java.time.LocalDateTime;

/**
 * Aviso de que un pedido cambió (alta, edición, cancelación o cobro).
 * Lo reciben las pantallas abiertas para actualizarse solas.
 */
public record EventoPedidoDTO(LocalDateTime hora, int folio, String descripcion) {

    /** "20:13 · P-0144 se pagó · $285.00". */
    public String texto() {
        return Formatos.hora(hora) + " · " + Formatos.folioPedido(folio) + " " + descripcion;
    }
}
