package negocio.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de un cobro. Para registrar se llenan metodo, montoTarjeta, efectivoRecibido
 * y referenciaTerminal; el resto lo calcula el servicio.
 */
public class PagoDTO {

    private String metodo;
    private BigDecimal monto;
    private BigDecimal montoTarjeta = BigDecimal.ZERO;
    private BigDecimal efectivoRecibido = BigDecimal.ZERO;
    private BigDecimal efectivoAplicado = BigDecimal.ZERO;
    private BigDecimal cambio = BigDecimal.ZERO;
    private String referenciaTerminal;
    private LocalDateTime fechaHora;
    private String folioComprobante;
    private String nombreCajero;
    private int numero;
    private List<PartidaPagoDTO> partidas = new ArrayList<>();

    public PagoDTO() {
    }

    /** Constructor para registrar un cobro. */
    public PagoDTO(String metodo, BigDecimal montoTarjeta, BigDecimal efectivoRecibido,
                   String referenciaTerminal) {
        this.metodo = metodo;
        this.montoTarjeta = montoTarjeta != null ? montoTarjeta : BigDecimal.ZERO;
        this.efectivoRecibido = efectivoRecibido != null ? efectivoRecibido : BigDecimal.ZERO;
        this.referenciaTerminal = referenciaTerminal;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
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

    public BigDecimal getEfectivoAplicado() {
        return efectivoAplicado;
    }

    public void setEfectivoAplicado(BigDecimal efectivoAplicado) {
        this.efectivoAplicado = efectivoAplicado;
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(BigDecimal cambio) {
        this.cambio = cambio;
    }

    public String getReferenciaTerminal() {
        return referenciaTerminal;
    }

    public void setReferenciaTerminal(String referenciaTerminal) {
        this.referenciaTerminal = referenciaTerminal;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getFolioComprobante() {
        return folioComprobante;
    }

    public void setFolioComprobante(String folioComprobante) {
        this.folioComprobante = folioComprobante;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public List<PartidaPagoDTO> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<PartidaPagoDTO> partidas) {
        this.partidas = partidas;
    }

    public String getNombreCajero() {
        return nombreCajero;
    }

    public void setNombreCajero(String nombreCajero) {
        this.nombreCajero = nombreCajero;
    }
}
