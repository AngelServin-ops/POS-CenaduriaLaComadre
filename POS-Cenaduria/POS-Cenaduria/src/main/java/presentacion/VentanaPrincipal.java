package presentacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Ventana de la aplicación: barra lateral a la izquierda y la pantalla elegida a la derecha.
 * Abre en 1280×760, se puede agrandar o maximizar y el contenido se acomoda solo.
 */
public class VentanaPrincipal extends JFrame {

    public static final Dimension TAMANO_INICIAL = new Dimension(1280, 760);
    public static final Dimension TAMANO_MINIMO = new Dimension(1100, 660);

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel pantallas = new JPanel(tarjetas);
    private final BarraLateral barra;
    private final Map<String, Supplier<JComponent>> fabricas = new HashMap<>();
    private final Map<String, JComponent> creadas = new HashMap<>();
    private String actual;

    public VentanaPrincipal() {
        super("Cenaduría La Comadre");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Cada pantalla se crea la primera vez que se visita
        fabricas.put("Inicio", PanelInicio::new);
        fabricas.put("Punto de venta", PuntoDeVentaView::new);
        fabricas.put("Inventario", GestionarInventarioView::new);
        fabricas.put("Reportes", PanelReportes::new);
        fabricas.put("Usuarios", PanelUsuarios::new);
        fabricas.put("Configuración", PanelConfiguracion::new);

        barra = new BarraLateral(this::ir);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(barra, BorderLayout.WEST);
        raiz.add(pantallas, BorderLayout.CENTER);
        setContentPane(raiz);

        Navegacion.setVentana(this);
        ContextoApp.alCambiarSesion(this::alCambiarSesion);
        ir("Inicio");

        setSize(TAMANO_INICIAL);
        setMinimumSize(TAMANO_MINIMO);
        setLocationRelativeTo(null);
    }

    public void ir(String opcion) {
        if (Navegacion.INICIAR_SESION.equals(opcion)) {
            DialogoLogin.mostrar(this, null);
            return;
        }
        if (Navegacion.CERRAR_SESION.equals(opcion)) {
            ContextoApp.cerrarSesion();
            return;
        }
        if (!fabricas.containsKey(opcion)) {
            return;
        }
        if (!BarraLateral.OPCIONES_CAJA.contains(opcion) && !ContextoApp.esAdministrador()
                && !DialogoLogin.mostrar(this, opcion + " es solo para administradores. Escribe la contraseña.")) {
            return;
        }
        JComponent pantalla = creadas.computeIfAbsent(opcion, o -> {
            JComponent c = fabricas.get(o).get();
            pantallas.add(PanelAdaptable.envolver(c), o);
            return c;
        });
        if (pantalla instanceof Recargable r) {
            r.recargar();
        }
        actual = opcion;
        barra.setActivo(opcion);
        tarjetas.show(pantallas, opcion);
    }

    /** Abre Punto de venta y ejecuta algo en él (p. ej. "Agregar pedido" desde Inicio). */
    public void irAPuntoDeVenta(Consumer<PuntoDeVentaView> accion) {
        ir("Punto de venta");
        accion.accept((PuntoDeVentaView) creadas.get("Punto de venta"));
    }

    private void alCambiarSesion() {
        barra.refrescar();
        if (!ContextoApp.esAdministrador() && actual != null && !BarraLateral.OPCIONES_CAJA.contains(actual)) {
            ir("Inicio"); // al cerrar sesión no se queda abierta una pantalla de administrador
        } else if (creadas.get(actual) instanceof Recargable r) {
            r.recargar();
        }
    }
}
