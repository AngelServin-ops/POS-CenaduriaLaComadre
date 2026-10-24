package persistencia.interfaces;

import persistencia.entidades.Administrador;
import persistencia.excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;

public interface IAdministradorDAO {

    void guardar(Administrador administrador) throws PersistenciaException;

    void actualizar(Administrador administrador) throws PersistenciaException;

    Optional<Administrador> buscarPorId(Long id) throws PersistenciaException;

    List<Administrador> listarTodos() throws PersistenciaException;
}
