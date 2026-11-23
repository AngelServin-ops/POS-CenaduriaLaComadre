package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PedidoDTO;
import negocio.dtos.PlatilloDTO;
import negocio.dtos.VariacionDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IPedidoService;

/**
 * CU Gestionar pedido · "AGREGAR PEDIDO" y "EDITAR PEDIDO".
 * Los cambios se hacen sobre una copia y solo se aplican al pulsar Guardar.
 */
public class PanelEditorPedido extends JPanel {

    private static final int COL_CANTIDAD = 2;
    private static final VariacionDTO SIN_EXTRA = new VariacionDTO(null, "Sin extra", BigDecimal.ZERO);
    private static final String[] TIPOS = {"EN_MESA", "PARA_LLEVAR", "DOMICILIO"};

    private final PuntoDeVentaView nav;
    private final IPedidoService servicio = ContextoApp.PEDIDOS;
    private final PedidoDTO original;          // null = agregar
    private final List<DetallePedidoDTO> lineas = new ArrayList<>();

    private final JComboBox<String> cbTipo = Estilos.combo(TIPOS, Estilos::textoTipo);
    private final JComboBox<String> cbMesa = Estilos.combo(mesas());
    private final JComboBox<PlatilloDTO> cbProducto = Estilos.combo(new PlatilloDTO[0]);
    private final JComboBox<VariacionDTO> cbExtra = Estilos.combo(new VariacionDTO[0]);
    private final JSpinner spCantidad = Estilos.spinner(1, 1, 99);
    private final JTextField txtNota = Estilos.campoTexto("Ej. Sin cebolla");
    private final BotonRedondeado btnAgregar = Estilos.boton("Agregar producto", Estilos.NARANJA);

    private final ModeloLineas modelo = new ModeloLineas();
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblProductos = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final JLabel lblTotal = Estilos.texto("", Estilos.TOTAL, Color.BLACK);
    private final JLabel lblCambios = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.GUINDA);

    public PanelEditorPedido(PuntoDeVentaView nav, PedidoDTO pedido) {
        this.nav = nav;
        this.original = pedido;
        if (pedido != null) {
            pedido.getDetalles().forEach(d -> lineas.add(d.copia()));
        }

        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);
        pantalla.add(crearEncabezado(), BorderLayout.NORTH);
        pantalla.add(crearTarjeta(), BorderLayout.CENTER);

        cargarPlatillos();
        cargarEncabezado();
        actualizarVista();
    }

    // ------------------------------------------------------------ Construcción
    private JPanel crearEncabezado() {
        boolean edicion = original != null;
        String folio;
        LocalDateTime cuando;
        if (edicion) {
            folio = original.getFolioTexto();
            cuando = original.getFechaHora();
        } else {
            try {
                folio = Formatos.folioPedido(servicio.siguienteFolio());
            } catch (NegocioException e) {
                folio = "Folio nuevo";
            }
            cuando = LocalDateTime.now();
        }
        return Estilos.encabezado(edicion ? "EDITAR PEDIDO" : "AGREGAR PEDIDO",
                folio + " · " + Formatos.fechaHora(cuando) + " · Los cambios se aplican al guardar.");
    }

    private JPanel crearTarjeta() {
        JPanel col = new JPanel(new BorderLayout(0, 14));
        col.setOpaque(false);

        // --- Encabezado del pedido
        cbTipo.addActionListener(e -> cbMesa.setEnabled("EN_MESA".equals(cbTipo.getSelectedItem())));
        JTextField estado = Estilos.campoLectura("Activo · Pago pendiente");
        JPanel fila1 = new JPanel(new GridLayout(1, 3, 12, 0));
        fila1.setOpaque(false);
        fila1.add(Estilos.campo("Tipo de servicio", cbTipo));
        fila1.add(Estilos.campo("Mesa", cbMesa));
        fila1.add(Estilos.campo("Estado del pedido", estado));

        // --- Selector de producto
        estilizarSpinner();
        cbProducto.addActionListener(e -> cargarExtras());
        cbExtra.addActionListener(e -> actualizarBoton());
        txtNota.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarBoton(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarBoton(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarBoton(); }
        });
        btnAgregar.addActionListener(e -> agregarOActualizar());
        btnAgregar.setPreferredSize(new Dimension(170, 34));

        JPanel fila2 = new JPanel(new GridBagLayout());
        fila2.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.SOUTH;
        gc.insets = new Insets(0, 0, 0, 12);
        gc.gridy = 0;
        Object[][] celdas = {
                {"Producto", cbProducto, 2.0},
                {"Extra", cbExtra, 1.0},
                {"Cantidad", spCantidad, 0.0},
                {"Nota del cliente", txtNota, 1.6},
                {" ", btnAgregar, 0.0}};
        for (int i = 0; i < celdas.length; i++) {
            gc.gridx = i;
            gc.weightx = (double) celdas[i][2];
            if (i == celdas.length - 1) {
                gc.insets = new Insets(0, 0, 0, 0);
            }
            fila2.add(Estilos.campo((String) celdas[i][0], (JComponent) celdas[i][1]), gc);
        }

        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setOpaque(false);
        form.add(fila1, BorderLayout.NORTH);
        form.add(fila2, BorderLayout.CENTER);
        col.add(form, BorderLayout.NORTH);

        // --- Tabla de productos
        PanelRedondeado tarjeta = Estilos.tarjeta();
        tarjeta.setLayout(new BorderLayout(0, 10));
        configurarTabla();
        tarjeta.add(Estilos.scroll(tabla), BorderLayout.CENTER);

        JPanel pie = new JPanel();
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
        pie.setOpaque(false);
        JPanel totales = new JPanel(new BorderLayout());
        totales.setOpaque(false);
        totales.add(lblProductos, BorderLayout.WEST);
        totales.add(lblTotal, BorderLayout.EAST);
        PanelPedidos.agregarIzq(pie, totales);
        PanelPedidos.agregarIzq(pie, lblCambios);

        BotonRedondeado btnCancelar = Estilos.boton("Cancelar", Estilos.GUINDA);
        btnCancelar.addActionListener(e -> nav.irAPedidos());
        BotonRedondeado btnGuardar = Estilos.boton(original == null ? "Guardar pedido" : "Guardar cambios",
                Estilos.NARANJA);
        btnGuardar.addActionListener(e -> guardar());
        JPanel botones = new JPanel(new BorderLayout());
        botones.setOpaque(false);
        botones.add(btnCancelar, BorderLayout.WEST);
        JLabel pago = Estilos.texto("Pago: Pendiente · Sin cobros registrados", Estilos.TEXTO_CHICO,
                Estilos.GRIS_TEXTO);
        pago.setHorizontalAlignment(SwingConstants.CENTER);
        botones.add(pago, BorderLayout.CENTER);
        botones.add(btnGuardar, BorderLayout.EAST);
        PanelPedidos.agregarIzq(pie, botones);
        tarjeta.add(pie, BorderLayout.SOUTH);

        col.add(tarjeta, BorderLayout.CENTER);
        return col;
    }

    private void configurarTabla() {
        Estilos.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(240);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(170);
        tabla.getColumnModel().getColumn(2).setMinWidth(120);
        tabla.getColumnModel().getColumn(3).setMinWidth(120);
        tabla.getColumnModel().getColumn(4).setMinWidth(100);
        tabla.getColumnModel().getColumn(COL_CANTIDAD).setCellRenderer(new RenderCantidad());
        tabla.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                Estilos.pintarCelda(this, t, r, Estilos.GUINDA);
                setFont(Estilos.TEXTO_CHICO);
                return this;
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila < 0) {
                    return;
                }
                if (tabla.columnAtPoint(e.getPoint()) == COL_CANTIDAD) {
                    Rectangle celda = tabla.getCellRect(fila, COL_CANTIDAD, false);
                    int x = e.getX() - celda.x;
                    if (x < celda.width / 3) {
                        cambiarCantidad(fila, -1);
                    } else if (x > celda.width * 2 / 3) {
                        cambiarCantidad(fila, +1);
                    }
                    return;
                }
                seleccionarLinea(lineas.get(fila));
            }
        });
        tabla.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                tabla.setCursor(Cursor.getPredefinedCursor(tabla.rowAtPoint(e.getPoint()) >= 0
                        ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void estilizarSpinner() {
        spCantidad.setMinimumSize(new Dimension(90, 34));
        spCantidad.addChangeListener(e -> actualizarBoton());
    }

    // ------------------------------------------------------------ Carga
    private void cargarPlatillos() {
        try {
            for (PlatilloDTO p : ContextoApp.PLATILLOS.listarDisponibles()) {
                cbProducto.addItem(p);
            }
        } catch (NegocioException e) {
            Modal.error(this, "No se pudo cargar el menú", e.getMessage());
        }
        cargarExtras();
    }

    private void cargarExtras() {
        cbExtra.removeAllItems();
        cbExtra.addItem(SIN_EXTRA);
        PlatilloDTO p = (PlatilloDTO) cbProducto.getSelectedItem();
        if (p != null) {
            p.getVariaciones().forEach(cbExtra::addItem);
        }
        cbExtra.setEnabled(cbExtra.getItemCount() > 1);
        actualizarBoton();
    }

    private void cargarEncabezado() {
        if (original != null) {
            cbTipo.setSelectedItem(original.getTipo());
            if (original.getMesa() != null) {
                cbMesa.setSelectedItem(Formatos.mesa(original.getMesa()));
            }
        }
        cbMesa.setEnabled("EN_MESA".equals(cbTipo.getSelectedItem()));
    }

    // ------------------------------------------------------------ Captura de productos
    /** La línea que se está capturando (producto + extra + nota). */
    private DetallePedidoDTO lineaCapturada() {
        PlatilloDTO p = (PlatilloDTO) cbProducto.getSelectedItem();
        if (p == null) {
            return null;
        }
        VariacionDTO extra = (VariacionDTO) cbExtra.getSelectedItem();
        List<VariacionDTO> extras = extra == null || extra == SIN_EXTRA ? List.of() : List.of(extra);
        BigDecimal precio = p.getPrecio();
        for (VariacionDTO v : extras) {
            precio = precio.add(v.precioExtra());
        }
        String nota = txtNota.getText().isBlank() ? null : txtNota.getText().trim();
        return new DetallePedidoDTO(null, p.getId(), p.getNombre(), (int) spCantidad.getValue(), precio, extras, nota);
    }

    private DetallePedidoDTO buscarIgual(DetallePedidoDTO capturada) {
        return lineas.stream().filter(l -> l.mismoProducto(capturada)).findFirst().orElse(null);
    }

    /** "Actualizar cantidad" si el producto ya está en el pedido; si no, "Agregar producto". */
    private void actualizarBoton() {
        DetallePedidoDTO c = lineaCapturada();
        btnAgregar.setText(c != null && buscarIgual(c) != null ? "Actualizar cantidad" : "Agregar producto");
    }

    private void agregarOActualizar() {
        DetallePedidoDTO capturada = lineaCapturada();
        if (capturada == null) {
            return;
        }
        DetallePedidoDTO existente = buscarIgual(capturada);
        if (existente != null) {
            existente.setCantidad(capturada.getCantidad());
        } else {
            lineas.add(capturada);
        }
        txtNota.setText("");
        spCantidad.setValue(1);
        actualizarVista();
    }

    private void seleccionarLinea(DetallePedidoDTO l) {
        for (int i = 0; i < cbProducto.getItemCount(); i++) {
            if (cbProducto.getItemAt(i).getId().equals(l.getIdPlatillo())) {
                cbProducto.setSelectedIndex(i);
                break;
            }
        }
        cbExtra.setSelectedItem(SIN_EXTRA);
        for (int i = 0; i < cbExtra.getItemCount(); i++) {
            VariacionDTO v = cbExtra.getItemAt(i);
            if (!l.getVariaciones().isEmpty() && Objects.equals(v.id(), l.getVariaciones().get(0).id())) {
                cbExtra.setSelectedIndex(i);
            }
        }
        txtNota.setText(l.getNota() != null ? l.getNota() : "");
        spCantidad.setValue(l.getCantidad());
        actualizarBoton();
    }

    /** − baja la cantidad (en 1 quita el producto); + la sube. */
    private void cambiarCantidad(int fila, int delta) {
        DetallePedidoDTO l = lineas.get(fila);
        int nueva = l.getCantidad() + delta;
        if (nueva <= 0) {
            lineas.remove(fila);
        } else if (nueva <= 99) {
            l.setCantidad(nueva);
        }
        actualizarVista();
    }

    // ------------------------------------------------------------ Totales y cambios
    private BigDecimal total() {
        return lineas.stream().map(DetallePedidoDTO::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void actualizarVista() {
        modelo.fireTableDataChanged();
        int piezas = lineas.stream().mapToInt(DetallePedidoDTO::getCantidad).sum();
        lblProductos.setText(piezas + " productos · Precios en MXN");
        lblTotal.setText("Total: " + Formatos.dinero(total()));
        actualizarBoton();

        if (original != null) {
            List<String> cambios = ResumenCambios.calcular(original.getDetalles(), lineas);
            if (cambios.isEmpty()) {
                lblCambios.setText(" ");
            } else {
                BigDecimal antes = original.getTotal();
                BigDecimal diferencia = total().subtract(antes);
                String signo = diferencia.signum() >= 0 ? " + " : " − ";
                lblCambios.setText("<html>" + String.join(" · ", cambios) + " · Total anterior "
                        + Formatos.dinero(antes) + signo + Formatos.dinero(diferencia.abs()) + " = "
                        + Formatos.dinero(total()) + "</html>");
            }
        }
    }

    // ------------------------------------------------------------ Guardar
    private void guardar() {
        String tipo = (String) cbTipo.getSelectedItem();
        Integer mesa = "EN_MESA".equals(tipo) ? Integer.valueOf((String) cbMesa.getSelectedItem()) : null;
        PedidoDTO dto = new PedidoDTO(original != null ? original.getId() : null, tipo, mesa,
                original != null ? original.getNotas() : null, lineas);
        try {
            if (original == null) {
                PedidoDTO guardado = servicio.registrarPedido(dto, ContextoApp.getUsuario().getId());
                confirmarAlta(guardado);
                nav.irAPedidos(null, null, guardado.getId());
            } else {
                List<String> cambios = ResumenCambios.calcular(original.getDetalles(), lineas);
                PedidoDTO guardado = servicio.actualizarPedido(dto);
                nav.irAPedidos(
                        Estilos.aviso(Estilos.TipoAviso.EXITO, "Pedido actualizado correctamente",
                                guardado.getFolioTexto() + " · " + (cambios.isEmpty() ? "Sin cambios en productos"
                                        : String.join(" · ", cambios)) + " · Nuevo total: "
                                        + Formatos.dinero(guardado.getTotal()) + " MXN"),
                        tarjetaResumen(guardado, cambios), guardado.getId());
            }
        } catch (NegocioException e) {
            Modal.error(this, "No se pudo guardar el pedido", e.getMessage());
        }
    }

    /** Paso 3: "Pedido guardado". */
    private void confirmarAlta(PedidoDTO p) {
        Modal m = new Modal(this, "Pedido guardado",
                "El pedido " + p.getFolioTexto() + " se agregó correctamente.", 620);
        m.agregar(Estilos.aviso(Estilos.TipoAviso.EXITO, "Alta registrada · " + Formatos.dinero(p.getTotal()) + " MXN",
                p.getServicioTexto() + " · " + p.getTotalProductos() + " productos · "
                        + Formatos.fechaHora(p.getFechaHora())));
        JPanel datos = new JPanel(new GridLayout(1, 2, 20, 0));
        datos.setOpaque(false);
        datos.add(Estilos.dato("Estado del pedido", "Activo", Estilos.NARANJA));
        datos.add(Estilos.dato("Estado del pago", "Pendiente", Estilos.GUINDA));
        m.agregar(datos);
        m.parrafo("El pedido queda activo hasta que se cobre la cuenta completa.");
        m.boton("Aceptar y volver al listado", Estilos.NARANJA, true);
        m.mostrar();
    }

    /** Paso 5: tarjeta "Resumen de cambios" debajo del listado. */
    private JComponent tarjetaResumen(PedidoDTO p, List<String> cambios) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        PanelPedidos.agregarIzq(t, Estilos.texto("Resumen de cambios · " + p.getFolioTexto(),
                Estilos.TITULO_TARJETA, Color.BLACK));
        for (String c : cambios) {
            PanelPedidos.agregarIzq(t, Estilos.texto(c, Estilos.TEXTO, Color.BLACK));
        }
        PanelPedidos.agregarIzq(t, Estilos.texto("Total: " + Formatos.dinero(original.getTotal()) + " → "
                + Formatos.dinero(p.getTotal()), Estilos.TEXTO_NEGRITA, Color.BLACK));
        JPanel datos = new JPanel(new GridLayout(1, 2, 20, 0));
        datos.setOpaque(false);
        datos.add(Estilos.dato("Estado del pedido", Estilos.textoEstadoPedido(p.getEstado()),
                Estilos.colorEstadoPedido(p.getEstado())));
        datos.add(Estilos.dato("Estado del pago", Estilos.textoPago(p), Estilos.GUINDA));
        PanelPedidos.agregarIzq(t, datos);
        return t;
    }

    // ------------------------------------------------------------ Utilidades
    private static String[] mesas() {
        String[] m = new String[ContextoApp.configuracion().getNumeroMesas()];
        for (int i = 0; i < m.length; i++) {
            m[i] = Formatos.mesa(i + 1);
        }
        return m;
    }

    // ============================================================ Tabla
    private class ModeloLineas extends AbstractTableModel {

        private final String[] columnas = {"Producto", "Nota", "Cantidad", "Precio unitario", "Importe"};

        @Override public int getRowCount() { return lineas.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            DetallePedidoDTO l = lineas.get(fila);
            return switch (col) {
                case 0 -> l.descripcion();
                case 1 -> l.getNota() != null ? l.getNota() : "";
                case 2 -> l.getCantidad();
                case 3 -> Formatos.dinero(l.getPrecioUnitario());
                default -> Formatos.dinero(l.calcularSubtotal());
            };
        }
    }

    /** "−   2   +" */
    private static class RenderCantidad extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, "−      " + v + "      +", false, false, r, c);
            Estilos.pintarCelda(this, t, r, Color.BLACK);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(Estilos.TEXTO_NEGRITA);
            return this;
        }
    }
}
