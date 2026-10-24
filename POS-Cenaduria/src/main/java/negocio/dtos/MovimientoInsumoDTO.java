package negocio.dtos;

import java.time.LocalDateTime;

public class MovimientoInsumoDTO {

    private Long id;
    private String tipo;
    private LocalDateTime fecha;
    private float cantidad;
    private InsumoDTO insumo;

    public MovimientoInsumoDTO() {
    }

    public MovimientoInsumoDTO(Long id, String tipo, LocalDateTime fecha,
                               float cantidad, InsumoDTO insumo) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.insumo = insumo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
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

    public InsumoDTO getInsumo() {
        return insumo;
    }

    public void setInsumo(InsumoDTO insumo) {
        this.insumo = insumo;
    }
}
