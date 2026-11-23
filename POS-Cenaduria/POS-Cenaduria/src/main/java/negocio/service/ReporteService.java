package negocio.service;

import negocio.dtos.ReporteDTO;
import negocio.excepciones.NegocioException;
import persistencia.entidades.DetallePedido;
import persistencia.entidades.MovimientoInsumo;
import persistencia.entidades.Pago;
import persistencia.entidades.Pedido;
import persistencia.enums.EstadoPedido;
import persistencia.enums.MetodoPago;
import persistencia.enums.TipoMovimiento;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IMovimientoInsumosDAO;
import persistencia.interfaces.IPedidoDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteService implements IReporteService {

    private static final int DIAS_MAXIMOS = 366;

    private final IPedidoDAO pedidoDAO;
    private final IMovimientoInsumosDAO movimientoDAO;

    public ReporteService(IPedidoDAO pedidoDAO, IMovimientoInsumosDAO movimientoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.movimientoDAO = movimientoDAO;
    }

    @Override
    public ReporteDTO generar(LocalDate desde, LocalDate hasta) throws NegocioException {
        if (desde == null || hasta == null) {
            throw new NegocioException("Indica las fechas del reporte");
        }
        if (hasta.isBefore(desde)) {
            throw new NegocioException("La fecha final no puede ser anterior a la inicial");
        }
        if (desde.plusDays(DIAS_MAXIMOS).isBefore(hasta)) {
            throw new NegocioException("El periodo no puede pasar de un año");
        }
        try {
            List<Pedido> pedidos = pedidoDAO.listarEntre(desde, hasta);

            BigDecimal total = BigDecimal.ZERO;
            int pagados = 0;
            int cancelados = 0;
            Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
            for (MetodoPago m : MetodoPago.values()) {
                porMetodo.put(m.name(), BigDecimal.ZERO);
            }
            Map<String, int[]> cantidades = new LinkedHashMap<>();
            Map<String, BigDecimal> importes = new LinkedHashMap<>();

            BigDecimal totalLiquidados = BigDecimal.ZERO;
            for (Pedido p : pedidos) {
                if (p.getEstado() == EstadoPedido.CANCELADO) {
                    cancelados++;
                }
                // Todo lo cobrado cuenta como venta, aunque la cuenta separada no esté liquidada
                for (Pago pago : p.getPagos()) {
                    total = total.add(pago.getMonto());
                    porMetodo.merge(pago.getMetodo().name(), pago.getMonto(), BigDecimal::add);
                }
                if (p.getEstado() != EstadoPedido.PAGADO) {
                    continue;
                }
                pagados++;
                totalLiquidados = totalLiquidados.add(p.calcularTotal());
                for (DetallePedido d : p.getDetalles()) {
                    String nombre = d.getPlatillo().getNombre();
                    cantidades.computeIfAbsent(nombre, k -> new int[1])[0] += d.getCantidad();
                    importes.merge(nombre, d.calcularSubtotal(), BigDecimal::add);
                }
            }

            List<ReporteDTO.VentaPlatillo> platillos = new ArrayList<>();
            for (String nombre : cantidades.keySet()) {
                platillos.add(new ReporteDTO.VentaPlatillo(nombre, cantidades.get(nombre)[0], importes.get(nombre)));
            }
            platillos.sort(Comparator.comparing(ReporteDTO.VentaPlatillo::importe).reversed());

            BigDecimal promedio = pagados == 0 ? BigDecimal.ZERO
                    : totalLiquidados.divide(BigDecimal.valueOf(pagados), 2, RoundingMode.HALF_UP);

            return new ReporteDTO(desde, hasta, total, pagados, cancelados, promedio, porMetodo,
                    platillos, consumoInsumos(desde, hasta));
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo generar el reporte", e);
        }
    }

    private List<ReporteDTO.ConsumoInsumo> consumoInsumos(LocalDate desde, LocalDate hasta)
            throws PersistenciaException {
        Map<String, float[]> acumulado = new LinkedHashMap<>(); // compras, consumo, merma, ajustes
        Map<String, String> unidades = new LinkedHashMap<>();
        for (MovimientoInsumo m : movimientoDAO.listarPorFecha(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX))) {
            String nombre = m.getInsumo().getNombre();
            unidades.put(nombre, m.getInsumo().getUnidad().name());
            float[] a = acumulado.computeIfAbsent(nombre, k -> new float[4]);
            float signo = m.getTipo() == TipoMovimiento.ENTRADA ? 1 : -1;
            switch (m.getMotivo()) {
                case COMPRA -> a[0] += m.getCantidad();
                case CONSUMO_JORNADA -> a[1] += m.getCantidad();
                case MERMA -> a[2] += m.getCantidad();
                case AJUSTE -> a[3] += signo * m.getCantidad();
            }
        }
        List<ReporteDTO.ConsumoInsumo> lista = new ArrayList<>();
        for (Map.Entry<String, float[]> e : acumulado.entrySet()) {
            float[] a = e.getValue();
            lista.add(new ReporteDTO.ConsumoInsumo(e.getKey(), unidades.get(e.getKey()), a[0], a[1], a[2], a[3]));
        }
        lista.sort(Comparator.comparing(ReporteDTO.ConsumoInsumo::consumo).reversed());
        return lista;
    }
}
