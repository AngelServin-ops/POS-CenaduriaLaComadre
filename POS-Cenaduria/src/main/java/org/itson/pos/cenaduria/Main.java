package org.itson.pos.cenaduria;

import jakarta.persistence.EntityManager;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import persistencia.conexion.ConexionDB;
import persistencia.entidades.Administrador;
import presentacion.GestionarInventarioView;

public class Main {

    public static void main(String[] args) {
        // 1. Crear/conectar la base de datos ANTES de mostrar la pantalla
        if (!inicializarBaseDeDatos()) {
            return;
        }

        // 2. Abrir la pantalla
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame ventana = new JFrame("Cenaduría La Comadre");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JScrollPane scroll = new JScrollPane(new GestionarInventarioView());
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            scroll.getHorizontalScrollBar().setUnitIncrement(20);

            ventana.setContentPane(scroll);
            ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
            ventana.setVisible(true);
        });
    }

    /**
     * Al pedir el EntityManager, Hibernate crea la base de datos y las tablas
     * si no existen. Además se crea un administrador inicial si no hay ninguno.
     */
    private static boolean inicializarBaseDeDatos() {
        EntityManager em = null;
        try {
            em = ConexionDB.getEntityManager();

            Long total = em.createQuery("SELECT COUNT(a) FROM Administrador a", Long.class)
                    .getSingleResult();

            if (total == 0) {
                em.getTransaction().begin();
                em.persist(new Administrador("Administrador"));
                em.getTransaction().commit();
                System.out.println("Administrador inicial creado");
            }

            System.out.println("Base de datos conectada correctamente");
            return true;

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos.\n\n"
                    + "Revisa que MySQL esté encendido y que el usuario y la contraseña\n"
                    + "del archivo persistence.xml sean correctos.\n\n"
                    + "Detalle: " + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}