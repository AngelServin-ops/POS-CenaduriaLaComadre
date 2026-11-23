package persistencia.daos;

import persistencia.entidades.ConfiguracionNegocio;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IConfiguracionDAO;

import java.util.Optional;

public class ConfiguracionDAO implements IConfiguracionDAO {

    @Override
    public Optional<ConfiguracionNegocio> obtener() throws PersistenciaException {
        return OperacionesJPA.consultar(em -> em.createQuery(
                        "SELECT c FROM ConfiguracionNegocio c ORDER BY c.id", ConfiguracionNegocio.class)
                .setMaxResults(1)
                .getResultList().stream().findFirst());
    }

    @Override
    public void guardar(ConfiguracionNegocio c) throws PersistenciaException {
        OperacionesJPA.enTransaccion(em -> {
            if (c.getId() == null) {
                em.persist(c);
            } else {
                em.merge(c);
            }
        });
    }
}
