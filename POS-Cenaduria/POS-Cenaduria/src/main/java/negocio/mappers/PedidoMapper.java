package negocio.mappers;

import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PagoDTO;
import negocio.dtos.PartidaPagoDTO;
import negocio.dtos.PedidoDTO;
import persistencia.entidades.DetallePedido;
import persistencia.entidades.Pago;
import persistencia.entidades.Pedido;

public class PedidoMapper {

    public static PedidoDTO aDTO(Pedido p) {
        PedidoDTO dto = new PedidoDTO(
                p.getId(),
                p.getTipo().name(),
                p.getMesa(),
                p.getNotas(),
                p.getDetalles().stream().map(d -> {
                    DetallePedidoDTO x = aDTO(d);
                    x.setCantidadPagada(p.cantidadPagada(d));
                    return x;
                }).toList()
        );
        dto.setFolio(p.getFolio());
        dto.setFechaHora(p.getFechaHora());
        dto.setEstado(p.getEstado().name());
        dto.setModificable(p.puedeModificarse());
        dto.setMotivoCancelacion(p.getMotivoCancelacion());
        dto.setFechaCancelacion(p.getFechaCancelacion());
        dto.setTotal(p.calcularTotal());
        dto.setTotalPagado(p.calcularTotalPagado());
        dto.setSaldo(p.calcularSaldo());
        dto.setNombreUsuario(p.getUsuario() != null ? p.getUsuario().getNombre() : null);
        dto.setPagos(p.getPagos().stream().map(PedidoMapper::aDTO).toList());
        return dto;
    }

    public static DetallePedidoDTO aDTO(DetallePedido d) {
        return new DetallePedidoDTO(
                d.getId(),
                d.getPlatillo().getId(),
                d.getPlatillo().getNombre(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getVariaciones().stream().map(PlatilloMapper::aDTO).toList(),
                d.getNota()
        );
    }

    public static PagoDTO aDTO(Pago pago) {
        PagoDTO dto = new PagoDTO(pago.getMetodo().name(), pago.getMontoTarjeta(),
                pago.getEfectivoRecibido(), pago.getReferenciaTerminal());
        dto.setMonto(pago.getMonto());
        dto.setEfectivoAplicado(pago.calcularEfectivoAplicado());
        dto.setCambio(pago.calcularCambio());
        dto.setFechaHora(pago.getFechaHora());
        dto.setNumero(pago.getNumero());
        dto.setFolioComprobante(Formatos.folioComprobante(pago.getPedido().getFolio(), pago.getNumero()));
        dto.setPartidas(pago.getPartidas().stream().map(pp -> new PartidaPagoDTO(
                aDTO(pp.getDetalle()).descripcion(), pp.getCantidad(),
                pp.getDetalle().getPrecioUnitario(), pp.getImporte())).toList());
        dto.setNombreCajero(pago.getUsuario() != null ? pago.getUsuario().getNombre() : null);
        return dto;
    }
}
