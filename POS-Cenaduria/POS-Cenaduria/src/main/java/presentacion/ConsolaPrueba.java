package presentacion;

import java.math.BigDecimal;
import java.util.List;

import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.InsumoDTO;
import negocio.dtos.MovimientoInsumoDTO;
import negocio.dtos.PagoDTO;
import negocio.dtos.PedidoDTO;
import negocio.dtos.PlatilloDTO;
import negocio.dtos.UsuarioDTO;
import negocio.dtos.VariacionDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IInsumoService;
import negocio.service.IPedidoService;
import negocio.service.IPlatilloService;
import negocio.service.IUsuarioService;
import negocio.service.InsumoService;
import negocio.service.PedidoService;
import negocio.service.PlatilloService;
import negocio.service.UsuarioService;
import persistencia.daoMemoria.InsumoDAOMemoria;
import persistencia.daoMemoria.MovimientoInsumoDAOMemoria;
import persistencia.daoMemoria.PedidoDAOMemoria;
import persistencia.daoMemoria.PlatilloDAOMemoria;
import persistencia.daoMemoria.UsuarioDAOMemoria;
import persistencia.interfaces.IInsumoDAO;
import persistencia.interfaces.IMovimientoInsumosDAO;
import persistencia.interfaces.IPlatilloDAO;
import persistencia.interfaces.IUsuarioDAO;

public class ConsolaPrueba {

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        // ---- Único lugar que cambia cuando pases a base de datos ----
        IUsuarioDAO usuarioDAO = new UsuarioDAOMemoria();
        IInsumoDAO insumoDAO = new InsumoDAOMemoria();
        IMovimientoInsumosDAO movimientoDAO = new MovimientoInsumoDAOMemoria();
        // --------------------------------------------------------------

        IUsuarioService usuarioService = new UsuarioService(usuarioDAO);
        IInsumoService insumoService = new InsumoService(insumoDAO, movimientoDAO, usuarioDAO);

        try {
            System.out.println("=== Usuarios ===");
            UsuarioDTO admin = usuarioService.registrarUsuario(
                    new UsuarioDTO("Carlos", "carlos", "secreto123", "ADMINISTRADOR"));
            usuarioService.registrarUsuario(
                    new UsuarioDTO("Lupita", "lupita", "mesera01", "MESERO"));
            UsuarioDTO sesion = usuarioService.iniciarSesion("carlos", "secreto123");
            System.out.println("Sesión iniciada: " + sesion.getNombre() + " (" + sesion.getRol() + ")");
            try {
                usuarioService.iniciarSesion("carlos", "incorrecta");
            } catch (NegocioException e) {
                System.out.println("Contraseña incorrecta -> " + e.getMessage());
            }

            // Cada insumo con su propio stock mínimo
            insumoService.registrarInsumo(new InsumoDTO("Pollo", 10f, 4f, "KG", "CARNES"));      // DISPONIBLE
            InsumoDTO tomate = insumoService.registrarInsumo(
                    new InsumoDTO("Tomate", 2f, 3f, "KG", "VERDURAS"));                          // BAJO
            insumoService.registrarInsumo(new InsumoDTO("Cebolla", 0f, 5f, "PIEZA", "VERDURAS")); // AGOTADO

            System.out.println("\n=== Lista inicial ===");
            mostrarLista(insumoService.listarInsumos());

            // Compra de tomate: entra al inventario
            insumoService.registrarMovimiento(new MovimientoInsumoDTO(
                    "ENTRADA", "COMPRA", 8f, "Mercado de abastos", tomate.getId(), admin.getId()));
            // Cierre del día: se registra lo que se usó durante la jornada
            insumoService.registrarMovimiento(new MovimientoInsumoDTO(
                    "SALIDA", "CONSUMO_JORNADA", 5f, null, tomate.getId(), admin.getId()));

            System.out.println("\n=== Después de la compra y el cierre del día ===");
            mostrarLista(insumoService.listarInsumos());

            System.out.println("\n=== Historial del tomate ===");
            for (MovimientoInsumoDTO m : insumoService.listarMovimientos(tomate.getId())) {
                System.out.printf("%-8s %-16s %5.1f  por %s%n",
                        m.getTipo(), m.getMotivo(), m.getCantidad(), m.getNombreUsuario());
            }

            System.out.println("\n=== Pruebas de validación ===");
            probar(() -> insumoService.registrarMovimiento(new MovimientoInsumoDTO(
                    "ENTRADA", "MERMA", 1f, null, tomate.getId(), admin.getId())));
            probar(() -> insumoService.registrarMovimiento(new MovimientoInsumoDTO(
                    "SALIDA", "CONSUMO_JORNADA", 100f, null, tomate.getId(), admin.getId())));
            probar(() -> insumoService.registrarInsumo(new InsumoDTO("Pan", 5f, 1f, "PIEZA", "PANES")));

        } catch (NegocioException e) {
            System.out.println("Error de negocio: " + e.getMessage());
        }

        demostrarPedido();
    }

    /** Flujo completo de pedidos con los servicios: alta, edición, cobro y cancelación. */
    private static void demostrarPedido() {
        System.out.println("\n=== Pedidos ===");
        IUsuarioDAO usuarioDAO = new UsuarioDAOMemoria();
        IPlatilloDAO platilloDAO = new PlatilloDAOMemoria();
        IPedidoService pedidos = new PedidoService(new PedidoDAOMemoria(), platilloDAO, usuarioDAO);
        IPlatilloService menu = new PlatilloService(platilloDAO);
        pedidos.agregarEscucha(e -> System.out.println("  [aviso] " + e.texto()));

        try {
            UsuarioDTO mesera = new UsuarioService(usuarioDAO).registrarUsuario(
                    new UsuarioDTO("Lupita", "lupita", "mesera01", "MESERO"));

            PlatilloDTO enchiladas = new PlatilloDTO("Enchiladas", "Orden de 4", new BigDecimal("85.00"), "PLATILLO");
            enchiladas.getVariaciones().add(new VariacionDTO(null, "Extra queso", new BigDecimal("15.00")));
            enchiladas = menu.registrarPlatillo(enchiladas);
            PlatilloDTO horchata = menu.registrarPlatillo(
                    new PlatilloDTO("Agua de horchata", "Litro", new BigDecimal("30.00"), "BEBIDA"));

            VariacionDTO extraQueso = enchiladas.getVariaciones().get(0);
            PedidoDTO nuevo = new PedidoDTO(null, "EN_MESA", 3, null, List.of(
                    new DetallePedidoDTO(null, enchiladas.getId(), enchiladas.getNombre(), 2, null,
                            List.of(extraQueso), "Sin cebolla"),
                    new DetallePedidoDTO(null, horchata.getId(), horchata.getNombre(), 1, null, null, null)));
            PedidoDTO pedido = pedidos.registrarPedido(nuevo, mesera.getId());
            mostrarPedido(pedido);

            // Edición: 2 horchatas en lugar de 1
            pedido.getDetalles().get(1).setCantidad(2);
            pedido = pedidos.actualizarPedido(pedido);
            System.out.println("Después de editar: " + Formatos.dinero(pedido.getTotal()));

            final Long idPedido = pedido.getId();
            final Long idMesera = mesera.getId();
            probar(() -> pedidos.cobrarPedido(idPedido,
                    new PagoDTO("EFECTIVO", null, new BigDecimal("100"), null), idMesera));

            PedidoDTO pagado = pedidos.cobrarPedido(idPedido,
                    new PagoDTO("MIXTO", new BigDecimal("100"), new BigDecimal("200"), null), idMesera);
            System.out.println("Cobro mixto -> " + pagado.getEstado() + ", cambio "
                    + Formatos.dinero(pagado.getPagos().get(0).getCambio()) + ", comprobante "
                    + pagado.getPagos().get(0).getFolioComprobante());

            probar(() -> pedidos.cobrarPedido(idPedido,
                    new PagoDTO("EFECTIVO", null, new BigDecimal("500"), null), idMesera));
            probar(() -> pedidos.cancelarPedido(idPedido, "El cliente se fue"));
        } catch (NegocioException e) {
            System.out.println("Error de negocio: " + e.getMessage());
        }
    }

    private static void mostrarPedido(PedidoDTO p) {
        System.out.println(p.getFolioTexto() + " · " + p.getServicioTexto() + " · " + p.getEstado()
                + " / pago " + Estilos.textoPago(p));
        for (DetallePedidoDTO d : p.getDetalles()) {
            System.out.printf("  %d x %-28s %9s  %s%n", d.getCantidad(), d.descripcion(),
                    Formatos.dinero(d.calcularSubtotal()), d.getNota() != null ? "Nota: " + d.getNota() : "");
        }
        System.out.println("  Total: " + Formatos.dinero(p.getTotal()));
    }

    private interface Accion {
        void ejecutar() throws NegocioException;
    }

    private static void probar(Accion accion) {
        try {
            accion.ejecutar();
            System.out.println("(no falló, pero debía fallar)");
        } catch (NegocioException e) {
            System.out.println("OK, rechazado -> " + e.getMessage());
        }
    }

    private static void mostrarLista(List<InsumoDTO> insumos) {
        System.out.printf("%-4s %-12s %-8s %-8s %-8s %-10s %s%n",
                "ID", "NOMBRE", "STOCK", "MÍNIMO", "UNIDAD", "CATEGORÍA", "ESTADO");
        for (InsumoDTO i : insumos) {
            System.out.printf("%-4d %-12s %-8.1f %-8.1f %-8s %-10s %s%n",
                    i.getId(), i.getNombre(), i.getStock(), i.getStockMinimo(), i.getUnidad(),
                    i.getCategoria(), ColorEstado.pintar(i.getEstado()));
        }
    }
}
