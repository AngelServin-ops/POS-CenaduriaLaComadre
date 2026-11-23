package presentacion;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class AgregarPedidoView extends JPanel {

    private static final int ANCHO = 1920, ALTO = 1080;
    private static final double PROPORCION_MENU = 0.205; // franja guinda

    public AgregarPedidoView() {
    setPreferredSize(new Dimension(ANCHO, ALTO));
    setLayout(new AbsoluteLayout());

    int x = (int) (ANCHO * PROPORCION_MENU);
    add(new MenuLateral("Punto de venta", op -> System.out.println("Ir a: " + op)),
            new AbsoluteConstraints(MenuLateral.X, MenuLateral.Y, MenuLateral.ANCHO, MenuLateral.ALTO));
    add(new PanelAgregarPedido(), new AbsoluteConstraints(x, 0, ANCHO - x, ALTO));
    add(new JLabel(new ImageIcon(getClass().getResource("/imagenes/FondoCenaduria.png"))),
            new AbsoluteConstraints(0, 0));
}
    
    
}