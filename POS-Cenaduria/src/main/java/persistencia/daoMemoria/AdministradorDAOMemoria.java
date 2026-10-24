package persistencia.daoMemoria;

import persistencia.entidades.Administrador;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IAdministradorDAO;

import java.util.*;

public class AdministradorDAOMemoria implements IAdministradorDAO {

    private final Map<Long, Administrador> datos = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public void guardar(Administrador administrador) throws PersistenciaException {
        administrador.setId(siguienteId++);
        datos.put(administrador.getId(), administrador);
    }

    @Override
    public void actualizar(Administrador administrador) throws PersistenciaException {
        if (administrador.getId() == null || !datos.containsKey(administrador.getId())) {
            throw new PersistenciaException("El administrador no existe");
        }
        datos.put(administrador.getId(), administrador);
    }

    @Override
    public Optional<Administrador> buscarPorId(Long id) throws PersistenciaException {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Administrador> listarTodos() throws PersistenciaException {
        return new ArrayList<>(datos.values());
    }
}
