package presentacion;

import java.util.function.Consumer;
import javax.swing.JOptionPane;

/** Punto único para cambiar de pantalla desde el menú lateral o desde otras pantallas. */
public final class Navegacion {

    public static final String INICIAR_SESION = "__iniciar_sesion__";
    public static final String CERRAR_SESION = "__cerrar_sesion__";

    private static Consumer<String> destino = op ->
            JOptionPane.showMessageDialog(null, "Pantalla \"" + op + "\" pendiente");
    private static VentanaPrincipal ventana;

    private Navegacion() {
    }

    /** Lo configura la ventana principal al arrancar. */
    public static void setVentana(VentanaPrincipal v) {
        ventana = v;
        destino = v::ir;
    }

    /** @param opcion texto de la opción del menú ("Inventario", "Punto de venta"...) */
    public static void ir(String opcion) {
        destino.accept(opcion);
    }

    /** Abre Punto de venta y ejecuta una acción en él (p. ej. abrir "Agregar pedido"). */
    public static void puntoDeVenta(Consumer<PuntoDeVentaView> accion) {
        if (ventana != null) {
            ventana.irAPuntoDeVenta(accion);
        }
    }
}
