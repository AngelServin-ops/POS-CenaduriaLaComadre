package persistencia.interfaces;

import persistencia.entidades.Usuario;
import persistencia.excepciones.PersistenciaException;

import java.util.List;
import java.util.Optional;

public interface IUsuarioDAO {

    void guardar(Usuario usuario) throws PersistenciaException;

    void actualizar(Usuario usuario) throws PersistenciaException;

    Optional<Usuario> buscarPorId(Long id) throws PersistenciaException;

    List<Usuario> listarTodos() throws PersistenciaException;
}
