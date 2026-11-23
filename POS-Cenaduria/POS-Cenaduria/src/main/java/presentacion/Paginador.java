package presentacion;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * "‹ Página 1 de 3 ›" del pie de las tablas. Recibe la lista completa y entrega
 * a la tabla solo las filas de la página actual.
 */
public class Paginador<T> extends JPanel {

    private final int porPagina;
    private final Consumer<List<T>> mostrar;
    private final JLabel anterior = flecha("‹");
    private final JLabel siguiente = flecha("›");
    private final JLabel texto = Estilos.texto("", Estilos.TEXTO_CHICO, Color.BLACK);
    private List<T> datos = List.of();
    private int pagina = 0;

    public Paginador(int porPagina, Consumer<List<T>> mostrar) {
        this.porPagina = porPagina;
        this.mostrar = mostrar;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(anterior);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(texto);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(siguiente);

        anterior.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                irA(pagina - 1);
            }
        });
        siguiente.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                irA(pagina + 1);
            }
        });
    }

    /** Carga una lista nueva. Conserva la página si sigue existiendo. */
    public void setDatos(List<T> nuevos) {
        datos = nuevos;
        irA(Math.min(pagina, totalPaginas() - 1));
    }

    /** Página donde está el elemento en la posición indice de la lista completa. */
    public void irAElemento(int indice) {
        if (indice >= 0) {
            irA(indice / porPagina);
        }
    }

    /** Posición dentro de la página actual del elemento con ese índice, o -1 si está en otra página. */
    public int filaEnPagina(int indice) {
        int fila = indice - pagina * porPagina;
        return fila >= 0 && fila < porPagina ? fila : -1;
    }

    public void reiniciar() {
        pagina = 0;
    }

    private void irA(int nueva) {
        pagina = Math.max(0, Math.min(nueva, totalPaginas() - 1));
        int desde = pagina * porPagina;
        int hasta = Math.min(datos.size(), desde + porPagina);
        mostrar.accept(datos.subList(desde, hasta));
        texto.setText("Página " + (pagina + 1) + " de " + totalPaginas());
        anterior.setForeground(pagina > 0 ? Color.BLACK : Estilos.BORDE_CAMPO);
        siguiente.setForeground(pagina < totalPaginas() - 1 ? Color.BLACK : Estilos.BORDE_CAMPO);
    }

    private int totalPaginas() {
        return Math.max(1, (datos.size() + porPagina - 1) / porPagina);
    }

    private static JLabel flecha(String s) {
        JLabel l = Estilos.texto(s, Estilos.TEXTO_NEGRITA, Color.BLACK);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }
}
