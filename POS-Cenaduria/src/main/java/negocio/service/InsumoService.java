package negocio.service;

import negocio.dtos.InsumoDTO;
import negocio.excepciones.NegocioException;
import negocio.mappers.InsumoMapper;
import persistencia.entidades.Administrador;
import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.enums.UnidadInsumo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IAdministradorDAO;
import persistencia.interfaces.IInsumoDAO;

import java.util.List;
import java.util.Map;

public class InsumoService implements IInsumoService{

    // Regla de negocio: stock mínimo por categoría (ajusta los valores a tu cenaduría)
    private static final Map<CategoriaInsumo, Float> STOCK_MINIMO = Map.of(
            CategoriaInsumo.CARNES, 5f,
            CategoriaInsumo.VERDURAS, 3f
    );

    private final IInsumoDAO insumoDAO;
    private final IAdministradorDAO administradorDAO;

    public InsumoService(IInsumoDAO insumoDAO, IAdministradorDAO administradorDAO) {
        this.insumoDAO = insumoDAO;
        this.administradorDAO = administradorDAO;
    }

    @Override
    public InsumoDTO registrarInsumo(InsumoDTO dto) throws NegocioException {
        validar(dto);
        if (dto.getAdministrador() == null || dto.getAdministrador().getId() == null) {
            throw new NegocioException("El insumo debe tener un administrador");
        }

        try {
            Administrador admin = administradorDAO.buscarPorId(dto.getAdministrador().getId())
                    .orElseThrow(() -> new NegocioException("Administrador no encontrado"));

            Insumo insumo = InsumoMapper.aEntidad(dto, admin);
            insumo.setEstadoInsumo(evaluarEstado(insumo));
            insumoDAO.guardar(insumo);
            return InsumoMapper.aDTO(insumo);

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
            Insumo insumo = insumoDAO.buscarPorId(dto.getId())
                    .orElseThrow(() -> new NegocioException("Insumo no encontrado"));

            insumo.setNombreInsumo(dto.getNombre());
            insumo.setStock(dto.getStock());
            insumo.setUnidadInsumo(UnidadInsumo.valueOf(dto.getUnidad()));
            insumo.setCategoria(CategoriaInsumo.valueOf(dto.getCategoria()));
            insumo.setEstadoInsumo(evaluarEstado(insumo)); // se recalcula siempre

            insumoDAO.actualizar(insumo);
            return InsumoMapper.aDTO(insumo);

        } catch (PersistenciaException e) {
            throw new NegocioException("Error al actualizar el insumo", e);
        }
    }

    @Override
    public List<InsumoDTO> listarInsumos() throws NegocioException {
        try {
            return insumoDAO.listarTodos().stream()
                    .map(InsumoMapper::aDTO)
                    .toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al listar los insumos", e);
        }
    }

    // ---------------- Regla de negocio ----------------

    private EstadoInsumo evaluarEstado(Insumo insumo) {
        float stock = insumo.getStock();
        if (stock <= 0) {
            return EstadoInsumo.AGOTADO;
        }
        if (stock <= STOCK_MINIMO.get(insumo.getCategoria())) {
            return EstadoInsumo.BAJO;
        }
        return EstadoInsumo.DISPONIBLE;
    }

    // ---------------- Validaciones ----------------

    private void validar(InsumoDTO dto) throws NegocioException {
        if (dto == null) {
            throw new NegocioException("El insumo no puede ser nulo");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new NegocioException("El nombre del insumo es obligatorio");
        }
        if (dto.getStock() < 0) {
            throw new NegocioException("El stock no puede ser negativo");
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
}
