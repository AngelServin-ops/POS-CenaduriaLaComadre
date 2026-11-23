package persistencia.interfaces;

import persistencia.entidades.Platillo;
import persistencia.excepciones.PersistenciaException;

import java.util.List;
import java.util.Optional;

public interface IPlatilloDAO {

    void guardar(Platillo platillo) throws PersistenciaException;

    void actualizar(Platillo platillo) throws PersistenciaException;

    Optional<Platillo> buscarPorId(Long id) throws PersistenciaException;

    List<Platillo> listarTodos() throws PersistenciaException;
}
