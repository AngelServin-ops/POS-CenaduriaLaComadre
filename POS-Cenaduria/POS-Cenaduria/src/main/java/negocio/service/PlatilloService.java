package negocio.service;

import negocio.dtos.PlatilloDTO;
import negocio.dtos.VariacionDTO;
import negocio.excepciones.NegocioException;
import negocio.mappers.PlatilloMapper;
import persistencia.entidades.Platillo;
import persistencia.entidades.VariacionPlatillo;
import persistencia.enums.CategoriaPlatillo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IPlatilloDAO;

import java.math.BigDecimal;
import java.util.List;

public class PlatilloService implements IPlatilloService {

    private final IPlatilloDAO platilloDAO;

    public PlatilloService(IPlatilloDAO platilloDAO) {
        this.platilloDAO = platilloDAO;
    }

    @Override
    public PlatilloDTO registrarPlatillo(PlatilloDTO dto) throws NegocioException {
        CategoriaPlatillo categoria = validar(dto);
        Platillo platillo = new Platillo(dto.getNombre().trim(), limpiar(dto.getDescripcion()), dto.getPrecio(), categoria);
        for (VariacionDTO v : dto.getVariaciones()) {
            platillo.agregarVariacion(new VariacionPlatillo(v.nombre().trim(), precioExtra(v)));
        }
        try {
            platilloDAO.guardar(platillo);
            return PlatilloMapper.aDTO(platillo);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al registrar el platillo", e);
        }
    }

    @Override
    public PlatilloDTO actualizarPlatillo(PlatilloDTO dto) throws NegocioException {
        CategoriaPlatillo categoria = validar(dto);
        try {
            Platillo platillo = platilloDAO.buscarPorId(dto.getId())
                    .orElseThrow(() -> new NegocioException("Platillo no encontrado"));
            platillo.setNombre(dto.getNombre().trim());
            platillo.setDescripcion(limpiar(dto.getDescripcion()));
            platillo.setPrecio(dto.getPrecio());
            platillo.setCategoria(categoria);
            platillo.setDisponible(dto.isDisponible());

            List<Long> conservar = dto.getVariaciones().stream()
                    .map(VariacionDTO::id).filter(java.util.Objects::nonNull).toList();
            for (VariacionPlatillo v : platillo.getVariaciones()) {
                if (v.isActiva() && !conservar.contains(v.getId())) {
                    v.setActiva(false);
                }
            }
            for (VariacionDTO v : dto.getVariaciones()) {
                if (v.id() == null) {
                    platillo.agregarVariacion(new VariacionPlatillo(v.nombre().trim(), precioExtra(v)));
                } else {
                    for (VariacionPlatillo existente : platillo.getVariaciones()) {
                        if (existente.getId().equals(v.id())) {
                            existente.setNombre(v.nombre().trim());
                            existente.setPrecioExtra(precioExtra(v));
                        }
                    }
                }
            }
            platilloDAO.actualizar(platillo);
            return PlatilloMapper.aDTO(platilloDAO.buscarPorId(platillo.getId()).orElse(platillo));
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al actualizar el platillo", e);
        }
    }

    @Override
    public List<PlatilloDTO> listarTodos() throws NegocioException {
        try {
            return platilloDAO.listarTodos().stream().map(PlatilloMapper::aDTO).toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al listar los platillos", e);
        }
    }

    @Override
    public List<PlatilloDTO> listarDisponibles() throws NegocioException {
        try {
            return platilloDAO.listarTodos().stream()
                    .filter(Platillo::isDisponible)
                    .map(PlatilloMapper::aDTO)
                    .toList();
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al listar los platillos", e);
        }
    }

    @Override
    public void cambiarDisponibilidad(Long idPlatillo, boolean disponible) throws NegocioException {
        try {
            Platillo platillo = platilloDAO.buscarPorId(idPlatillo)
                    .orElseThrow(() -> new NegocioException("Platillo no encontrado"));
            platillo.setDisponible(disponible);
            platilloDAO.actualizar(platillo);
        } catch (PersistenciaException e) {
            throw new NegocioException("Error al actualizar el platillo", e);
        }
    }

    // ---------------- Validaciones ----------------

    private CategoriaPlatillo validar(PlatilloDTO dto) throws NegocioException {
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new NegocioException("El nombre del platillo es obligatorio");
        }
        if (dto.getPrecio() == null || dto.getPrecio().signum() <= 0) {
            throw new NegocioException("El precio debe ser mayor a cero");
        }
        for (VariacionDTO v : dto.getVariaciones()) {
            if (v.nombre() == null || v.nombre().isBlank()) {
                throw new NegocioException("Cada extra necesita un nombre");
            }
            if (precioExtra(v).signum() < 0) {
                throw new NegocioException("El precio extra no puede ser negativo");
            }
        }
        try {
            return CategoriaPlatillo.valueOf(dto.getCategoria());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new NegocioException("Categoría no válida", e);
        }
    }

    private static BigDecimal precioExtra(VariacionDTO v) {
        return v.precioExtra() != null ? v.precioExtra() : BigDecimal.ZERO;
    }

    private static String limpiar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
