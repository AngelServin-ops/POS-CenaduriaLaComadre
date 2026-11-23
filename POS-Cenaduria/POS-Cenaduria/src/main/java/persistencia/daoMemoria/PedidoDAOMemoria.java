package persistencia.daoMemoria;

import persistencia.entidades.DetallePedido;
import persistencia.entidades.Pago;
import persistencia.entidades.PartidaPago;
import persistencia.entidades.Pedido;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IPedidoDAO;

import java.time.LocalDate;
import java.util.*;

public class PedidoDAOMemoria implements IPedidoDAO {

    private final Map<Long, Pedido> datos = new LinkedHashMap<>();
    private long siguienteId = 1;
    private long siguienteIdDetalle = 1;
    private long siguienteIdPago = 1;
    private long siguienteIdPartida = 1;

    @Override
    public synchronized void guardar(Pedido pedido) throws PersistenciaException {
        pedido.setId(siguienteId++);
        asignarIds(pedido);
        datos.put(pedido.getId(), pedido);
    }

    @Override
    public synchronized void actualizar(Pedido pedido) throws PersistenciaException {
        if (pedido.getId() == null || !datos.containsKey(pedido.getId())) {
            throw new PersistenciaException("El pedido no existe");
        }
        asignarIds(pedido);
        datos.put(pedido.getId(), pedido);
    }

    @Override
    public synchronized Optional<Pedido> buscarPorId(Long id) throws PersistenciaException {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public synchronized List<Pedido> listarPorFecha(LocalDate fecha) throws PersistenciaException {
        return datos.values().stream()
                .filter(p -> p.getFechaHora().toLocalDate().equals(fecha))
                .sorted(Comparator.comparingInt(Pedido::getFolio).reversed())
                .toList();
    }

    @Override
    public synchronized List<Pedido> listarEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        return datos.values().stream()
                .filter(p -> {
                    LocalDate f = p.getFechaHora().toLocalDate();
                    return !f.isBefore(desde) && !f.isAfter(hasta);
                })
                .toList();
    }

    @Override
    public synchronized int obtenerUltimoFolio() throws PersistenciaException {
        return datos.values().stream().mapToInt(Pedido::getFolio).max().orElse(0);
    }

    // Simula los ids que asignaría la base de datos a los detalles y al pago
    private void asignarIds(Pedido pedido) {
        for (DetallePedido d : pedido.getDetalles()) {
            if (d.getId() == null) {
                d.setId(siguienteIdDetalle++);
            }
        }
        for (Pago pago : pedido.getPagos()) {
            if (pago.getId() == null) {
                pago.setId(siguienteIdPago++);
            }
            for (PartidaPago pp : pago.getPartidas()) {
                if (pp.getId() == null) {
                    pp.setId(siguienteIdPartida++);
                }
            }
        }
    }
}
