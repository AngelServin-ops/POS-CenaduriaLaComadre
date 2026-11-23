package persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Una línea del pedido: qué platillo, cuántos, con qué extras y con qué indicaciones.
 */
@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int cantidad;

    // Precio del platillo + extras AL MOMENTO de la venta.
    // Si después cambia el precio del platillo, este pedido no se altera.
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Indicaciones del cliente: "Sin cebolla", "Bien dorado", "Salsa aparte"...
    @Column(length = 255)
    private String nota;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    // * DetallePedido -> 1 Platillo
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_platillo", nullable = false)
    private Platillo platillo;

    // * DetallePedido -> * VariacionPlatillo (extras con costo)
    @ManyToMany
    @JoinTable(name = "detalle_pedido_variaciones",
            joinColumns = @JoinColumn(name = "id_detalle_pedido"),
            inverseJoinColumns = @JoinColumn(name = "id_variacion"))
    private List<VariacionPlatillo> variaciones = new ArrayList<>();

    public DetallePedido() {
    }

    public DetallePedido(Platillo platillo, int cantidad, List<VariacionPlatillo> variaciones, String nota) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.platillo = platillo;
        this.cantidad = cantidad;
        this.nota = nota;
        if (variaciones != null) {
            this.variaciones.addAll(variaciones);
        }
        this.precioUnitario = platillo.getPrecio().add(
                this.variaciones.stream()
                        .map(VariacionPlatillo::getPrecioExtra)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Platillo getPlatillo() {
        return platillo;
    }

    public void setPlatillo(Platillo platillo) {
        this.platillo = platillo;
    }

    public List<VariacionPlatillo> getVariaciones() {
        return variaciones;
    }

    @Override
    public String toString() {
        return "DetallePedido{" +
                "cantidad=" + cantidad +
                ", platillo=" + (platillo != null ? platillo.getNombre() : null) +
                ", precioUnitario=" + precioUnitario +
                ", nota='" + nota + '\'' +
                '}';
    }
}
