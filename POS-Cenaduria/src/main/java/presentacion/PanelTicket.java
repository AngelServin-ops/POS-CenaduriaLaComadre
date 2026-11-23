package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import negocio.dtos.Formatos;
import negocio.dtos.PagoDTO;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;

/** CU Cobrar pedido · paso 5 "TICKET DE COMPRA": comprobante del cobro, imprimible. */
public class PanelTicket extends JPanel {

    /** @param numeroPago qué pago del pedido (1, 2, 3...); en cuentas separadas cada pago tiene su ticket */
    public PanelTicket(PuntoDeVentaView nav, Long idPedido, int numeroPago) {
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);

        PedidoDTO p;
        try {
            p = ContextoApp.PEDIDOS.buscarPorId(idPedido);
        } catch (NegocioException e) {
            pantalla.add(Estilos.encabezado("TICKET DE COMPRA", null), BorderLayout.NORTH);
            pantalla.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "No se pudo consultar el pedido", e.getMessage()),
                    BorderLayout.CENTER);
            return;
        }
        PagoDTO pago = p.getPagos().stream().filter(x -> x.getNumero() == numeroPago).findFirst().orElse(null);
        if (pago == null) {
            pantalla.add(Estilos.encabezado("TICKET DE COMPRA", p.getFolioTexto()), BorderLayout.NORTH);
            pantalla.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "Ticket no emitido",
                    "El ticket se emite únicamente después del cobro."), BorderLayout.CENTER);
            return;
        }

        pantalla.add(Estilos.encabezado("TICKET DE COMPRA", "Ticket " + pago.getFolioComprobante() + " · Pedido "
                + p.getFolioTexto() + " · Estado " + Estilos.textoEstadoPedido(p.getEstado())), BorderLayout.NORTH);

        PanelRedondeado ticket = Estilos.tarjeta();
        ticket.setLayout(new BoxLayout(ticket, BoxLayout.Y_AXIS));
        negocio.dtos.ConfiguracionDTO negocio = ContextoApp.configuracion();
        PanelPedidos.agregarIzq(ticket, Estilos.texto(negocio.getNombreNegocio(), Estilos.TITULO_TARJETA, Color.BLACK));
        String contacto = String.join(" · ", java.util.stream.Stream.of(negocio.getDireccion(), negocio.getTelefono())
                .filter(x -> x != null && !x.isBlank()).toList());
        if (!contacto.isEmpty()) {
            PanelPedidos.agregarIzq(ticket, Estilos.texto(contacto, Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        }
        PanelPedidos.agregarIzq(ticket, Estilos.texto(p.getServicioTexto() + " · "
                + Formatos.fechaHora(pago.getFechaHora()) + " · " + ContextoApp.nombreCaja(), Estilos.TEXTO_CHICO, Color.BLACK));
        // Solo lo que cubrió este pago (en cuenta completa es todo el pedido)
        java.util.List<Object[]> filas = new java.util.ArrayList<>();
        int piezas = 0;
        for (negocio.dtos.PartidaPagoDTO pp : pago.getPartidas()) {
            filas.add(new Object[]{pp.descripcion(), pp.cantidad(), Formatos.dinero(pp.precioUnitario()),
                    Formatos.dinero(pp.importe())});
            piezas += pp.cantidad();
        }
        PanelPedidos.agregarIzq(ticket, Estilos.tablaFija(
                new String[]{"Producto", "Cantidad", "Precio unitario", "Importe"}, filas, ""));
        JPanel total = new JPanel(new BorderLayout());
        total.setOpaque(false);
        total.add(Estilos.texto(piezas + (piezas == 1 ? " producto" : " productos") + " · Precios en MXN", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO),
                BorderLayout.WEST);
        total.add(Estilos.texto("Total: " + Formatos.dinero(pago.getMonto()), Estilos.TOTAL, Color.BLACK),
                BorderLayout.EAST);
        PanelPedidos.agregarIzq(ticket, total);
        PanelPedidos.agregarIzq(ticket, lineaPago(pago));
        if (p.getPagos().size() > 1 || p.esActivo()) {
            PanelPedidos.agregarIzq(ticket, Estilos.texto("Cuenta separada · pago " + pago.getNumero() + " de la cuenta de "
                    + Formatos.dinero(p.getTotal()) + (p.esActivo() ? " · Saldo pendiente " + Formatos.dinero(p.getSaldo())
                    : " · Cuenta liquidada"), Estilos.TEXTO_CHICO, Estilos.GUINDA));
        }
        if (negocio.getMensajeTicket() != null) {
            PanelPedidos.agregarIzq(ticket, Estilos.texto(negocio.getMensajeTicket(), Estilos.TEXTO, Estilos.GUINDA));
        }

        BotonRedondeado btnImprimir = Estilos.boton("Imprimir comprobante", Estilos.NARANJA);
        btnImprimir.addActionListener(e -> imprimir(ticket));
        BotonRedondeado btnVolver = Estilos.botonSecundario("Volver a pedidos");
        btnVolver.addActionListener(e -> nav.irAConsultar());

        JPanel col = new JPanel(new BorderLayout(0, 14));
        col.setOpaque(false);
        col.add(ticket, BorderLayout.NORTH);
        col.add(Estilos.filaIzquierda(10, btnImprimir, btnVolver), BorderLayout.CENTER);
        pantalla.add(PanelDetallePedido.envolver(col), BorderLayout.CENTER);
        pantalla.add(Estilos.texto("Entrega el cambio y el comprobante. Este folio ya no se puede volver a cobrar.",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO), BorderLayout.SOUTH);
    }

    /** "Método: Mixto · Tarjeta: $100.00 · Efectivo recibido: $50.00 · Cambio: $5.00" */
    private static JComponent lineaPago(PagoDTO pago) {
        String detalle = switch (pago.getMetodo()) {
            case "TARJETA" -> "Tarjeta: " + Formatos.dinero(pago.getMontoTarjeta()) + " · Referencia: "
                    + pago.getReferenciaTerminal();
            case "MIXTO" -> "Tarjeta: " + Formatos.dinero(pago.getMontoTarjeta()) + " · Efectivo recibido: "
                    + Formatos.dinero(pago.getEfectivoRecibido()) + " · Aplicado: "
                    + Formatos.dinero(pago.getEfectivoAplicado()) + " · Cambio: " + Formatos.dinero(pago.getCambio());
            default -> "Recibido: " + Formatos.dinero(pago.getEfectivoRecibido()) + " · Cambio: "
                    + Formatos.dinero(pago.getCambio());
        };
        return Estilos.texto("<html>Método: " + Estilos.textoMetodo(pago.getMetodo()) + " · " + detalle + "</html>",
                Estilos.TEXTO_CHICO, Color.BLACK);
    }

    /** Imprime el ticket tal como se ve, escalado al ancho de la hoja. */
    private void imprimir(JComponent ticket) {
        PrinterJob trabajo = PrinterJob.getPrinterJob();
        trabajo.setJobName("Ticket Cenaduría La Comadre");
        trabajo.setPrintable((g, formato, pagina) -> {
            if (pagina > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(formato.getImageableX(), formato.getImageableY());
            double escala = Math.min(1.0, formato.getImageableWidth() / ticket.getWidth());
            g2.scale(escala, escala);
            ticket.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (trabajo.printDialog()) {
            try {
                trabajo.print();
            } catch (PrinterException e) {
                Modal.error(this, "No se pudo imprimir", e.getMessage());
            }
        }
    }
}
