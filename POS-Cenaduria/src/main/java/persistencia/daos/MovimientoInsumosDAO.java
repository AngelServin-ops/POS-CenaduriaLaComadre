package persistencia.daos;

import persistencia.entidades.MovimientoInsumo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IMovimientoInsumosDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MovimientoInsumosDAO implements IMovimientoInsumosDAO {

    @Override
    public void guardar(MovimientoInsumo movimiento) throws PersistenciaException {
        // insumo y usuario ya existen: se guardan solo como referencia (sin cascada)
        OperacionesJPA.enTransaccion(em -> em.persist(movimiento));
    }

    @Override
    public Optional<MovimientoInsumo> buscarPorId(Long id) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> Optional.ofNullable(em.find(MovimientoInsumo.class, id)));
    }

    @Override
    public List<MovimientoInsumo> listarTodos() throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                "SELECT m FROM MovimientoInsumo m ORDER BY m.fecha", MovimientoInsumo.class).getResultList());
    }

    @Override
    public List<MovimientoInsumo> listarPorInsumo(Long idInsumo) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                        "SELECT m FROM MovimientoInsumo m WHERE m.insumo.id = :id ORDER BY m.fecha",
                        MovimientoInsumo.class)
                .setParameter("id", idInsumo)
                .getResultList());
    }

    @Override
    public List<MovimientoInsumo> listarPorFecha(LocalDateTime desde, LocalDateTime hasta)
            throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                        "SELECT m FROM MovimientoInsumo m WHERE m.fecha BETWEEN :desde AND :hasta ORDER BY m.fecha",
                        MovimientoInsumo.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList());
    }
}
