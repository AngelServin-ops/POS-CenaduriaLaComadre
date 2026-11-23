package persistencia.daos;

import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IInsumoDAO;

import java.util.List;
import java.util.Optional;

public class InsumoDAO implements IInsumoDAO {

    @Override
    public void guardar(Insumo insumo) throws PersistenciaException {
        OperacionesJPA.enTransaccion(em -> em.persist(insumo));
    }

    @Override
    public void actualizar(Insumo insumo) throws PersistenciaException {
        if (insumo.getId() == null) {
            throw new PersistenciaException("El insumo no existe");
        }
        OperacionesJPA.enTransaccion(em -> em.merge(insumo));
    }

    @Override
    public Optional<Insumo> buscarPorId(Long id) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> Optional.ofNullable(em.find(Insumo.class, id)));
    }

    @Override
    public List<Insumo> listarTodos() throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                "SELECT i FROM Insumo i ORDER BY i.id", Insumo.class).getResultList());
    }

    @Override
    public List<Insumo> buscarPorCategoria(CategoriaInsumo categoria) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                        "SELECT i FROM Insumo i WHERE i.categoria = :c ORDER BY i.nombre", Insumo.class)
                .setParameter("c", categoria)
                .getResultList());
    }

    @Override
    public List<Insumo> buscarPorNombre(String nombre) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                        "SELECT i FROM Insumo i WHERE LOWER(i.nombre) LIKE :n ORDER BY i.nombre", Insumo.class)
                .setParameter("n", "%" + nombre.toLowerCase().trim() + "%")
                .getResultList());
    }
}
