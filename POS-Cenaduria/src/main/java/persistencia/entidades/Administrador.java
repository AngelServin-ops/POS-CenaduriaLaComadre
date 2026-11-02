package persistencia.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    public Administrador() {
        super();
        setRol("ADMINISTRADOR");
    }

    public Administrador(String nombre) {
        super(nombre, "ADMINISTRADOR");
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                '}';
    }
}