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
import persistencia.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un cobro de un pedido. La cuenta se puede pagar en un solo pago o separada en varios;
 * cada pago dice qué platillos cubre (partidas). Efectivo, tarjeta o mixto
 * (parte con tarjeta y el resto en efectivo). Con tarjeta no se da cambio.
 */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número de pago dentro del pedido: 1, 2, 3... (para el folio del comprobante)
    @Column(nullable = false)
    private int numero;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodo;

    // Total cobrado en este pago (suma de sus partidas)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_tarjeta", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTarjeta = BigDecimal.ZERO;

    // Lo que entregó el cliente en efectivo
    @Column(name = "efectivo_recibido", nullable = false, precision = 10, scale = 2)
    private BigDecimal efectivoRecibido = BigDecimal.ZERO;

    // Número de aprobación de la terminal (solo pagos con tarjeta)
    @Column(name = "referencia_terminal", length = 50)
    private String referenciaTerminal;

    // * Pago -> 1 Pedido
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    // 1 Pago <>-- 1..* PartidaPago: qué platillos (y cuántos) cubre este pago
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartidaPago> partidas = new ArrayList<>();

    // * Pago -> 1 Usuario (quién cobró)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public Pago() {
    }

    public Pago(int numero, MetodoPago metodo, List<PartidaPago> partidas, BigDecimal montoTarjeta,
                BigDecimal efectivoRecibido, String referenciaTerminal, Usuario usuario) {
        this.numero = numero;
        this.metodo = metodo;
        for (PartidaPago p : partidas) {
            p.setPago(this);
            this.partidas.add(p);
        }
        this.monto = partidas.stream().map(PartidaPago::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.montoTarjeta = montoTarjeta != null ? montoTarjeta : BigDecimal.ZERO;
        this.efectivoRecibido = efectivoRecibido != null ? efectivoRecibido : BigDecimal.ZERO;
        this.referenciaTerminal = referenciaTerminal;
        this.usuario = usuario;
        this.fechaHora = LocalDateTime.now();
    }

    /** Parte del monto que se cubre con efectivo. */
    public BigDecimal calcularEfectivoAplicado() {
        return monto.subtract(montoTarjeta).max(BigDecimal.ZERO);
    }

    public BigDecimal calcularCambio() {
        return efectivoRecibido.subtract(calcularEfectivoAplicado()).max(BigDecimal.ZERO);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMontoTarjeta() {
        return montoTarjeta;
    }

    public void setMontoTarjeta(BigDecimal montoTarjeta) {
        this.montoTarjeta = montoTarjeta;
    }

    public BigDecimal getEfectivoRecibido() {
        return efectivoRecibido;
    }

    public void setEfectivoRecibido(BigDecimal efectivoRecibido) {
        this.efectivoRecibido = efectivoRecibido;
    }

    public String getReferenciaTerminal() {
        return referenciaTerminal;
    }

    public void setReferenciaTerminal(String referenciaTerminal) {
        this.referenciaTerminal = referenciaTerminal;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public List<PartidaPago> getPartidas() {
        return partidas;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", numero=" + numero +
                ", metodo=" + metodo +
                ", monto=" + monto +
                ", montoTarjeta=" + montoTarjeta +
                ", efectivoRecibido=" + efectivoRecibido +
                ", cambio=" + calcularCambio() +
                '}';
    }
}
