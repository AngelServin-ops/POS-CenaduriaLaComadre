package negocio.service;

import negocio.dtos.AdministradorDTO;
import negocio.excepciones.NegocioException;

public interface IAdministradorService {

    AdministradorDTO registrarAdministrador(AdministradorDTO dto) throws NegocioException;

}
