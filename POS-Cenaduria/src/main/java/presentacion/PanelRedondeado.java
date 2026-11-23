package presentacion;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Panel con esquinas redondeadas (tarjetas y pop ups). */
public class PanelRedondeado extends JPanel {

    private final int radio;
    private Color borde;

    public PanelRedondeado(int radio, Color fondo) {
        this.radio = radio;
        setBackground(fondo);
        setOpaque(false);
    }

    public void setColorBorde(Color borde) {
        this.borde = borde;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
        if (borde != null) {
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
