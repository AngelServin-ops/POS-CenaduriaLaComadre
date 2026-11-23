package negocio.service;

import negocio.dtos.ReporteDTO;
import negocio.excepciones.NegocioException;

import java.time.LocalDate;

public interface IReporteService {

    ReporteDTO generar(LocalDate desde, LocalDate hasta) throws NegocioException;
}
