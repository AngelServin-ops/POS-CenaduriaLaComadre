package presentacion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import negocio.dtos.MovimientoInsumoDTO;
import negocio.excepciones.NegocioException;
import persistencia.entidades.Insumo;
import persistencia.enums.MotivoMovimiento;

/**
 * Pop up "Mover stock": solo registra un movimiento de un insumo y su motivo.
 * Por defecto es "Consumo del día" (el cierre de jornada). Para Consumo, Compra y Merma
 * se escribe la cantidad que salió o entró; para Ajuste, el stock que se contó.
 */
public final class DialogoMovimientoStock {

    private DialogoMovimientoStock() {
    }

    /** @return true si se registró el movimiento */
    public static boolean mostrar(Component padre, Insumo insumo) {
        String unidad = textoUnidad(insumo);
        Modal m = new Modal(padre, "Mover stock · " + insumo.getNombre(),
                "Stock actual: " + numero(insumo.getStock()) + " " + unidad, 520);

        JComboBox<MotivoMovimiento> cbMotivo = Estilos.combo(MotivoMovimiento.values(), DialogoMovimientoStock::texto);
        cbMotivo.setSelectedItem(MotivoMovimiento.CONSUMO_JORNADA);
        JTextField txtCantidad = Estilos.campoTexto("0");
        JTextField txtObservacion = Estilos.campoTexto("Opcional");
        JPanel campoCantidad = Estilos.campo("Cantidad que se usó (" + unidad + ")", txtCantidad);
        JLabel etiquetaCantidad = (JLabel) campoCantidad.getComponent(0);

        JPanel fila = new JPanel(new GridLayout(1, 2, 12, 0));
        fila.setOpaque(false);
        fila.add(Estilos.campo("Motivo", cbMotivo));
        fila.add(campoCantidad);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        m.agregar(fila);
        JPanel obs = Estilos.campo("Observación", txtObservacion);
        obs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        m.agregar(obs);
        JLabel vista = Estilos.texto(" ", Estilos.TEXTO_NEGRITA, Color.BLACK);
        JLabel error = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.ROJO);
        m.agregar(vista);
        m.agregar(error);

        // Vista previa: "Stock: 8 kg → 5 kg"
        Runnable actualizar = () -> {
            MotivoMovimiento motivo = (MotivoMovimiento) cbMotivo.getSelectedItem();
            etiquetaCantidad.setText(switch (motivo) {
                case CONSUMO_JORNADA -> "Cantidad que se usó (" + unidad + ")";
                case COMPRA -> "Cantidad que llegó (" + unidad + ")";
                case MERMA -> "Cantidad que se perdió (" + unidad + ")";
                case AJUSTE -> "Stock contado (" + unidad + ")";
            });
            error.setText(" ");
            Estilos.marcarError(txtCantidad, false);
            Float cantidad = leer(txtCantidad.getText());
            if (cantidad == null) {
                vista.setText(" ");
                return;
            }
            float nuevo = nuevoStock(insumo.getStock(), motivo, cantidad);
            vista.setText("Stock: " + numero(insumo.getStock()) + " " + unidad + "  →  " + numero(nuevo) + " " + unidad);
            vista.setForeground(nuevo < 0 ? Estilos.ROJO : Color.BLACK);
        };
        cbMotivo.addActionListener(e -> actualizar.run());
        txtCantidad.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizar.run(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar.run(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar.run(); }
        });
        actualizar.run();

        Runnable guardar = () -> {
            MotivoMovimiento motivo = (MotivoMovimiento) cbMotivo.getSelectedItem();
            Float cantidad = leer(txtCantidad.getText());
            if (cantidad == null || cantidad < 0 || (cantidad == 0 && motivo != MotivoMovimiento.AJUSTE)) {
                Estilos.marcarError(txtCantidad, true);
                error.setText("Escribe una cantidad mayor a cero, por ejemplo 2 o 1.5.");
                txtCantidad.requestFocusInWindow();
                return;
            }
            String observacion = txtObservacion.getText().isBlank() ? null : txtObservacion.getText().trim();
            Long usuario = ContextoApp.getUsuario().getId();
            try {
                if (motivo == MotivoMovimiento.AJUSTE) {
                    ContextoApp.INSUMOS.ajustarStock(insumo.getId(), cantidad, motivo.name(), observacion, usuario);
                } else {
                    String tipo = motivo == MotivoMovimiento.COMPRA ? "ENTRADA" : "SALIDA";
                    ContextoApp.INSUMOS.registrarMovimiento(new MovimientoInsumoDTO(
                            tipo, motivo.name(), cantidad, observacion, insumo.getId(), usuario));
                }
                m.cerrar(true);
            } catch (NegocioException ex) {
                error.setText(ex.getMessage());
            }
        };
        txtCantidad.addActionListener(e -> guardar.run()); // Enter = registrar
        m.boton("Cancelar", null, false);
        m.botonAccion("Registrar movimiento", Estilos.NARANJA, guardar);
        SwingUtilities.invokeLater(txtCantidad::requestFocusInWindow);
        return Boolean.TRUE.equals(m.mostrar());
    }

    static float nuevoStock(float actual, MotivoMovimiento motivo, float cantidad) {
        return switch (motivo) {
            case COMPRA -> actual + cantidad;
            case CONSUMO_JORNADA, MERMA -> actual - cantidad;
            case AJUSTE -> cantidad;
        };
    }

    static String texto(MotivoMovimiento m) {
        return switch (m) {
            case CONSUMO_JORNADA -> "Consumo del día (sale)";
            case COMPRA -> "Compra (entra)";
            case MERMA -> "Merma (sale)";
            case AJUSTE -> "Ajuste de conteo";
        };
    }

    private static Float leer(String texto) {
        try {
            return Float.parseFloat(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String textoUnidad(Insumo i) {
        if (i.getUnidad() == null) {
            return "";
        }
        return switch (i.getUnidad()) {
            case KG -> "kg";
            case GRAMO -> "g";
            case LITRO -> "L";
            case PIEZA -> "pzs";
            case PAQUETE -> "paquetes";
        };
    }

    private static String numero(float n) {
        return n == (long) n ? String.valueOf((long) n) : String.valueOf(n);
    }
}
