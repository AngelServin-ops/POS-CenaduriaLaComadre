package negocio.dtos;

public class ConfiguracionDTO {

    private String nombreNegocio;
    private String direccion;
    private String telefono;
    private String mensajeTicket;
    private String nombreCaja;
    private int numeroMesas;

    public ConfiguracionDTO() {
    }

    public ConfiguracionDTO(String nombreNegocio, String direccion, String telefono, String mensajeTicket,
                            String nombreCaja, int numeroMesas) {
        this.nombreNegocio = nombreNegocio;
        this.direccion = direccion;
        this.telefono = telefono;
        this.mensajeTicket = mensajeTicket;
        this.nombreCaja = nombreCaja;
        this.numeroMesas = numeroMesas;
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
