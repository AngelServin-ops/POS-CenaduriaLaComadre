package negocio.service;

import negocio.dtos.EventoPedidoDTO;
import negocio.dtos.PagoDTO;
import negocio.dtos.PartidaCobroDTO;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public interface IPedidoService {

    // ---------------- CU Gestionar pedido ----------------

    /** Folio que recibirá el próximo pedido (para mostrarlo en "Agregar pedido"). */
    int siguienteFolio() throws NegocioException;

    PedidoDTO registrarPedido(PedidoDTO dto, Long idUsuario) throws NegocioException;

    /** Solo pedidos activos y sin ningún pago. */
    PedidoDTO actualizarPedido(PedidoDTO dto) throws NegocioException;

    /** Solo pedidos activos y sin ningún pago. El pedido se conserva en el historial. */
    PedidoDTO cancelarPedido(Long idPedido, String motivo) throws NegocioException;

    // ---------------- CU Consultar pedido ----------------

    PedidoDTO buscarPorId(Long idPedido) throws NegocioException;

    /**
     * @param busqueda folio ("P-0142", "142") o mesa ("04", "Mesa 4"); null o vacío = todos
     * @param fecha    día a consultar (turno)
     * @param estado   ACTIVO, PAGADO, CANCELADO; null = todos
     */
    List<PedidoDTO> consultar(String busqueda, LocalDate fecha, String estado) throws NegocioException;

    // ---------------- CU Cobrar pedido ----------------

    /**
     * Valida un cobro sin registrarlo y calcula efectivo aplicado y cambio.
     * Sirve para que la pantalla avise mientras se capturan los importes.
     */
    PagoDTO previsualizarCobro(BigDecimal total, PagoDTO pago) throws NegocioException;

    /** Cobra todo lo que falta de la cuenta (cuenta completa). */
    PedidoDTO cobrarPedido(Long idPedido, PagoDTO pago, Long idUsuario) throws NegocioException;

    /**
     * Cuenta separada: cobra solo las unidades indicadas de cada línea. Cuando ya no queda saldo,
     * el pedido pasa a PAGADO. Es atómico: o se registra el pago completo o no cambia nada.
     */
    PedidoDTO cobrarPartidas(Long idPedido, List<PartidaCobroDTO> partidas, PagoDTO pago, Long idUsuario)
            throws NegocioException;

    // ---------------- Avisos en tiempo real ----------------

    void agregarEscucha(Consumer<EventoPedidoDTO> escucha);

    void quitarEscucha(Consumer<EventoPedidoDTO> escucha);
}
