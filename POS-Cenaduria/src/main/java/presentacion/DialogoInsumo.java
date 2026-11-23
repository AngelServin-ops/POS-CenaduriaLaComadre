package presentacion;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import negocio.dtos.InsumoDTO;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.UnidadInsumo;

/**
 * Pop up para agregar o actualizar un insumo.
 * Oscurece la pantalla de fondo mientras está abierto y devuelve un InsumoDTO
 * con los datos capturados, o null si el usuario canceló.
 */
public class DialogoInsumo extends JDialog {

    private static final Color NARANJA = new Color(0xD96A29);
    private static final Color GUINDA = new Color(0x8E0E0E);
    private static final Color FONDO = new Color(0xF7F1EC);
    private static final Color GRIS_TEXTO = new Color(0x666666);
    private static final Color BORDE_CAMPO = new Color(0xDDD5CE);

    private static final Font TITULO = new Font("Georgia", Font.BOLD, 24);
    private static final Font SUBTITULO = new Font("Georgia", Font.PLAIN, 12);
    private static final Font ETIQUETA = new Font("Georgia", Font.PLAIN, 13);
    private static final Font CAMPO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BOTON = new Font("Segoe UI", Font.BOLD, 13);

    private final JTextField txtNombre = new CampoTexto("Pechuga de pollo");
    private final JTextField txtCantidad = new CampoTexto("5");
    private final JComboBox<UnidadInsumo> cbUnidad = new JComboBox<>(UnidadInsumo.values());
    private final JComboBox<CategoriaInsumo> cbCategoria = new JComboBox<>(CategoriaInsumo.values());

    private final Long idInsumo;   // null = agregar, con valor = actualizar
    private InsumoDTO resultado;   // queda en null si se cancela

    // ------------------------------------------------------------ API pública
    /** Abre el pop up "Agregar Insumo". Devuelve null si se canceló. */
    public static InsumoDTO mostrarAgregar(Component padre) {
        return mostrar(padre, null);
    }

    /** Abre el pop up "Actualizar Insumo" con los datos del insumo. Devuelve null si se canceló. */
    public static InsumoDTO mostrarActualizar(Component padre, Insumo insumo) {
        return mostrar(padre, insumo);
    }

    private static InsumoDTO mostrar(Component padre, Insumo insumo) {
        Window ventana = SwingUtilities.getWindowAncestor(padre);
        DialogoInsumo dialogo = new DialogoInsumo(ventana, insumo);

        // Capa oscura sobre la pantalla de inventario (como en el diseño)
        JRootPane raiz = ventana instanceof RootPaneContainer rpc ? rpc.getRootPane() : null;
        Component glassAnterior = raiz != null ? raiz.getGlassPane() : null;
        if (raiz != null) {
            raiz.setGlassPane(new CapaOscura());
            raiz.getGlassPane().setVisible(true);
        }

        dialogo.setVisible(true); // modal: se bloquea aquí hasta cerrar

        if (raiz != null) {
            raiz.getGlassPane().setVisible(false);
            raiz.setGlassPane(glassAnterior);
        }
        return dialogo.resultado;
    }

    // ------------------------------------------------------------ Construcción
    private DialogoInsumo(Window padre, Insumo insumo) {
        super(padre, ModalityType.APPLICATION_MODAL);
        this.idInsumo = insumo != null ? insumo.getId() : null;
        boolean esEdicion = insumo != null;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        PanelRedondeado contenido = new PanelRedondeado(16, FONDO);
        contenido.setLayout(new BorderLayout(0, 22));
        contenido.setBorder(new EmptyBorder(26, 34, 30, 34));

        contenido.add(crearEncabezado(esEdicion), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        cuerpo.add(crearFormulario(), gc);

        gc.weightx = 0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 50, 0, 0);
        cuerpo.add(crearBotones(esEdicion), gc);

        contenido.add(cuerpo, BorderLayout.CENTER);
        setContentPane(contenido);

        if (esEdicion) {
            cargarDatos(insumo);
        }

        // Esc = Cancelar
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setSize(Math.max(getWidth(), 640), getHeight());
        setLocationRelativeTo(padre);
    }

    private JPanel crearEncabezado(boolean esEdicion) {
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);

        JLabel titulo = new JLabel(esEdicion ? "Actualizar Insumo" : "Agregar Insumo");
        titulo.setFont(TITULO);

        JLabel subtitulo = new JLabel(esEdicion
                ? "Actualiza la información del insumo."
                : "Añade la información del insumo.");
        subtitulo.setFont(SUBTITULO);
        subtitulo.setForeground(GRIS_TEXTO);

        encabezado.add(titulo);
        encabezado.add(Box.createRigidArea(new Dimension(0, 4)));
        encabezado.add(subtitulo);
        return encabezado;
    }

    private JPanel crearFormulario() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        estilizarCampo(txtNombre);
        estilizarCampo(txtCantidad);
        estilizarCombo(cbUnidad);
        estilizarCombo(cbCategoria);

        cbUnidad.setRenderer(rendererTexto());
        cbCategoria.setRenderer(rendererTexto());

        agregarCampo(form, "Nombre del Insumo", txtNombre);
        agregarCampo(form, "Cantidad", txtCantidad);
        agregarCampo(form, "Unidad", cbUnidad);
        agregarCampo(form, "Categoría", cbCategoria);
        return form;
    }

    private JPanel crearBotones(boolean esEdicion) {
        JPanel botones = new JPanel();
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        botones.setOpaque(false);

        BotonRedondeado btnGuardar = crearBoton(esEdicion ? "Guardar Insumo" : "Agregar Insumo", NARANJA);
        btnGuardar.addActionListener(e -> guardar());

        BotonRedondeado btnCancelar = crearBoton("Cancelar", GUINDA);
        btnCancelar.addActionListener(e -> dispose());

        botones.add(btnGuardar);
        botones.add(Box.createRigidArea(new Dimension(0, 28)));
        botones.add(btnCancelar);

        getRootPane().setDefaultButton(btnGuardar); // Enter = guardar
        return botones;
    }

    // ------------------------------------------------------------ Lógica
    private void cargarDatos(Insumo insumo) {
        txtNombre.setText(insumo.getNombreInsumo());
        float s = insumo.getStock();
        txtCantidad.setText(s == (long) s ? String.valueOf((long) s) : String.valueOf(s));
        cbUnidad.setSelectedItem(insumo.getUnidadInsumo());
        if (insumo.getCategoria() != null) {
            cbCategoria.setSelectedItem(insumo.getCategoria());
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            error("El nombre del insumo es obligatorio.", txtNombre);
            return;
        }

        float cantidad;
        try {
            cantidad = Float.parseFloat(txtCantidad.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            error("La cantidad debe ser un número.", txtCantidad);
            return;
        }
        if (cantidad < 0) {
            error("La cantidad no puede ser negativa.", txtCantidad);
            return;
        }

        UnidadInsumo unidad = (UnidadInsumo) cbUnidad.getSelectedItem();
        CategoriaInsumo categoria = (CategoriaInsumo) cbCategoria.getSelectedItem();

        // El estado lo calcula InsumoService, por eso va en null
        resultado = new InsumoDTO(idInsumo, nombre, cantidad, null,
                unidad.name(), categoria.name(), null);
        dispose();
    }

    private void error(String mensaje, JComponent campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        campo.requestFocusInWindow();
    }

    // ------------------------------------------------------------ Estilos
    private void agregarCampo(JPanel form, String etiqueta, JComponent campo) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(ETIQUETA);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(lbl);
        form.add(Box.createRigidArea(new Dimension(0, 5)));
        form.add(campo);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(CAMPO);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        Dimension tam = new Dimension(280, 34);
        campo.setPreferredSize(tam);
        campo.setMaximumSize(tam);
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(CAMPO);
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);
        Dimension tam = new Dimension(280, 34);
        combo.setPreferredSize(tam);
        combo.setMaximumSize(tam);
    }

    private BotonRedondeado crearBoton(String texto, Color color) {
        BotonRedondeado b = new BotonRedondeado(texto, color);
        b.setForeground(Color.WHITE);
        b.setFont(BOTON);
        Dimension tam = new Dimension(180, 38);
        b.setPreferredSize(tam);
        b.setMaximumSize(tam);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    private static DefaultListCellRenderer rendererTexto() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                                                          boolean sel, boolean foco) {
                return super.getListCellRendererComponent(lista, texto(valor), i, sel, foco);
            }
        };
    }

    private static String texto(Object valor) {
        if (valor instanceof UnidadInsumo u) {
            return switch (u) {
                case KG -> "kg";
                case PIEZA -> "pzs";
                case PAQUETE -> "paquetes";
            };
        }
        if (valor instanceof CategoriaInsumo c) {
            return switch (c) {
                case CARNES -> "Carnes";
                case VERDURAS -> "Verduras";
            };
        }
        return valor == null ? "" : valor.toString();
    }

    // ============================================================ Campo con texto de ejemplo
    private static class CampoTexto extends JTextField {

        private final String placeholder;

        CampoTexto(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0xAAAAAA));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, getInsets().left,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        }
    }

    // ============================================================ Capa oscura de fondo
    private static class CapaOscura extends JComponent {

        CapaOscura() {
            // Consume los clics para que no lleguen a la pantalla de atrás
            addMouseListener(new java.awt.event.MouseAdapter() { });
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 110));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // ============================================================ Panel con esquinas redondeadas
    private static class PanelRedondeado extends JPanel {

        private final int radio;

        PanelRedondeado(int radio, Color fondo) {
            this.radio = radio;
            setBackground(fondo);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}