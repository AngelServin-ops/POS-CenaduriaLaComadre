package presentacion;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import negocio.reglas.EvaluadorStock;
import persistencia.entidades.Insumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;

public class PanelInventario extends JPanel {

    private static final Color NARANJA = new Color(0xD96A29);
    private static final Color FONDO = new Color(0xF7F1EC);
    private static final Color GRIS_LINEA = Estilos.GRIS_LINEA;

    // Mismas fuentes que Punto de venta (Georgia dibujaba los números como "ooo6")
    private static final Font TITULO = Estilos.TITULO;
    private static final Font TEXTO_TABLA = Estilos.TEXTO.deriveFont(15f);
    private static final Font ENCABEZADO = Estilos.ENCABEZADO_TABLA.deriveFont(16f);

    private static final EvaluadorStock EVALUADOR = new EvaluadorStock();
    private final ModeloInsumos modelo = new ModeloInsumos();
    private JTable tabla;
    private TableRowSorter<ModeloInsumos> sorter;

    // Puntos de conexión con el resto de la aplicación
    private Runnable accionAgregar = () -> { };
    private Consumer<Insumo> accionEditar = i -> { };
    private Consumer<Insumo> accionEliminar = i -> { };
    private Consumer<Insumo> accionMoverStock = i -> { };

    public PanelInventario() {
        setLayout(new BorderLayout(0, 18));
        setBackground(FONDO);
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(crearParteSuperior(), BorderLayout.NORTH);
        add(crearTarjetaTabla(), BorderLayout.CENTER);
    }

    // ------------------------------------------------------------ API pública
    public void setInsumos(List<Insumo> insumos) {
        modelo.setDatos(insumos);
    }

    public void setAccionAgregar(Runnable accion) {
        this.accionAgregar = accion;
    }

    public void setAccionEditar(Consumer<Insumo> accion) {
        this.accionEditar = accion;
    }

    public void setAccionEliminar(Consumer<Insumo> accion) {
        this.accionEliminar = accion;
    }

    /** Botón "Mover stock" (flechas): registrar consumo del día, compra, merma o ajuste. */
    public void setAccionMoverStock(Consumer<Insumo> accion) {
        this.accionMoverStock = accion;
    }

    // ------------------------------------------------------------ Parte superior
    private JPanel crearParteSuperior() {
        JPanel superior = new JPanel(new BorderLayout(0, 14));
        superior.setOpaque(false);

        JLabel titulo = new JLabel("INVENTARIO");
        titulo.setFont(TITULO);
        superior.add(titulo, BorderLayout.NORTH);

        JPanel barra = new JPanel(new BorderLayout(16, 0));
        barra.setOpaque(false);
        barra.add(crearBuscador(), BorderLayout.CENTER);

        BotonRedondeado btnAgregar = new BotonRedondeado("+ Agregar Insumo", NARANJA);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(Estilos.BOTON);
        btnAgregar.setPreferredSize(new Dimension(160, 36));
        btnAgregar.addActionListener(e -> accionAgregar.run());
        barra.add(btnAgregar, BorderLayout.EAST);

        superior.add(barra, BorderLayout.CENTER);
        return superior;
    }

    private JTextField crearBuscador() {
        JTextField txt = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getText().isEmpty()) {
                    g2.setColor(new Color(0x888888));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Buscar insumo...", getInsets().left,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.setColor(new Color(0x333333));
                g2.setStroke(new BasicStroke(1.6f));
                int cy = getHeight() / 2;
                g2.drawOval(getWidth() - 30, cy - 8, 11, 11);
                g2.drawLine(getWidth() - 21, cy + 3, getWidth() - 16, cy + 8);
                g2.dispose();
            }
        };
        txt.setFont(Estilos.CAMPO);
        txt.setBackground(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE_CAMPO, 1, true), new EmptyBorder(7, 12, 7, 40)));
        txt.setPreferredSize(new Dimension(0, 36));

        txt.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String t = txt.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(t), 0));
            }

            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        return txt;
    }

    // ------------------------------------------------------------ Tabla
    private JPanel crearTarjetaTabla() {
        PanelRedondeado tarjeta = new PanelRedondeado(14, Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(4, 12, 8, 12));

        tabla = new JTable(modelo);
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        tabla.setRowHeight(42);
        tabla.setRowSelectionAllowed(false); // sin la fila "seleccionada" de Swing
        tabla.setBorder(BorderFactory.createEmptyBorder());
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(GRIS_LINEA);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setSelectionBackground(new Color(0xFBE9DC));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setFocusable(false);
        tabla.setBackground(Color.WHITE);
        tabla.setFillsViewportHeight(true);

        // Ojo: DefaultTableCellRenderer reinicia fuente y borde en cada celda;
        // por eso el estilo se aplica DESPUÉS de llamar a super (antes se perdía).
        DefaultTableCellRenderer baseEncabezado = new DefaultTableCellRenderer();
        javax.swing.table.JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer((t, v, s, f, r, c) -> {
            JLabel l = (JLabel) baseEncabezado.getTableCellRendererComponent(t, v, false, false, r, c);
            l.setFont(ENCABEZADO);
            l.setForeground(Color.BLACK);
            l.setBackground(Color.WHITE);
            l.setOpaque(true);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRIS_LINEA));
            return l;
        });
        header.setPreferredSize(new Dimension(0, 48));
        header.setBackground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer centrado = new RenderTexto(SwingConstants.CENTER, 0);
        DefaultTableCellRenderer izquierda = new RenderTexto(SwingConstants.LEFT, 18);

        tabla.getColumnModel().getColumn(0).setCellRenderer(izquierda);
        tabla.getColumnModel().getColumn(1).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(2).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RenderAcciones());
        tabla.getColumnModel().getColumn(4).setMaxWidth(150);
        tabla.getColumnModel().getColumn(4).setMinWidth(150);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());
                if (fila < 0 || col != 4) {
                    return;
                }
                Insumo insumo = modelo.getInsumo(tabla.convertRowIndexToModel(fila));
                switch (accionEn(e, fila)) {
                    case 0 -> accionMoverStock.accept(insumo);
                    case 1 -> accionEditar.accept(insumo);
                    default -> accionEliminar.accept(insumo);
                }
            }
        });
        tabla.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean sobreAcciones = tabla.columnAtPoint(e.getPoint()) == 4
                        && tabla.rowAtPoint(e.getPoint()) >= 0;
                tabla.setCursor(sobreAcciones
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                tabla.setToolTipText(sobreAcciones
                        ? new String[]{"Mover stock", "Editar", "Eliminar"}[accionEn(e, tabla.rowAtPoint(e.getPoint()))]
                        : null);
            }
        });

        tarjeta.add(Estilos.scroll(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    /** 0 = mover stock, 1 = editar, 2 = eliminar, según en qué tercio de la celda se hizo clic. */
    private int accionEn(MouseEvent e, int fila) {
        Rectangle celda = tabla.getCellRect(fila, 4, false);
        int x = e.getX() - celda.x;
        return Math.min(2, x * 3 / Math.max(1, celda.width));
    }

    // ------------------------------------------------------------ Utilidades
    private static Color colorEstado(EstadoInsumo estado) {
        if (estado == null) {
            return Color.GRAY;
        }
        return switch (estado) {
            case AGOTADO -> new Color(0xE00000);
            case BAJO -> new Color(0xFF9800);
            case DISPONIBLE -> new Color(0x3CC400);
        };
    }

    private static String textoUnidad(UnidadInsumo u) {
        if (u == null) {
            return "";
        }
        return switch (u) {
            case KG -> "kg";
            case GRAMO -> "g";
            case LITRO -> "L";
            case PIEZA -> "pzs";
            case PAQUETE -> "paquetes";
        };
    }

    private static String textoStock(float s) {
        return s == (long) s ? String.valueOf((long) s) : String.valueOf(s);
    }

    // ============================================================ Modelo de tabla
    private static class ModeloInsumos extends AbstractTableModel {

        private final String[] columnas = {"Insumo", "Stock Actual", "Unidad", "Estado", ""};
        private List<Insumo> datos = new ArrayList<>();

        void setDatos(List<Insumo> nuevos) {
            datos = new ArrayList<>(nuevos);
            fireTableDataChanged();
        }

        Insumo getInsumo(int fila) {
            return datos.get(fila);
        }

        @Override public int getRowCount() { return datos.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            Insumo i = datos.get(fila);
            return switch (col) {
                case 0 -> i.getNombre();
                case 1 -> textoStock(i.getStock());
                case 2 -> textoUnidad(i.getUnidad());
                case 3 -> EVALUADOR.evaluar(i);
                default -> null;
            };
        }
    }

    // ============================================================ Renderers
    private static class RenderEstado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, "", false, false, fila, col);
            setBackground(Color.WHITE);
            setBorder(null);
            setHorizontalAlignment(SwingConstants.CENTER);
            setIcon(new PuntoIcon(colorEstado((EstadoInsumo) v)));
            return this;
        }
    }

    /** Texto de celda con la fuente y el margen del Figma. */
    private static class RenderTexto extends DefaultTableCellRenderer {

        private final int alineacion;
        private final int margen;

        RenderTexto(int alineacion, int margen) {
            this.alineacion = alineacion;
            this.margen = margen;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, false, false, fila, col);
            setFont(TEXTO_TABLA);
            setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            setHorizontalAlignment(alineacion);
            setBorder(new EmptyBorder(0, margen, 0, 0));
            return this;
        }
    }

    private static class RenderAcciones extends DefaultTableCellRenderer {

        // Un tercio de la celda por ícono: así el clic coincide con lo que se ve
        private final JPanel panel = new JPanel(new GridLayout(1, 3));

        RenderAcciones() {
            panel.add(new JLabel(new MoverIcon(), SwingConstants.CENTER));
            panel.add(new JLabel(new EditarIcon(), SwingConstants.CENTER));
            panel.add(new JLabel(new BorrarIcon(), SwingConstants.CENTER));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foco, int fila, int col) {
            panel.setBackground(Color.WHITE);
            return panel;
        }
    }

    // ============================================================ Íconos dibujados
    private static class PuntoIcon implements Icon {

        private final Color color;

        PuntoIcon(Color color) { this.color = color; }

        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, 18, 18);
            g2.dispose();
        }
    }

    /** Flechas arriba/abajo: entrada y salida de stock. */
    private static class MoverIcon implements Icon {

        @Override public int getIconWidth() { return 24; }
        @Override public int getIconHeight() { return 24; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xD96A29));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 7, y + 20, x + 7, y + 4);
            g2.drawPolyline(new int[]{x + 3, x + 7, x + 11}, new int[]{y + 8, y + 4, y + 8}, 3);
            g2.drawLine(x + 17, y + 4, x + 17, y + 20);
            g2.drawPolyline(new int[]{x + 13, x + 17, x + 21}, new int[]{y + 16, y + 20, y + 16}, 3);
            g2.dispose();
        }
    }

    private static class EditarIcon implements Icon {

        @Override public int getIconWidth() { return 24; }
        @Override public int getIconHeight() { return 24; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x + 2, y + 4, 16, 17, 4, 4);
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 10, y + 14, x + 21, y + 3);
            g2.dispose();
        }
    }

    private static class BorrarIcon implements Icon {

        @Override public int getIconWidth() { return 24; }
        @Override public int getIconHeight() { return 24; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 3, y + 6, x + 21, y + 6);
            g2.drawPolyline(new int[]{x + 9, x + 9, x + 15, x + 15},
                            new int[]{y + 6, y + 3, y + 3, y + 6}, 4);
            g2.drawRoundRect(x + 5, y + 8, 14, 13, 3, 3);
            g2.drawLine(x + 10, y + 11, x + 10, y + 18);
            g2.drawLine(x + 14, y + 11, x + 14, y + 18);
            g2.dispose();
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