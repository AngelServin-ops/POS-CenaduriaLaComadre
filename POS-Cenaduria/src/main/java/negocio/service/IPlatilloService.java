package negocio.service;

import negocio.dtos.PlatilloDTO;
import negocio.excepciones.NegocioException;

import java.util.List;

public interface IPlatilloService {

    /** Registra el platillo junto con sus variaciones (extras). */
    PlatilloDTO registrarPlatillo(PlatilloDTO dto) throws NegocioException;

    /** Platillos que se pueden vender ahora (disponible = true). */
    List<PlatilloDTO> listarDisponibles() throws NegocioException;

    void cambiarDisponibilidad(Long idPlatillo, boolean disponible) throws NegocioException;

    /** Todos los platillos, disponibles o no (para administrar el menú). */
    List<PlatilloDTO> listarTodos() throws NegocioException;

    /**
     * Cambia nombre, descripción, precio, categoría y extras. Los extras que ya no vengan
     * en la lista se desactivan (no se borran, para no romper pedidos anteriores).
     * Cambiar el precio no afecta pedidos ya registrados.
     */
    PlatilloDTO actualizarPlatillo(PlatilloDTO dto) throws NegocioException;
}
