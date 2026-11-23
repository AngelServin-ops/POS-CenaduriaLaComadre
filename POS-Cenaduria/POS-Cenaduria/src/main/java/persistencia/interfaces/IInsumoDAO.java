package persistencia.interfaces;

import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.excepciones.PersistenciaException;

import java.util.List;
import java.util.Optional;

/**
 * No hay búsqueda por estado porque el estado ya no se guarda en la base de datos:
 * lo calcula EvaluadorStock (ver InsumoService.listarPorEstado).
 */
public interface IInsumoDAO {

    void guardar(Insumo insumo) throws PersistenciaException;

    void actualizar(Insumo insumo) throws PersistenciaException;

    Optional<Insumo> buscarPorId(Long id) throws PersistenciaException;

    List<Insumo> listarTodos() throws PersistenciaException;

    List<Insumo> buscarPorCategoria(CategoriaInsumo categoria) throws PersistenciaException;

    List<Insumo> buscarPorNombre(String nombre) throws PersistenciaException;
}
