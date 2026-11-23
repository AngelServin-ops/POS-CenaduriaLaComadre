package persistencia.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import persistencia.enums.EstadoPedido;
import persistencia.enums.TipoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número que ve el cliente / la cocina
    @Column(nullable = false, unique = true)
    private int folio;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPedido tipo;

    // Solo aplica cuando tipo = EN_MESA
    private Integer mesa;

    @Column(name = "nombre_cliente", length = 100)
    private String nombreCliente;

    // Notas generales del pedido (p. ej. "paga con billete de $500").
    // Las indicaciones de cada platillo van en DetallePedido.nota
    @Column(length = 255)
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.ACTIVO;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    // * Pedido -> 1 Usuario (quién lo registró)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // 1 Pedido <>-- 1..* DetallePedido (composición)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    // 1 Pedido -- 0..* Pago (la cuenta se puede pagar completa o separada en varios pagos)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<Pago> pagos = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(int folio, TipoPedido tipo, Integer mesa, String nombreCliente,
                  String notas, Usuario usuario) {
        this.folio = folio;
        this.tipo = tipo;
        this.mesa = mesa;
        this.nombreCliente = nombreCliente;
        this.notas = notas;
        this.usuario = usuario;
        this.fechaHora = LocalDateTime.now();
    }

    // ---------------- Comportamiento ----------------

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }

    public void quitarDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        detalle.setPedido(null);
    }

    /** El total no se guarda: se calcula a partir de los detalles. */
    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetallePedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Solo un pedido activo y sin ningún pago se puede editar o cancelar. */
    public boolean puedeModificarse() {
        return estado == EstadoPedido.ACTIVO && pagos.isEmpty();
    }

    public BigDecimal calcularTotalPagado() {
        return pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lo que falta por cobrar. */
    public BigDecimal calcularSaldo() {
        return calcularTotal().subtract(calcularTotalPagado());
    }

    /** Cuántas unidades de esa línea ya se cobraron (en cuentas separadas). */
    public int cantidadPagada(DetallePedido detalle) {
        return pagos.stream()
                .flatMap(p -> p.getPartidas().stream())
                .filter(pp -> pp.getDetalle() == detalle)
                .mapToInt(PartidaPago::getCantidad)
                .sum();
    }

    public void agregarPago(Pago pago) {
        pago.setPedido(this);
        pagos.add(pago);
    }

    public void quitarPago(Pago pago) {
        pagos.remove(pago);
    }

    public void cancelar(String motivo) {
        if (!puedeModificarse()) {
            throw new IllegalStateException("Solo los pedidos activos y sin pagos pueden cancelarse");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo de la cancelación");
        }
        this.estado = EstadoPedido.CANCELADO;
        this.motivoCancelacion = motivo.trim();
        this.fechaCancelacion = LocalDateTime.now();
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

    public TipoPedido getTipo() {
        return tipo;
    }

    public void setTipo(TipoPedido tipo) {
        this.tipo = tipo;
    }

    public Integer getMesa() {
        return mesa;
    }

    public void setMesa(Integer mesa) {
        this.mesa = mesa;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", folio=" + folio +
                ", fechaHora=" + fechaHora +
                ", tipo=" + tipo +
                ", mesa=" + mesa +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", estado=" + estado +
                ", total=" + calcularTotal() +
                '}';
    }
}
