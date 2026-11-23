package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PartidaCobroDTO;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;

/**
 * CU Cobrar pedido · "SEPARAR CUENTA": se eligen los platillos (y cuántos) que paga
 * esta persona. Se cobran y se vuelve a separar hasta que el saldo llega a $0.
 */
public class PanelSepararCuenta extends JPanel {

    private static final int COL_A_COBRAR = 2;

    private final PuntoDeVentaView nav;
    private PedidoDTO pedido;
    private final List<DetallePedidoDTO> pendientes = new ArrayList<>();
    private int[] seleccion = new int[0];

    private final ModeloSeleccion modelo = new ModeloSeleccion();
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblSeleccion = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final JLabel lblSubtotal = Estilos.texto("", Estilos.TOTAL, Color.BLACK);
    private final BotonRedondeado btnContinuar = Estilos.boton("Cobrar selección", Estilos.NARANJA);

    public PanelSepararCuenta(PuntoDeVentaView nav, Long idPedido) {
        this.nav = nav;
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);
        try {
            pedido = ContextoApp.PEDIDOS.buscarPorId(idPedido);
        } catch (NegocioException e) {
            pantalla.add(Estilos.encabezado("SEPARAR CUENTA", null), BorderLayout.NORTH);
            pantalla.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "No se pudo consultar el pedido", e.getMessage()),
                    BorderLayout.CENTER);
            return;
        }
        for (DetallePedidoDTO d : pedido.getDetalles()) {
            if (d.getPendiente() > 0) {
                pendientes.add(d);
            }
        }
        seleccion = new int[pendientes.size()];

        pantalla.add(Estilos.encabezado("SEPARAR CUENTA", pedido.getFolioTexto() + " · " + pedido.getServicioTexto()
                + " · Saldo por cobrar " + Formatos.dinero(pedido.getSaldo())), BorderLayout.NORTH);

        Estilos.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(260);
        tabla.getColumnModel().getColumn(3).setMinWidth(130);
        tabla.getColumnModel().getColumn(COL_A_COBRAR).setCellRenderer(new RenderSelector());
        tabla.getColumnModel().getColumn(COL_A_COBRAR).setPreferredWidth(170);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila < 0) {
                    return;
                }
                if (tabla.columnAtPoint(e.getPoint()) == COL_A_COBRAR) {
                    Rectangle celda = tabla.getCellRect(fila, COL_A_COBRAR, false);
                    int x = e.getX() - celda.x;
                    cambiar(fila, x < celda.width / 3 ? -1 : x > celda.width * 2 / 3 ? 1 : 0);
                } else {
                    // Clic en la fila: alterna entre nada y todo lo que falta de ese platillo
                    seleccion[fila] = seleccion[fila] == 0 ? pendientes.get(fila).getPendiente() : 0;
                    actualizar();
                }
            }
        });
        tabla.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                tabla.setCursor(Cursor.getPredefinedCursor(tabla.rowAtPoint(e.getPoint()) >= 0
                        ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        BotonRedondeado todo = Estilos.botonSecundario("Seleccionar todo");
        todo.addActionListener(e -> {
            for (int i = 0; i < seleccion.length; i++) {
                seleccion[i] = pendientes.get(i).getPendiente();
            }
            actualizar();
        });
        BotonRedondeado limpiar = Estilos.botonSecundario("Limpiar selección");
        limpiar.addActionListener(e -> {
            java.util.Arrays.fill(seleccion, 0);
            actualizar();
        });

        PanelRedondeado tarjeta = Estilos.tarjetaTabla(tabla, lblSeleccion, lblSubtotal);

        BotonRedondeado volver = Estilos.botonSecundario("Volver");
        volver.addActionListener(e -> nav.irACobrar(pedido.getId()));
        btnContinuar.addActionListener(e -> continuar());
        JPanel botones = new JPanel(new BorderLayout());
        botones.setOpaque(false);
        botones.add(Estilos.filaIzquierda(10, volver, todo, limpiar), BorderLayout.WEST);
        botones.add(btnContinuar, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(Estilos.texto("Marca con − y + lo que paga esta persona (o haz clic en la fila para tomar todo "
                + "ese platillo). Después cobra y repite hasta liquidar la cuenta.", Estilos.TEXTO_CHICO,
                Estilos.GRIS_TEXTO), BorderLayout.NORTH);
        centro.add(tarjeta, BorderLayout.CENTER);
        centro.add(botones, BorderLayout.SOUTH);
        pantalla.add(centro, BorderLayout.CENTER);
        actualizar();
    }

    private void cambiar(int fila, int delta) {
        int nuevo = seleccion[fila] + delta;
        if (nuevo >= 0 && nuevo <= pendientes.get(fila).getPendiente()) {
            seleccion[fila] = nuevo;
            actualizar();
        }
    }

    private BigDecimal subtotal() {
        BigDecimal s = BigDecimal.ZERO;
        for (int i = 0; i < seleccion.length; i++) {
            s = s.add(pendientes.get(i).getPrecioUnitario().multiply(BigDecimal.valueOf(seleccion[i])));
        }
        return s;
    }

    private void actualizar() {
        modelo.fireTableDataChanged();
        int piezas = java.util.Arrays.stream(seleccion).sum();
        BigDecimal sub = subtotal();
        lblSeleccion.setText(piezas == 0 ? "Nada seleccionado"
                : piezas + " productos seleccionados · Quedarán " + Formatos.dinero(pedido.getSaldo().subtract(sub))
                + " por cobrar");
        lblSubtotal.setText("A cobrar: " + Formatos.dinero(sub));
        Estilos.habilitar(btnContinuar, piezas > 0, Estilos.NARANJA);
        btnContinuar.setText(piezas > 0 ? "Cobrar selección · " + Formatos.dinero(sub) : "Cobrar selección");
        btnContinuar.setPreferredSize(new java.awt.Dimension(240, 36));
    }

    private void continuar() {
        List<PartidaCobroDTO> partidas = new ArrayList<>();
        for (int i = 0; i < seleccion.length; i++) {
            if (seleccion[i] > 0) {
                partidas.add(new PartidaCobroDTO(pendientes.get(i).getId(), seleccion[i]));
            }
        }
        if (!partidas.isEmpty()) {
            nav.irARegistrarCobro(pedido, partidas);
        }
    }

    // ============================================================ Tabla
    private class ModeloSeleccion extends AbstractTableModel {

        private final String[] columnas = {"Producto", "Por cobrar", "A cobrar ahora", "Precio unitario", "Importe"};

        @Override public int getRowCount() { return pendientes.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            DetallePedidoDTO d = pendientes.get(fila);
            return switch (col) {
                case 0 -> d.descripcion();
                case 1 -> d.getPendiente() + " de " + d.getCantidad();
                case 2 -> seleccion[fila];
                case 3 -> Formatos.dinero(d.getPrecioUnitario());
                default -> Formatos.dinero(d.getPrecioUnitario().multiply(BigDecimal.valueOf(seleccion[fila])));
            };
        }
    }

    /** "−   2   +", resaltado en naranja si se va a cobrar algo de esa línea. */
    private static class RenderSelector extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            int n = (Integer) v;
            super.getTableCellRendererComponent(t, "−      " + n + "      +", false, false, r, c);
            Estilos.pintarCelda(this, t, r, n > 0 ? Estilos.NARANJA : Color.BLACK);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(Estilos.TEXTO_NEGRITA);
            return this;
        }
    }
}
