package presentacion;

import java.math.BigDecimal;
import java.util.List;
import negocio.dtos.ConfiguracionDTO;
import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.MovimientoInsumoDTO;
import negocio.dtos.InsumoDTO;
import negocio.dtos.PagoDTO;
import negocio.dtos.PartidaCobroDTO;
import negocio.dtos.PedidoDTO;
import negocio.dtos.PlatilloDTO;
import negocio.dtos.UsuarioDTO;
import negocio.dtos.VariacionDTO;
import negocio.excepciones.NegocioException;
import negocio.service.ConfiguracionService;
import negocio.service.IConfiguracionService;
import negocio.service.IInsumoService;
import negocio.service.IReporteService;
import negocio.service.ReporteService;
import negocio.service.IPedidoService;
import negocio.service.IPlatilloService;
import negocio.service.IUsuarioService;
import negocio.service.InsumoService;
import negocio.service.PedidoService;
import negocio.service.PlatilloService;
import negocio.service.UsuarioService;
import persistencia.daos.ConfiguracionDAO;
import persistencia.daos.InsumoDAO;
import persistencia.daos.MovimientoInsumosDAO;
import persistencia.daos.PedidoDAO;
import persistencia.daos.PlatilloDAO;
import persistencia.daos.UsuarioDAO;
import persistencia.daoMemoria.ConfiguracionDAOMemoria;
import persistencia.daoMemoria.InsumoDAOMemoria;
import persistencia.daoMemoria.MovimientoInsumoDAOMemoria;
import persistencia.daoMemoria.PedidoDAOMemoria;
import persistencia.daoMemoria.PlatilloDAOMemoria;
import persistencia.daoMemoria.UsuarioDAOMemoria;
import persistencia.interfaces.IConfiguracionDAO;
import persistencia.interfaces.IInsumoDAO;
import persistencia.interfaces.IMovimientoInsumosDAO;
import persistencia.interfaces.IPedidoDAO;
import persistencia.interfaces.IPlatilloDAO;
import persistencia.interfaces.IUsuarioDAO;

/**
 * Servicios compartidos por todas las pantallas y la sesión.
 *
 * Normalmente trabaja con MySQL (DAO JPA). La base de datos empieza vacía: al arrancar
 * solo se crean, si no existen, el usuario "Caja" y un administrador (admin / admin123)
 * para poder entrar. Los datos de ejemplo se cargan con el script SQL.
 *
 * Para probar sin MySQL: ejecutar con -Dpos.memoria=true (DAO en memoria + datos de prueba).
 */
public final class ContextoApp {

    /** true = sin base de datos, con datos de prueba en memoria (-Dpos.memoria=true). */
    public static final boolean EN_MEMORIA = Boolean.getBoolean("pos.memoria");

    // ---- DAO: MySQL (JPA) o memoria ----
    private static final IUsuarioDAO USUARIO_DAO = EN_MEMORIA ? new UsuarioDAOMemoria() : new UsuarioDAO();
    private static final IInsumoDAO INSUMO_DAO = EN_MEMORIA ? new InsumoDAOMemoria() : new InsumoDAO();
    private static final IMovimientoInsumosDAO MOVIMIENTO_DAO =
            EN_MEMORIA ? new MovimientoInsumoDAOMemoria() : new MovimientoInsumosDAO();
    private static final IPlatilloDAO PLATILLO_DAO = EN_MEMORIA ? new PlatilloDAOMemoria() : new PlatilloDAO();
    private static final IPedidoDAO PEDIDO_DAO = EN_MEMORIA ? new PedidoDAOMemoria() : new PedidoDAO();
    private static final IConfiguracionDAO CONFIGURACION_DAO =
            EN_MEMORIA ? new ConfiguracionDAOMemoria() : new ConfiguracionDAO();
    // ------------------------------------

    public static final IUsuarioService USUARIOS = new UsuarioService(USUARIO_DAO);
    public static final IInsumoService INSUMOS = new InsumoService(INSUMO_DAO, MOVIMIENTO_DAO, USUARIO_DAO);
    public static final IPlatilloService PLATILLOS = new PlatilloService(PLATILLO_DAO);
    public static final IPedidoService PEDIDOS = new PedidoService(PEDIDO_DAO, PLATILLO_DAO, USUARIO_DAO);
    public static final IConfiguracionService CONFIGURACION = new ConfiguracionService(CONFIGURACION_DAO);
    public static final IReporteService REPORTES = new ReporteService(PEDIDO_DAO, MOVIMIENTO_DAO);

    // ---- Sesión ----
    // Sin iniciar sesión la app trabaja como "Caja" (registrar, consultar y cobrar pedidos).
    // Con la contraseña de un administrador se abren Inventario, Reportes, Usuarios y Configuración.
    private static UsuarioDTO usuarioCaja;
    private static UsuarioDTO administrador;
    private static final List<Runnable> alCambiarSesion = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        if (EN_MEMORIA) {
            cargarDatosPrueba();
        } else {
            prepararBaseDeDatos();
        }
    }

    private ContextoApp() {
    }

    /** Quien está usando la caja: el administrador si inició sesión, si no el usuario "Caja". */
    public static UsuarioDTO getUsuario() {
        return administrador != null ? administrador : usuarioCaja;
    }

    /** Usuario fijo con el que la caja registra pedidos cuando nadie inició sesión. */
    public static UsuarioDTO getUsuarioCaja() {
        return usuarioCaja;
    }

    public static boolean esAdministrador() {
        return administrador != null;
    }

    public static UsuarioDTO getAdministrador() {
        return administrador;
    }

    /** Valida la contraseña y abre el modo administrador. */
    public static void iniciarSesion(String contrasena) throws NegocioException {
        administrador = USUARIOS.iniciarSesionAdministrador(contrasena);
        alCambiarSesion.forEach(Runnable::run);
    }

    public static void cerrarSesion() {
        administrador = null;
        alCambiarSesion.forEach(Runnable::run);
    }

    public static void alCambiarSesion(Runnable r) {
        alCambiarSesion.add(r);
    }

    /** Nombre de la caja configurado ("Caja 01"). */
    public static String nombreCaja() {
        return configuracion().getNombreCaja();
    }

    public static ConfiguracionDTO configuracion() {
        try {
            return CONFIGURACION.obtener();
        } catch (NegocioException e) {
            return new ConfiguracionDTO("Cenaduría La Comadre", null, null, null, "Caja 01", 20);
        }
    }

    /**
     * Con MySQL: deja la base lista para usarse sin meter datos de ejemplo.
     * Solo crea lo indispensable si falta: el usuario "Caja" (con el que se registran
     * los pedidos sin sesión) y un administrador para poder entrar.
     */
    private static void prepararBaseDeDatos() {
        try {
            List<UsuarioDTO> usuarios = USUARIOS.listarUsuarios();
            boolean hayAdministrador = usuarios.stream()
                    .anyMatch(x -> x.isActivo() && "ADMINISTRADOR".equals(x.getRol()));
            if (!hayAdministrador) {
                USUARIOS.registrarUsuario(new UsuarioDTO("Administrador", "admin", "admin123", "ADMINISTRADOR"));
                System.out.println("Administrador inicial creado (contraseña: admin123). Cámbiala en Configuración.");
            }
            usuarioCaja = usuarios.stream()
                    .filter(x -> "caja".equalsIgnoreCase(x.getNombreUsuario()))
                    .findFirst()
                    .orElse(null);
            if (usuarioCaja == null) {
                usuarioCaja = USUARIOS.registrarUsuario(new UsuarioDTO("Caja", "caja", null, "CAJERO"));
            }
        } catch (NegocioException e) {
            throw new IllegalStateException("No se pudo preparar la base de datos: " + e.getMessage(), e);
        }
    }

    /** Datos de prueba para el modo memoria (sin MySQL). */
    private static void cargarDatosPrueba() {
        try {
            UsuarioDTO admin = USUARIOS.registrarUsuario(
                    new UsuarioDTO("Administrador", "admin", "admin123", "ADMINISTRADOR"));
            usuarioCaja = USUARIOS.registrarUsuario(new UsuarioDTO("Caja", "caja", null, "CAJERO"));
            USUARIOS.registrarUsuario(new UsuarioDTO("Lupita Gómez", "lupita", null, "MESERO"));
            CONFIGURACION.guardar(new ConfiguracionDTO("Cenaduría La Comadre", "Calle Juárez 123, Centro",
                    "644 123 4567", "¡Gracias por su visita, vuelva pronto!", "Caja 01", 20));

            InsumoDTO jitomate = INSUMOS.registrarInsumo(new InsumoDTO("Jitomate", 2f, 3f, "KG", "VERDURAS"));
            INSUMOS.registrarInsumo(new InsumoDTO("Cebolla", 0f, 2f, "KG", "VERDURAS"));
            INSUMOS.registrarInsumo(new InsumoDTO("Queso", 1f, 2f, "KG", "LACTEOS"));
            InsumoDTO pollo = INSUMOS.registrarInsumo(new InsumoDTO("Pechuga de pollo", 8f, 3f, "KG", "CARNES"));
            // Movimientos de ejemplo para Reportes
            movimiento("ENTRADA", "COMPRA", 20, jitomate, admin);
            movimiento("SALIDA", "CONSUMO_JORNADA", 19, jitomate, admin);
            movimiento("SALIDA", "MERMA", 1, jitomate, admin);
            movimiento("ENTRADA", "COMPRA", 6, pollo, admin);
            movimiento("SALIDA", "CONSUMO_JORNADA", 6, pollo, admin);

            PlatilloDTO pozoleRojo = platillo("Pozole rojo", "65", "PLATILLO");
            PlatilloDTO pozoleBlanco = platillo("Pozole blanco", "65", "PLATILLO");
            PlatilloDTO tostada = platillo("Tostada de tinga", "35", "PLATILLO", "Aguacate extra", "10");
            platillo("Tacos dorados", "55", "PLATILLO", "Extra queso", "10");
            platillo("Sope", "30", "PLATILLO");
            PlatilloDTO chilaquiles = platillo("Chilaquiles rojos", "40", "PLATILLO");
            PlatilloDTO jamaica = platillo("Agua de jamaica", "25", "BEBIDA");
            platillo("Agua de horchata", "25", "BEBIDA");

            Long u = usuarioCaja.getId();
            // Pagado y entregado
            PedidoDTO p1 = PEDIDOS.registrarPedido(pedido("EN_MESA", 1,
                    linea(pozoleRojo, 1, null), linea(jamaica, 1, null)), u);
            PEDIDOS.cobrarPedido(p1.getId(), new PagoDTO("EFECTIVO", null, new BigDecimal("100"), null), u);
            // Cancelado
            PedidoDTO p2 = PEDIDOS.registrarPedido(pedido("EN_MESA", 9,
                    linea(tostada, 2, null)), u);
            PEDIDOS.cancelarPedido(p2.getId(), "El cliente se retiró antes de ordenar");
            // Activo, sin pagar
            PedidoDTO p3 = PEDIDOS.registrarPedido(pedido("EN_MESA", 2,
                    linea(pozoleBlanco, 2, "Sin cebolla"), linea(jamaica, 1, null)), u);
            // Activo listo para entregar
            PedidoDTO p4 = PEDIDOS.registrarPedido(pedido("EN_MESA", 7,
                    linea(pozoleBlanco, 1, "Sin cebolla"), linea(tostada, 3, null),
                    linea(jamaica, 2, null), linea(chilaquiles, 1, null)), u);
            // Pagados con tarjeta y mixto
            PedidoDTO p6 = PEDIDOS.registrarPedido(pedido("EN_MESA", 5,
                    linea(pozoleRojo, 2, null), linea(tostada, 2, null), linea(jamaica, 2, null)), u);
            PEDIDOS.cobrarPedido(p6.getId(), new PagoDTO("TARJETA", null, null, "Aprobada · 0090"), u);
            PedidoDTO p7 = PEDIDOS.registrarPedido(pedido("PARA_LLEVAR", null,
                    linea(chilaquiles, 3, null), linea(jamaica, 1, null)), u);
            PEDIDOS.cobrarPedido(p7.getId(),
                    new PagoDTO("MIXTO", new BigDecimal("100"), new BigDecimal("50"), null), u);
            // Cuenta separada a medias: ya pagaron 2 pozoles, falta lo demás
            PedidoDTO p8 = PEDIDOS.registrarPedido(pedido("EN_MESA", 3,
                    linea(pozoleRojo, 4, null), linea(jamaica, 4, null)), u);
            PEDIDOS.cobrarPartidas(p8.getId(), List.of(new PartidaCobroDTO(p8.getDetalles().get(0).getId(), 2)),
                    new PagoDTO("EFECTIVO", null, new BigDecimal("200"), null), u);
            // Para llevar, sin pagar (se puede editar)
            PEDIDOS.registrarPedido(pedido("PARA_LLEVAR", null,
                    linea(pozoleRojo, 1, null), linea(chilaquiles, 2, "Salsa aparte")), u);
        } catch (NegocioException e) {
            throw new IllegalStateException("No se pudieron cargar los datos de prueba: " + e.getMessage(), e);
        }
    }

    private static void movimiento(String tipo, String motivo, float cantidad, InsumoDTO insumo, UsuarioDTO quien)
            throws NegocioException {
        INSUMOS.registrarMovimiento(new MovimientoInsumoDTO(tipo, motivo, cantidad, null, insumo.getId(), quien.getId()));
    }

    private static PlatilloDTO platillo(String nombre, String precio, String categoria, String... extra)
            throws NegocioException {
        PlatilloDTO p = new PlatilloDTO(nombre, null, new BigDecimal(precio), categoria);
        if (extra.length == 2) {
            p.getVariaciones().add(new VariacionDTO(null, extra[0], new BigDecimal(extra[1])));
        }
        return PLATILLOS.registrarPlatillo(p);
    }

    private static DetallePedidoDTO linea(PlatilloDTO p, int cantidad, String nota) {
        return new DetallePedidoDTO(null, p.getId(), p.getNombre(), cantidad, null, null, nota);
    }

    private static PedidoDTO pedido(String tipo, Integer mesa, DetallePedidoDTO... lineas) {
        return new PedidoDTO(null, tipo, mesa, null, List.of(lineas));
    }

}
