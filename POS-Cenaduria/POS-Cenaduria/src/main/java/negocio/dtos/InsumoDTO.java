package negocio.dtos;

public class InsumoDTO {

    private Long id;
    private String nombre;
    private float stock;
    private float stockMinimo;
    // Calculado por EvaluadorStock; se ignora al registrar/actualizar
    private String estado;
    private String unidad;
    private String categoria;
    private boolean activo = true;

    public InsumoDTO() {
    }

    public InsumoDTO(Long id, String nombre, float stock, float stockMinimo, String estado,
                     String unidad, String categoria, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
        this.unidad = unidad;
        this.categoria = categoria;
        this.activo = activo;
    }

    /** Constructor para registrar un insumo nuevo. */
    public InsumoDTO(String nombre, float stock, float stockMinimo, String unidad, String categoria) {
        this(null, nombre, stock, stockMinimo, null, unidad, categoria, true);
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

    public float getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(float stockMinimo) {
        this.stockMinimo = stockMinimo;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
