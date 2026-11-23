package presentacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;

/** Describe en texto qué cambió entre la versión guardada de un pedido y la editada. */
public final class ResumenCambios {

    private ResumenCambios() {
    }

    public static List<String> calcular(List<DetallePedidoDTO> antes, List<DetallePedidoDTO> despues) {
        List<String> cambios = new ArrayList<>();
        Map<Long, DetallePedidoDTO> originales = antes.stream()
                .collect(Collectors.toMap(DetallePedidoDTO::getId, Function.identity()));

        for (DetallePedidoDTO d : despues) {
            DetallePedidoDTO o = d.getId() != null ? originales.remove(d.getId()) : null;
            if (o == null) {
                cambios.add("Se agregó " + d.descripcion() + ": " + d.getCantidad()
                        + (d.getCantidad() == 1 ? " unidad" : " unidades") + " a "
                        + Formatos.dinero(d.getPrecioUnitario()));
                continue;
            }
            if (o.getCantidad() != d.getCantidad()) {
                cambios.add(d.descripcion() + ": " + o.getCantidad() + " → " + d.getCantidad()
                        + " unidades a " + Formatos.dinero(d.getPrecioUnitario()));
            }
            if (!Objects.equals(vacioANull(o.getNota()), vacioANull(d.getNota()))) {
                cambios.add(d.descripcion() + ": nota "
                        + (vacioANull(d.getNota()) == null ? "eliminada" : "«" + d.getNota().trim() + "»"));
            }
        }
        for (DetallePedidoDTO quitado : originales.values()) {
            cambios.add("Se quitó " + quitado.descripcion() + " (" + quitado.getCantidad() + ")");
        }
        return cambios;
    }

    private static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
