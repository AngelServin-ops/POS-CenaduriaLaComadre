package persistencia.entidades;

import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;

public class Insumo {

    private Long id;
    private String nombreInsumo;
    private float stock;
    private EstadoInsumo estadoInsumo;
    private UnidadInsumo unidadInsumo;
    private CategoriaInsumo categoria;
    private Administrador administrador; // 1 Administrador -> * Insumo

    public Insumo() {
    }

    public Insumo(Long id, String nombreInsumo, float stock, EstadoInsumo estadoInsumo, UnidadInsumo unidadInsumo,
                  CategoriaInsumo categoria, Administrador administrador) {
        this.id = id;
        this.nombreInsumo = nombreInsumo;
        this.stock = stock;
        this.estadoInsumo = estadoInsumo;
        this.unidadInsumo = unidadInsumo;
        this.categoria = categoria;
        this.administrador = administrador;
    }

    public Insumo(String nombreInsumo, float stock, UnidadInsumo unidadInsumo,
                  CategoriaInsumo categoria, Administrador administrador) {
        this.nombreInsumo = nombreInsumo;
        this.stock = stock;
        this.unidadInsumo = unidadInsumo;
        this.categoria = categoria;
        this.administrador = administrador;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public float getStock() {
        return stock;
    }

    public void setStock(float stock) {
        this.stock = stock;
    }

    public EstadoInsumo getEstadoInsumo() {
        return estadoInsumo;
    }

    public void setEstadoInsumo(EstadoInsumo estadoInsumo) {
        this.estadoInsumo = estadoInsumo;
    }

    public UnidadInsumo getUnidadInsumo() {
        return unidadInsumo;
    }

    public void setUnidadInsumo(UnidadInsumo unidadInsumo) {
        this.unidadInsumo = unidadInsumo;
    }

    public CategoriaInsumo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaInsumo categoria) {
        this.categoria = categoria;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    @Override
    public String toString() {
        return "Insumo{" +
                "id=" + id +
                ", nombreInsumo='" + nombreInsumo + '\'' +
                ", stock=" + stock +
                ", estadoInsumo=" + estadoInsumo +
                ", unidadInsumo=" + unidadInsumo +
                ", categoria=" + categoria +
                ", administrador=" + administrador +
                '}';
    }
}

