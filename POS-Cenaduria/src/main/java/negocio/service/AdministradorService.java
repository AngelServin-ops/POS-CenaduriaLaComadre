package negocio.service;

import negocio.dtos.AdministradorDTO;
import negocio.excepciones.NegocioException;
import negocio.mappers.AdministradorMapper;
import persistencia.entidades.Administrador;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IAdministradorDAO;

public class AdministradorService implements IAdministradorService {

    private final IAdministradorDAO administradorDAO;

    public AdministradorService(IAdministradorDAO administradorDAO) {
        this.administradorDAO = administradorDAO;
    }

    @Override
    public AdministradorDTO registrarAdministrador(AdministradorDTO dto) throws NegocioException {
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new NegocioException("El nombre del administrador es obligatorio");
        }
        try {
            Administrador admin = AdministradorMapper.aEntidad(dto);
            administradorDAO.guardar(admin);
            return AdministradorMapper.aDTO(admin);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al registrar el administrador", e);
        }
    }
}
