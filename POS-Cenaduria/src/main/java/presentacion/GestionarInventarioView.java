
package presentacion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;
import negocio.dtos.AdministradorDTO;
import negocio.dtos.InsumoDTO;
import negocio.excepciones.NegocioException;
import negocio.service.AdministradorService;
import negocio.service.IAdministradorService;
import negocio.service.IInsumoService;
import negocio.service.InsumoService;
import persistencia.daoMemoria.AdministradorDAOMemoria;
import persistencia.daoMemoria.InsumoDAOMemoria;
import persistencia.interfaces.IAdministradorDAO;
import persistencia.interfaces.IInsumoDAO;

/**
 *
 * @author Usuario
 */
public class GestionarInventarioView extends javax.swing.JPanel {

    /**
     * Creates new form GestionarInventarioView
     */

    // ===== Constantes (ajústalas si el menú o los botones no cuadran con tu fondo) =====
private static final Color NARANJA = new Color(0xD96A29);
private static final double PROPORCION_MENU = 0.205; // ancho de la franja guinda respecto al total
private static final int MENU_Y = 380;               // altura donde empiezan los botones (debajo del logo)
// ===== Servicios (único lugar que cambia cuando pasen a base de datos) =====
private final IAdministradorDAO adminDAO = new AdministradorDAOMemoria();
private final IInsumoDAO insumoDAO = new InsumoDAOMemoria();
private final IAdministradorService adminService = new AdministradorService(adminDAO);
private final IInsumoService insumoService = new InsumoService(insumoDAO, adminDAO);

private PanelInventario panelInventario;
private AdministradorDTO administrador;

public GestionarInventarioView() {
    initComponents();
    configurarMenu();
    configurarContenido();
}

/** Tamaño total del panel, calculado a partir del layout absoluto. */
private Dimension tamanoPanel() {
    Dimension d = getPreferredSize();
    if (d.width < 400 || d.height < 300) {
        d = jLabel_Fondo.getPreferredSize();
    }
    return d;
}

private void configurarMenu() {
    add(new MenuLateral("Inventario", op -> JOptionPane.showMessageDialog(this, "Pantalla \"" + op + "\" pendiente")),
        new AbsoluteConstraints(MenuLateral.X, MenuLateral.Y, MenuLateral.ANCHO, MenuLateral.ALTO), 0);
}

private void configurarContenido() {
    Dimension total = tamanoPanel();
    int xContenido = (int) (total.width * PROPORCION_MENU);

    panelInventario = new PanelInventario();
    cargarDatosPrueba();
    refrescarTabla();

    panelInventario.setAccionAgregar(this::agregarInsumo);
    panelInventario.setAccionEditar(this::actualizarInsumo);
    panelInventario.setAccionEliminar(i ->
            JOptionPane.showMessageDialog(this, "Eliminar: " + i.getNombreInsumo()));

    add(panelInventario, new AbsoluteConstraints(xContenido, 0, total.width - xContenido, total.height), 0);

    revalidate();
    repaint();
}

// ===== CU: Agregar / Actualizar insumo =====

private void agregarInsumo() {
    InsumoDTO nuevo = DialogoInsumo.mostrarAgregar(this);
    if (nuevo == null) {
        return; // canceló
    }
    try {
        nuevo.setAdministrador(administrador);
        insumoService.registrarInsumo(nuevo);
        refrescarTabla();
        JOptionPane.showMessageDialog(this, "Insumo agregado correctamente");
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void actualizarInsumo(Insumo insumo) {
    InsumoDTO cambios = DialogoInsumo.mostrarActualizar(this, insumo);
    if (cambios == null) {
        return; // canceló
    }
    try {
        insumoService.actualizarInsumo(cambios);
        refrescarTabla();
        JOptionPane.showMessageDialog(this, "Insumo actualizado correctamente");
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

/** Vuelve a pedir la lista al servicio y la pinta en la tabla. */
private void refrescarTabla() {
    try {
        List<Insumo> filas = new ArrayList<>();
        for (InsumoDTO dto : insumoService.listarInsumos()) {
            filas.add(new Insumo(dto.getId(), dto.getNombre(), dto.getStock(),
                    dto.getEstado() != null ? EstadoInsumo.valueOf(dto.getEstado()) : null,
                    UnidadInsumo.valueOf(dto.getUnidad()),
                    CategoriaInsumo.valueOf(dto.getCategoria()), null));
        }
        panelInventario.setInsumos(filas);
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

/** Datos de prueba (luego se reemplazan por los de la base de datos). */
private void cargarDatosPrueba() {
    try {
        administrador = adminService.registrarAdministrador(new AdministradorDTO(null, "Administrador"));
        insumoService.registrarInsumo(new InsumoDTO(null, "Jitomate", 2f, null, "KG", "VERDURAS", administrador));
        insumoService.registrarInsumo(new InsumoDTO(null, "Cebolla", 0f, null, "KG", "VERDURAS", administrador));
        insumoService.registrarInsumo(new InsumoDTO(null, "Queso", 1f, null, "KG", "CARNES", administrador));
        insumoService.registrarInsumo(new InsumoDTO(null, "Pechuga de pollo", 8f, null, "KG", "CARNES", administrador));
    } catch (NegocioException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel_Fondo = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1920, 1080));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel_Fondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/FondoCenaduria.png"))); // NOI18N
        add(jLabel_Fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel_Fondo;
    // End of variables declaration//GEN-END:variables
}
