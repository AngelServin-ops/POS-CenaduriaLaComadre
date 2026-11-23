package presentacion;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import negocio.dtos.InsumoDTO;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.MotivoMovimiento;
import persistencia.enums.UnidadInsumo;

/**
 * Pop up para agregar o actualizar un insumo.
 * Oscurece la pantalla de fondo mientras está abierto y devuelve un InsumoDTO
 * con los datos capturados, o null si el usuario canceló.
 * Al actualizar también devuelve el motivo del cambio de cantidad, porque
 * el stock solo cambia mediante movimientos (compra, consumo del día, merma o ajuste).
 */
public class DialogoInsumo extends JDialog {

    private static final Color NARANJA = new Color(0xD96A29);
    private static final Color GUINDA = new Color(0x8E0E0E);
    private static final Color FONDO = new Color(0xF7F1EC);
    private static final Color GRIS_TEXTO = new Color(0x666666);
    private static final Color BORDE_CAMPO = new Color(0xDDD5CE);

    // Mismas fuentes que Punto de venta
    private static final Font TITULO = Estilos.TITULO;
    private static final Font SUBTITULO = Estilos.SUBTITULO;
    private static final Font ETIQUETA = Estilos.ETIQUETA.deriveFont(13f);
    private static final Font CAMPO = Estilos.CAMPO;
    private static final Font BOTON = Estilos.BOTON;

    private final JTextField txtNombre = new CampoTexto("Pechuga de pollo");
    private final JTextField txtCantidad = new CampoTexto("5");
    private final JTextField txtStockMinimo = new CampoTexto("2");
    private final JComboBox<MotivoMovimiento> cbMotivo = Estilos.combo(MotivoMovimiento.values(), DialogoInsumo::texto);
    private final JComboBox<UnidadInsumo> cbUnidad = Estilos.combo(UnidadInsumo.values(), DialogoInsumo::texto);
    private final JComboBox<CategoriaInsumo> cbCategoria = Estilos.combo(CategoriaInsumo.values(), DialogoInsumo::texto);

    private final Long idInsumo;   // null = agregar, con valor = actualizar
    private InsumoDTO resultado;   // queda en null si se cancela

    /** Lo que devuelve el pop up al actualizar: los datos y el motivo si cambió la cantidad. */
    public record Resultado(InsumoDTO insumo, MotivoMovimiento motivo) {
    }

    // ------------------------------------------------------------ API pública
    /** Abre el pop up "Agregar Insumo". Devuelve null si se canceló. */
    public static InsumoDTO mostrarAgregar(Component padre) {
        return mostrar(padre);
    }

    /** Abre el pop up "Actualizar Insumo" con los datos del insumo. Devuelve null si se canceló. */
    public static Resultado mostrarActualizar(Component padre, Insumo insumo) {
        DialogoInsumo dialogo = mostrar(padre, insumo);
        if (dialogo.resultado == null) {
            return null;
        }
        return new Resultado(dialogo.resultado, (MotivoMovimiento) dialogo.cbMotivo.getSelectedItem());
    }

    private static InsumoDTO mostrar(Component padre) {
        return mostrar(padre, null).resultado;
    }

    private static DialogoInsumo mostrar(Component padre, Insumo insumo) {
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
        return dialogo;
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
        cuerpo.add(crearFormulario(esEdicion), gc);

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

    private JPanel crearFormulario(boolean esEdicion) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        estilizarCampo(txtNombre);
        estilizarCampo(txtCantidad);
        estilizarCampo(txtStockMinimo);
        estilizarCombo(cbUnidad);
        estilizarCombo(cbCategoria);
        estilizarCombo(cbMotivo);

        cbMotivo.setSelectedItem(MotivoMovimiento.CONSUMO_JORNADA); // lo más común: cierre del día

        agregarCampo(form, "Nombre del Insumo", txtNombre);
        agregarCampo(form, esEdicion ? "Cantidad actual (conteo)" : "Cantidad", txtCantidad);
        if (esEdicion) {
            agregarCampo(form, "Motivo del cambio de cantidad", cbMotivo);
        }
        agregarCampo(form, "Stock mínimo", txtStockMinimo);
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
        txtNombre.setText(insumo.getNombre());
        txtCantidad.setText(textoNumero(insumo.getStock()));
        txtStockMinimo.setText(textoNumero(insumo.getStockMinimo()));
        cbUnidad.setSelectedItem(insumo.getUnidad());
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

        Float cantidad = leerNumero(txtCantidad, "La cantidad");
        if (cantidad == null) {
            return;
        }
        Float stockMinimo = leerNumero(txtStockMinimo, "El stock mínimo");
        if (stockMinimo == null) {
            return;
        }

        UnidadInsumo unidad = (UnidadInsumo) cbUnidad.getSelectedItem();
        CategoriaInsumo categoria = (CategoriaInsumo) cbCategoria.getSelectedItem();

        // El estado lo calcula EvaluadorStock, por eso va en null
        resultado = new InsumoDTO(idInsumo, nombre, cantidad, stockMinimo, null,
                unidad.name(), categoria.name(), true);
        dispose();
    }

    /** Lee un número no negativo del campo; muestra el error y devuelve null si no es válido. */
    private Float leerNumero(JTextField campo, String nombreCampo) {
        float valor;
        try {
            valor = Float.parseFloat(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            error(nombreCampo + " debe ser un número.", campo);
            return null;
        }
        if (valor < 0) {
            error(nombreCampo + " no puede ser negativo.", campo);
            return null;
        }
        return valor;
    }

    private static String textoNumero(float n) {
        return n == (long) n ? String.valueOf((long) n) : String.valueOf(n);
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

    private static String texto(Object valor) {
        if (valor instanceof UnidadInsumo u) {
            return switch (u) {
                case KG -> "kg";
                case GRAMO -> "g";
                case LITRO -> "L";
                case PIEZA -> "pzs";
                case PAQUETE -> "paquetes";
            };
        }
        if (valor instanceof CategoriaInsumo c) {
            return switch (c) {
                case CARNES -> "Carnes";
                case VERDURAS -> "Verduras";
                case LACTEOS -> "Lácteos";
                case ABARROTES -> "Abarrotes";
                case BEBIDAS -> "Bebidas";
                case DESECHABLES -> "Desechables";
            };
        }
        if (valor instanceof MotivoMovimiento m) {
            return switch (m) {
                case COMPRA -> "Compra (entra)";
                case CONSUMO_JORNADA -> "Consumo del día (sale)";
                case MERMA -> "Merma (sale)";
                case AJUSTE -> "Ajuste de conteo";
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