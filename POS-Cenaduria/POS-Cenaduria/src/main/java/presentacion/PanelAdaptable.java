package presentacion;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/**
 * Hace que una pantalla llene la ventana cuando cabe y que aparezca una barra
 * de desplazamiento solo cuando la ventana es más chica que el contenido.
 */
public class PanelAdaptable extends JPanel implements Scrollable {

    private PanelAdaptable(JComponent contenido) {
        super(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
    }

    public static JScrollPane envolver(JComponent contenido) {
        JScrollPane scroll = new JScrollPane(new PanelAdaptable(contenido));
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(Estilos.FONDO);
        return scroll;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int orientacion, int direccion) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int orientacion, int direccion) {
        return 200;
    }

    // Siempre toma el ancho de la ventana (nunca hay barra horizontal: el contenido se acomoda)
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    // Toma el alto de la ventana si cabe; si no, aparece la barra vertical
    

    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container p = getParent();
        return p instanceof JViewport && p.getHeight() > getPreferredSize().height;
    }
}
