package negocio.dtos;

public class InsumoDTO {

    private Long id;
    private String nombre;
    private float stock;
    private String estado;
    private String unidad;
    private String categoria;
    private AdministradorDTO administrador;

    public InsumoDTO() {
    }

    public InsumoDTO(Long id, String nombre, float stock, String estado,
                     String unidad, String categoria, AdministradorDTO administrador) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.estado = estado;
        this.unidad = unidad;
        this.categoria = categoria;
        this.administrador = administrador;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getStock() {
        return stock;
    }

    public void setStock(float stock) {
        this.stock = stock;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public AdministradorDTO getAdministrador() {
        return administrador;
    }

    public void setAdministrador(AdministradorDTO administrador) {
        this.administrador = administrador;
    }
}
