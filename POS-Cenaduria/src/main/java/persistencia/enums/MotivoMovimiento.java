package persistencia.enums;

/**
 * Por qué cambió el stock de un insumo.
 * COMPRA siempre es ENTRADA; CONSUMO_JORNADA y MERMA siempre son SALIDA;
 * AJUSTE puede ser cualquiera de las dos (corrección de conteo).
 */
public enum MotivoMovimiento {
    COMPRA, CONSUMO_JORNADA, MERMA, AJUSTE
}
