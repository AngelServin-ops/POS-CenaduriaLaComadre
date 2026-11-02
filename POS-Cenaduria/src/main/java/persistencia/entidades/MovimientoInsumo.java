package persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import persistencia.enums.TipoMovimiento;

@Entity
@Table(name = "movimientos_insumo")
public class MovimientoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private float cantidad;

    // * Movimiento -> 1 Insumo
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    public MovimientoInsumo() {
    }

    public MovimientoInsumo(Long id, TipoMovimiento tipo, LocalDateTime fecha, float cantidad, Insumo insumo) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.insumo = insumo;
    }

    public MovimientoInsumo(TipoMovimiento tipo, float cantidad, Insumo insumo) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.insumo = insumo;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    @Override
    public String toString() {
        return "MovimientoInsumo{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", fecha=" + fecha +
                ", cantidad=" + cantidad +
                ", insumo=" + insumo +
                '}';
    }
}