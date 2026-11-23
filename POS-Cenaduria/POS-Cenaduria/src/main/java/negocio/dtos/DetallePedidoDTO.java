package negocio.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Una línea del pedido: platillo, cantidad, extras y nota del cliente. */
public class DetallePedidoDTO {

    private Long id;               // null = línea nueva
    private Long idPlatillo;
    private String nombrePlatillo;
    private int cantidad;
    // Precio del platillo + extras. En líneas nuevas lo calcula el servicio al guardar.
    private BigDecimal precioUnitario;
    private List<VariacionDTO> variaciones = new ArrayList<>();
    // Indicaciones del cliente: "Sin cebolla", "Salsa aparte"...
    private String nota;
    // Unidades de esta línea que ya se cobraron (cuenta separada)
    private int cantidadPagada;

    public DetallePedidoDTO() {
    }

    public DetallePedidoDTO(Long id, Long idPlatillo, String nombrePlatillo, int cantidad,
                            BigDecimal precioUnitario, List<VariacionDTO> variaciones, String nota) {
        this.id = id;
        this.idPlatillo = idPlatillo;
        this.nombrePlatillo = nombrePlatillo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.variaciones = variaciones != null ? new ArrayList<>(variaciones) : new ArrayList<>();
        this.nota = nota;
    }

    public int getCantidadPagada() {
        return cantidadPagada;
    }

    public void setCantidadPagada(int cantidadPagada) {
        this.cantidadPagada = cantidadPagada;
    }

    /** Unidades que faltan por cobrar. */
    public int getPendiente() {
        return cantidad - cantidadPagada;
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario == null ? BigDecimal.ZERO
                : precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** "Tostada de tinga + Aguacate extra". */
    public String descripcion() {
        StringBuilder sb = new StringBuilder(nombrePlatillo);
        for (VariacionDTO v : variaciones) {
            sb.append(" + ").append(v.nombre());
        }
        return sb.toString();
    }

    /** Dos líneas son "la misma" si tienen el mismo platillo, extras y nota. */
    public boolean mismoProducto(DetallePedidoDTO otra) {
        return Objects.equals(idPlatillo, otra.idPlatillo)
                && idsVariaciones().equals(otra.idsVariaciones())
                && Objects.equals(notaNormalizada(), otra.notaNormalizada());
    }

    public List<Long> idsVariaciones() {
        return variaciones.stream().map(VariacionDTO::id).sorted().toList();
    }

    private String notaNormalizada() {
        return nota == null || nota.isBlank() ? null : nota.trim().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPlatillo() {
        return idPlatillo;
    }

    public void setIdPlatillo(Long idPlatillo) {
        this.idPlatillo = idPlatillo;
    }

    public String getNombrePlatillo() {
        return nombrePlatillo;
    }

    public void setNombrePlatillo(String nombrePlatillo) {
        this.nombrePlatillo = nombrePlatillo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public List<VariacionDTO> getVariaciones() {
        return variaciones;
    }

    public void setVariaciones(List<VariacionDTO> variaciones) {
        this.variaciones = variaciones;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public DetallePedidoDTO copia() {
        DetallePedidoDTO c = new DetallePedidoDTO(id, idPlatillo, nombrePlatillo, cantidad, precioUnitario, variaciones, nota);
        c.setCantidadPagada(cantidadPagada);
        return c;
    }
}
