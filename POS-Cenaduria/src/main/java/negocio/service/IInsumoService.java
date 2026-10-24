package negocio.service;

import negocio.dtos.InsumoDTO;
import negocio.excepciones.NegocioException;

import java.util.List;

public interface IInsumoService {

    InsumoDTO registrarInsumo(InsumoDTO dto) throws NegocioException;

    InsumoDTO actualizarInsumo(InsumoDTO dto) throws NegocioException;

    List<InsumoDTO> listarInsumos() throws NegocioException;
}
