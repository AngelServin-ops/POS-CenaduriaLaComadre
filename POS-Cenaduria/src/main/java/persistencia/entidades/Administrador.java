package persistencia.entidades;

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
        return "Administrador{}";
    }
}
