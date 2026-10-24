package persistencia.daoMemoria;

import persistencia.entidades.MovimientoInsumo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IMovimientoInsumosDAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovimientoInsumoDAOMemoria implements IMovimientoInsumosDAO {

    private final List<MovimientoInsumo> datos = new ArrayList<>();
    private long siguienteId = 1;

    @Override
    public void guardar(MovimientoInsumo movimiento) throws PersistenciaException {
        movimiento.setId(siguienteId++);
        datos.add(movimiento);
    }

    @Override
    public Optional<MovimientoInsumo> buscarPorId(Long id) throws PersistenciaException {
        return datos.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<MovimientoInsumo> listarTodos() throws PersistenciaException {
        return new ArrayList<>(datos);
    }

    @Override
    public List<MovimientoInsumo> listarPorInsumo(Long idInsumo) throws PersistenciaException {
        return datos.stream()
                .filter(m -> m.getInsumo().getId().equals(idInsumo))
                .toList();
    }

    @Override
    public List<MovimientoInsumo> listarPorFecha(LocalDateTime desde, LocalDateTime hasta)
            throws PersistenciaException {
        return datos.stream()
                .filter(m -> !m.getFecha().isBefore(desde) && !m.getFecha().isAfter(hasta))
                .toList();
    }
}
