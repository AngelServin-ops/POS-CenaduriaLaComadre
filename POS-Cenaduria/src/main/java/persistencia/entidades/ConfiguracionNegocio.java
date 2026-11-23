package persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Datos del negocio que salen en el ticket y ajustes de la caja. Hay un solo registro. */
@Entity
@Table(name = "configuracion_negocio")
public class ConfiguracionNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_negocio", nullable = false, length = 100)
    private String nombreNegocio = "Cenaduría La Comadre";

    @Column(length = 200)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    // Texto al pie del ticket: "¡Gracias por su visita!"
    @Column(name = "mensaje_ticket", length = 200)
    private String mensajeTicket;

    @Column(name = "nombre_caja", nullable = false, length = 30)
    private String nombreCaja = "Caja 01";

    @Column(name = "numero_mesas", nullable = false)
    private int numeroMesas = 20;

    public ConfiguracionNegocio() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public void setNombreNegocio(String nombreNegocio) {
        this.nombreNegocio = nombreNegocio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMensajeTicket() {
        return mensajeTicket;
    }

    public void setMensajeTicket(String mensajeTicket) {
        this.mensajeTicket = mensajeTicket;
    }

    public String getNombreCaja() {
        return nombreCaja;
    }

    public void setNombreCaja(String nombreCaja) {
        this.nombreCaja = nombreCaja;
    }

    public int getNumeroMesas() {
        return numeroMesas;
    }

    public void setNumeroMesas(int numeroMesas) {
        this.numeroMesas = numeroMesas;
    }
}
