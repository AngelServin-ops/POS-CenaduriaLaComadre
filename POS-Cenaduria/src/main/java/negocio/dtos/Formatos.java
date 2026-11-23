package negocio.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos de texto que comparten las pantallas: dinero, folios y fechas. */
public final class Formatos {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    private Formatos() {
    }

    public static String dinero(BigDecimal monto) {
        BigDecimal m = monto == null ? BigDecimal.ZERO : monto.setScale(2, RoundingMode.HALF_UP);
        return String.format(Locale.US, "$%,.2f", m);
    }

    public static String folioPedido(int folio) {
        return String.format("P-%04d", folio);
    }

    public static String folioComprobante(int folio) {
        return String.format("C-%04d", folio);
    }

    /** C-0142 para el primer pago; C-0142-2, C-0142-3... si la cuenta se separó. */
    public static String folioComprobante(int folio, int numeroPago) {
        return numeroPago <= 1 ? folioComprobante(folio) : folioComprobante(folio) + "-" + numeroPago;
    }

    public static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    public static String hora(LocalDateTime fechaHora) {
        return fechaHora.format(HORA);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora.format(FECHA_HORA);
    }

    /** "02" para la mesa 2. */
    public static String mesa(Integer mesa) {
        return mesa == null ? "" : String.format("%02d", mesa);
    }
}
