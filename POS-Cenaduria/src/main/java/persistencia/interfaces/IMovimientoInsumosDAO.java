package persistencia.interfaces;

import persistencia.entidades.MovimientoInsumo;
import persistencia.excepciones.PersistenciaException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IMovimientoInsumosDAO {

    void guardar(MovimientoInsumo movimiento) throws PersistenciaException;

    Optional<MovimientoInsumo> buscarPorId(Long id) throws PersistenciaException;

    List<MovimientoInsumo> listarTodos() throws PersistenciaException;

    List<MovimientoInsumo> listarPorInsumo(Long idInsumo) throws PersistenciaException;

    List<MovimientoInsumo> listarPorFecha(LocalDateTime desde, LocalDateTime hasta)
            throws PersistenciaException;
}
