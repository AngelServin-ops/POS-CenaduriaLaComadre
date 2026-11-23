
package presentacion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.UnidadInsumo;
import negocio.dtos.InsumoDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IInsumoService;

/**
 * Pantalla "Inventario" (solo administradores). Antes era un formulario de NetBeans con
 * la imagen de fondo; ahora el menú lo pone VentanaPrincipal y esta pantalla se adapta al tamaño.
 */
public class GestionarInventarioView extends JPanel implements Recargable {

private final IInsumoService insumoService = ContextoApp.INSUMOS;
private final PanelInventario panelInventario = new PanelInventario();

public GestionarInventarioView() {
    setLayout(new BorderLayout());
    panelInventario.setAccionAgregar(this::agregarInsumo);
    panelInventario.setAccionEditar(this::actualizarInsumo);
    panelInventario.setAccionEliminar(this::eliminarInsumo);
    panelInventario.setAccionMoverStock(this::moverStock);
    add(panelInventario, BorderLayout.CENTER);
    refrescarTabla();
}

@Override
public void recargar() {
    refrescarTabla();
}

// ===== Mover stock: consumo del día, compra, merma o ajuste =====
private void moverStock(Insumo insumo) {
    if (DialogoMovimientoStock.mostrar(this, insumo)) {
        refrescarTabla();
    }
}

// ===== CU: Agregar / Actualizar insumo =====

private void agregarInsumo() {
    InsumoDTO nuevo = DialogoInsumo.mostrarAgregar(this);
    if (nuevo == null) {
        return; // canceló
    }
    try {
        insumoService.registrarInsumo(nuevo);
        refrescarTabla();
        JOptionPane.showMessageDialog(this, "Insumo agregado correctamente");
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void actualizarInsumo(Insumo insumo) {
    DialogoInsumo.Resultado resultado = DialogoInsumo.mostrarActualizar(this, insumo);
    if (resultado == null) {
        return; // canceló
    }
    InsumoDTO cambios = resultado.insumo();
    try {
        // 1) nombre, stock mínimo, unidad y categoría
        insumoService.actualizarInsumo(cambios);
        // 2) la cantidad se cambia con un movimiento para que quede en el historial
        insumoService.ajustarStock(cambios.getId(), cambios.getStock(),
                resultado.motivo().name(), null, ContextoApp.getUsuario().getId());
        refrescarTabla();
        JOptionPane.showMessageDialog(this, "Insumo actualizado correctamente");
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// ===== CU: Eliminar insumo (baja lógica: conserva su historial) =====
private void eliminarInsumo(Insumo insumo) {
    int opcion = JOptionPane.showConfirmDialog(this,
            "¿Eliminar \"" + insumo.getNombre() + "\" del inventario?",
            "Eliminar insumo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (opcion != JOptionPane.YES_OPTION) {
        return;
    }
    try {
        insumoService.desactivarInsumo(insumo.getId());
        refrescarTabla();
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

/** Vuelve a pedir la lista al servicio y la pinta en la tabla. */
private void refrescarTabla() {
    try {
        List<Insumo> filas = new ArrayList<>();
        for (InsumoDTO dto : insumoService.listarInsumos()) {
            filas.add(new Insumo(dto.getId(), dto.getNombre(), dto.getStock(), dto.getStockMinimo(),
                    UnidadInsumo.valueOf(dto.getUnidad()),
                    CategoriaInsumo.valueOf(dto.getCategoria())));
        }
        panelInventario.setInsumos(filas);
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

}
