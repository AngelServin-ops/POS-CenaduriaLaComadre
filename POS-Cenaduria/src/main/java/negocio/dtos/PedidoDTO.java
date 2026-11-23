package negocio.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDTO {

    private Long id;
    private int folio;
    private LocalDateTime fechaHora;
    private String tipo;
    private Integer mesa;
    private String notas;
    private String estado;           // ACTIVO, PAGADO, CANCELADO
    private boolean modificable;     // se puede editar o cancelar (activo y sin pagos)
    private String motivoCancelacion;
    private LocalDateTime fechaCancelacion;
    private List<DetallePedidoDTO> detalles = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal totalPagado = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;
    private List<PagoDTO> pagos = new ArrayList<>();
    private String nombreUsuario;

    public PedidoDTO() {
    }

    /** Constructor para registrar o editar un pedido. */
    public PedidoDTO(Long id, String tipo, Integer mesa, String notas, List<DetallePedidoDTO> detalles) {
        this.id = id;
        this.tipo = tipo;
        this.mesa = mesa;
        this.notas = notas;
        this.detalles = new ArrayList<>(detalles);
    }

    // ---------------- Textos para pantalla ----------------

    public String getFolioTexto() {
        return Formatos.folioPedido(folio);
    }

    /** "Mesa 04", "Para llevar" o "Domicilio". */
    public String getServicioTexto() {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case "EN_MESA" -> "Mesa " + Formatos.mesa(mesa);
            case "PARA_LLEVAR" -> "Para llevar";
            case "DOMICILIO" -> "Domicilio";
            default -> tipo;
        };
    }

    public int getTotalProductos() {
        return detalles.stream().mapToInt(DetallePedidoDTO::getCantidad).sum();
    }

    public boolean esActivo() {
        return "ACTIVO".equals(estado);
    }

    public boolean esPagado() {
        return "PAGADO".equals(estado);
    }

    public boolean esCancelado() {
        return "CANCELADO".equals(estado);
    }

    /** Activo y con al menos un pago (cuenta separada a medio cobrar). */
    public boolean esParcial() {
        return esActivo() && !pagos.isEmpty();
    }

    // ---------------- Getters y setters ----------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getMesa() {
        return mesa;
    }

    public void setMesa(Integer mesa) {
        this.mesa = mesa;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isModificable() {
        return modificable;
    }

    public void setModificable(boolean modificable) {
        this.modificable = modificable;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public List<DetallePedidoDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoDTO> detalles) {
        this.detalles = detalles;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public List<PagoDTO> getPagos() {
        return pagos;
    }

    public void setPagos(List<PagoDTO> pagos) {
        this.pagos = pagos;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
