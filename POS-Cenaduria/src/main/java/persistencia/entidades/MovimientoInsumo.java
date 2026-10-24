package persistencia.entidades;

import persistencia.enums.TipoMovimiento;

import java.time.LocalDateTime;

public class MovimientoInsumo {

    private Long id;
    private TipoMovimiento tipo;
    private LocalDateTime fecha;
    private float cantidad;
    private Insumo insumo; // * Movimiento -> 1 Insumo

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

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }


}
