package persistencia.daoMemoria;

import persistencia.entidades.Usuario;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IUsuarioDAO;

import java.util.*;

public class UsuarioDAOMemoria implements IUsuarioDAO {

    private final Map<Long, Usuario> datos = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public void guardar(Usuario usuario) throws PersistenciaException {
        usuario.setId(siguienteId++);
        datos.put(usuario.getId(), usuario);
    }

    @Override
    public void actualizar(Usuario usuario) throws PersistenciaException {
        if (usuario.getId() == null || !datos.containsKey(usuario.getId())) {
            throw new PersistenciaException("El usuario no existe");
        }
        datos.put(usuario.getId(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) throws PersistenciaException {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws PersistenciaException {
        return datos.values().stream()
                .filter(u -> u.getNombreUsuario().equalsIgnoreCase(nombreUsuario))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() throws PersistenciaException {
        return new ArrayList<>(datos.values());
    }
}
