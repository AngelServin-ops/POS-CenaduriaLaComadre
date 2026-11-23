package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import negocio.dtos.EventoPedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IPedidoService;

/**
 * CU Gestionar pedido · listado "PEDIDOS" del turno.
 * Desde aquí se agrega un pedido y, si está pendiente y sin pago, se edita o se cancela.
 */
public class PanelPedidos extends JPanel {

    private static final int COL_ACCIONES = 6;

    private final PuntoDeVentaView nav;
    private final IPedidoService servicio = ContextoApp.PEDIDOS;
    private final ModeloPedidos modelo = new ModeloPedidos();
    private final Paginador<PedidoDTO> paginador = new Paginador<>(Estilos.FILAS_POR_PAGINA, modelo::setDatos);
    private List<PedidoDTO> pedidos = List.of();
    private Long idResaltado;
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilos.campoTexto("Folio o mesa...");
    private final JLabel lblConteo = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final JPanel zonaAviso = new JPanel(new BorderLayout());
    private final JPanel zonaResumen = new JPanel(new BorderLayout());

    public PanelPedidos(PuntoDeVentaView nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);

        pantalla.add(Estilos.encabezado("PEDIDOS",
                "Administra los pedidos del turno y conserva sus movimientos."), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(crearParteSuperior(), BorderLayout.NORTH);
        centro.add(crearTabla(), BorderLayout.CENTER);
        pantalla.add(centro, BorderLayout.CENTER);

        zonaResumen.setOpaque(false);
        pantalla.add(zonaResumen, BorderLayout.SOUTH);

        // Si otra pantalla cambia un pedido, el listado se actualiza solo
        Consumer<EventoPedidoDTO> escucha = e -> SwingUtilities.invokeLater(this::recargarTabla);
        servicio.agregarEscucha(escucha);
    }

    // ------------------------------------------------------------ API para el navegador
    /** Vuelve a cargar el listado y muestra (o quita) el aviso y la tarjeta de resumen. */
    public void recargar(JComponent aviso, JComponent resumen) {
        recargar(aviso, resumen, null);
    }

    /** Igual que {@link #recargar(JComponent, JComponent)} y además resalta ese pedido en la tabla. */
    public void recargar(JComponent aviso, JComponent resumen, Long resaltar) {
        idResaltado = resaltar;
        zonaAviso.removeAll();
        if (aviso != null) {
            zonaAviso.add(aviso, BorderLayout.CENTER);
            zonaAviso.setBorder(new EmptyBorder(0, 0, 12, 0));
        }
        zonaResumen.removeAll();
        if (resumen != null) {
            zonaResumen.add(resumen, BorderLayout.CENTER);
            zonaResumen.setBorder(new EmptyBorder(14, 0, 0, 0));
        }
        recargarTabla();
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------ Construcción
    private JPanel crearParteSuperior() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        zonaAviso.setOpaque(false);
        zonaAviso.setAlignmentX(LEFT_ALIGNMENT);
        zonaAviso.putClientProperty("altoLibre", true);
        p.add(zonaAviso);

        JLabel etiqueta = Estilos.texto("Buscar pedido", Estilos.ETIQUETA, Color.BLACK);
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        p.add(etiqueta);
        p.add(Box.createRigidArea(new Dimension(0, 4)));

        BotonRedondeado btnBuscar = Estilos.boton("Buscar", Estilos.NARANJA);
        btnBuscar.addActionListener(e -> recargarTabla());
        txtBuscar.addActionListener(e -> recargarTabla());
        BotonRedondeado btnAgregar = Estilos.boton("+ Agregar pedido", Estilos.NARANJA);
        btnAgregar.addActionListener(e -> nav.irAAgregarPedido());
        BotonRedondeado btnConsultar = Estilos.botonSecundario("Consultar y cobrar");
        btnConsultar.addActionListener(e -> nav.irAConsultar());

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(txtBuscar, BorderLayout.CENTER);
        barra.add(Estilos.filaDerecha(10, btnBuscar, btnAgregar, btnConsultar), BorderLayout.EAST);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(barra);
        return p;
    }

    private JPanel crearTabla() {
        Estilos.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new RenderColor(true));
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RenderColor(false));
        tabla.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new RenderAcciones());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setPreferredWidth(150);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila < 0 || tabla.columnAtPoint(e.getPoint()) != COL_ACCIONES) {
                    return;
                }
                PedidoDTO p = modelo.get(fila);
                if (p.isModificable()) {
                    Rectangle celda = tabla.getCellRect(fila, COL_ACCIONES, false);
                    if (e.getY() < celda.y + celda.height / 2) {
                        editar(p);
                    } else {
                        cancelar(p);
                    }
                } else {
                    nav.irADetalle(p.getId(), nav::irAPedidos);
                }
            }
        });
        tabla.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean sobreAccion = tabla.columnAtPoint(e.getPoint()) == COL_ACCIONES
                        && tabla.rowAtPoint(e.getPoint()) >= 0;
                tabla.setCursor(Cursor.getPredefinedCursor(sobreAccion ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        PanelRedondeado tarjeta = Estilos.tarjetaTabla(tabla, lblConteo, paginador);

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(tarjeta, BorderLayout.CENTER);
        p.add(Estilos.texto("Solo los pedidos activos y sin pagos pueden editarse o cancelarse.",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO), BorderLayout.SOUTH);
        return p;
    }

    // ------------------------------------------------------------ Datos
    private void recargarTabla() {
        try {
            pedidos = servicio.consultar(txtBuscar.getText(), LocalDate.now(), null);
            lblConteo.setText(pedidos.size() + " pedidos · " + Formatos.fecha(LocalDate.now()));
        } catch (NegocioException e) {
            pedidos = List.of();
            lblConteo.setText("No se pudo cargar el listado: " + e.getMessage());
        }
        int indice = -1;
        for (int i = 0; i < pedidos.size(); i++) {
            if (pedidos.get(i).getId().equals(idResaltado)) {
                indice = i;
            }
        }
        paginador.setDatos(pedidos);
        paginador.irAElemento(indice); // muestra la página donde quedó el pedido resaltado
        Estilos.resaltarFila(tabla, paginador.filaEnPagina(indice));
    }

    // ------------------------------------------------------------ Acciones
    private void editar(PedidoDTO p) {
        try {
            nav.irAEditarPedido(servicio.buscarPorId(p.getId())); // versión más reciente
        } catch (NegocioException e) {
            Modal.error(this, "No se pudo abrir el pedido", e.getMessage());
        }
    }

    /** Paso 6: pide el motivo y confirma. "Conservar pedido" no cambia nada. */
    private void cancelar(PedidoDTO p) {
        Modal m = new Modal(this, "¿Cancelar el pedido " + p.getFolioTexto() + "?",
                p.getServicioTexto() + " · " + p.getTotalProductos() + " productos · Total: "
                        + Formatos.dinero(p.getTotal()) + " MXN", 560);
        JTextField txtMotivo = Estilos.campoTexto("Ej. El cliente canceló antes de la preparación");
        JLabel lblError = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.ROJO);
        JPanel campo = Estilos.campo("Motivo de cancelación", txtMotivo);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        m.agregar(campo);
        m.agregar(lblError);
        m.parrafo("El pedido quedará Cancelado y se conservará en el historial. "
                + "No se registrará un pago ni se borrarán sus productos.");

        PedidoDTO[] cancelado = new PedidoDTO[1];
        m.boton("Conservar pedido", Estilos.GUINDA, null);
        m.botonAccion("Confirmar cancelación", Estilos.NARANJA, () -> {
            if (txtMotivo.getText().isBlank()) {
                Estilos.marcarError(txtMotivo, true);
                lblError.setText("Escribe el motivo de la cancelación.");
                txtMotivo.requestFocusInWindow();
                return;
            }
            try {
                cancelado[0] = servicio.cancelarPedido(p.getId(), txtMotivo.getText());
                m.cerrar(true);
            } catch (NegocioException ex) {
                lblError.setText(ex.getMessage());
            }
        });
        m.mostrar();

        if (cancelado[0] != null) {
            PedidoDTO c = cancelado[0];
            recargar(Estilos.aviso(Estilos.TipoAviso.EXITO, "Pedido cancelado correctamente",
                            c.getFolioTexto() + " permanece en el historial con su consumo original de "
                                    + Formatos.dinero(c.getTotal()) + "."),
                    tarjetaCancelacion(c), c.getId());
        }
    }

    /** Paso 7: registro de cancelación. */
    private JComponent tarjetaCancelacion(PedidoDTO c) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        agregarIzq(t, Estilos.texto("Registro de cancelación · " + c.getFolioTexto(),
                Estilos.TITULO_TARJETA, Color.BLACK));
        JPanel datos = new JPanel(new java.awt.GridLayout(1, 2, 20, 0));
        datos.setOpaque(false);
        datos.add(Estilos.dato("Estado del pedido", "Cancelado", Estilos.ROJO));
        datos.add(Estilos.dato("Estado del pago", "No aplica", Estilos.GUINDA));
        agregarIzq(t, datos);
        agregarIzq(t, Estilos.texto(Formatos.fechaHora(c.getFechaCancelacion()) + " · " + ContextoApp.nombreCaja(),
                Estilos.TEXTO_CHICO, Color.BLACK));
        agregarIzq(t, Estilos.texto("Motivo: " + c.getMotivoCancelacion(), Estilos.TEXTO_CHICO, Color.BLACK));
        agregarIzq(t, Estilos.texto("Importe histórico: " + Formatos.dinero(c.getTotal())
                + " · Saldo a cobrar: $0.00 · Sin cobros registrados", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        return t;
    }

    /**
     * Agrega c debajo de lo anterior, alineado a la izquierda. Los paneles no se estiran
     * a lo alto (para que no queden huecos), salvo los marcados con la propiedad "altoLibre".
     */
    static void agregarIzq(JPanel contenedor, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        if (c instanceof JPanel && !Boolean.TRUE.equals(c.getClientProperty("altoLibre"))) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        }
        if (contenedor.getComponentCount() > 0) {
            contenedor.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        contenedor.add(c);
    }

    // ============================================================ Tabla
    private static class ModeloPedidos extends AbstractTableModel {

        private final String[] columnas = {"Folio", "Servicio", "Hora", "Estado", "Pago", "Total", "Acciones"};
        private List<PedidoDTO> datos = new ArrayList<>();

        void setDatos(List<PedidoDTO> nuevos) {
            datos = new ArrayList<>(nuevos);
            fireTableDataChanged();
        }

        PedidoDTO get(int fila) {
            return datos.get(fila);
        }

        @Override public int getRowCount() { return datos.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            PedidoDTO p = datos.get(fila);
            return switch (col) {
                case 0 -> p.getFolioTexto();
                case 1 -> p.getServicioTexto();
                case 2 -> Formatos.hora(p.getFechaHora());
                case 3 -> p;
                case 4 -> p;
                case 5 -> Formatos.dinero(p.getTotal());
                default -> p;
            };
        }
    }

    /** Columna "Estado" (Activo / Pagado / Cancelado) y "Pago" (Pendiente / Parcial / Pagado). */
    private static class RenderColor extends javax.swing.table.DefaultTableCellRenderer {

        private final boolean esPedido;

        RenderColor(boolean esPedido) {
            this.esPedido = esPedido;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            PedidoDTO p = (PedidoDTO) v;
            String texto;
            Color color = Color.BLACK;
            if (esPedido) {
                texto = Estilos.textoEstadoPedido(p.getEstado());
                color = Estilos.colorEstadoPedido(p.getEstado());
            } else {
                texto = Estilos.textoPago(p);
                color = Estilos.colorPago(p);
            }
            super.getTableCellRendererComponent(t, texto, false, false, r, c);
            return Estilos.pintarCelda(this, t, r, color);
        }
    }

    /** "Editar pedido / Cancelar pedido" si se puede modificar; si no, "Ver detalle". */
    private static class RenderAcciones implements javax.swing.table.TableCellRenderer {

        private final JPanel panel = new JPanel();
        private final JLabel primero = Estilos.texto("", Estilos.ENLACE, Estilos.NARANJA);
        private final JLabel segundo = Estilos.texto("Cancelar pedido", Estilos.ENLACE, Estilos.GUINDA);

        RenderAcciones() {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(0, 20, 0, 8));
            panel.add(Box.createVerticalGlue());
            panel.add(primero);
            panel.add(Box.createRigidArea(new Dimension(0, 4)));
            panel.add(segundo);
            panel.add(Box.createVerticalGlue());
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            PedidoDTO p = (PedidoDTO) v;
            primero.setText(p.isModificable() ? "Editar pedido" : "Ver detalle");
            segundo.setVisible(p.isModificable());
            panel.setBackground(Estilos.fondoFila(t, r));
            return panel;
        }
    }
}
