package persistencia.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import persistencia.enums.CategoriaPlatillo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "platillos")
public class Platillo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaPlatillo categoria;

    // Se cambia a mano, p. ej. cuando se acaba el pozole a media jornada
    @Column(nullable = false)
    private boolean disponible = true;

    // 1 Platillo <>-- * VariacionPlatillo (composición)
    @OneToMany(mappedBy = "platillo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariacionPlatillo> variaciones = new ArrayList<>();

    public Platillo() {
    }

    public Platillo(String nombre, String descripcion, BigDecimal precio, CategoriaPlatillo categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
    }

    public void agregarVariacion(VariacionPlatillo variacion) {
        variacion.setPlatillo(this);
        variaciones.add(variacion);
    }

    public void quitarVariacion(VariacionPlatillo variacion) {
        variaciones.remove(variacion);
        variacion.setPlatillo(null);
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

    public CategoriaPlatillo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaPlatillo categoria) {
        this.categoria = categoria;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public List<VariacionPlatillo> getVariaciones() {
        return variaciones;
    }

    @Override
    public String toString() {
        return "Platillo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", categoria=" + categoria +
                ", disponible=" + disponible +
                '}';
    }
}
