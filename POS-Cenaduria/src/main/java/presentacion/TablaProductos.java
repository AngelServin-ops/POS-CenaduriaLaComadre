package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PedidoDTO;
import negocio.dtos.VariacionDTO;

/** Tabla de solo lectura con los productos de un pedido (detalle, cobro y ticket). */
public final class TablaProductos {

    private TablaProductos() {
    }

    public static JComponent crear(List<DetallePedidoDTO> detalles) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Producto", "Cantidad", "Precio unitario", "Importe"}, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
        for (DetallePedidoDTO d : detalles) {
            modelo.addRow(new Object[]{d.descripcion(), d.getCantidad(),
                    Formatos.dinero(d.getPrecioUnitario()), Formatos.dinero(d.calcularSubtotal())});
        }
        JTable tabla = new JTable(modelo);
        Estilos.estilizarTabla(tabla);
        tabla.setRowSelectionAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(360);

        // Sin JScrollPane: la tabla mide lo que sus filas, como en el prototipo
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(tabla.getTableHeader(), BorderLayout.NORTH);
        p.add(tabla, BorderLayout.CENTER);
        p.setBorder(new MatteBorder(0, 0, 1, 0, Estilos.GRIS_LINEA));
        tabla.setPreferredScrollableViewportSize(new Dimension(600, tabla.getRowHeight() * detalles.size()));
        return p;
    }

    /** Tabla con lo cobrado y lo pendiente de cada línea (cuenta separada). */
    public static JComponent crearConPagos(List<DetallePedidoDTO> detalles) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Producto", "Cantidad", "Cobrado", "Por cobrar", "Precio unitario", "Importe"}, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
        for (DetallePedidoDTO d : detalles) {
            modelo.addRow(new Object[]{d.descripcion(), d.getCantidad(), d.getCantidadPagada(),
                    d.getPendiente() == 0 ? "—" : d.getPendiente(),
                    Formatos.dinero(d.getPrecioUnitario()), Formatos.dinero(d.calcularSubtotal())});
        }
        JTable tabla = new JTable(modelo);
        Estilos.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(4).setMinWidth(130);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(tabla.getTableHeader(), BorderLayout.NORTH);
        p.add(tabla, BorderLayout.CENTER);
        p.setBorder(new MatteBorder(0, 0, 1, 0, Estilos.GRIS_LINEA));
        return p;
    }

    /** "7 productos · Precios en MXN" a la izquierda y "Total: $285.00" a la derecha. */
    public static JPanel pieTotal(PedidoDTO pedido) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Estilos.texto(pedido.getTotalProductos() + (pedido.getTotalProductos() == 1 ? " producto" : " productos")
                + " · Precios en MXN",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO), BorderLayout.WEST);
        p.add(Estilos.texto("Total: " + Formatos.dinero(pedido.getTotal()), Estilos.TOTAL, Color.BLACK),
                BorderLayout.EAST);
        return p;
    }

    /** "Pozole blanco: Sin cebolla · Tostada de tinga: con Aguacate extra", o null si no hay. */
    public static String textoNotas(PedidoDTO pedido) {
        List<String> partes = new ArrayList<>();
        for (DetallePedidoDTO d : pedido.getDetalles()) {
            List<String> cosas = new ArrayList<>();
            for (VariacionDTO v : d.getVariaciones()) {
                cosas.add("con " + v.nombre().toLowerCase());
            }
            if (d.getNota() != null && !d.getNota().isBlank()) {
                cosas.add(d.getNota());
            }
            if (!cosas.isEmpty()) {
                partes.add(d.getNombrePlatillo() + ": " + String.join(", ", cosas));
            }
        }
        if (pedido.getNotas() != null && !pedido.getNotas().isBlank()) {
            partes.add(pedido.getNotas());
        }
        return partes.isEmpty() ? null : String.join(" · ", partes);
    }
}
