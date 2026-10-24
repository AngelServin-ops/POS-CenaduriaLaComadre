package persistencia.daoMemoria;

import persistencia.entidades.Insumo;
import persistencia.enums.CategoriaInsumo;
import persistencia.enums.EstadoInsumo;
import persistencia.excepciones.PersistenciaException;
import persistencia.interfaces.IInsumoDAO;

import java.util.*;

public class InsumoDAOMemoria implements IInsumoDAO {

    private final Map<Long, Insumo> datos = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public void guardar(Insumo insumo) throws PersistenciaException {
        insumo.setId(siguienteId++);
        datos.put(insumo.getId(), insumo);
    }

    @Override
    public void actualizar(Insumo insumo) throws PersistenciaException {
        if (insumo.getId() == null || !datos.containsKey(insumo.getId())) {
            throw new PersistenciaException("El insumo no existe");
        }
        datos.put(insumo.getId(), insumo);
    }

    @Override
    public Optional<Insumo> buscarPorId(Long id) throws PersistenciaException {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Insumo> listarTodos() throws PersistenciaException {
        return new ArrayList<>(datos.values());
    }

    @Override
    public List<Insumo> buscarPorCategoria(CategoriaInsumo categoria) throws PersistenciaException {
        return datos.values().stream()
                .filter(i -> i.getCategoria() == categoria)
                .toList();
    }

    @Override
    public List<Insumo> buscarPorEstado(EstadoInsumo estado) throws PersistenciaException {
        return datos.values().stream()
                .filter(i -> i.getEstadoInsumo() == estado)
                .toList();
    }

    @Override
    public List<Insumo> buscarPorNombre(String nombre) throws PersistenciaException {
        String buscado = nombre.toLowerCase();
        return datos.values().stream()
                .filter(i -> i.getNombreInsumo().toLowerCase().contains(buscado))
                .toList();
    }
}
