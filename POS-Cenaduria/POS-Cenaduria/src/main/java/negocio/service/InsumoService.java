package negocio.service;

import negocio.dtos.InsumoDTO;
import negocio.dtos.MovimientoInsumoDTO;
import negocio.excepciones.NegocioException;
import negocio.mappers.InsumoMapper;
import negocio.mappers.MovimientoInsumoMapper;
import negocio.reglas.EvaluadorStock;
import persistencia.entidades.Insumo;
import persistencia.entidades.MovimientoInsumo;
import persistencia.entidades.Usuario;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.MotivoMovimiento;
import persistencia.enums.TipoMovimiento;
import persistencia.enums.UnidadInsumo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IInsumoDAO;
import persistencia.interfaces.IMovimientoInsumosDAO;
import persistencia.interfaces.IUsuarioDAO;

import java.util.List;

public class InsumoService implements IInsumoService {

    private final IInsumoDAO insumoDAO;
    private final IMovimientoInsumosDAO movimientoDAO;
    private final IUsuarioDAO usuarioDAO;
    private final EvaluadorStock evaluador = new EvaluadorStock();

    public InsumoService(IInsumoDAO insumoDAO, IMovimientoInsumosDAO movimientoDAO,
                         IUsuarioDAO usuarioDAO) {
        this.insumoDAO = insumoDAO;
        this.movimientoDAO = movimientoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public InsumoDTO registrarInsumo(InsumoDTO dto) throws NegocioException {
        validar(dto);
        if (dto.getStock() < 0) {
            throw new NegocioException("El stock no puede ser negativo");
        }
        try {
            Insumo insumo = InsumoMapper.aEntidad(dto);
            insumoDAO.guardar(insumo);
            return aDTO(insumo);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al registrar el insumo", e);
        }
    }

    @Override
    public InsumoDTO actualizarInsumo(InsumoDTO dto) throws NegocioException {
        validar(dto);
        if (dto.getId() == null) {
            throw new NegocioException("Debe indicar el id del insumo a actualizar");
        }
        try {
            Insumo insumo = buscarInsumo(dto.getId());
            insumo.setNombre(dto.getNombre());
            insumo.setStockMinimo(dto.getStockMinimo());
            insumo.setUnidad(UnidadInsumo.valueOf(dto.getUnidad()));
            insumo.setCategoria(CategoriaInsumo.valueOf(dto.getCategoria()));
            insumoDAO.actualizar(insumo);
            return aDTO(insumo);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al actualizar el insumo", e);
        }
    }

    @Override
    public void desactivarInsumo(Long id) throws NegocioException {
        try {
            Insumo insumo = buscarInsumo(id);
            insumo.setActivo(false);
            insumoDAO.actualizar(insumo);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al desactivar el insumo", e);
        }
    }

    @Override
    public List<InsumoDTO> listarInsumos() throws NegocioException {
        try {
            return insumoDAO.listarTodos().stream()
                    .filter(Insumo::isActivo)
                    .map(this::aDTO)
                    .toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al listar los insumos", e);
        }
    }

    @Override
    public List<InsumoDTO> listarPorEstado(String estado) throws NegocioException {
        EstadoInsumo buscado;
        try {
            buscado = EstadoInsumo.valueOf(estado);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Estado no válido", e);
        }
        return listarInsumos().stream()
                .filter(i -> buscado.name().equals(i.getEstado()))
                .toList();
    }

    @Override
    public MovimientoInsumoDTO registrarMovimiento(MovimientoInsumoDTO dto) throws NegocioException {
        if (dto == null) {
            throw new NegocioException("El movimiento no puede ser nulo");
        }
        if (dto.getCantidad() <= 0) {
            throw new NegocioException("La cantidad debe ser mayor a cero");
        }
        TipoMovimiento tipo;
        MotivoMovimiento motivo;
        try {
            tipo = TipoMovimiento.valueOf(dto.getTipo());
            motivo = MotivoMovimiento.valueOf(dto.getMotivo());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Tipo o motivo de movimiento no válido", e);
        }
        validarTipoContraMotivo(tipo, motivo);

        try {
            Insumo insumo = buscarInsumo(dto.getIdInsumo());
            if (!insumo.isActivo()) {
                throw new NegocioException("No se pueden registrar movimientos de un insumo desactivado");
            }
            Usuario usuario = usuarioDAO.buscarPorId(dto.getIdUsuario())
                    .orElseThrow(() -> new NegocioException("Usuario no encontrado"));

            float nuevoStock = tipo == TipoMovimiento.ENTRADA
                    ? insumo.getStock() + dto.getCantidad()
                    : insumo.getStock() - dto.getCantidad();
            if (nuevoStock < 0) {
                throw new NegocioException("No hay suficiente " + insumo.getNombre()
                        + ": hay " + numero(insumo.getStock()) + " y se quieren sacar " + numero(dto.getCantidad()));
            }

            MovimientoInsumo movimiento = new MovimientoInsumo(
                    tipo, motivo, dto.getCantidad(), dto.getObservacion(), insumo, usuario);
            insumo.setStock(nuevoStock);

            movimientoDAO.guardar(movimiento);
            insumoDAO.actualizar(insumo);
            return MovimientoInsumoMapper.aDTO(movimiento);

        } catch (PersistenciaException e) {
            throw new NegocioException("Error al registrar el movimiento", e);
        }
    }

    @Override
    public InsumoDTO ajustarStock(Long idInsumo, float nuevoStock, String motivo,
                                  String observacion, Long idUsuario) throws NegocioException {
        if (nuevoStock < 0) {
            throw new NegocioException("La cantidad no puede ser negativa");
        }
        try {
            Insumo insumo = buscarInsumo(idInsumo);
            float diferencia = nuevoStock - insumo.getStock();
            if (diferencia != 0) {
                String tipo = diferencia > 0 ? TipoMovimiento.ENTRADA.name() : TipoMovimiento.SALIDA.name();
                registrarMovimiento(new MovimientoInsumoDTO(
                        tipo, motivo, Math.abs(diferencia), observacion, idInsumo, idUsuario));
            }
            return aDTO(insumo);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al ajustar el stock", e);
        }
    }

    @Override
    public List<MovimientoInsumoDTO> listarMovimientos(Long idInsumo) throws NegocioException {
        try {
            return movimientoDAO.listarPorInsumo(idInsumo).stream()
                    .map(MovimientoInsumoMapper::aDTO)
                    .toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al listar los movimientos", e);
        }
    }

    // ---------------- Reglas de negocio ----------------

    /** COMPRA = entrada; CONSUMO_JORNADA y MERMA = salida; AJUSTE = cualquiera. */
    private void validarTipoContraMotivo(TipoMovimiento tipo, MotivoMovimiento motivo)
            throws NegocioException {
        boolean valido = switch (motivo) {
            case COMPRA -> tipo == TipoMovimiento.ENTRADA;
            case CONSUMO_JORNADA, MERMA -> tipo == TipoMovimiento.SALIDA;
            case AJUSTE -> true;
        };
        if (!valido) {
            throw new NegocioException(tipo == TipoMovimiento.ENTRADA
                    ? "La cantidad subió: el motivo debe ser COMPRA o AJUSTE, no " + motivo
                    : "La cantidad bajó: el motivo debe ser CONSUMO_JORNADA, MERMA o AJUSTE, no " + motivo);
        }
    }

    // ---------------- Utilidades ----------------

    private Insumo buscarInsumo(Long id) throws NegocioException, PersistenciaException {
        if (id == null) {
            throw new NegocioException("Debe indicar el insumo");
        }
        return insumoDAO.buscarPorId(id)
                .orElseThrow(() -> new NegocioException("Insumo no encontrado"));
    }

    private InsumoDTO aDTO(Insumo insumo) {
        return InsumoMapper.aDTO(insumo, evaluador.evaluar(insumo));
    }

    // ---------------- Validaciones ----------------

    private void validar(InsumoDTO dto) throws NegocioException {
        if (dto == null) {
            throw new NegocioException("El insumo no puede ser nulo");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new NegocioException("El nombre del insumo es obligatorio");
        }
        if (dto.getStockMinimo() < 0) {
            throw new NegocioException("El stock mínimo no puede ser negativo");
        }
        try {
            UnidadInsumo.valueOf(dto.getUnidad());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Unidad no válida", e);
        }
        try {
            CategoriaInsumo.valueOf(dto.getCategoria());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Categoría no válida", e);
        }
    }

    private static String numero(float n) {
        return n == (long) n ? String.valueOf((long) n) : String.valueOf(n);
    }
}
