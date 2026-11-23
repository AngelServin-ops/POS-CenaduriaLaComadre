package negocio.reglas;

import persistencia.entidades.Insumo;
import persistencia.enums.EstadoInsumo;

/**
 * Regla de negocio: el estado de un insumo se calcula comparando
 * su stock actual contra su propio stock mínimo.
 */
public class EvaluadorStock {

    public EstadoInsumo evaluar(Insumo insumo) {
        float stock = insumo.getStock();
        if (stock <= 0) {
            return EstadoInsumo.AGOTADO;
        }
        if (stock <= insumo.getStockMinimo()) {
            return EstadoInsumo.BAJO;
        }
        return EstadoInsumo.DISPONIBLE;
    }
}
