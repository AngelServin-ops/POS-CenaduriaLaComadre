package negocio.service;

import negocio.dtos.ConfiguracionDTO;
import negocio.excepciones.NegocioException;

public interface IConfiguracionService {

    /** Devuelve la configuración guardada, o los valores por defecto si aún no hay. */
    ConfiguracionDTO obtener() throws NegocioException;

    ConfiguracionDTO guardar(ConfiguracionDTO dto) throws NegocioException;
}
