package presentacion;

public class ColorEstado {

    private static final String RESET = "\u001B[0m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARILLO = "\u001B[33m";
    private static final String ROJO = "\u001B[31m";

    private ColorEstado() {
    }

    public static String pintar(String estado) {
        if (estado == null) {
            return "SIN ESTADO";
        }
        return switch (estado) {
            case "DISPONIBLE" -> VERDE + estado + RESET;
            case "BAJO" -> AMARILLO + estado + RESET;
            case "AGOTADO" -> ROJO + estado + RESET;
            default -> estado;
        };
    }
}
