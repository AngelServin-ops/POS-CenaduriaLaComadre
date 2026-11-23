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
 * Extra con costo que se puede agregar a un platillo, p. ej. "Extra queso" (+$15).
 * Las indicaciones sin costo ("Sin cebolla") van en la nota de DetallePedido.
 */
@Entity
@Table(name = "variaciones_platillo")
public class VariacionPlatillo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "precio_extra", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioExtra = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_platillo", nullable = false)
    private Platillo platillo;

    // Un extra que ya se usó en pedidos no se borra (rompería el historial): se desactiva
    @Column(nullable = false)
    private boolean activa = true;

    public VariacionPlatillo() {
    }

    public VariacionPlatillo(String nombre, BigDecimal precioExtra) {
        this.nombre = nombre;
        this.precioExtra = precioExtra;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioExtra() {
        return precioExtra;
    }

    public void setPrecioExtra(BigDecimal precioExtra) {
        this.precioExtra = precioExtra;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Platillo getPlatillo() {
        return platillo;
    }

    public void setPlatillo(Platillo platillo) {
        this.platillo = platillo;
    }

    @Override
    public String toString() {
        return "VariacionPlatillo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precioExtra=" + precioExtra +
                '}';
    }
}
