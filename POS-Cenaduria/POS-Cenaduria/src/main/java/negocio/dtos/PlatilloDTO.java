package negocio.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlatilloDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoria;
    private boolean disponible = true;
    private List<VariacionDTO> variaciones = new ArrayList<>();

    public PlatilloDTO() {
    }

    public PlatilloDTO(Long id, String nombre, String descripcion, BigDecimal precio,
                       String categoria, boolean disponible, List<VariacionDTO> variaciones) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.disponible = disponible;
        this.variaciones = new ArrayList<>(variaciones);
    }

    /** Constructor para registrar un platillo nuevo. */
    public PlatilloDTO(String nombre, String descripcion, BigDecimal precio, String categoria) {
        this(null, nombre, descripcion, precio, categoria, true, List.of());
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public List<VariacionDTO> getVariaciones() {
        return variaciones;
    }

    public void setVariaciones(List<VariacionDTO> variaciones) {
        this.variaciones = variaciones;
    }

    /** Texto para los combos: "Tostada de tinga · $35.00". */
    @Override
    public String toString() {
        return nombre + " · " + Formatos.dinero(precio);
    }
}
