package negocio.dtos;

/** Para separar la cuenta: cuántas unidades de una línea del pedido se van a cobrar. */
public record PartidaCobroDTO(Long idDetalle, int cantidad) {
}
