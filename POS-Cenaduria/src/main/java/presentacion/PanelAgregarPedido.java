package presentacion;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PanelAgregarPedido extends JPanel {

    private static final Color NARANJA = new Color(0xD96A29);
    private static final Color GUINDA = new Color(0xA00000);
    private static final Color FONDO = new Color(0xF7F1EC);

    private record Platillo(String nombre, double precio) {
        @Override public String toString() { return nombre + " · $" + (int) precio; }
    }

    private final JComboBox<String> cmbServicio = new JComboBox<>(new String[]{"En mesa", "Para llevar"});
    private final JComboBox<String> cmbMesa = new JComboBox<>(new String[]{"01", "02", "03", "04", "05", "06", "07", "08"});
    private final JComboBox<Platillo> cmbProducto = new JComboBox<>(new Platillo[]{
        new Platillo("Pozole rojo", 65),
        new Platillo("Tostada de tinga", 35),
        new Platillo("Agua de jamaica", 25),
        new Platillo("Tacos dorados", 55),
        new Platillo("Sope", 30)
    });
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Producto", "Cantidad", "Precio unitario", "Importe"}, 0) {
        @Override public boolean isCellEditable(int f, int c) { return false; }
    };
    private final JLabel lblTotal = new JLabel("Total: $0.00");
    private final JLabel lblResumen = new JLabel("0 productos · Precios en MXN");

    public PanelAgregarPedido() {
        setLayout(new BorderLayout(0, 14));
        setBackground(FONDO);
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTarjeta(), BorderLayout.CENTER);

        cmbServicio.addActionListener(e -> cmbMesa.setEnabled(cmbServicio.getSelectedIndex() == 0));
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel titulo = new JLabel("AGREGAR PEDIDO");
        titulo.setFont(new Font("Georgia", Font.BOLD, 22));
        JLabel fecha = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " · Caja 01");
        fecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(titulo, BorderLayout.WEST);
        p.add(fecha, BorderLayout.EAST);
        return p;
    }

    private JPanel crearTarjeta() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 14));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Formulario
        JTextField estado = new JTextField("Pendiente");
        estado.setEditable(false);

        BotonRedondeado btnAgregar = new BotonRedondeado("Agregar producto", NARANJA);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.addActionListener(e -> agregarProducto());

        JPanel form = new JPanel(new GridLayout(2, 3, 12, 10));
        form.setOpaque(false);
        form.add(campo("Tipo de servicio", cmbServicio));
        form.add(campo("Mesa", cmbMesa));
        form.add(campo("Estado del pedido", estado));
        form.add(campo("Producto", cmbProducto));
        form.add(campo("Cantidad", spnCantidad));
        form.add(campo(" ", btnAgregar));
        tarjeta.add(form, BorderLayout.NORTH);

        // Tabla
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(36);
        tabla.setFont(new Font("Georgia", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));
        DefaultTableCellRenderer dinero = new DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                setText("$" + String.format("%.2f", (Double) v));
            }
        };
        tabla.getColumnModel().getColumn(2).setCellRenderer(dinero);
        tabla.getColumnModel().getColumn(3).setCellRenderer(dinero);
        tarjeta.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Pie: total + botones
        lblTotal.setFont(new Font("Georgia", Font.BOLD, 18));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        BotonRedondeado btnCancelar = new BotonRedondeado("Cancelar", GUINDA);
        BotonRedondeado btnGuardar = new BotonRedondeado("Guardar pedido", NARANJA);
        for (BotonRedondeado b : new BotonRedondeado[]{btnCancelar, btnGuardar}) {
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.setPreferredSize(new Dimension(150, 36));
        }
        btnCancelar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());

        JPanel botones = new JPanel(new BorderLayout());
        botones.setOpaque(false);
        botones.add(btnCancelar, BorderLayout.WEST);
        botones.add(lblResumen, BorderLayout.CENTER);
        botones.add(btnGuardar, BorderLayout.EAST);

        JPanel pie = new JPanel(new BorderLayout(0, 10));
        pie.setOpaque(false);
        pie.add(lblTotal, BorderLayout.NORTH);
        pie.add(botones, BorderLayout.SOUTH);
        tarjeta.add(pie, BorderLayout.SOUTH);

        return tarjeta;
    }

    private JPanel campo(String etiqueta, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(etiqueta);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void agregarProducto() {
        Platillo p = (Platillo) cmbProducto.getSelectedItem();
        int cant = (int) spnCantidad.getValue();

        for (int i = 0; i < modelo.getRowCount(); i++) {       // si ya está, suma cantidad
            if (modelo.getValueAt(i, 0).equals(p.nombre())) {
                int nueva = (int) modelo.getValueAt(i, 1) + cant;
                modelo.setValueAt(nueva, i, 1);
                modelo.setValueAt(nueva * p.precio(), i, 3);
                actualizarTotal();
                return;
            }
        }
        modelo.addRow(new Object[]{p.nombre(), cant, p.precio(), cant * p.precio()});
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = 0;
        int piezas = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            piezas += (int) modelo.getValueAt(i, 1);
            total += (double) modelo.getValueAt(i, 3);
        }
        lblTotal.setText("Total: $" + String.format("%.2f", total));
        lblResumen.setText(piezas + " productos · Precios en MXN");
    }

    private void guardar() {
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Agrega al menos un producto.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Pedido guardado (" + lblTotal.getText() + ")");
        limpiar();
    }

    private void limpiar() {
        modelo.setRowCount(0);
        spnCantidad.setValue(1);
        actualizarTotal();
    }
}