package presentacion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MenuLateral extends JPanel {

    private static final Color NARANJA = new Color(0xD96A29);
    private static final String[] OPCIONES = {"Inicio", "Punto de venta", "Inventario", "Reportes", "Usuarios", "Configuración"};

    public static final int X = 25, Y = 380, ANCHO = 343, ALTO = OPCIONES.length * 46;

    /**
     * @param activo     opción resaltada en naranja
     * @param alNavegar  recibe el nombre de la opción que se pulsó
     */
    public MenuLateral(String activo, Consumer<String> alNavegar) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        for (String opcion : OPCIONES) {
            BotonRedondeado b = new BotonRedondeado(opcion, opcion.equals(activo) ? NARANJA : null);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setForeground(Color.WHITE);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(9, 16, 9, 16));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            Dimension tam = new Dimension(ANCHO, 40);
            b.setPreferredSize(tam);
            b.setMaximumSize(tam);
            b.addActionListener(e -> {
                if (alNavegar != null) {
                    alNavegar.accept(opcion);
                }
            });
            add(b);
            add(Box.createRigidArea(new Dimension(0, 6)));
        }
    }
}