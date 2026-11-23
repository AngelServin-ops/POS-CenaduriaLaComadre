package persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Parte de un pago: "2 de los 3 pozoles". Permite separar la cuenta
 * y saber qué falta por cobrar de cada línea del pedido.
 */
@Entity
@Table(name = "partidas_pago")
public class PartidaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pago", nullable = false)
    private Pago pago;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_detalle_pedido", nullable = false)
    private DetallePedido detalle;

    @Column(nullable = false)
    private int cantidad;

    // cantidad × precio unitario de la línea al momento del cobro
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    public PartidaPago() {
    }

    public PartidaPago(DetallePedido detalle, int cantidad) {
        this.detalle = detalle;
        this.cantidad = cantidad;
        this.importe = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public DetallePedido getDetalle() {
        return detalle;
    }

    public void setDetalle(DetallePedido detalle) {
        this.detalle = detalle;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}
