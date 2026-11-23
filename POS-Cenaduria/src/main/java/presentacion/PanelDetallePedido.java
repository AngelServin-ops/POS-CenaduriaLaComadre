package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import negocio.dtos.Formatos;
import negocio.dtos.PagoDTO;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IPedidoService;

/**
 * CU Consultar pedido · "DETALLE DEL PEDIDO". Es de solo lectura: el cobro se hace
 * desde Cobrar pedido. Muestra cada pago registrado con su comprobante.
 */
public class PanelDetallePedido extends JPanel {

    private final PuntoDeVentaView nav;
    private final IPedidoService servicio = ContextoApp.PEDIDOS;
    private final Long idPedido;
    private final Runnable volver;

    public PanelDetallePedido(PuntoDeVentaView nav, Long idPedido, Runnable volver) {
        this.nav = nav;
        this.idPedido = idPedido;
        this.volver = volver;
        setLayout(new BorderLayout());
        construir();
    }

    private void construir() {
        removeAll();
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);

        PedidoDTO p;
        try {
            p = servicio.buscarPorId(idPedido);
        } catch (NegocioException e) {
            pantalla.add(Estilos.encabezado("DETALLE DEL PEDIDO", null), BorderLayout.NORTH);
            pantalla.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "No se pudo consultar el pedido", e.getMessage()),
                    BorderLayout.CENTER);
            return;
        }

        pantalla.add(Estilos.encabezado("DETALLE DEL PEDIDO", p.getFolioTexto() + " · " + p.getServicioTexto()
                + " · Creado el " + Formatos.fecha(p.getFechaHora().toLocalDate()) + " a las "
                + Formatos.hora(p.getFechaHora())), BorderLayout.NORTH);

        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));

        JPanel estados = new JPanel(new GridLayout(1, 2, 20, 0));
        estados.setOpaque(false);
        estados.add(Estilos.dato("Estado del pedido", Estilos.textoEstadoPedido(p.getEstado()),
                Estilos.colorEstadoPedido(p.getEstado())));
        estados.add(Estilos.dato("Pago", textoPagoDetallado(p), Estilos.colorPago(p)));
        PanelPedidos.agregarIzq(t, estados);
        PanelPedidos.agregarIzq(t, p.getPagos().isEmpty() ? TablaProductos.crear(p.getDetalles())
                : TablaProductos.crearConPagos(p.getDetalles()));
        PanelPedidos.agregarIzq(t, TablaProductos.pieTotal(p));
        PanelPedidos.agregarIzq(t, bloquePagos(p));

        String notas = TablaProductos.textoNotas(p);
        PanelPedidos.agregarIzq(t, Estilos.texto("Variaciones y notas", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        PanelPedidos.agregarIzq(t, Estilos.texto(notas != null ? notas : "Sin información registrada",
                Estilos.TEXTO_CHICO, Color.BLACK));
        PanelPedidos.agregarIzq(t, crearBotones(p));

        JScrollPane scroll = new JScrollPane(envolver(t));
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        pantalla.add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** "Pendiente", "Parcial · $130.00 de $230.00" o "Pagado". */
    static String textoPagoDetallado(PedidoDTO p) {
        if (p.esParcial()) {
            return "Parcial · " + Formatos.dinero(p.getTotalPagado()) + " de " + Formatos.dinero(p.getTotal());
        }
        return Estilos.textoPago(p);
    }

    /** Lista de pagos registrados, cada uno con su comprobante, y el saldo si falta. */
    private JComponent bloquePagos(PedidoDTO p) {
        JPanel b = columna();
        if (p.esCancelado()) {
            PanelPedidos.agregarIzq(b, Estilos.texto("Pedido cancelado", Estilos.TEXTO_NEGRITA, Estilos.ROJO));
            PanelPedidos.agregarIzq(b, Estilos.texto("Motivo: " + p.getMotivoCancelacion(), Estilos.TEXTO_CHICO,
                    Color.BLACK));
            PanelPedidos.agregarIzq(b, Estilos.texto("Importe histórico: " + Formatos.dinero(p.getTotal())
                    + " · Saldo a cobrar: $0.00", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
            return b;
        }
        PanelPedidos.agregarIzq(b, Estilos.texto(p.getPagos().isEmpty() ? "Sin pagos registrados"
                : "Pagos registrados", Estilos.TEXTO_NEGRITA, Color.BLACK));
        for (PagoDTO pago : p.getPagos()) {
            JPanel fila = new JPanel(new BorderLayout(12, 0));
            fila.setOpaque(false);
            JPanel textos = columna();
            PanelPedidos.agregarIzq(textos, Estilos.texto(pago.getFolioComprobante() + " · "
                    + Formatos.fechaHora(pago.getFechaHora()) + " · " + Estilos.textoMetodo(pago.getMetodo())
                    + " · " + Formatos.dinero(pago.getMonto()), Estilos.TEXTO, Color.BLACK));
            PanelPedidos.agregarIzq(textos, Estilos.texto(textoImportes(pago), Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
            fila.add(textos, BorderLayout.CENTER);
            BotonRedondeado ver = Estilos.botonSecundario("Ver comprobante");
            ver.addActionListener(e -> nav.irATicket(p.getId(), pago.getNumero()));
            fila.add(ver, BorderLayout.EAST);
            fila.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, Estilos.GRIS_LINEA));
            PanelPedidos.agregarIzq(b, fila);
        }
        if (p.esActivo()) {
            PanelPedidos.agregarIzq(b, Estilos.texto("Saldo por cobrar: " + Formatos.dinero(p.getSaldo()),
                    Estilos.TEXTO_NEGRITA, Estilos.GUINDA));
        }
        return b;
    }

    private JComponent crearBotones(PedidoDTO p) {
        BotonRedondeado btnVolver = Estilos.botonSecundario("Volver a resultados");
        btnVolver.addActionListener(e -> volver.run());

        java.util.List<JComponent> derecha = new java.util.ArrayList<>();
        if (p.esActivo()) {
            BotonRedondeado btnCobrar = Estilos.boton("Cobrar", Estilos.NARANJA);
            btnCobrar.addActionListener(e -> nav.irACobrar(p.getId()));
            derecha.add(btnCobrar);
        } else {
            derecha.add(Estilos.texto("Solo lectura · Sin cambios en el pedido", Estilos.TEXTO_CHICO,
                    Estilos.GRIS_TEXTO));
        }

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(btnVolver, BorderLayout.WEST);
        fila.add(Estilos.filaDerecha(10, derecha.toArray(new JComponent[0])), BorderLayout.EAST);
        return fila;
    }

    static String textoImportes(PagoDTO pago) {
        return switch (pago.getMetodo()) {
            case "TARJETA" -> "Tarjeta: " + Formatos.dinero(pago.getMontoTarjeta()) + " · Referencia "
                    + pago.getReferenciaTerminal() + " · Sin cambio";
            case "MIXTO" -> "Tarjeta: " + Formatos.dinero(pago.getMontoTarjeta()) + " · Efectivo recibido: "
                    + Formatos.dinero(pago.getEfectivoRecibido()) + " · Cambio: " + Formatos.dinero(pago.getCambio());
            default -> "Recibido " + Formatos.dinero(pago.getEfectivoRecibido()) + " · Cambio "
                    + Formatos.dinero(pago.getCambio());
        };
    }

    private static JPanel columna() {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        return b;
    }

    /** Coloca la tarjeta arriba, sin estirarla a todo el alto. */
    static JPanel envolver(JComponent tarjeta) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(tarjeta, BorderLayout.NORTH);
        return p;
    }
}
