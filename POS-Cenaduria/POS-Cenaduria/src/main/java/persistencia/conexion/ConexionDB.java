package persistencia.conexion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ConexionDB {

    private static final String UNIDAD_PERSISTENCIA = "cenaduriaPU";
    private static EntityManagerFactory emf;

    private ConexionDB() {
    }

    public static synchronized EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA);
        }
        return emf.createEntityManager();
    }

    public static synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}