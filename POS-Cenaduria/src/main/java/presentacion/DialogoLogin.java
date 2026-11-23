package presentacion;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import negocio.excepciones.NegocioException;

/** Pop up para entrar al modo administrador: solo pide la contraseña. */
public final class DialogoLogin {

    private DialogoLogin() {
    }

    /** @return true si la contraseña fue correcta y ya hay sesión de administrador */
    public static boolean mostrar(Component padre, String motivo) {
        Modal m = new Modal(padre, "Iniciar sesión",
                motivo != null ? motivo : "Escribe la contraseña de administrador.", 440);
        JPasswordField txt = new JPasswordField();
        Estilos.estilizar(txt);
        JPanel campo = Estilos.campo("Contraseña", txt);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel error = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.ROJO);
        m.agregar(campo);
        m.agregar(error);

        Runnable entrar = () -> {
            try {
                ContextoApp.iniciarSesion(new String(txt.getPassword()));
                m.cerrar(true);
            } catch (NegocioException e) {
                error.setText(e.getMessage());
                Estilos.marcarError(txt, true);
                txt.selectAll();
                txt.requestFocusInWindow();
            }
        };
        txt.addActionListener(e -> entrar.run()); // Enter = entrar
        m.boton("Cancelar", null, false);
        m.botonAccion("Entrar", Estilos.NARANJA, entrar);
        SwingUtilities.invokeLater(txt::requestFocusInWindow);
        return Boolean.TRUE.equals(m.mostrar());
    }
}
