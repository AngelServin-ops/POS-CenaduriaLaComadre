package org.itson.pos.cenaduria;

import jakarta.persistence.EntityManager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import persistencia.conexion.ConexionDB;
import presentacion.ContextoApp;
import presentacion.VentanaPrincipal;

public class Main {

    public static void main(String[] args) {
        // 1. Conectar a MySQL y crear la base de datos/tablas ANTES de mostrar la pantalla
        if (!ContextoApp.EN_MEMORIA && !inicializarBaseDeDatos()) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(ConexionDB::cerrar));

        // 2. Abrir la pantalla
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new VentanaPrincipal().setVisible(true);
        });
    }

    /**
     * Al pedir el primer EntityManager, Hibernate crea la base de datos "cenaduria_la_comadre"
     * (createDatabaseIfNotExist en la URL) y las tablas que falten (hbm2ddl.auto = update).
     * Las tablas quedan vacías; ContextoApp solo agrega el usuario "Caja" y un administrador
     * si no existen. Los datos de ejemplo se cargan con el script datos_ejemplo.sql.
     */
    private static boolean inicializarBaseDeDatos() {
        EntityManager em = null;
        try {
            em = ConexionDB.getEntityManager();
            em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
            ContextoApp.getUsuarioCaja(); // carga ContextoApp: crea Caja y administrador si faltan
            System.out.println("Base de datos conectada correctamente");
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            Throwable causa = e instanceof ExceptionInInitializerError && e.getCause() != null ? e.getCause() : e;
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos.\n\n"
                    + "Revisa que MySQL esté encendido y que el usuario y la contraseña\n"
                    + "del archivo persistence.xml sean correctos.\n\n"
                    + "Detalle: " + causa.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
