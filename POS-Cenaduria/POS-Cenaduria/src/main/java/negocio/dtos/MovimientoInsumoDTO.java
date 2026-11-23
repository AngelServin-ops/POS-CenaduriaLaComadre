package negocio.dtos;

import java.time.LocalDateTime;

public class MovimientoInsumoDTO {

    private Long id;
    private String tipo;
    private String motivo;
    private LocalDateTime fecha;
    private float cantidad;
    private String observacion;
    private Long idInsumo;
    private String nombreInsumo;
    private Long idUsuario;
    private String nombreUsuario;

    public MovimientoInsumoDTO() {
    }

    /** Constructor para registrar un movimiento nuevo. */
    public MovimientoInsumoDTO(String tipo, String motivo, float cantidad, String observacion,
                               Long idInsumo, Long idUsuario) {
        this.tipo = tipo;
        this.motivo = motivo;
        this.cantidad = cantidad;
        this.observacion = observacion;
        this.idInsumo = idInsumo;
        this.idUsuario = idUsuario;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
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

    public Long getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Long idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
