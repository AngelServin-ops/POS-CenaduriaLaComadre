package persistencia.daos;

import persistencia.entidades.DetallePedido;
import persistencia.entidades.Pago;
import persistencia.entidades.Pedido;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IPedidoDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Pedidos con sus detalles, pagos y partidas. Detalles y pagos se guardan en cascada
 * desde el pedido, así que alta, edición, cancelación y cobro son una sola transacción.
 */
public class PedidoDAO implements IPedidoDAO {

    @Override
    public void guardar(Pedido pedido) throws PersistenciaException {
        OperacionesJPA.enTransaccion(em -> em.persist(pedido));
    }

    @Override
    public void actualizar(Pedido pedido) throws PersistenciaException {
        if (pedido.getId() == null) {
            throw new PersistenciaException("El pedido no existe");
        }
        // merge copia los cambios (detalles agregados/quitados, pagos nuevos) a la base de datos
        OperacionesJPA.enTransaccion(em -> em.merge(pedido));
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> {
            Pedido p = em.find(Pedido.class, id);
            if (p != null) {
                cargar(p);
            }
            return Optional.ofNullable(p);
        });
    }

    @Override
    public List<Pedido> listarPorFecha(LocalDate fecha) throws PersistenciaException {
        return listar(fecha, fecha, "p.folio DESC");
    }

    @Override
    public List<Pedido> listarEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        return listar(desde, hasta, "p.folio");
    }

    @Override
    public int obtenerUltimoFolio() throws PersistenciaException {
        return OperacionesJPA.consultar(em -> {
            Integer max = em.createQuery("SELECT MAX(p.folio) FROM Pedido p", Integer.class).getSingleResult();
            return max == null ? 0 : max;
        });
    }

    private List<Pedido> listar(LocalDate desde, LocalDate hasta, String orden) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> {
            List<Pedido> lista = em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.fechaHora >= :desde AND p.fechaHora < :hasta ORDER BY " + orden,
                            Pedido.class)
                    .setParameter("desde", desde.atStartOfDay())
                    .setParameter("hasta", hasta.plusDays(1).atStartOfDay())
                    .getResultList();
            lista.forEach(PedidoDAO::cargar);
            return lista;
        });
    }

    /**
     * Carga todo lo que las pantallas usan (detalles, extras, pagos y partidas) mientras
     * el EntityManager sigue abierto; así no hay LazyInitializationException después.
     */
    static void cargar(Pedido p) {
        for (DetallePedido d : p.getDetalles()) {
            d.getVariaciones().size();
            PlatilloDAO.cargar(d.getPlatillo());
        }
        for (Pago pago : p.getPagos()) {
            pago.getPartidas().size();
        }
    }
}
