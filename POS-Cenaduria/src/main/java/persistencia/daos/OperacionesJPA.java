package persistencia.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import persistencia.conexion.ConexionDB;
import persistencia.excepciones.PersistenciaException;

/**
 * Abre un EntityManager por operación, maneja la transacción y convierte
 * cualquier error de JPA/MySQL en PersistenciaException.
 *
 * Importante: las entidades salen "desconectadas" (el EntityManager se cierra).
 * Por eso cada DAO carga sus listas perezosas (LAZY) antes de devolverlas.
 */
final class OperacionesJPA {

    interface Consulta<T> {
        T ejecutar(EntityManager em) throws PersistenciaException;
    }

    interface Cambio {
        void ejecutar(EntityManager em) throws PersistenciaException;
    }

    private OperacionesJPA() {
    }

    /** Lectura sin transacción. */
    static <T> T consultar(Consulta<T> consulta) throws PersistenciaException {
        EntityManager em = null;
        try {
            em = ConexionDB.getEntityManager();
            return consulta.ejecutar(em);
        } catch (PersistenciaException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PersistenciaException("Error al consultar la base de datos: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /** Escritura dentro de una transacción: si algo falla se hace rollback completo. */
    static void enTransaccion(Cambio cambio) throws PersistenciaException {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = ConexionDB.getEntityManager();
            tx = em.getTransaction();
            tx.begin();
            cambio.ejecutar(em);
            tx.commit();
        } catch (PersistenciaException | RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            if (e instanceof PersistenciaException pe) {
                throw pe;
            }
            throw new PersistenciaException("Error al guardar en la base de datos: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
