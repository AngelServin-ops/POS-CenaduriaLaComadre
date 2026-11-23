package persistencia.interfaces;

import persistencia.entidades.ConfiguracionNegocio;
import persistencia.excepciones.PersistenciaException;

import java.util.Optional;

public interface IConfiguracionDAO {

    /** El único registro de configuración, si ya existe. */
    Optional<ConfiguracionNegocio> obtener() throws PersistenciaException;

    /** Crea el registro si no existe o lo actualiza. */
    void guardar(ConfiguracionNegocio configuracion) throws PersistenciaException;
}
