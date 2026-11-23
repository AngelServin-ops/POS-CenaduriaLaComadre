package presentacion;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class BotonRedondeado extends JButton {

    private Color relleno; // null = transparente

    public BotonRedondeado(String texto, Color relleno) {
        super(texto);
        this.relleno = relleno;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setRelleno(Color relleno) {
        this.relleno = relleno;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (relleno != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() && isEnabled() ? relleno.brighter() : relleno);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            if (!isEnabled()) {
                // Deshabilitado: texto blanco sobre gris (el look and feel lo pintaría gris sobre gris)
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
                return;
            }
            g2.dispose();
        }
        super.paintComponent(g);
    }
}