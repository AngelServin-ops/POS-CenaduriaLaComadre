package persistencia.daoMemoria;

import persistencia.entidades.ConfiguracionNegocio;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IConfiguracionDAO;

import java.util.Optional;

public class ConfiguracionDAOMemoria implements IConfiguracionDAO {

    private ConfiguracionNegocio configuracion;

    @Override
    public synchronized Optional<ConfiguracionNegocio> obtener() throws PersistenciaException {
        return Optional.ofNullable(configuracion);
    }

    @Override
    public synchronized void guardar(ConfiguracionNegocio c) throws PersistenciaException {
        if (c.getId() == null) {
            c.setId(1L);
        }
        configuracion = c;
    }
}
