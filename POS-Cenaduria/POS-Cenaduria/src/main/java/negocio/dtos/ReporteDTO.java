package negocio.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Resultado de un reporte por periodo: ventas, platillos más vendidos y consumo de insumos.
 * Solo cuentan como venta los pedidos PAGADOS.
 */
public record ReporteDTO(
        LocalDate desde,
        LocalDate hasta,
        BigDecimal totalVendido,
        int pedidosPagados,
        int pedidosCancelados,
        BigDecimal ticketPromedio,
        Map<String, BigDecimal> ventasPorMetodo,
        List<VentaPlatillo> platillos,
        List<ConsumoInsumo> insumos) {

    /** Unidades vendidas e importe de un platillo en el periodo. */
    public record VentaPlatillo(String nombre, int cantidad, BigDecimal importe) {
    }

    /** Movimientos de un insumo en el periodo (las salidas en positivo). */
    public record ConsumoInsumo(String nombre, String unidad, float compras, float consumo,
                                float merma, float ajustes) {
    }
}
