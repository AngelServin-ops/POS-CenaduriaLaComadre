package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import negocio.dtos.Formatos;
import negocio.dtos.InsumoDTO;
import negocio.dtos.PedidoDTO;
import negocio.dtos.ReporteDTO;
import negocio.excepciones.NegocioException;

/**
 * "Inicio": resumen del turno. Ventas del día, pedidos por atender e insumos por reponer,
 * con accesos rápidos. Se actualiza solo cuando cambia un pedido.
 */
public class PanelInicio extends JPanel implements Recargable {

    private static final int MAXIMO_FILAS = 6;
    private static final int MAXIMO_POR_ATENDER = 10;

    private final JPanel pantalla = Estilos.pantalla();

    public PanelInicio() {
        setLayout(new BorderLayout());
        add(pantalla, BorderLayout.CENTER);
        ContextoApp.PEDIDOS.agregarEscucha(e -> SwingUtilities.invokeLater(() -> {
            if (isShowing()) {
                recargar();
            }
        }));
        recargar();
    }

    @Override
    public void recargar() {
        pantalla.removeAll();
        String nombre = ContextoApp.esAdministrador() ? ContextoApp.getAdministrador().getNombre() : "";
        pantalla.add(Estilos.encabezado((saludo() + (nombre.isEmpty() ? "" : ", " + nombre)).toUpperCase(),
                "Resumen del turno · " + (ContextoApp.esAdministrador() ? "Administrador" : "Modo caja")),
                BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        try {
            LocalDate hoy = LocalDate.now();
            ReporteDTO reporte = ContextoApp.REPORTES.generar(hoy, hoy);
            List<PedidoDTO> activos = ContextoApp.PEDIDOS.consultar(null, hoy, "ACTIVO");
            // Por cobrar = pedidos activos (sin pagar o con cuenta separada a medias).
            // Salen de la lista en cuanto la cuenta queda liquidada.
            List<PedidoDTO> porAtender = new ArrayList<>(activos);
            java.util.Collections.reverse(porAtender); // del más antiguo al más reciente
            List<InsumoDTO> porReponer = new ArrayList<>();
            if (ContextoApp.esAdministrador()) {
                for (InsumoDTO i : ContextoApp.INSUMOS.listarInsumos()) {
                    if (!"DISPONIBLE".equals(i.getEstado())) {
                        porReponer.add(i);
                    }
                }
            }

            JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
            kpis.setOpaque(false);
            kpis.add(kpi("Ventas del día", Formatos.dinero(reporte.totalVendido()), Color.BLACK));
            kpis.add(kpi("Pedidos activos", String.valueOf(activos.size()), Color.BLACK));
            kpis.add(kpi("Ticket promedio", Formatos.dinero(reporte.ticketPromedio()), Color.BLACK));
            kpis.add(ContextoApp.esAdministrador()
                    ? kpi("Insumos por reponer", String.valueOf(porReponer.size()),
                            porReponer.isEmpty() ? Estilos.VERDE : Estilos.ROJO)
                    : kpi("Pedidos pagados hoy", String.valueOf(reporte.pedidosPagados()), Color.BLACK));
            cuerpo.add(kpis, BorderLayout.NORTH);

            JPanel listas = new JPanel();
            listas.setLayout(new BoxLayout(listas, BoxLayout.Y_AXIS));
            listas.setOpaque(false);
            PanelPedidos.agregarIzq(listas, tarjetaPedidos(porAtender));
            if (ContextoApp.esAdministrador()) {
                PanelPedidos.agregarIzq(listas, tarjetaInsumos(porReponer));
            }
            cuerpo.add(PanelDetallePedido.envolver(listas), BorderLayout.CENTER);
        } catch (NegocioException e) {
            cuerpo.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "No se pudo cargar el resumen", e.getMessage()),
                    BorderLayout.NORTH);
        }
        cuerpo.add(accesosRapidos(), BorderLayout.SOUTH);
        pantalla.add(cuerpo, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------ Piezas
    private static JComponent kpi(String etiqueta, String valor, Color color) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(14, 18, 14, 18));
        PanelPedidos.agregarIzq(t, Estilos.texto(etiqueta, Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        PanelPedidos.agregarIzq(t, Estilos.texto(valor, Estilos.TOTAL.deriveFont(24f), color));
        return t;
    }

    /** Pedidos por cobrar con el botón "Cobrar" a la mano. */
    private JComponent tarjetaPedidos(List<PedidoDTO> porCobrar) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        PanelPedidos.agregarIzq(t, Estilos.texto("Pedidos por cobrar", Estilos.TITULO_TARJETA, Color.BLACK));
        PanelPedidos.agregarIzq(t, Estilos.texto("Salen de la lista cuando la cuenta queda liquidada.",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        if (porCobrar.isEmpty()) {
            PanelPedidos.agregarIzq(t, Estilos.texto("Todo al día: no hay cuentas pendientes de cobrar.",
                    Estilos.TEXTO, Estilos.VERDE));
            return t;
        }
        PanelPedidos.agregarIzq(t, filaPedido(null));
        for (PedidoDTO p : porCobrar.subList(0, Math.min(MAXIMO_POR_ATENDER, porCobrar.size()))) {
            PanelPedidos.agregarIzq(t, filaPedido(p));
        }
        if (porCobrar.size() > MAXIMO_POR_ATENDER) {
            PanelPedidos.agregarIzq(t, Estilos.texto("y " + (porCobrar.size() - MAXIMO_POR_ATENDER)
                    + " más en Punto de venta", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        }
        return t;
    }

    /** Una fila de la lista; p == null dibuja los encabezados. */
    private JComponent filaPedido(PedidoDTO p) {
        JPanel f = new JPanel(new java.awt.GridBagLayout());
        f.setOpaque(false);
        f.setBorder(new MatteBorder(p == null ? 0 : 1, 0, 0, 0, Estilos.GRIS_LINEA));
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;
        double[] pesos = {0.8, 1, 0.6, 1.3, 0.9};
        String[] textos;
        Color[] colores = {Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK};
        if (p == null) {
            textos = new String[]{"Folio", "Servicio", "Hora", "Pago", "Por cobrar"};
        } else {
            textos = new String[]{p.getFolioTexto(), p.getServicioTexto(), Formatos.hora(p.getFechaHora()),
                    p.esParcial() ? "Parcial · " + Formatos.dinero(p.getTotalPagado()) + " de "
                            + Formatos.dinero(p.getTotal()) : "Pendiente",
                    Formatos.dinero(p.getSaldo())};
            colores[3] = Estilos.colorPago(p);
        }
        for (int i = 0; i < textos.length; i++) {
            gc.gridx = i;
            gc.weightx = pesos[i];
            JLabel l = p == null ? Estilos.texto(textos[i], Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO)
                    : celda(textos[i], colores[i]);
            // Ancho preferido 0: las columnas se reparten solo por peso y quedan alineadas entre filas
            l.setPreferredSize(new java.awt.Dimension(0, l.getPreferredSize().height));
            f.add(l, gc);
        }
        gc.gridx = textos.length;
        gc.weightx = 0;
        JComponent accion = new JLabel();
        if (p != null) {
            BotonRedondeado cobrar = Estilos.boton("Cobrar", Estilos.NARANJA);
            cobrar.setPreferredSize(new java.awt.Dimension(110, 30));
            cobrar.addActionListener(e -> Navegacion.puntoDeVenta(v -> v.irACobrar(p.getId())));
            accion = Estilos.filaDerecha(0, cobrar);
        }
        accion.setPreferredSize(new java.awt.Dimension(120, p == null ? 20 : 40));
        f.add(accion, gc);
        return f;
    }

    private JComponent tarjetaInsumos(List<InsumoDTO> porReponer) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        PanelPedidos.agregarIzq(t, Estilos.texto("Insumos por reponer", Estilos.TITULO_TARJETA, Color.BLACK));
        if (porReponer.isEmpty()) {
            PanelPedidos.agregarIzq(t, Estilos.texto("Todo el inventario está disponible.",
                    Estilos.TEXTO, Estilos.VERDE));
        }
        for (InsumoDTO i : porReponer.subList(0, Math.min(MAXIMO_FILAS, porReponer.size()))) {
            boolean agotado = "AGOTADO".equals(i.getEstado());
            String detalle = agotado ? "Agotado" : "Bajo · " + numero(i.getStock()) + " "
                    + i.getUnidad().toLowerCase();
            PanelPedidos.agregarIzq(t, fila(i.getNombre(), "", detalle,
                    agotado ? Estilos.ROJO : Estilos.NARANJA, ""));
        }
        return t;
    }

    private static JComponent fila(String a, String b, String c, Color colorC, String d) {
        JPanel f = new JPanel(new GridLayout(1, 4, 8, 0));
        f.setOpaque(false);
        f.setBorder(new MatteBorder(1, 0, 0, 0, Estilos.GRIS_LINEA));
        f.add(celda(a, Color.BLACK));
        f.add(celda(b, Color.BLACK));
        f.add(celda(c, colorC));
        f.add(celda(d, Color.BLACK));
        return f;
    }

    private static JLabel celda(String texto, Color color) {
        JLabel l = Estilos.texto(texto, Estilos.TEXTO, color);
        l.setBorder(new EmptyBorder(9, 0, 9, 0));
        return l;
    }

    private static JComponent accesosRapidos() {
        BotonRedondeado agregar = Estilos.boton("+ Agregar pedido", Estilos.NARANJA);
        agregar.addActionListener(e -> Navegacion.puntoDeVenta(PuntoDeVentaView::irAAgregarPedido));
        BotonRedondeado cobrar = Estilos.botonSecundario("Consultar y cobrar");
        cobrar.addActionListener(e -> Navegacion.puntoDeVenta(PuntoDeVentaView::irAConsultar));
        if (!ContextoApp.esAdministrador()) {
            return Estilos.filaIzquierda(10, agregar, cobrar);
        }
        BotonRedondeado inventario = Estilos.botonSecundario("Ir a inventario");
        inventario.addActionListener(e -> Navegacion.ir("Inventario"));
        BotonRedondeado reportes = Estilos.botonSecundario("Ver reportes");
        reportes.addActionListener(e -> Navegacion.ir("Reportes"));
        return Estilos.filaIzquierda(10, agregar, cobrar, inventario, reportes);
    }

    private static String saludo() {
        int h = LocalTime.now().getHour();
        return h < 12 ? "Buenos días" : h < 19 ? "Buenas tardes" : "Buenas noches";
    }

    private static String numero(float n) {
        return n == (long) n ? String.valueOf((long) n) : String.valueOf(n);
    }
}
