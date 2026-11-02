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
import persistencia.entidades.Insumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;

public class PanelInventario extends JPanel {

    private static final Color NARANJA = new Color(0xD96A29);
    private static final Color FONDO = new Color(0xF7F1EC);
    private static final Color GRIS_LINEA = new Color(0xE5E5E5);

    private static final Font TITULO = new Font("Georgia", Font.BOLD, 22);
    private static final Font TEXTO_TABLA = new Font("Georgia", Font.PLAIN, 15);
    private static final Font ENCABEZADO = new Font("Georgia", Font.BOLD, 16);

    private final ModeloInsumos modelo = new ModeloInsumos();
    private JTable tabla;
    private TableRowSorter<ModeloInsumos> sorter;

    // Puntos de conexión con el resto de la aplicación
    private Runnable accionAgregar = () -> { };
    private Consumer<Insumo> accionEditar = i -> { };
    private Consumer<Insumo> accionEliminar = i -> { };

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
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBackground(Color.WHITE);
        txt.setBorder(new EmptyBorder(8, 12, 8, 40));
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
        tarjeta.setBorder(new EmptyBorder(6, 6, 6, 6));

        tabla = new JTable(modelo);
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        tabla.setRowHeight(40);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(GRIS_LINEA);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setSelectionBackground(new Color(0xFBE9DC));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setFocusable(false);
        tabla.setBackground(Color.WHITE);
        tabla.setFillsViewportHeight(true);

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setHorizontalAlignment(SwingConstants.CENTER);
        encabezado.setFont(ENCABEZADO);
        encabezado.setBackground(Color.WHITE);
        encabezado.setBorder(new EmptyBorder(10, 0, 10, 0));
        tabla.getTableHeader().setDefaultRenderer(encabezado);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setBorder(null);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        centrado.setFont(TEXTO_TABLA);

        DefaultTableCellRenderer izquierda = new DefaultTableCellRenderer();
        izquierda.setHorizontalAlignment(SwingConstants.LEFT);
        izquierda.setFont(TEXTO_TABLA);
        izquierda.setBorder(new EmptyBorder(0, 14, 0, 0));

        tabla.getColumnModel().getColumn(0).setCellRenderer(izquierda);
        tabla.getColumnModel().getColumn(1).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(2).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RenderAcciones());
        tabla.getColumnModel().getColumn(4).setMaxWidth(110);
        tabla.getColumnModel().getColumn(4).setMinWidth(110);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());
                if (fila < 0 || col != 4) {
                    return;
                }
                Rectangle celda = tabla.getCellRect(fila, col, false);
                boolean editar = e.getX() < celda.x + celda.width / 2;
                Insumo insumo = modelo.getInsumo(tabla.convertRowIndexToModel(fila));
                if (editar) {
                    accionEditar.accept(insumo);
                } else {
                    accionEliminar.accept(insumo);
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
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        tarjeta.add(scroll, BorderLayout.CENTER);
        return tarjeta;
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
                case 0 -> i.getNombreInsumo();
                case 1 -> textoStock(i.getStock());
                case 2 -> textoUnidad(i.getUnidadInsumo());
                case 3 -> i.getEstadoInsumo();
                default -> null;
            };
        }
    }

    // ============================================================ Renderers
    private static class RenderEstado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, "", sel, foco, fila, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setIcon(new PuntoIcon(colorEstado((EstadoInsumo) v)));
            return this;
        }
    }

    private static class RenderAcciones extends DefaultTableCellRenderer {

        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        RenderAcciones() {
            panel.add(new JLabel(new EditarIcon()));
            panel.add(new JLabel(new BorrarIcon()));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foco, int fila, int col) {
            panel.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
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