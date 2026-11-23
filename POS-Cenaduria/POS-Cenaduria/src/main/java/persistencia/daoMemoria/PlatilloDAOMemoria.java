package persistencia.daoMemoria;

import persistencia.entidades.Platillo;
import persistencia.entidades.VariacionPlatillo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IPlatilloDAO;

import java.util.*;

public class PlatilloDAOMemoria implements IPlatilloDAO {

    private final Map<Long, Platillo> datos = new LinkedHashMap<>();
    private long siguienteId = 1;
    private long siguienteIdVariacion = 1;

    @Override
    public synchronized void guardar(Platillo platillo) throws PersistenciaException {
        platillo.setId(siguienteId++);
        asignarIdsVariaciones(platillo);
        datos.put(platillo.getId(), platillo);
    }

    @Override
    public synchronized void actualizar(Platillo platillo) throws PersistenciaException {
        if (platillo.getId() == null || !datos.containsKey(platillo.getId())) {
            throw new PersistenciaException("El platillo no existe");
        }
        asignarIdsVariaciones(platillo);
        datos.put(platillo.getId(), platillo);
    }

    @Override
    public synchronized Optional<Platillo> buscarPorId(Long id) throws PersistenciaException {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public synchronized List<Platillo> listarTodos() throws PersistenciaException {
        return new ArrayList<>(datos.values());
    }

    private void asignarIdsVariaciones(Platillo platillo) {
        for (VariacionPlatillo v : platillo.getVariaciones()) {
            if (v.getId() == null) {
                v.setId(siguienteIdVariacion++);
            }
        }
    }
}
