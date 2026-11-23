package negocio.service;

import negocio.dtos.ConfiguracionDTO;
import negocio.excepciones.NegocioException;
import persistencia.entidades.ConfiguracionNegocio;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IConfiguracionDAO;

public class ConfiguracionService implements IConfiguracionService {

    private static final int MESAS_MAXIMAS = 99;

    private final IConfiguracionDAO dao;

    public ConfiguracionService(IConfiguracionDAO dao) {
        this.dao = dao;
    }

    @Override
    public ConfiguracionDTO obtener() throws NegocioException {
        try {
            return aDTO(dao.obtener().orElseGet(ConfiguracionNegocio::new));
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo leer la configuración", e);
        }
    }

    @Override
    public ConfiguracionDTO guardar(ConfiguracionDTO dto) throws NegocioException {
        if (dto == null || dto.getNombreNegocio() == null || dto.getNombreNegocio().isBlank()) {
            throw new NegocioException("El nombre del negocio es obligatorio");
        }
        if (dto.getNombreCaja() == null || dto.getNombreCaja().isBlank()) {
            throw new NegocioException("El nombre de la caja es obligatorio");
        }
        if (dto.getNumeroMesas() < 1 || dto.getNumeroMesas() > MESAS_MAXIMAS) {
            throw new NegocioException("El número de mesas debe estar entre 1 y " + MESAS_MAXIMAS);
        }
        try {
            ConfiguracionNegocio c = dao.obtener().orElseGet(ConfiguracionNegocio::new);
            c.setNombreNegocio(dto.getNombreNegocio().trim());
            c.setDireccion(limpiar(dto.getDireccion()));
            c.setTelefono(limpiar(dto.getTelefono()));
            c.setMensajeTicket(limpiar(dto.getMensajeTicket()));
            c.setNombreCaja(dto.getNombreCaja().trim());
            c.setNumeroMesas(dto.getNumeroMesas());
            dao.guardar(c);
            return aDTO(c);
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo guardar la configuración", e);
        }
    }

    private static ConfiguracionDTO aDTO(ConfiguracionNegocio c) {
        return new ConfiguracionDTO(c.getNombreNegocio(), c.getDireccion(), c.getTelefono(),
                c.getMensajeTicket(), c.getNombreCaja(), c.getNumeroMesas());
    }

    private static String limpiar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
