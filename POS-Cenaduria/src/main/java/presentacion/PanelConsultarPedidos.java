package presentacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.dtos.EventoPedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IPedidoService;

/**
 * CU Consultar pedido · listado del turno con filtros por folio, fecha y estado.
 * Activo permite cobrar; Pagado y Cancelado solo permiten ver el detalle.
 * Se actualiza solo cuando otra pantalla cambia un pedido, y conserva los filtros.
 */
public class PanelConsultarPedidos extends JPanel {

    private static final int COL_ACCIONES = 6;
    private static final String[] FILTROS = {"Todos", "Activos", "Pagados", "Cancelados"};
    private static final String[] ESTADOS = {null, "ACTIVO", "PAGADO", "CANCELADO"};

    private final PuntoDeVentaView nav;
    private final IPedidoService servicio = ContextoApp.PEDIDOS;

    private final JTextField txtFolio = Estilos.campoTexto("Buscar folio...");
    private final JComboBox<LocalDate> cbFecha = Estilos.combo(ultimosDias(), Formatos::fecha);
    private final JComboBox<String> cbEstado = Estilos.combo(FILTROS);
    private final JLabel lblSubtitulo = Estilos.texto("", Estilos.SUBTITULO, Estilos.GRIS_TEXTO);
    private final JLabel lblEstado = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final JLabel lblConteo = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final JLabel lblErrorDetalle = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);

    private final ModeloConsulta modelo = new ModeloConsulta();
    private final Paginador<PedidoDTO> paginador = new Paginador<>(Estilos.FILAS_POR_PAGINA, modelo::setDatos);
    private final JTable tabla = new JTable(modelo);
    private final CardLayout vistas = new CardLayout();
    private final JPanel zonaResultados = new JPanel(vistas);

    public PanelConsultarPedidos(PuntoDeVentaView nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);

        JPanel encabezado = Estilos.encabezado("CONSULTAR PEDIDO", null);
        encabezado.add(lblSubtitulo, BorderLayout.CENTER);
        pantalla.add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(crearFiltros(), BorderLayout.NORTH);
        zonaResultados.setOpaque(false);
        zonaResultados.add(crearTabla(), "tabla");
        zonaResultados.add(crearVacio(), "vacio");
        zonaResultados.add(crearError(), "error");
        centro.add(zonaResultados, BorderLayout.CENTER);
        pantalla.add(centro, BorderLayout.CENTER);
        pantalla.add(Estilos.texto("Filtros: Todos · Activos · Pagados · Cancelados. Activo permite cobrar; "
                + "Pagado y Cancelado solo permiten ver el detalle.", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO),
                BorderLayout.SOUTH);

        // Actualización en tiempo real: llega el aviso, se recarga conservando filtros
        servicio.agregarEscucha(e -> SwingUtilities.invokeLater(() -> consultar(e)));
    }

    // ------------------------------------------------------------ Construcción
    private JPanel crearFiltros() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        cbFecha.addActionListener(e -> consultar(null));
        cbEstado.addActionListener(e -> consultar(null));
        txtFolio.addActionListener(e -> consultar(null));

        JPanel campos = new JPanel(new GridLayout(1, 3, 14, 0));
        campos.setOpaque(false);
        campos.add(Estilos.campo("Folio", txtFolio));
        campos.add(Estilos.campo("Fecha", cbFecha));
        campos.add(Estilos.campo("Estado del pedido", cbEstado));
        campos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        PanelPedidos.agregarIzq(p, campos);

        BotonRedondeado btnActualizar = Estilos.boton("Actualizar", Estilos.NARANJA);
        btnActualizar.addActionListener(e -> consultar(null));
        BotonRedondeado btnLimpiar = Estilos.botonSecundario("Limpiar filtros");
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        BotonRedondeado btnVolver = Estilos.botonSecundario("Volver a pedidos");
        btnVolver.addActionListener(e -> nav.irAPedidos());
        JPanel acciones = new JPanel(new BorderLayout());
        acciones.setOpaque(false);
        acciones.add(Estilos.filaIzquierda(10, btnActualizar, btnLimpiar, lblEstado), BorderLayout.WEST);
        acciones.add(btnVolver, BorderLayout.EAST);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        PanelPedidos.agregarIzq(p, acciones);
        return p;
    }

    private JPanel crearTabla() {
        Estilos.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RenderPago());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new RenderBoton());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setPreferredWidth(150);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila < 0) {
                    return;
                }
                PedidoDTO p = modelo.get(fila);
                boolean enBoton = tabla.columnAtPoint(e.getPoint()) == COL_ACCIONES;
                if (enBoton && p.esActivo()) {
                    nav.irACobrar(p.getId());
                } else {
                    nav.irADetalle(p.getId(), nav::irAConsultar);
                }
            }
        });
        tabla.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                tabla.setCursor(Cursor.getPredefinedCursor(tabla.rowAtPoint(e.getPoint()) >= 0
                        ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        PanelRedondeado tarjeta = Estilos.tarjetaTabla(tabla, lblConteo, paginador);
        return tarjeta;
    }

    /** Lista vacía: el mismo mensaje sirve para turno sin pedidos y para filtros sin coincidencias. */
    private JPanel crearVacio() {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(40, 40, 40, 40));
        PanelPedidos.agregarIzq(t, Estilos.texto("No hay pedidos para mostrar en este estado",
                Estilos.TITULO_GRANDE, Estilos.GUINDA));
        PanelPedidos.agregarIzq(t, Estilos.texto("Sin coincidencias para estos filtros. "
                + "El mismo mensaje aplica si el turno no tiene pedidos.", Estilos.TEXTO, Color.BLACK));
        PanelPedidos.agregarIzq(t, Estilos.texto("Limpia el folio o cambia el filtro: Todos, Activos, "
                + "Pagados o Cancelados.", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        BotonRedondeado corregir = Estilos.boton("Corregir búsqueda", Estilos.NARANJA);
        corregir.addActionListener(e -> {
            txtFolio.requestFocusInWindow();
            txtFolio.selectAll();
        });
        BotonRedondeado limpiar = Estilos.botonSecundario("Limpiar filtros");
        limpiar.addActionListener(e -> limpiarFiltros());
        PanelPedidos.agregarIzq(t, Estilos.filaIzquierda(10, corregir, limpiar));
        return envolverArriba(t);
    }

    /** Error de consulta: no se confunde con lista vacía y no muestra datos viejos. */
    private JPanel crearError() {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(40, 40, 40, 40));
        PanelPedidos.agregarIzq(t, Estilos.texto("Error de consulta",
                Estilos.TITULO_GRANDE, Estilos.GUINDA));
        PanelPedidos.agregarIzq(t, Estilos.texto("No se pudo conectar a la base de datos o la consulta falló.",
                Estilos.TEXTO, Color.BLACK));
        PanelPedidos.agregarIzq(t, Estilos.texto("No se muestran datos obsoletos. Revisa la conexión y "
                + "vuelve a intentar.", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        PanelPedidos.agregarIzq(t, lblErrorDetalle);
        BotonRedondeado reintentar = Estilos.boton("Reintentar", Estilos.NARANJA);
        reintentar.addActionListener(e -> consultar(null));
        BotonRedondeado menu = Estilos.botonSecundario("Menú principal");
        menu.addActionListener(e -> nav.irAPedidos());
        PanelPedidos.agregarIzq(t, Estilos.filaIzquierda(10, reintentar, menu));
        return envolverArriba(t);
    }

    private static JPanel envolverArriba(JPanel contenido) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(contenido, BorderLayout.NORTH);
        return p;
    }

    // ------------------------------------------------------------ Consulta
    /**
     * Ejecuta la consulta con los filtros actuales.
     * @param evento si la recarga la provocó un cambio en otra pantalla, se informa cuál
     */
    public void consultar(EventoPedidoDTO evento) {
        int filtro = cbEstado.getSelectedIndex();
        String nombreFiltro = FILTROS[filtro];
        lblSubtitulo.setText(filtro == 0 && txtFolio.getText().isBlank()
                ? "Listado del turno actual · Filtro Todos predeterminado"
                : "Filtro " + nombreFiltro + (txtFolio.getText().isBlank() ? "" : " + folio " + txtFolio.getText().trim())
                + " · Conserva el turno actual");
        try {
            LocalDate fecha = (LocalDate) cbFecha.getSelectedItem();
            List<PedidoDTO> pedidos = servicio.consultar(txtFolio.getText(), fecha, ESTADOS[filtro]);
            paginador.setDatos(pedidos);
            lblConteo.setText(pedidos.size() + " pedidos" + (filtro == 0 ? "" : " " + nombreFiltro.toLowerCase())
                    + " · " + (LocalDate.now().equals(fecha) ? "Turno actual" : Formatos.fecha(fecha)));
            lblEstado.setText(evento != null
                    ? evento.texto()
                    : "Filtro " + nombreFiltro + " · Actualizado " + Formatos.hora(LocalDateTime.now()));
            vistas.show(zonaResultados, pedidos.isEmpty() ? "vacio" : "tabla");
        } catch (NegocioException e) {
            paginador.setDatos(List.of()); // nunca se dejan datos viejos a la vista
            lblErrorDetalle.setText("Detalle: " + e.getMessage());
            lblEstado.setText("Error de consulta · " + Formatos.hora(LocalDateTime.now()));
            vistas.show(zonaResultados, "error");
        }
    }

    private void limpiarFiltros() {
        paginador.reiniciar();
        txtFolio.setText("");
        cbFecha.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
        consultar(null);
    }

    private static LocalDate[] ultimosDias() {
        LocalDate[] dias = new LocalDate[7];
        for (int i = 0; i < dias.length; i++) {
            dias[i] = LocalDate.now().minusDays(i);
        }
        return dias;
    }

    // ============================================================ Tabla
    private static class ModeloConsulta extends AbstractTableModel {

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
                case 5 -> Formatos.dinero(p.getTotal());
                default -> p;
            };
        }
    }

    private static class RenderEstado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            PedidoDTO p = (PedidoDTO) v;
            super.getTableCellRendererComponent(t, Estilos.textoEstadoPedido(p.getEstado()), false, false, r, c);
            return Estilos.pintarCelda(this, t, r, Estilos.colorEstadoPedido(p.getEstado()));
        }
    }

    private static class RenderPago extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            PedidoDTO p = (PedidoDTO) v;
            String texto = p.esParcial() ? "Parcial · saldo " + Formatos.dinero(p.getSaldo()) : Estilos.textoPago(p);
            super.getTableCellRendererComponent(t, texto, false, false, r, c);
            return Estilos.pintarCelda(this, t, r, Estilos.colorPago(p));
        }
    }

    /** Botón naranja "Cobrar" (Activo) o "Ver detalle" (Pagado / Cancelado). */
    private static class RenderBoton implements javax.swing.table.TableCellRenderer {

        private final JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 11));
        private final BotonRedondeado boton = Estilos.boton("Ver detalle", Estilos.NARANJA);

        RenderBoton() {
            boton.setPreferredSize(new Dimension(124, 30));
            boton.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(boton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            PedidoDTO p = (PedidoDTO) v;
            boton.setText(p.esActivo() ? "Cobrar" : "Ver detalle");
            panel.setBackground(Estilos.fondoFila(t, r));
            return panel;
        }
    }
}
