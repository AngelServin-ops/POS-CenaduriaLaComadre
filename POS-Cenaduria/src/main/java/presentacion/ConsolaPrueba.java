package presentacion;

import java.util.List;

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

public class ConsolaPrueba {

    public static void ejecutar() {
        // ---- Único lugar que cambia cuando pases a base de datos ----
        IAdministradorDAO adminDAO = new AdministradorDAOMemoria();
        IInsumoDAO insumoDAO = new InsumoDAOMemoria();
        // --------------------------------------------------------------

        IAdministradorService adminService = new AdministradorService(adminDAO);
        IInsumoService insumoService = new InsumoService(insumoDAO, adminDAO);

        try {
            AdministradorDTO admin = adminService.registrarAdministrador(
                    new AdministradorDTO(null, "Carlos"));

            // Tres insumos, uno por cada estado
            insumoService.registrarInsumo(
                    new InsumoDTO(null, "Pollo", 10f, null, "KG", "CARNES", admin));      // DISPONIBLE
            InsumoDTO tomate = insumoService.registrarInsumo(
                    new InsumoDTO(null, "Tomate", 2f, null, "KG", "VERDURAS", admin));    // BAJO
            insumoService.registrarInsumo(
                    new InsumoDTO(null, "Cebolla", 0f, null, "PIEZA", "VERDURAS", admin)); // AGOTADO

            System.out.println("=== Lista inicial ===");
            mostrarLista(insumoService.listarInsumos());

            // Actualizar: el tomate sube a 8 kg y pasa de BAJO a DISPONIBLE
            tomate.setStock(8f);
            insumoService.actualizarInsumo(tomate);

            System.out.println("\n=== Después de actualizar el tomate ===");
            mostrarLista(insumoService.listarInsumos());

            // Esto debe fallar: categoría inválida
            System.out.println("\n=== Prueba de validación ===");
            insumoService.registrarInsumo(
                    new InsumoDTO(null, "Pan", 5f, null, "PIEZA", "PANES", admin));

        } catch (NegocioException e) {
            System.out.println("Error de negocio: " + e.getMessage());
        }
    }

    private static void mostrarLista(List<InsumoDTO> insumos) {
        System.out.printf("%-4s %-12s %-8s %-8s %-10s %s%n",
                "ID", "NOMBRE", "STOCK", "UNIDAD", "CATEGORÍA", "ESTADO");
        for (InsumoDTO i : insumos) {
            System.out.printf("%-4d %-12s %-8.1f %-8s %-10s %s%n",
                    i.getId(), i.getNombre(), i.getStock(), i.getUnidad(),
                    i.getCategoria(), ColorEstado.pintar(i.getEstado()));
        }
    }
}