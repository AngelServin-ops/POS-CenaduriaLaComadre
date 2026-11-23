package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.dtos.Formatos;

/**
 * Colores, fuentes y piezas visuales compartidas por las pantallas de Punto de venta,
 * para que todas se vean igual que los prototipos.
 */
public final class Estilos {

    // ---------------- Colores ----------------
    public static final Color NARANJA = new Color(0xD96A29);
    public static final Color GUINDA = new Color(0x8E0E0E);
    public static final Color FONDO = new Color(0xF7F1EC);
    public static final Color FILA_RESALTADA = new Color(0xFDF1E8);
    public static final Color GRIS_TEXTO = new Color(0x6B6B6B);
    public static final Color GRIS_LINEA = new Color(0xEAE3DC);
    public static final Color BORDE_CAMPO = new Color(0xDDD5CE);
    public static final Color GRIS_DESHABILITADO = new Color(0x8A8A8A);
    public static final Color VERDE = new Color(0x2E7D32);
    public static final Color VERDE_FONDO = new Color(0xEAF4EA);
    public static final Color ROJO = new Color(0xB3261E);
    public static final Color ROJO_FONDO = new Color(0xFBEAEA);
    public static final Color NARANJA_FONDO = new Color(0xFDF0E6);
    public static final Color ENCABEZADO_FONDO = new Color(0xFAF8F6);

    private static final int PADDING_CELDA = 20;

    // ---------------- Fuentes ----------------
    // Serif con números "normales" (Georgia los dibuja de estilo antiguo: "ooo6").
    // Si agregan los .ttf de Source Serif 4 o Inter en /resources/fuentes, se usan esos (como en el Figma).
    private static final String SERIF = fuenteDisponible(
            new String[]{"SourceSerif4-Regular.ttf", "SourceSerif4-Bold.ttf"},
            "Source Serif 4", "Source Serif Pro", "Cambria", "Georgia", Font.SERIF);
    private static final String SANS = fuenteDisponible(
            new String[]{"Inter-Regular.ttf", "Inter-Bold.ttf"},
            "Inter", "Segoe UI", Font.SANS_SERIF);

    public static final Font TITULO = new Font(SERIF, Font.BOLD, 24);
    public static final Font TITULO_TARJETA = new Font(SERIF, Font.BOLD, 16);
    public static final Font TITULO_MODAL = new Font(SERIF, Font.BOLD, 22);
    public static final Font TITULO_GRANDE = new Font(SERIF, Font.PLAIN, 26);
    public static final Font SUBTITULO = new Font(SANS, Font.PLAIN, 12);
    public static final Font ETIQUETA = new Font(SANS, Font.PLAIN, 12);
    public static final Font TEXTO = new Font(SERIF, Font.PLAIN, 14);
    public static final Font TEXTO_NEGRITA = new Font(SERIF, Font.BOLD, 14);
    public static final Font ENCABEZADO_TABLA = new Font(SERIF, Font.BOLD, 14);
    public static final Font TEXTO_CHICO = new Font(SANS, Font.PLAIN, 12);
    public static final Font ENLACE = new Font(SANS, Font.PLAIN, 12);
    public static final Font CAMPO = new Font(SANS, Font.PLAIN, 13);
    public static final Font BOTON = new Font(SANS, Font.BOLD, 13);
    public static final Font TOTAL = new Font(SERIF, Font.BOLD, 20);

    public static final int FILAS_POR_PAGINA = 10;


    private Estilos() {
    }

    /**
     * Registra los .ttf incluidos en /fuentes (si existen) y devuelve la primera familia instalada.
     */
    private static String fuenteDisponible(String[] archivos, String... familias) {
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String archivo : archivos) {
            try (java.io.InputStream in = Estilos.class.getResourceAsStream("/fuentes/" + archivo)) {
                if (in != null) {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, in));
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar la fuente " + archivo + ": " + e.getMessage());
            }
        }
        java.util.Set<String> instaladas = new java.util.HashSet<>(
                java.util.Arrays.asList(ge.getAvailableFontFamilyNames()));
        for (String f : familias) {
            if (instaladas.contains(f) || Font.SERIF.equals(f) || Font.SANS_SERIF.equals(f)) {
                return f;
            }
        }
        return Font.DIALOG;
    }

    // ---------------- Botones ----------------

    public static BotonRedondeado boton(String texto, Color relleno) {
        BotonRedondeado b = new BotonRedondeado(texto, relleno);
        b.setForeground(Color.WHITE);
        b.setFont(BOTON);
        FontMetrics fm = b.getFontMetrics(BOTON);
        b.setPreferredSize(new Dimension(Math.max(120, fm.stringWidth(texto) + 36), 36));
        return b;
    }

    /** Botón blanco con borde, para acciones secundarias ("Volver a pedidos"). */
    public static BotonRedondeado botonSecundario(String texto) {
        BotonRedondeado b = new BotonRedondeado(texto, Color.WHITE) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDE_CAMPO);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        b.setForeground(Color.BLACK);
        b.setFont(BOTON);
        FontMetrics fm = b.getFontMetrics(BOTON);
        b.setPreferredSize(new Dimension(Math.max(120, fm.stringWidth(texto) + 36), 36));
        return b;
    }

    /** Habilita/deshabilita un botón pintándolo gris, como "Confirmar cobro" con monto insuficiente. */
    public static void habilitar(BotonRedondeado b, boolean habilitado, Color colorNormal) {
        b.setEnabled(habilitado);
        b.setRelleno(habilitado ? colorNormal : GRIS_DESHABILITADO);
    }

    // ---------------- Campos ----------------

    public static JTextField campoTexto(String placeholder) {
        JTextField campo = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && placeholder != null) {
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
        };
        estilizar(campo);
        return campo;
    }

    public static void estilizar(JTextField campo) {
        campo.setFont(CAMPO);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        campo.setPreferredSize(new Dimension(180, 34));
    }

    /** Campo de solo lectura (estado del pedido, cambio a entregar...). */
    public static JTextField campoLectura(String texto) {
        JTextField campo = campoTexto(null);
        campo.setText(texto);
        campo.setEditable(false);
        campo.setFocusable(false);
        campo.setBackground(new Color(0xFBF9F7));
        return campo;
    }

    public static void marcarError(JTextField campo, boolean error) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(error ? ROJO : BORDE_CAMPO, error ? 2 : 1, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    /** Combo plano: borde como los campos de texto y flecha simple, sin el botón de Windows. */
    public static <T> JComboBox<T> combo(T[] valores) {
        return combo(valores, null);
    }

    /** @param texto cómo mostrar cada opción (null = toString) */
    public static <T> JComboBox<T> combo(T[] valores, java.util.function.Function<T, String> texto) {
        JComboBox<T> combo = new JComboBox<>(valores);
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected javax.swing.JButton createArrowButton() {
                return new BotonFlecha(true);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle r, boolean foco) {
                g.setColor(comboBox.isEnabled() ? Color.WHITE : new Color(0xFBF9F7));
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            @Override
            public void paintCurrentValue(Graphics g, java.awt.Rectangle r, boolean foco) {
                super.paintCurrentValue(g, r, false); // sin el resaltado azul al tener el foco
            }
        });
        combo.setFont(CAMPO);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1, true));
        combo.setPreferredSize(new Dimension(180, 34));
        combo.setRenderer(rendererLista(texto));
        return combo;
    }

    @SuppressWarnings("unchecked")
    private static <T> javax.swing.ListCellRenderer<T> rendererLista(java.util.function.Function<T, String> texto) {
        javax.swing.DefaultListCellRenderer base = new javax.swing.DefaultListCellRenderer();
        return (lista, valor, indice, seleccionado, foco) -> {
            String t = valor == null ? "" : texto != null ? texto.apply(valor) : valor.toString();
            JLabel l = (JLabel) base.getListCellRendererComponent(lista, t, indice, seleccionado, false);
            l.setBorder(new EmptyBorder(6, 10, 6, 10));
            l.setFont(CAMPO);
            l.setBackground(seleccionado && indice >= 0 ? FILA_RESALTADA : Color.WHITE);
            l.setForeground(lista.isEnabled() || indice >= 0 ? Color.BLACK : GRIS_TEXTO);
            return l;
        };
    }

    /** Campo numérico con flechas planas (en lugar del JSpinner de Windows). */
    public static javax.swing.JSpinner spinner(int inicial, int minimo, int maximo) {
        javax.swing.JSpinner sp = new javax.swing.JSpinner(
                new javax.swing.SpinnerNumberModel(inicial, minimo, maximo, 1));
        sp.setUI(new javax.swing.plaf.basic.BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                BotonFlecha b = new BotonFlecha(false);
                installNextButtonListeners(b);
                return b;
            }

            @Override
            protected Component createPreviousButton() {
                BotonFlecha b = new BotonFlecha(true);
                installPreviousButtonListeners(b);
                return b;
            }
        });
        sp.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1, true));
        sp.setFont(CAMPO);
        javax.swing.JComponent editor = sp.getEditor();
        if (editor instanceof javax.swing.JSpinner.DefaultEditor de) {
            de.getTextField().setBorder(new EmptyBorder(6, 10, 6, 4));
            de.getTextField().setFont(CAMPO);
            de.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
            de.getTextField().setBackground(Color.WHITE);
        }
        sp.setPreferredSize(new Dimension(90, 34));
        return sp;
    }

    /** Flecha dibujada (▾ / ▴) sin fondo ni borde. */
    private static class BotonFlecha extends javax.swing.JButton {

        private final boolean abajo;

        BotonFlecha(boolean abajo) {
            this.abajo = abajo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFocusable(false);
            setPreferredSize(new Dimension(26, 14));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? new Color(0x333333) : BORDE_CAMPO);
            g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2 - 2, cy = getHeight() / 2;
            if (abajo) {
                g2.drawPolyline(new int[]{cx - 4, cx, cx + 4}, new int[]{cy - 2, cy + 2, cy - 2}, 3);
            } else {
                g2.drawPolyline(new int[]{cx - 4, cx, cx + 4}, new int[]{cy + 2, cy - 2, cy + 2}, 3);
            }
            g2.dispose();
        }
    }

    /** Etiqueta arriba y el campo abajo. */
    public static JPanel campo(String etiqueta, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(etiqueta);
        l.setFont(ETIQUETA);
        p.add(l, BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    // ---------------- Textos ----------------

    public static JLabel texto(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    /** Etiqueta chica gris arriba y valor en negritas abajo ("Estado del pedido / Activo"). */
    public static JPanel dato(String etiqueta, String valor, Color colorValor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel e = texto(etiqueta, TEXTO_CHICO, GRIS_TEXTO);
        JLabel v = texto(valor, TEXTO_NEGRITA, colorValor);
        e.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(e);
        p.add(Box.createRigidArea(new Dimension(0, 3)));
        p.add(v);
        return p;
    }

    // ---------------- Contenedores ----------------

    /** Encabezado de pantalla: título a la izquierda, fecha y caja a la derecha, subtítulo abajo. */
    public static JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(texto(titulo, TITULO, Color.BLACK), BorderLayout.WEST);
        fila.add(texto(Formatos.fecha(LocalDate.now()) + " · " + ContextoApp.nombreCaja(), TEXTO_CHICO, GRIS_TEXTO), BorderLayout.EAST);
        p.add(fila, BorderLayout.NORTH);
        if (subtitulo != null) {
            p.add(texto(subtitulo, SUBTITULO, GRIS_TEXTO), BorderLayout.CENTER);
        }
        return p;
    }

    public static PanelRedondeado tarjeta() {
        PanelRedondeado t = new PanelRedondeado(12, Color.WHITE);
        t.setBorder(new EmptyBorder(18, 20, 18, 20));
        return t;
    }

    /** Pantalla base: fondo crema y márgenes como en el prototipo. */
    public static JPanel pantalla() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(FONDO);
        p.setBorder(new EmptyBorder(28, 32, 28, 32));
        return p;
    }

    public enum TipoAviso { EXITO, ERROR, INFO }

    /** Recuadro con barra de color a la izquierda: "Pedido guardado", "Monto insuficiente"... */
    public static JPanel aviso(TipoAviso tipo, String titulo, String detalle) {
        Color barra = switch (tipo) {
            case EXITO -> VERDE;
            case ERROR -> ROJO;
            case INFO -> NARANJA;
        };
        Color fondo = switch (tipo) {
            case EXITO -> VERDE_FONDO;
            case ERROR -> ROJO_FONDO;
            case INFO -> NARANJA_FONDO;
        };
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(fondo);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 3, 0, 0, barra), new EmptyBorder(9, 12, 9, 12)));
        JLabel t = texto(titulo, new Font("Segoe UI", Font.BOLD, 13), barra);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        if (detalle != null && !detalle.isBlank()) {
            JLabel d = texto("<html>" + detalle + "</html>", TEXTO_CHICO, new Color(0x333333));
            d.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(Box.createRigidArea(new Dimension(0, 3)));
            p.add(d);
        }
        return p;
    }

    /** Componentes en fila, pegados a la izquierda, separados por espacio px. */
    public static JPanel filaIzquierda(int espacio, JComponent... componentes) {
        return fila(espacio, false, componentes);
    }

    /** Componentes en fila, pegados a la derecha, separados por espacio px. */
    public static JPanel filaDerecha(int espacio, JComponent... componentes) {
        return fila(espacio, true, componentes);
    }

    private static JPanel fila(int espacio, boolean derecha, JComponent... componentes) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        if (derecha) {
            p.add(Box.createHorizontalGlue());
        }
        for (int i = 0; i < componentes.length; i++) {
            if (i > 0) {
                p.add(Box.createRigidArea(new Dimension(espacio, 0)));
            }
            if (!(componentes[i] instanceof JLabel)) { // las etiquetas cambian de texto: no se fija su ancho
                componentes[i].setMaximumSize(componentes[i].getPreferredSize());
            }
            componentes[i].setAlignmentY(Component.CENTER_ALIGNMENT);
            p.add(componentes[i]);
        }
        if (!derecha) {
            p.add(Box.createHorizontalGlue());
        }
        return p;
    }

    // ---------------- Tablas ----------------

    /**
     * Tabla con el look del Figma: sin rejilla vertical, encabezado claro alineado con las celdas,
     * líneas finas entre filas y sin selección azul de Swing. La fila resaltada (p. ej. el pedido
     * recién editado) se marca con {@link #resaltarFila(JTable, int)}.
     */
    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(52);
        tabla.setFont(TEXTO);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(GRIS_LINEA);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setRowSelectionAllowed(false);
        tabla.setCellSelectionEnabled(false);
        tabla.setFocusable(false);
        tabla.setBackground(Color.WHITE);
        tabla.setFillsViewportHeight(true);
        tabla.setBorder(BorderFactory.createEmptyBorder());

        javax.swing.table.JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setBackground(ENCABEZADO_FONDO);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 46));
        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        header.setDefaultRenderer((t, v, s, f, r, c) -> {
            JLabel l = (JLabel) encabezado.getTableCellRendererComponent(t, v, false, false, r, c);
            // El borde se pone DESPUÉS: DefaultTableCellRenderer lo reinicia en cada llamada
            l.setBorder(new EmptyBorder(0, PADDING_CELDA, 0, 8));
            l.setFont(ENCABEZADO_TABLA);
            l.setForeground(Color.BLACK);
            l.setBackground(ENCABEZADO_FONDO);
            l.setOpaque(true);
            l.setHorizontalAlignment(SwingConstants.LEFT);
            return l;
        });

        DefaultTableCellRenderer celda = new DefaultTableCellRenderer();
        tabla.setDefaultRenderer(Object.class, (t, v, s, f, r, c) -> {
            JLabel l = (JLabel) celda.getTableCellRendererComponent(t, v, false, false, r, c);
            return pintarCelda(l, t, r, Color.BLACK);
        });
    }

    /** Aplica el estilo de celda (fuente, márgenes y fondo de la fila) a un renderer propio. */
    public static JLabel pintarCelda(JLabel l, JTable tabla, int fila, Color texto) {
        l.setFont(TEXTO);
        l.setForeground(texto);
        l.setBorder(new EmptyBorder(0, PADDING_CELDA, 0, 8));
        l.setHorizontalAlignment(SwingConstants.LEFT);
        l.setOpaque(true);
        l.setBackground(fondoFila(tabla, fila));
        return l;
    }

    public static Color fondoFila(JTable tabla, int fila) {
        Object resaltada = tabla.getClientProperty("filaResaltada");
        return resaltada instanceof Integer i && i == fila ? FILA_RESALTADA : Color.WHITE;
    }

    /** Pinta una fila con fondo naranja claro (-1 = ninguna). */
    public static void resaltarFila(JTable tabla, int fila) {
        tabla.putClientProperty("filaResaltada", fila);
        tabla.repaint();
    }

    /**
     * JScrollPane sin el borde gris del look and feel. Ojo: setBorder(null) no basta,
     * JTable vuelve a ponerle el borde de Windows al agregarse; un borde vacío sí se respeta.
     */
    public static JScrollPane scroll(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        JPanel esquina = new JPanel();
        esquina.setBackground(ENCABEZADO_FONDO);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, esquina);
        return scroll;
    }

    /**
     * Tarjeta blanca con la tabla y un pie "N pedidos · fecha" a la izquierda
     * y el paginador a la derecha, como en el Figma.
     */
    public static PanelRedondeado tarjetaTabla(JTable tabla, JComponent pieIzquierdo, JComponent pieDerecho) {
        PanelRedondeado tarjeta = new PanelRedondeado(12, Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(0, 0, 0, 0));
        tarjeta.add(scroll(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, GRIS_LINEA), new EmptyBorder(12, PADDING_CELDA, 12, PADDING_CELDA)));
        pie.add(pieIzquierdo, BorderLayout.WEST);
        if (pieDerecho != null) {
            pie.add(pieDerecho, BorderLayout.EAST);
        }
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    /**
     * Tabla de solo lectura que mide lo que sus filas (sin scroll propio), para tarjetas
     * dentro de pantallas que ya se desplazan completas (Reportes, Inicio...).
     */
    public static JComponent tablaFija(String[] columnas, java.util.List<Object[]> filas, String vacio) {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
        filas.forEach(modelo::addRow);
        JTable tabla = new JTable(modelo);
        estilizarTabla(tabla);
        tabla.setRowHeight(40);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(tabla.getTableHeader(), BorderLayout.NORTH);
        if (filas.isEmpty()) {
            JLabel l = texto(vacio, TEXTO_CHICO, GRIS_TEXTO);
            l.setBorder(new EmptyBorder(12, PADDING_CELDA, 12, 0));
            p.add(l, BorderLayout.CENTER);
        } else {
            p.add(tabla, BorderLayout.CENTER);
        }
        return p;
    }

    // ---------------- Estados ----------------

    public static String textoEstadoPedido(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "ACTIVO" -> "Activo";
            case "PAGADO" -> "Pagado";
            case "CANCELADO" -> "Cancelado";
            default -> estado;
        };
    }

    public static Color colorEstadoPedido(String estado) {
        if (estado == null) {
            return Color.BLACK;
        }
        return switch (estado) {
            case "ACTIVO" -> NARANJA;
            case "PAGADO" -> VERDE;
            case "CANCELADO" -> ROJO;
            default -> Color.BLACK;
        };
    }

    /** Pago: "Pendiente", "Parcial" (cuenta separada a medias), "Pagado" o "No aplica" (cancelado). */
    public static String textoPago(negocio.dtos.PedidoDTO p) {
        if (p.esCancelado()) {
            return "No aplica";
        }
        if (p.esPagado()) {
            return "Pagado";
        }
        return p.esParcial() ? "Parcial" : "Pendiente";
    }

    public static Color colorPago(negocio.dtos.PedidoDTO p) {
        if (p.esPagado()) {
            return VERDE;
        }
        if (p.esCancelado()) {
            return GRIS_TEXTO;
        }
        return p.esParcial() ? NARANJA : Color.BLACK;
    }

    public static String textoMetodo(String metodo) {
        if (metodo == null) {
            return "";
        }
        return switch (metodo) {
            case "EFECTIVO" -> "Efectivo";
            case "TARJETA" -> "Tarjeta";
            case "MIXTO" -> "Mixto";
            default -> metodo;
        };
    }

    public static String textoTipo(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case "EN_MESA" -> "En mesa";
            case "PARA_LLEVAR" -> "Para llevar";
            case "DOMICILIO" -> "Domicilio";
            default -> tipo;
        };
    }
}
