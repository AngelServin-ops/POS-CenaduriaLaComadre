package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Menú guinda de la izquierda con el logo. Sin sesión solo muestra lo que usa la caja;
 * con sesión de administrador muestra todo. Abajo indica el modo y permite entrar o salir.
 */
public class BarraLateral extends JPanel {

    public static final Color GUINDA_MENU = new Color(0x570001);
    public static final List<String> OPCIONES_CAJA = List.of("Inicio", "Punto de venta");
    public static final List<String> OPCIONES_ADMIN =
            List.of("Inicio", "Punto de venta", "Inventario", "Reportes", "Usuarios", "Configuración");
    private static final int ANCHO = 250;

    private final Consumer<String> alNavegar;
    private final JPanel opciones = new JPanel();
    private final JPanel zonaSesion = new JPanel();
    private final Map<String, BotonRedondeado> botones = new LinkedHashMap<>();
    private String activo = "Inicio";

    public BarraLateral(Consumer<String> alNavegar) {
        this.alNavegar = alNavegar;
        setLayout(new BorderLayout());
        setBackground(GUINDA_MENU);
        setPreferredSize(new Dimension(ANCHO, 600));
        setBorder(new EmptyBorder(24, 18, 20, 18));

        JLabel logo = new JLabel(logo(170));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(new EmptyBorder(0, 0, 26, 0));
        add(logo, BorderLayout.NORTH);

        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
        opciones.setOpaque(false);
        add(opciones, BorderLayout.CENTER);

        zonaSesion.setLayout(new BoxLayout(zonaSesion, BoxLayout.Y_AXIS));
        zonaSesion.setOpaque(false);
        add(zonaSesion, BorderLayout.SOUTH);

        refrescar();
    }

    /** Vuelve a armar el menú según haya o no sesión de administrador. */
    public final void refrescar() {
        opciones.removeAll();
        botones.clear();
        for (String opcion : ContextoApp.esAdministrador() ? OPCIONES_ADMIN : OPCIONES_CAJA) {
            BotonRedondeado b = new BotonRedondeado(opcion, null);
            b.setFont(new Font(Estilos.BOTON.getFamily(), Font.BOLD, 14));
            b.setForeground(Color.WHITE);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(9, 16, 9, 16));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.addActionListener(e -> alNavegar.accept(opcion));
            botones.put(opcion, b);
            opciones.add(b);
            opciones.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        setActivo(activo);

        zonaSesion.removeAll();
        boolean admin = ContextoApp.esAdministrador();
        JLabel modo = Estilos.texto(admin ? "Administrador" : "Modo caja", Estilos.TEXTO_CHICO,
                new Color(0xE9C9B8));
        JLabel quien = Estilos.texto(admin ? ContextoApp.getAdministrador().getNombre()
                : ContextoApp.nombreCaja(), new Font(Estilos.BOTON.getFamily(), Font.BOLD, 14), Color.WHITE);
        BotonRedondeado accion = new BotonRedondeado(admin ? "Cerrar sesión" : "Iniciar sesión",
                admin ? null : Estilos.NARANJA);
        accion.setFont(Estilos.BOTON);
        accion.setForeground(Color.WHITE);
        accion.setBorder(new EmptyBorder(8, 14, 8, 14));
        accion.addActionListener(e -> alNavegar.accept(admin ? Navegacion.CERRAR_SESION : Navegacion.INICIAR_SESION));
        for (JLabel l : new JLabel[]{modo, quien}) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            zonaSesion.add(l);
            zonaSesion.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        accion.setAlignmentX(Component.LEFT_ALIGNMENT);
        zonaSesion.add(Box.createRigidArea(new Dimension(0, 6)));
        zonaSesion.add(accion);
        revalidate();
        repaint();
    }

    public void setActivo(String opcion) {
        activo = opcion;
        botones.forEach((nombre, b) -> b.setRelleno(nombre.equals(opcion) ? Estilos.NARANJA : null));
    }

    private static ImageIcon logo(int tam) {
        java.net.URL url = BarraLateral.class.getResource("/imagenes/LogoComadre.png");
        if (url == null) {
            return null;
        }
        Image img = new ImageIcon(url).getImage().getScaledInstance(tam, tam, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
