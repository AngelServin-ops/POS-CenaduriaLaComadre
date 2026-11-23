package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import negocio.dtos.Formatos;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;

/**
 * CU Cobrar pedido · "COBRAR PEDIDO": revisar el consumo y elegir cómo se paga:
 * la cuenta completa o separada (eligiendo platillos hasta liquidar).
 * También es el destino de "Cancelar cobro": vuelve aquí sin guardar nada.
 */
public class PanelCobrarPedido extends JPanel {

    public PanelCobrarPedido(PuntoDeVentaView nav, Long idPedido, boolean cobroCancelado) {
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);

        PedidoDTO p;
        try {
            p = ContextoApp.PEDIDOS.buscarPorId(idPedido);
        } catch (NegocioException e) {
            pantalla.add(Estilos.encabezado("COBRAR PEDIDO", null), BorderLayout.NORTH);
            pantalla.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "No se pudo consultar el pedido", e.getMessage()),
                    BorderLayout.CENTER);
            return;
        }

        String subtitulo = cobroCancelado
                ? "Cobro cancelado · " + p.getFolioTexto() + " · Datos de pago descartados"
                : p.getFolioTexto() + " · " + p.getServicioTexto() + " · Registrado a las "
                        + Formatos.hora(p.getFechaHora());
        pantalla.add(Estilos.encabezado("COBRAR PEDIDO", subtitulo), BorderLayout.NORTH);

        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));

        if (!p.esActivo()) {
            PanelPedidos.agregarIzq(t, Estilos.aviso(p.esPagado() ? Estilos.TipoAviso.EXITO : Estilos.TipoAviso.ERROR,
                    p.esPagado() ? "Esta cuenta ya está liquidada" : "Este pedido ya no se puede cobrar",
                    p.getFolioTexto() + " está " + Estilos.textoEstadoPedido(p.getEstado()) + "."));
        }

        JPanel resumen = new JPanel(new GridLayout(1, 3, 20, 0));
        resumen.setOpaque(false);
        resumen.add(Estilos.dato("Total de la cuenta", Formatos.dinero(p.getTotal()), Color.BLACK));
        resumen.add(Estilos.dato("Ya pagado", Formatos.dinero(p.getTotalPagado()),
                p.getTotalPagado().signum() > 0 ? Estilos.VERDE : Color.BLACK));
        resumen.add(Estilos.dato("Saldo por cobrar", Formatos.dinero(p.getSaldo()), Estilos.GUINDA));
        PanelPedidos.agregarIzq(t, resumen);

        PanelPedidos.agregarIzq(t, p.getPagos().isEmpty() ? TablaProductos.crear(p.getDetalles())
                : TablaProductos.crearConPagos(p.getDetalles()));
        String notas = TablaProductos.textoNotas(p);
        if (notas != null) {
            PanelPedidos.agregarIzq(t, Estilos.texto("Variaciones y notas: " + notas, Estilos.TEXTO_CHICO,
                    Estilos.GRIS_TEXTO));
        }
        if (!p.getPagos().isEmpty()) {
            PanelPedidos.agregarIzq(t, Estilos.texto("Cuenta separada: " + p.getPagos().size()
                    + (p.getPagos().size() == 1 ? " pago registrado" : " pagos registrados")
                    + ". Cobra lo que falta completo o sigue separando.", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        }

        BotonRedondeado btnVolver = Estilos.botonSecundario("Volver al listado");
        btnVolver.addActionListener(e -> nav.irAConsultar());
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(btnVolver, BorderLayout.WEST);
        if (p.esActivo()) {
            final PedidoDTO pedido = p;
            BotonRedondeado btnSeparar = Estilos.botonSecundario("Separar cuenta");
            btnSeparar.addActionListener(e -> nav.irASepararCuenta(pedido.getId()));
            BotonRedondeado btnCompleta = Estilos.boton((p.getPagos().isEmpty() ? "Cobrar cuenta completa · "
                    : "Cobrar el resto · ") + Formatos.dinero(p.getSaldo()), Estilos.NARANJA);
            btnCompleta.addActionListener(e -> nav.irARegistrarCobro(pedido, null));
            fila.add(Estilos.filaDerecha(10, btnSeparar, btnCompleta), BorderLayout.EAST);
        }
        PanelPedidos.agregarIzq(t, fila);

        pantalla.add(PanelDetallePedido.envolver(t), BorderLayout.CENTER);
        if (cobroCancelado) {
            pantalla.add(Estilos.texto("Se regresó sin guardar: no se registró ningún cobro.",
                    Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO), BorderLayout.SOUTH);
        }
    }
}
