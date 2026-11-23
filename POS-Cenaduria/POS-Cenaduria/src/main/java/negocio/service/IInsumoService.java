package negocio.service;

import negocio.dtos.InsumoDTO;
import negocio.dtos.MovimientoInsumoDTO;
import negocio.excepciones.NegocioException;

import java.util.List;

public interface IInsumoService {

    InsumoDTO registrarInsumo(InsumoDTO dto) throws NegocioException;

    /** Edita nombre, unidad, categoría y stock mínimo. El stock solo cambia con movimientos. */
    InsumoDTO actualizarInsumo(InsumoDTO dto) throws NegocioException;

    /** Baja lógica: el insumo deja de aparecer pero conserva su historial. */
    void desactivarInsumo(Long id) throws NegocioException;

    /** Solo insumos activos. */
    List<InsumoDTO> listarInsumos() throws NegocioException;

    List<InsumoDTO> listarPorEstado(String estado) throws NegocioException;

    /** Registra una entrada o salida y actualiza el stock del insumo. */
    MovimientoInsumoDTO registrarMovimiento(MovimientoInsumoDTO dto) throws NegocioException;

    /**
     * Deja el stock en la cantidad contada y registra la diferencia como movimiento
     * (ENTRADA si subió, SALIDA si bajó). Si la cantidad no cambió, no hace nada.
     */
    InsumoDTO ajustarStock(Long idInsumo, float nuevoStock, String motivo,
                           String observacion, Long idUsuario) throws NegocioException;

    List<MovimientoInsumoDTO> listarMovimientos(Long idInsumo) throws NegocioException;
}
