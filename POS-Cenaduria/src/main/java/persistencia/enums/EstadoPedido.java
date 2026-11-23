package persistencia.enums;

/**
 * ACTIVO = falta cobrar (toda la cuenta o parte, si se separó);
 * PAGADO = cuenta liquidada; CANCELADO = no se cobra. PAGADO y CANCELADO ya no cambian.
 */
public enum EstadoPedido {
    ACTIVO, PAGADO, CANCELADO
}
