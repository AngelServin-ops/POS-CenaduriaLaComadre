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
import persistencia.enums.MotivoMovimiento;
import persistencia.enums.TipoMovimiento;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_insumo")
public class MovimientoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoMovimiento motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private float cantidad;

    @Column(length = 255)
    private String observacion;

    // * Movimiento -> 1 Insumo
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    // * Movimiento -> 1 Usuario (quién lo realizó)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public MovimientoInsumo() {
    }

    public MovimientoInsumo(TipoMovimiento tipo, MotivoMovimiento motivo, float cantidad,
                            String observacion, Insumo insumo, Usuario usuario) {
        this.tipo = tipo;
        this.motivo = motivo;
        this.cantidad = cantidad;
        this.observacion = observacion;
        this.insumo = insumo;
        this.usuario = usuario;
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

    public MotivoMovimiento getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoMovimiento motivo) {
        this.motivo = motivo;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "MovimientoInsumo{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", motivo=" + motivo +
                ", fecha=" + fecha +
                ", cantidad=" + cantidad +
                ", insumo=" + (insumo != null ? insumo.getNombre() : null) +
                ", usuario=" + (usuario != null ? usuario.getNombreUsuario() : null) +
                '}';
    }
}
