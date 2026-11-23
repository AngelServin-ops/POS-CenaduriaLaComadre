package negocio.excepciones;

public class NegocioException extends Exception {

    public NegocioException() {
        super();
    }

    public NegocioException(String mensaje) {
        super(mensaje);
    }

    public NegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public NegocioException(Throwable causa) {
        super(causa);
    }
}