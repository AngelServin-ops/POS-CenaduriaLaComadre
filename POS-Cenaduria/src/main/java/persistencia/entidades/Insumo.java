package persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;

@Entity
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_insumo", nullable = false, length = 100)
    private String nombreInsumo;

    @Column(nullable = false)
    private float stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_insumo", length = 20)
    private EstadoInsumo estadoInsumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_insumo", nullable = false, length = 20)
    private UnidadInsumo unidadInsumo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaInsumo categoria;

    // 1 Administrador -> * Insumo
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_administrador", nullable = false)
    private Administrador administrador;

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