package presentacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.dtos.ConfiguracionDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PlatilloDTO;
import negocio.dtos.VariacionDTO;
import negocio.excepciones.NegocioException;

/**
 * "Configuración" (solo administradores), en tres pestañas:
 * Menú (platillos, precios, extras y disponibilidad), Negocio y ticket, y Mi cuenta.
 */
public class PanelConfiguracion extends JPanel implements Recargable {

    private static final String[] PESTANAS = {"Menú", "Negocio y ticket", "Mi cuenta"};
    private static final String[] CATEGORIAS = {"PLATILLO", "BEBIDA"};
    private static final int COL_DISPONIBLE = 4;
    private static final int COL_ACCIONES = 5;

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final BotonRedondeado[] botonesPestana = new BotonRedondeado[PESTANAS.length];
    private final JPanel zonaAviso = new JPanel(new BorderLayout());

    // Menú
    private final ModeloMenu modeloMenu = new ModeloMenu();
    private final JTable tablaMenu = new JTable(modeloMenu);
    private final Paginador<PlatilloDTO> paginador = new Paginador<>(Estilos.FILAS_POR_PAGINA, modeloMenu::setDatos);
    private final JLabel lblConteo = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);

    // Negocio y ticket
    private final JTextField txtNegocio = Estilos.campoTexto("Cenaduría La Comadre");
    private final JTextField txtDireccion = Estilos.campoTexto("Calle, número, colonia");
    private final JTextField txtTelefono = Estilos.campoTexto("644 123 4567");
    private final JTextField txtMensaje = Estilos.campoTexto("¡Gracias por su visita!");
    private final JTextField txtCaja = Estilos.campoTexto("Caja 01");
    private final JSpinner spMesas = Estilos.spinner(20, 1, 99);

    // Mi cuenta
    private final JLabel lblCuenta = Estilos.texto("", Estilos.TEXTO_NEGRITA, Color.BLACK);

    public PanelConfiguracion() {
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);
        pantalla.add(Estilos.encabezado("CONFIGURACIÓN", "Menú, datos del negocio y tu cuenta"), BorderLayout.NORTH);

        for (int i = 0; i < PESTANAS.length; i++) {
            String nombre = PESTANAS[i];
            botonesPestana[i] = Estilos.botonSecundario(nombre);
            botonesPestana[i].addActionListener(e -> mostrarPestana(nombre));
        }
        zonaAviso.setOpaque(false);
        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(Estilos.filaIzquierda(8, botonesPestana), BorderLayout.NORTH);
        arriba.add(zonaAviso, BorderLayout.CENTER);

        contenido.setOpaque(false);
        contenido.add(crearMenu(), PESTANAS[0]);
        contenido.add(PanelDetallePedido.envolver(crearNegocio()), PESTANAS[1]);
        contenido.add(PanelDetallePedido.envolver(crearCuenta()), PESTANAS[2]);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(arriba, BorderLayout.NORTH);
        centro.add(contenido, BorderLayout.CENTER);
        pantalla.add(centro, BorderLayout.CENTER);

        mostrarPestana(PESTANAS[0]);
        recargar();
    }

    @Override
    public void recargar() {
        cargarMenu();
        cargarNegocio();
        if (ContextoApp.getAdministrador() != null) {
            lblCuenta.setText(ContextoApp.getAdministrador().getNombre() + " · "
                    + ContextoApp.getAdministrador().getNombreUsuario());
        }
    }

    private void mostrarPestana(String nombre) {
        for (int i = 0; i < PESTANAS.length; i++) {
            boolean activa = PESTANAS[i].equals(nombre);
            botonesPestana[i].setRelleno(activa ? Estilos.NARANJA : Color.WHITE);
            botonesPestana[i].setForeground(activa ? Color.WHITE : Color.BLACK);
        }
        zonaAviso.removeAll();
        tarjetas.show(contenido, nombre);
        revalidate();
        repaint();
    }

    private void avisar(Estilos.TipoAviso tipo, String titulo, String detalle) {
        zonaAviso.removeAll();
        zonaAviso.add(Estilos.aviso(tipo, titulo, detalle), BorderLayout.CENTER);
        zonaAviso.revalidate();
        zonaAviso.repaint();
    }

    // =====================================================================
    // Pestaña Menú
    // =====================================================================
    private JComponent crearMenu() {
        Estilos.estilizarTabla(tablaMenu);
        tablaMenu.getColumnModel().getColumn(COL_DISPONIBLE).setCellRenderer(new RenderDisponible());
        tablaMenu.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new RenderEnlace());
        tablaMenu.getColumnModel().getColumn(0).setPreferredWidth(220);
        tablaMenu.getColumnModel().getColumn(3).setPreferredWidth(220);
        tablaMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaMenu.rowAtPoint(e.getPoint());
                int col = tablaMenu.columnAtPoint(e.getPoint());
                if (fila < 0) {
                    return;
                }
                PlatilloDTO p = modeloMenu.get(fila);
                if (col == COL_DISPONIBLE) {
                    cambiarDisponible(p);
                } else if (col == COL_ACCIONES) {
                    abrirPlatillo(p);
                }
            }
        });
        tablaMenu.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = tablaMenu.columnAtPoint(e.getPoint());
                tablaMenu.setCursor(Cursor.getPredefinedCursor(tablaMenu.rowAtPoint(e.getPoint()) >= 0
                        && (col == COL_DISPONIBLE || col == COL_ACCIONES) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        BotonRedondeado agregar = Estilos.boton("+ Agregar platillo", Estilos.NARANJA);
        agregar.addActionListener(e -> abrirPlatillo(null));
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(Estilos.texto("Platillos y bebidas", Estilos.TITULO_TARJETA, Color.BLACK), BorderLayout.WEST);
        barra.add(agregar, BorderLayout.EAST);

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(barra, BorderLayout.NORTH);
        p.add(Estilos.tarjetaTabla(tablaMenu, lblConteo, paginador), BorderLayout.CENTER);
        p.add(Estilos.texto("Haz clic en \"Disponible\" para marcar un platillo como agotado hoy: deja de "
                + "aparecer al tomar pedidos. Cambiar un precio no afecta pedidos ya registrados.",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO), BorderLayout.SOUTH);
        return p;
    }

    private void cargarMenu() {
        try {
            List<PlatilloDTO> platillos = ContextoApp.PLATILLOS.listarTodos();
            paginador.setDatos(platillos);
            lblConteo.setText(platillos.size() + " platillos y bebidas");
        } catch (NegocioException e) {
            paginador.setDatos(List.of());
            lblConteo.setText("No se pudo cargar el menú: " + e.getMessage());
        }
    }

    private void cambiarDisponible(PlatilloDTO p) {
        try {
            ContextoApp.PLATILLOS.cambiarDisponibilidad(p.getId(), !p.isDisponible());
            avisar(Estilos.TipoAviso.EXITO, p.getNombre() + (p.isDisponible() ? " marcado como agotado"
                    : " disponible de nuevo"), p.isDisponible() ? "Ya no aparece al tomar pedidos." : null);
            cargarMenu();
        } catch (NegocioException e) {
            Modal.error(this, "No se pudo cambiar", e.getMessage());
        }
    }

    /** Pop up para agregar (p == null) o editar un platillo con sus extras. */
    private void abrirPlatillo(PlatilloDTO p) {
        boolean edicion = p != null;
        Modal m = new Modal(this, edicion ? "Editar platillo" : "Agregar platillo",
                edicion ? "Cambia los datos de " + p.getNombre() + "." : "Añade un platillo o bebida al menú.", 620);
        JTextField txtNombre = Estilos.campoTexto("Pozole rojo");
        JTextField txtDescripcion = Estilos.campoTexto("Orden grande con tostadas");
        JTextField txtPrecio = Estilos.campoTexto("65.00");
        JComboBox<String> cbCategoria = Estilos.combo(CATEGORIAS, c -> "PLATILLO".equals(c) ? "Platillo" : "Bebida");
        JCheckBox chkDisponible = new JCheckBox("Disponible para vender");
        chkDisponible.setOpaque(false);
        chkDisponible.setFont(Estilos.CAMPO);
        chkDisponible.setSelected(true);
        if (edicion) {
            txtNombre.setText(p.getNombre());
            txtDescripcion.setText(p.getDescripcion() != null ? p.getDescripcion() : "");
            txtPrecio.setText(p.getPrecio().toPlainString());
            cbCategoria.setSelectedItem(p.getCategoria());
            chkDisponible.setSelected(p.isDisponible());
        }
        JPanel form = new JPanel(new GridLayout(2, 3, 12, 10));
        form.setOpaque(false);
        form.add(Estilos.campo("Nombre", txtNombre));
        form.add(Estilos.campo("Precio (MXN)", txtPrecio));
        form.add(Estilos.campo("Categoría", cbCategoria));
        form.add(Estilos.campo("Descripción (opcional)", txtDescripcion));
        form.add(Estilos.campo(" ", chkDisponible));
        m.agregar(form);

        // Extras: filas nombre + precio + quitar
        List<Object[]> extras = new ArrayList<>(); // {Long id, JTextField nombre, JTextField precio, JPanel fila}
        JPanel listaExtras = new JPanel();
        listaExtras.setLayout(new BoxLayout(listaExtras, BoxLayout.Y_AXIS));
        listaExtras.setOpaque(false);
        Runnable[] redibujar = new Runnable[1];
        java.util.function.BiConsumer<VariacionDTO, Boolean> agregarExtra = (v, ignorado) -> {
            JTextField n = Estilos.campoTexto("Extra queso");
            JTextField pr = Estilos.campoTexto("15.00");
            if (v != null) {
                n.setText(v.nombre());
                pr.setText(v.precioExtra().toPlainString());
            }
            JLabel quitar = Estilos.texto("Quitar", Estilos.ENLACE, Estilos.GUINDA);
            quitar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JPanel fila = new JPanel(new BorderLayout(10, 0));
            fila.setOpaque(false);
            JPanel campos = new JPanel(new GridLayout(1, 2, 10, 0));
            campos.setOpaque(false);
            campos.add(n);
            campos.add(pr);
            fila.add(campos, BorderLayout.CENTER);
            fila.add(quitar, BorderLayout.EAST);
            fila.setBorder(new EmptyBorder(0, 0, 6, 0));
            Object[] registro = {v != null ? v.id() : null, n, pr, fila};
            extras.add(registro);
            quitar.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    extras.remove(registro);
                    redibujar[0].run();
                }
            });
        };
        redibujar[0] = () -> {
            listaExtras.removeAll();
            if (extras.isEmpty()) {
                listaExtras.add(Estilos.texto("Sin extras. Ej.: \"Extra queso\" por $15.00.", Estilos.TEXTO_CHICO,
                        Estilos.GRIS_TEXTO));
            }
            for (Object[] r : extras) {
                ((JComponent) r[3]).setAlignmentX(LEFT_ALIGNMENT);
                listaExtras.add((JComponent) r[3]);
            }
            listaExtras.revalidate();
            listaExtras.repaint();
            m.pack();
        };
        if (edicion) {
            p.getVariaciones().forEach(v -> agregarExtra.accept(v, true));
        }
        JLabel tituloExtras = Estilos.texto("Extras con costo (nombre y precio extra)", Estilos.ETIQUETA, Color.BLACK);
        BotonRedondeado masExtra = Estilos.botonSecundario("+ Agregar extra");
        masExtra.addActionListener(e -> {
            agregarExtra.accept(null, true);
            redibujar[0].run();
        });
        m.agregar(tituloExtras);
        m.agregar(listaExtras);
        m.agregar(Estilos.filaIzquierda(0, masExtra));
        JLabel error = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.ROJO);
        m.agregar(error);
        redibujar[0].run();

        m.boton("Cancelar", null, null);
        m.botonAccion(edicion ? "Guardar cambios" : "Agregar platillo", Estilos.NARANJA, () -> {
            try {
                BigDecimal precio = dinero(txtPrecio.getText(), "El precio");
                List<VariacionDTO> vs = new ArrayList<>();
                for (Object[] r : extras) {
                    vs.add(new VariacionDTO((Long) r[0], ((JTextField) r[1]).getText(),
                            dinero(((JTextField) r[2]).getText(), "El precio del extra")));
                }
                PlatilloDTO dto = new PlatilloDTO(edicion ? p.getId() : null, txtNombre.getText(),
                        txtDescripcion.getText(), precio, (String) cbCategoria.getSelectedItem(),
                        chkDisponible.isSelected(), vs);
                if (edicion) {
                    ContextoApp.PLATILLOS.actualizarPlatillo(dto);
                } else {
                    PlatilloDTO nuevo = ContextoApp.PLATILLOS.registrarPlatillo(dto);
                    if (!chkDisponible.isSelected()) {
                        ContextoApp.PLATILLOS.cambiarDisponibilidad(nuevo.getId(), false);
                    }
                }
                m.cerrar(true);
            } catch (NegocioException ex) {
                error.setText(ex.getMessage());
            }
        });
        if (Boolean.TRUE.equals(m.mostrar())) {
            avisar(Estilos.TipoAviso.EXITO, edicion ? "Platillo actualizado" : "Platillo agregado",
                    txtNombre.getText().trim());
            cargarMenu();
        }
    }

    private static BigDecimal dinero(String texto, String campo) throws NegocioException {
        try {
            return new BigDecimal(texto.replace("$", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new NegocioException(campo + " debe ser un número, por ejemplo 65 o 65.50");
        }
    }

    // =====================================================================
    // Pestaña Negocio y ticket
    // =====================================================================
    private JComponent crearNegocio() {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        PanelPedidos.agregarIzq(t, Estilos.texto("Datos del ticket", Estilos.TITULO_TARJETA, Color.BLACK));
        JPanel form = new JPanel(new GridLayout(2, 2, 14, 10));
        form.setOpaque(false);
        form.add(Estilos.campo("Nombre del negocio", txtNegocio));
        form.add(Estilos.campo("Teléfono", txtTelefono));
        form.add(Estilos.campo("Dirección", txtDireccion));
        form.add(Estilos.campo("Mensaje al pie del ticket", txtMensaje));
        PanelPedidos.agregarIzq(t, form);
        PanelPedidos.agregarIzq(t, Estilos.texto("Caja", Estilos.TITULO_TARJETA, Color.BLACK));
        JPanel caja = new JPanel(new GridLayout(1, 2, 14, 10));
        caja.setOpaque(false);
        caja.add(Estilos.campo("Nombre de la caja", txtCaja));
        caja.add(Estilos.campo("Número de mesas", spMesas));
        PanelPedidos.agregarIzq(t, caja);
        BotonRedondeado guardar = Estilos.boton("Guardar cambios", Estilos.NARANJA);
        guardar.addActionListener(e -> guardarNegocio());
        PanelPedidos.agregarIzq(t, Estilos.filaIzquierda(0, guardar));
        return t;
    }

    private void cargarNegocio() {
        ConfiguracionDTO c = ContextoApp.configuracion();
        txtNegocio.setText(c.getNombreNegocio());
        txtDireccion.setText(nulo(c.getDireccion()));
        txtTelefono.setText(nulo(c.getTelefono()));
        txtMensaje.setText(nulo(c.getMensajeTicket()));
        txtCaja.setText(c.getNombreCaja());
        spMesas.setValue(c.getNumeroMesas());
    }

    private void guardarNegocio() {
        try {
            ContextoApp.CONFIGURACION.guardar(new ConfiguracionDTO(txtNegocio.getText(), txtDireccion.getText(),
                    txtTelefono.getText(), txtMensaje.getText(), txtCaja.getText(), (int) spMesas.getValue()));
            avisar(Estilos.TipoAviso.EXITO, "Datos guardados",
                    "Se usan en el ticket y en el encabezado de las pantallas.");
        } catch (NegocioException e) {
            avisar(Estilos.TipoAviso.ERROR, "No se pudo guardar", e.getMessage());
        }
    }

    // =====================================================================
    // Pestaña Mi cuenta
    // =====================================================================
    private JComponent crearCuenta() {
        JPasswordField actual = new JPasswordField();
        JPasswordField nueva = new JPasswordField();
        JPasswordField confirmar = new JPasswordField();
        for (JPasswordField f : new JPasswordField[]{actual, nueva, confirmar}) {
            Estilos.estilizar(f);
        }
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        PanelPedidos.agregarIzq(t, Estilos.texto("Cambiar contraseña", Estilos.TITULO_TARJETA, Color.BLACK));
        JPanel form = new JPanel(new GridLayout(1, 3, 14, 0));
        form.setOpaque(false);
        form.add(Estilos.campo("Contraseña actual", actual));
        form.add(Estilos.campo("Nueva contraseña", nueva));
        form.add(Estilos.campo("Confirmar nueva", confirmar));
        PanelPedidos.agregarIzq(t, form);
        BotonRedondeado guardar = Estilos.boton("Cambiar contraseña", Estilos.NARANJA);
        guardar.addActionListener(e -> {
            String n = new String(nueva.getPassword());
            if (!n.equals(new String(confirmar.getPassword()))) {
                avisar(Estilos.TipoAviso.ERROR, "Las contraseñas no coinciden", null);
                return;
            }
            try {
                ContextoApp.USUARIOS.cambiarContrasena(ContextoApp.getAdministrador().getId(),
                        new String(actual.getPassword()), n);
                actual.setText("");
                nueva.setText("");
                confirmar.setText("");
                avisar(Estilos.TipoAviso.EXITO, "Contraseña actualizada", "Úsala la próxima vez que inicies sesión.");
            } catch (NegocioException ex) {
                avisar(Estilos.TipoAviso.ERROR, "No se pudo cambiar la contraseña", ex.getMessage());
            }
        });
        PanelPedidos.agregarIzq(t, Estilos.filaIzquierda(0, guardar));

        PanelRedondeado sesion = Estilos.tarjeta();
        sesion.setLayout(new BoxLayout(sesion, BoxLayout.Y_AXIS));
        PanelPedidos.agregarIzq(sesion, Estilos.texto("Sesión", Estilos.TITULO_TARJETA, Color.BLACK));
        PanelPedidos.agregarIzq(sesion, lblCuenta);
        PanelPedidos.agregarIzq(sesion, Estilos.texto("Al cerrar sesión la caja sigue funcionando para "
                + "registrar y cobrar pedidos.", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        BotonRedondeado salir = Estilos.boton("Cerrar sesión", Estilos.GUINDA);
        salir.addActionListener(e -> ContextoApp.cerrarSesion());
        PanelPedidos.agregarIzq(sesion, Estilos.filaIzquierda(0, salir));

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        PanelPedidos.agregarIzq(col, t);
        PanelPedidos.agregarIzq(col, sesion);
        return col;
    }

    private static String nulo(String s) {
        return s == null ? "" : s;
    }

    // ============================================================ Tabla del menú
    private static class ModeloMenu extends AbstractTableModel {

        private final String[] columnas = {"Platillo", "Categoría", "Precio", "Extras", "Disponible", "Acciones"};
        private List<PlatilloDTO> datos = new ArrayList<>();

        void setDatos(List<PlatilloDTO> nuevos) {
            datos = new ArrayList<>(nuevos);
            fireTableDataChanged();
        }

        PlatilloDTO get(int fila) {
            return datos.get(fila);
        }

        @Override public int getRowCount() { return datos.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            PlatilloDTO p = datos.get(fila);
            return switch (col) {
                case 0 -> p.getNombre();
                case 1 -> "PLATILLO".equals(p.getCategoria()) ? "Platillo" : "Bebida";
                case 2 -> Formatos.dinero(p.getPrecio());
                case 3 -> p.getVariaciones().isEmpty() ? "—" : p.getVariaciones().stream()
                        .map(v -> v.nombre() + " +" + Formatos.dinero(v.precioExtra()))
                        .collect(Collectors.joining(", "));
                case 4 -> p.isDisponible();
                default -> "Editar";
            };
        }
    }

    private static class RenderDisponible extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            boolean disponible = (Boolean) v;
            super.getTableCellRendererComponent(t, disponible ? "● Sí" : "○ Agotado hoy", false, false, r, c);
            return Estilos.pintarCelda(this, t, r, disponible ? Estilos.VERDE : Estilos.GRIS_TEXTO);
        }
    }

    private static class RenderEnlace extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            Estilos.pintarCelda(this, t, r, Estilos.NARANJA);
            setFont(Estilos.ENLACE);
            return this;
        }
    }
}
