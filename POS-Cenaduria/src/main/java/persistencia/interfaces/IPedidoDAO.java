package persistencia.interfaces;

import persistencia.entidades.Pedido;
import persistencia.excepciones.PersistenciaException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** El pago se guarda junto con su pedido (cascada desde Pedido). */
public interface IPedidoDAO {

    void guardar(Pedido pedido) throws PersistenciaException;

    void actualizar(Pedido pedido) throws PersistenciaException;

    Optional<Pedido> buscarPorId(Long id) throws PersistenciaException;

    List<Pedido> listarPorFecha(LocalDate fecha) throws PersistenciaException;

    /** Pedidos creados entre ambas fechas, incluidas. */
    List<Pedido> listarEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException;

    /** Folio más alto registrado, o 0 si no hay pedidos. */
    int obtenerUltimoFolio() throws PersistenciaException;
}
