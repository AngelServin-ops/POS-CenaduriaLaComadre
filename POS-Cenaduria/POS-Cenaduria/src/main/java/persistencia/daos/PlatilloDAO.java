package persistencia.daos;

import persistencia.entidades.Platillo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IPlatilloDAO;

import java.util.List;
import java.util.Optional;

public class PlatilloDAO implements IPlatilloDAO {

    @Override
    public void guardar(Platillo platillo) throws PersistenciaException {
        // Las variaciones se guardan en cascada con el platillo
        OperacionesJPA.enTransaccion(em -> em.persist(platillo));
    }

    @Override
    public void actualizar(Platillo platillo) throws PersistenciaException {
        if (platillo.getId() == null) {
            throw new PersistenciaException("El platillo no existe");
        }
        OperacionesJPA.enTransaccion(em -> em.merge(platillo));
    }

    @Override
    public Optional<Platillo> buscarPorId(Long id) throws PersistenciaException {
        return OperacionesJPA.consultar(em -> {
            Platillo p = em.find(Platillo.class, id);
            if (p != null) {
                cargar(p);
            }
            return Optional.ofNullable(p);
        });
    }

    @Override
    public List<Platillo> listarTodos() throws PersistenciaException {
        return OperacionesJPA.consultar(em -> {
            List<Platillo> lista = em.createQuery("SELECT p FROM Platillo p ORDER BY p.id", Platillo.class)
                    .getResultList();
            lista.forEach(PlatilloDAO::cargar);
            return lista;
        });
    }

    /** Carga las variaciones (LAZY) mientras el EntityManager sigue abierto. */
    static void cargar(Platillo p) {
        p.getVariaciones().size();
    }
}
