package presentacion;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import negocio.dtos.PedidoDTO;

/**
 * Pantalla "Punto de venta": muestra la pantalla del caso de uso en curso
 * (Gestionar, Consultar o Cobrar pedido).
 * También es el navegador entre esas pantallas.
 */
public class PuntoDeVentaView extends JPanel {

    private final JPanel contenido = new JPanel(new BorderLayout());
    // Estas dos se conservan para no perder búsqueda ni filtros al volver
    private final PanelPedidos panelPedidos;
    private final PanelConsultarPedidos panelConsultar;

    public PuntoDeVentaView() {
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        contenido.setBackground(Estilos.FONDO);
        add(contenido, BorderLayout.CENTER);

        panelPedidos = new PanelPedidos(this);
        panelConsultar = new PanelConsultarPedidos(this);
        irAPedidos();
    }

    // ---------------- CU Gestionar pedido ----------------

    public void irAPedidos() {
        panelPedidos.recargar(null, null);
        mostrar(panelPedidos);
    }

    /** Vuelve al listado mostrando un aviso arriba y una tarjeta de resumen abajo. */
    public void irAPedidos(JComponent aviso, JComponent resumen) {
        irAPedidos(aviso, resumen, null);
    }

    /** Además resalta (fondo naranja claro) el pedido indicado, como en el Figma. */
    public void irAPedidos(JComponent aviso, JComponent resumen, Long idResaltado) {
        panelPedidos.recargar(aviso, resumen, idResaltado);
        mostrar(panelPedidos);
    }

    public void irAAgregarPedido() {
        mostrar(new PanelEditorPedido(this, null));
    }

    public void irAEditarPedido(PedidoDTO pedido) {
        mostrar(new PanelEditorPedido(this, pedido));
    }

    // ---------------- CU Consultar pedido ----------------

    public void irAConsultar() {
        panelConsultar.consultar(null);
        mostrar(panelConsultar);
    }

    /** @param volver a dónde regresa "Volver" (listado de pedidos o resultados de la consulta) */
    public void irADetalle(Long idPedido, Runnable volver) {
        mostrar(new PanelDetallePedido(this, idPedido, volver));
    }

    // ---------------- CU Cobrar pedido ----------------

    public void irACobrar(Long idPedido) {
        mostrar(new PanelCobrarPedido(this, idPedido, false));
    }

    /** Regreso desde "Cancelar cobro": muestra el resumen avisando que no se guardó nada. */
    public void irACobroCancelado(Long idPedido) {
        mostrar(new PanelCobrarPedido(this, idPedido, true));
    }

    /** Elegir qué platillos se cobran ahora (cuenta separada). */
    public void irASepararCuenta(Long idPedido) {
        mostrar(new PanelSepararCuenta(this, idPedido));
    }

    /** @param partidas qué se cobra; null = todo lo que falta (cuenta completa) */
    public void irARegistrarCobro(PedidoDTO pedido, java.util.List<negocio.dtos.PartidaCobroDTO> partidas) {
        mostrar(new PanelRegistrarCobro(this, pedido, partidas));
    }

    /** Comprobante del pago número numeroPago del pedido (1 si la cuenta no se separó). */
    public void irATicket(Long idPedido, int numeroPago) {
        mostrar(new PanelTicket(this, idPedido, numeroPago));
    }

    private void mostrar(JComponent pantalla) {
        contenido.removeAll();
        contenido.add(pantalla, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }
}
