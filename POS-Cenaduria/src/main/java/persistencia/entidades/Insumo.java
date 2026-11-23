package persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.UnidadInsumo;

/**
 * El estado (AGOTADO / BAJO / DISPONIBLE) no se guarda: lo calcula
 * EvaluadorStock comparando stock contra stockMinimo.
 */
@Entity
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private float stock;

    @Column(name = "stock_minimo", nullable = false)
    private float stockMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadInsumo unidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaInsumo categoria;

    // Baja lógica: un insumo con movimientos no se borra, se desactiva
    @Column(nullable = false)
    private boolean activo = true;

    public Insumo() {
    }

    public Insumo(String nombre, float stock, float stockMinimo,
                  UnidadInsumo unidad, CategoriaInsumo categoria) {
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.unidad = unidad;
        this.categoria = categoria;
    }

    public Insumo(Long id, String nombre, float stock, float stockMinimo,
                  UnidadInsumo unidad, CategoriaInsumo categoria) {
        this(nombre, stock, stockMinimo, unidad, categoria);
        this.id = id;
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

    public UnidadInsumo getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadInsumo unidad) {
        this.unidad = unidad;
    }

    public CategoriaInsumo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaInsumo categoria) {
        this.categoria = categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Insumo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", stock=" + stock +
                ", stockMinimo=" + stockMinimo +
                ", unidad=" + unidad +
                ", categoria=" + categoria +
                ", activo=" + activo +
                '}';
    }
}
