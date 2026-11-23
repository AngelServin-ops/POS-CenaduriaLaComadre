package negocio.service;

import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.EventoPedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PagoDTO;
import negocio.dtos.PartidaCobroDTO;
import negocio.dtos.PedidoDTO;
import negocio.dtos.VariacionDTO;
import negocio.excepciones.NegocioException;
import negocio.mappers.PedidoMapper;
import persistencia.entidades.DetallePedido;
import persistencia.entidades.Pago;
import persistencia.entidades.PartidaPago;
import persistencia.entidades.Pedido;
import persistencia.entidades.Platillo;
import persistencia.entidades.Usuario;
import persistencia.entidades.VariacionPlatillo;
import persistencia.enums.EstadoPedido;
import persistencia.enums.MetodoPago;
import persistencia.enums.TipoPedido;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IPedidoDAO;
import persistencia.interfaces.IPlatilloDAO;
import persistencia.interfaces.IUsuarioDAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PedidoService implements IPedidoService {

    private static final int CANTIDAD_MAXIMA = 99;
    private static final int MESA_MAXIMA = 99;
    private static final int LONGITUD_NOTA = 255;

    private final IPedidoDAO pedidoDAO;
    private final IPlatilloDAO platilloDAO;
    private final IUsuarioDAO usuarioDAO;
    private final List<Consumer<EventoPedidoDTO>> escuchas = new CopyOnWriteArrayList<>();

    public PedidoService(IPedidoDAO pedidoDAO, IPlatilloDAO platilloDAO, IUsuarioDAO usuarioDAO) {
        this.pedidoDAO = pedidoDAO;
        this.platilloDAO = platilloDAO;
        this.usuarioDAO = usuarioDAO;
    }

    // =====================================================================
    // CU Gestionar pedido
    // =====================================================================

    @Override
    public int siguienteFolio() throws NegocioException {
        try {
            return pedidoDAO.obtenerUltimoFolio() + 1;
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al obtener el folio", e);
        }
    }

    @Override
    public synchronized PedidoDTO registrarPedido(PedidoDTO dto, Long idUsuario) throws NegocioException {
        validarEncabezado(dto);
        try {
            Usuario usuario = buscarUsuarioActivo(idUsuario);
            Map<Long, Platillo> platillos = cargarPlatillos();

            Pedido pedido = new Pedido(siguienteFolio(), TipoPedido.valueOf(dto.getTipo()),
                    mesaSegunTipo(dto), null, limpiar(dto.getNotas()), usuario);
            for (DetallePedidoDTO linea : dto.getDetalles()) {
                pedido.agregarDetalle(crearDetalle(linea, platillos));
            }

            pedidoDAO.guardar(pedido);
            avisar(pedido, "se registró");
            return PedidoMapper.aDTO(pedido);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al guardar el pedido", e);
        }
    }

    @Override
    public synchronized PedidoDTO actualizarPedido(PedidoDTO dto) throws NegocioException {
        validarEncabezado(dto);
        try {
            Pedido pedido = buscarPedido(dto.getId());
            if (!pedido.puedeModificarse()) {
                throw new NegocioException("Solo los pedidos activos y sin pagos pueden editarse");
            }
            Map<Long, Platillo> platillos = cargarPlatillos();

            // 1) Se arma la nueva lista completa antes de tocar el pedido
            List<DetallePedido> nuevos = new ArrayList<>();
            Map<Long, DetallePedido> existentes = pedido.getDetalles().stream()
                    .collect(Collectors.toMap(DetallePedido::getId, Function.identity()));
            for (DetallePedidoDTO linea : dto.getDetalles()) {
                if (linea.getId() != null && existentes.containsKey(linea.getId())) {
                    validarCantidadYNota(linea);
                    nuevos.add(existentes.get(linea.getId())); // conserva su precio original
                } else {
                    nuevos.add(crearDetalle(linea, platillos));
                }
            }

            // 2) Se aplican los cambios
            pedido.setTipo(TipoPedido.valueOf(dto.getTipo()));
            pedido.setMesa(mesaSegunTipo(dto));
            pedido.setNotas(limpiar(dto.getNotas()));
            for (DetallePedido viejo : new ArrayList<>(pedido.getDetalles())) {
                if (!nuevos.contains(viejo)) {
                    pedido.quitarDetalle(viejo);
                }
            }
            for (DetallePedidoDTO linea : dto.getDetalles()) {
                if (linea.getId() != null && existentes.containsKey(linea.getId())) {
                    DetallePedido d = existentes.get(linea.getId());
                    d.setCantidad(linea.getCantidad());
                    d.setNota(limpiar(linea.getNota()));
                }
            }
            for (DetallePedido d : nuevos) {
                if (d.getPedido() == null) {
                    pedido.agregarDetalle(d);
                }
            }

            pedidoDAO.actualizar(pedido);
            avisar(pedido, "se actualizó · Total " + Formatos.dinero(pedido.calcularTotal()));
            return PedidoMapper.aDTO(buscarPedido(pedido.getId())); // versión guardada, con ids nuevos
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al actualizar el pedido", e);
        }
    }

    @Override
    public synchronized PedidoDTO cancelarPedido(Long idPedido, String motivo) throws NegocioException {
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException("Escribe el motivo de la cancelación");
        }
        try {
            Pedido pedido = buscarPedido(idPedido);
            if (!pedido.puedeModificarse()) {
                throw new NegocioException("Solo los pedidos activos y sin pagos pueden cancelarse");
            }
            pedido.cancelar(motivo);
            pedidoDAO.actualizar(pedido);
            avisar(pedido, "se canceló");
            return PedidoMapper.aDTO(buscarPedido(pedido.getId()));
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al cancelar el pedido", e);
        }
    }

    // =====================================================================
    // CU Consultar pedido
    // =====================================================================

    @Override
    public PedidoDTO buscarPorId(Long idPedido) throws NegocioException {
        try {
            return PedidoMapper.aDTO(buscarPedido(idPedido));
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo consultar el pedido", e);
        }
    }

    @Override
    public List<PedidoDTO> consultar(String busqueda, LocalDate fecha, String estado) throws NegocioException {
        EstadoPedido filtro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoPedido.valueOf(estado);
            } catch (IllegalArgumentException e) {
                throw new NegocioException("Estado no válido", e);
            }
        }
        try {
            final EstadoPedido f = filtro;
            return pedidoDAO.listarPorFecha(fecha != null ? fecha : LocalDate.now()).stream()
                    .filter(p -> f == null || p.getEstado() == f)
                    .filter(p -> coincide(p, busqueda))
                    .map(PedidoMapper::aDTO)
                    .toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo cargar el listado de pedidos", e);
        }
    }

    /** "P-0142", "0142", "142" buscan por folio; "04", "Mesa 4" por mesa. */
    private boolean coincide(Pedido p, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }
        String b = busqueda.trim().toLowerCase();
        if (Formatos.folioPedido(p.getFolio()).toLowerCase().contains(b)) {
            return true;
        }
        String soloNumero = b.replace("p-", "").replace("mesa", "").trim();
        if (soloNumero.matches("\\d+")) {
            int n = Integer.parseInt(soloNumero);
            boolean pideMesa = b.contains("mesa");
            if (!pideMesa && p.getFolio() == n) {
                return true;
            }
            return p.getMesa() != null && p.getMesa() == n && (pideMesa || soloNumero.length() <= 2);
        }
        return false;
    }

    // =====================================================================
    // CU Cobrar pedido
    // =====================================================================

    @Override
    public PagoDTO previsualizarCobro(BigDecimal total, PagoDTO pago) throws NegocioException {
        if (total == null || total.signum() <= 0) {
            throw new NegocioException("El pedido no tiene importe por cobrar");
        }
        if (pago == null || pago.getMetodo() == null) {
            throw new NegocioException("Selecciona el método de pago");
        }
        MetodoPago metodo;
        try {
            metodo = MetodoPago.valueOf(pago.getMetodo());
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Método de pago no válido", e);
        }
        BigDecimal tarjeta = noNulo(pago.getMontoTarjeta());
        BigDecimal efectivo = noNulo(pago.getEfectivoRecibido());
        if (tarjeta.signum() < 0 || efectivo.signum() < 0) {
            throw new NegocioException("Los importes no pueden ser negativos");
        }

        switch (metodo) {
            case EFECTIVO -> tarjeta = BigDecimal.ZERO;
            case TARJETA -> {
                tarjeta = total; // con tarjeta se cobra exacto, no hay cambio
                efectivo = BigDecimal.ZERO;
                if (pago.getReferenciaTerminal() == null || pago.getReferenciaTerminal().isBlank()) {
                    throw new NegocioException("Registra la referencia de la terminal. "
                            + "Captura el número de aprobación antes de confirmar.");
                }
            }
            case MIXTO -> {
                if (tarjeta.signum() <= 0 || tarjeta.compareTo(total) >= 0) {
                    throw new NegocioException("El monto en tarjeta no es válido. En pago mixto debe ser mayor a "
                            + "$0.00 y menor al total de " + Formatos.dinero(total) + ".");
                }
            }
        }

        BigDecimal porCubrir = total.subtract(tarjeta);
        if (efectivo.compareTo(porCubrir) < 0) {
            BigDecimal faltan = porCubrir.subtract(efectivo);
            throw new NegocioException("El monto recibido es insuficiente. Faltan " + Formatos.dinero(faltan)
                    + ". Ingresa al menos " + Formatos.dinero(porCubrir) + " en efectivo. No se registró ningún cobro.");
        }

        PagoDTO resultado = new PagoDTO(metodo.name(), tarjeta, efectivo, limpiar(pago.getReferenciaTerminal()));
        resultado.setMonto(total);
        resultado.setEfectivoAplicado(porCubrir);
        resultado.setCambio(efectivo.subtract(porCubrir));
        return resultado;
    }

    @Override
    public PedidoDTO cobrarPedido(Long idPedido, PagoDTO datos, Long idUsuario) throws NegocioException {
        try {
            Pedido pedido = buscarPedido(idPedido);
            List<PartidaCobroDTO> todo = new ArrayList<>();
            for (DetallePedido d : pedido.getDetalles()) {
                int pendiente = d.getCantidad() - pedido.cantidadPagada(d);
                if (pendiente > 0) {
                    todo.add(new PartidaCobroDTO(d.getId(), pendiente));
                }
            }
            return cobrarPartidas(idPedido, todo, datos, idUsuario);
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo registrar el pago", e);
        }
    }

    @Override
    public synchronized PedidoDTO cobrarPartidas(Long idPedido, List<PartidaCobroDTO> partidas, PagoDTO datos,
                                                 Long idUsuario) throws NegocioException {
        Pedido pedido;
        Usuario cajero;
        try {
            pedido = buscarPedido(idPedido);
            cajero = buscarUsuarioActivo(idUsuario);
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo registrar el pago", e);
        }

        // Se revalida en el momento de confirmar: evita cobrar dos veces lo mismo
        if (pedido.getEstado() != EstadoPedido.ACTIVO) {
            throw new NegocioException("Solo se pueden cobrar pedidos activos. "
                    + Formatos.folioPedido(pedido.getFolio()) + " está " + pedido.getEstado());
        }
        if (partidas == null || partidas.isEmpty()) {
            throw new NegocioException("Selecciona al menos un platillo para cobrar");
        }
        List<PartidaPago> nuevas = new ArrayList<>();
        java.util.Set<Long> vistos = new java.util.HashSet<>();
        for (PartidaCobroDTO pc : partidas) {
            DetallePedido d = pedido.getDetalles().stream()
                    .filter(x -> x.getId().equals(pc.idDetalle()))
                    .findFirst()
                    .orElseThrow(() -> new NegocioException("El platillo seleccionado no es de este pedido"));
            if (!vistos.add(d.getId())) {
                throw new NegocioException("Hay un platillo repetido en la selección");
            }
            int pendiente = d.getCantidad() - pedido.cantidadPagada(d);
            if (pc.cantidad() < 1 || pc.cantidad() > pendiente) {
                throw new NegocioException("De " + d.getPlatillo().getNombre() + " quedan " + pendiente
                        + " por cobrar; no se pueden cobrar " + pc.cantidad());
            }
            nuevas.add(new PartidaPago(d, pc.cantidad()));
        }
        BigDecimal monto = nuevas.stream().map(PartidaPago::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
        PagoDTO calculado = previsualizarCobro(monto, datos);

        Pago pago = new Pago(pedido.getPagos().size() + 1, MetodoPago.valueOf(calculado.getMetodo()), nuevas,
                calculado.getMontoTarjeta(), calculado.getEfectivoRecibido(), calculado.getReferenciaTerminal(), cajero);
        pedido.agregarPago(pago);
        boolean liquidado = pedido.calcularSaldo().signum() <= 0;
        if (liquidado) {
            pedido.setEstado(EstadoPedido.PAGADO);
        }
        try {
            pedidoDAO.actualizar(pedido);
        } catch (PersistenciaException e) {
            // Rollback: el pedido queda exactamente como estaba
            pedido.quitarPago(pago);
            pedido.setEstado(EstadoPedido.ACTIVO);
            throw new NegocioException("No se pudo registrar el pago. El pedido sigue activo.", e);
        }
        avisar(pedido, liquidado ? "se pagó · " + Formatos.dinero(pago.getMonto())
                : "recibió un pago parcial · saldo " + Formatos.dinero(pedido.calcularSaldo()));
        try {
            return PedidoMapper.aDTO(buscarPedido(pedido.getId())); // versión guardada, con ids del pago
        } catch (PersistenciaException e) {
            return PedidoMapper.aDTO(pedido); // el pago ya quedó guardado; solo falló la relectura
        }
    }

    // =====================================================================
    // Avisos
    // =====================================================================

    @Override
    public void agregarEscucha(Consumer<EventoPedidoDTO> escucha) {
        escuchas.add(escucha);
    }

    @Override
    public void quitarEscucha(Consumer<EventoPedidoDTO> escucha) {
        escuchas.remove(escucha);
    }

    private void avisar(Pedido pedido, String descripcion) {
        EventoPedidoDTO evento = new EventoPedidoDTO(LocalDateTime.now(), pedido.getFolio(), descripcion);
        for (Consumer<EventoPedidoDTO> e : escuchas) {
            try {
                e.accept(evento);
            } catch (RuntimeException ex) {
                ex.printStackTrace(); // una pantalla con error no debe romper el registro del pedido
            }
        }
    }

    // =====================================================================
    // Validaciones y utilidades
    // =====================================================================

    private void validarEncabezado(PedidoDTO dto) throws NegocioException {
        if (dto == null) {
            throw new NegocioException("El pedido no puede ser nulo");
        }
        TipoPedido tipo;
        try {
            tipo = TipoPedido.valueOf(dto.getTipo());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Tipo de servicio no válido", e);
        }
        if (tipo == TipoPedido.EN_MESA
                && (dto.getMesa() == null || dto.getMesa() < 1 || dto.getMesa() > MESA_MAXIMA)) {
            throw new NegocioException("Selecciona la mesa");
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new NegocioException("Agrega al menos un producto");
        }
    }

    private void validarCantidadYNota(DetallePedidoDTO linea) throws NegocioException {
        if (linea.getCantidad() < 1 || linea.getCantidad() > CANTIDAD_MAXIMA) {
            throw new NegocioException("La cantidad de " + linea.getNombrePlatillo()
                    + " debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
        if (linea.getNota() != null && linea.getNota().length() > LONGITUD_NOTA) {
            throw new NegocioException("La nota no puede pasar de " + LONGITUD_NOTA + " caracteres");
        }
    }

    private DetallePedido crearDetalle(DetallePedidoDTO linea, Map<Long, Platillo> platillos)
            throws NegocioException {
        validarCantidadYNota(linea);
        Platillo platillo = platillos.get(linea.getIdPlatillo());
        if (platillo == null) {
            throw new NegocioException("El platillo seleccionado no existe");
        }
        if (!platillo.isDisponible()) {
            throw new NegocioException(platillo.getNombre() + " no está disponible");
        }
        List<VariacionPlatillo> extras = new ArrayList<>();
        for (VariacionDTO v : linea.getVariaciones()) {
            VariacionPlatillo encontrada = platillo.getVariaciones().stream()
                    .filter(x -> x.isActiva() && x.getId().equals(v.id()))
                    .findFirst()
                    .orElseThrow(() -> new NegocioException(
                            "El extra \"" + v.nombre() + "\" no corresponde a " + platillo.getNombre()));
            extras.add(encontrada);
        }
        return new DetallePedido(platillo, linea.getCantidad(), extras, limpiar(linea.getNota()));
    }

    private Map<Long, Platillo> cargarPlatillos() throws PersistenciaException {
        return platilloDAO.listarTodos().stream()
                .collect(Collectors.toMap(Platillo::getId, Function.identity()));
    }

    private Pedido buscarPedido(Long id) throws NegocioException, PersistenciaException {
        if (id == null) {
            throw new NegocioException("Debe indicar el pedido");
        }
        return pedidoDAO.buscarPorId(id).orElseThrow(() -> new NegocioException("Pedido no encontrado"));
    }

    private Usuario buscarUsuarioActivo(Long id) throws NegocioException, PersistenciaException {
        if (id == null) {
            throw new NegocioException("No hay un usuario con sesión iniciada");
        }
        Usuario u = usuarioDAO.buscarPorId(id).orElseThrow(() -> new NegocioException("Usuario no encontrado"));
        if (!u.isActivo()) {
            throw new NegocioException("El usuario está desactivado");
        }
        return u;
    }

    private static Integer mesaSegunTipo(PedidoDTO dto) {
        return "EN_MESA".equals(dto.getTipo()) ? dto.getMesa() : null;
    }

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static BigDecimal noNulo(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }

}
