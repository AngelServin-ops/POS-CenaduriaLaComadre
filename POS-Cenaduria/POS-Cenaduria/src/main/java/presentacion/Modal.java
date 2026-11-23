package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Window;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Pop up de Punto de venta: oscurece la pantalla de atrás, muestra título, contenido
 * y botones, y devuelve el valor del botón que se pulsó (null si se cerró con Esc).
 */
public class Modal extends JDialog {

    private final JPanel cuerpo = new JPanel();
    private final JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private Object resultado;
    private boolean cerrable = true;

    public Modal(Component padre, String titulo, String subtitulo, int ancho) {
        super(padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre),
                ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        PanelRedondeado contenido = new PanelRedondeado(14, Estilos.FONDO);
        contenido.setLayout(new BorderLayout(0, 16));
        contenido.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        JLabel lTitulo = Estilos.texto(titulo, Estilos.TITULO_MODAL, Color.BLACK);
        lTitulo.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(lTitulo);
        if (subtitulo != null) {
            JLabel lSub = Estilos.texto(subtitulo, Estilos.SUBTITULO, Estilos.GRIS_TEXTO);
            lSub.setAlignmentX(LEFT_ALIGNMENT);
            encabezado.add(Box.createRigidArea(new Dimension(0, 4)));
            encabezado.add(lSub);
        }
        contenido.add(encabezado, BorderLayout.NORTH);

        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);
        contenido.add(cuerpo, BorderLayout.CENTER);

        botones.setOpaque(false);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        getRootPane().registerKeyboardAction(e -> {
            if (cerrable) {
                cerrar(null);
            }
        }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        contenido.setPreferredSize(null);
        setMinimumSize(new Dimension(ancho, 10));
    }

    /** Agrega un bloque al cuerpo del pop up (texto, aviso, campos...). */
    public Modal agregar(JComponent componente) {
        componente.setAlignmentX(LEFT_ALIGNMENT);
        if (cuerpo.getComponentCount() > 0) {
            cuerpo.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        cuerpo.add(componente);
        return this;
    }

    /** Párrafo de texto que se ajusta al ancho del pop up. */
    public Modal parrafo(String texto) {
        JLabel l = Estilos.texto("<html><body style='width:" + (getMinimumSize().width - 80) + "px'>"
                + texto + "</body></html>", Estilos.TEXTO, Color.BLACK);
        return agregar(l);
    }

    /** Botón que cierra el pop up devolviendo valor. color null = botón blanco secundario. */
    public BotonRedondeado boton(String texto, Color color, Object valor) {
        BotonRedondeado b = color == null ? Estilos.botonSecundario(texto) : Estilos.boton(texto, color);
        b.addActionListener(e -> cerrar(valor));
        if (botones.getComponentCount() > 0) {
            botones.add(Box.createRigidArea(new Dimension(10, 0)));
        }
        botones.add(b);
        return b;
    }

    /** Botón que ejecuta una acción sin cerrar el pop up (p. ej. validar antes de confirmar). */
    public BotonRedondeado botonAccion(String texto, Color color, Runnable accion) {
        BotonRedondeado b = color == null ? Estilos.botonSecundario(texto) : Estilos.boton(texto, color);
        b.addActionListener(e -> accion.run());
        if (botones.getComponentCount() > 0) {
            botones.add(Box.createRigidArea(new Dimension(10, 0)));
        }
        botones.add(b);
        return b;
    }

    /** Pop up simple para avisar un error de negocio. */
    public static void error(Component padre, String titulo, String mensaje) {
        Modal m = new Modal(padre, titulo, null, 460);
        m.agregar(Estilos.aviso(Estilos.TipoAviso.ERROR, mensaje, null));
        m.boton("Entendido", Estilos.NARANJA, null);
        m.mostrar();
    }

    public void setCerrable(boolean cerrable) {
        this.cerrable = cerrable;
    }

    public void cerrar(Object valor) {
        resultado = valor;
        dispose();
    }

    /** Muestra el pop up (bloquea hasta que se cierre) y devuelve el valor del botón pulsado. */
    public Object mostrar() {
        pack();
        setLocationRelativeTo(getOwner());

        Window ventana = getOwner();
        JRootPane raiz = ventana instanceof RootPaneContainer rpc ? rpc.getRootPane() : null;
        Component glassAnterior = raiz != null ? raiz.getGlassPane() : null;
        if (raiz != null) {
            raiz.setGlassPane(new CapaOscura());
            raiz.getGlassPane().setVisible(true);
        }
        setVisible(true); // modal: se queda aquí hasta cerrar
        if (raiz != null) {
            raiz.getGlassPane().setVisible(false);
            raiz.setGlassPane(glassAnterior);
        }
        return resultado;
    }

    /** Capa semitransparente sobre la pantalla de atrás. */
    private static class CapaOscura extends JComponent {

        CapaOscura() {
            addMouseListener(new java.awt.event.MouseAdapter() { }); // bloquea clics
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 110));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
