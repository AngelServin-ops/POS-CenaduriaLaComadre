package persistencia.daos;

import persistencia.entidades.Usuario;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IUsuarioDAO;

import java.util.List;
import java.util.Optional;

public class UsuarioDAO implements IUsuarioDAO {

    @Override
    public void guardar(Usuario usuario) throws PersistenciaException {
        OperacionesJPA.enTransaccion(em -> em.persist(usuario));
    }

    @Override
    public void actualizar(Usuario usuario) throws PersistenciaException {
        if (usuario.getId() == null) {
            throw new PersistenciaException("El usuario no existe");
        }
        OperacionesJPA.enTransaccion(em -> em.merge(usuario));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> Optional.ofNullable(em.find(Usuario.class, id)));
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE LOWER(u.nombreUsuario) = LOWER(:n)", Usuario.class)
                .setParameter("n", nombreUsuario.trim())
                .getResultList().stream().findFirst());
    }

    @Override
    public List<Usuario> listarTodos() throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                "SELECT u FROM Usuario u ORDER BY u.id", Usuario.class).getResultList());
    }
}
