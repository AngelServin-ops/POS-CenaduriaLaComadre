package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.dtos.UsuarioDTO;
import negocio.excepciones.NegocioException;

/**
 * "Usuarios" (solo administradores). Los usuarios no se borran: se desactivan,
 * para conservar quién registró cada pedido y cada movimiento de inventario.
 */
public class PanelUsuarios extends JPanel implements Recargable {

    private static final int COL_ACCIONES = 4;
    private static final String[] ROLES = {"ADMINISTRADOR", "CAJERO", "MESERO"};

    private final ModeloUsuarios modelo = new ModeloUsuarios();
    private final JTable tabla = new JTable(modelo);
    private final Paginador<UsuarioDTO> paginador = new Paginador<>(Estilos.FILAS_POR_PAGINA, modelo::setDatos);
    private final JTextField txtBuscar = Estilos.campoTexto("Buscar usuario...");
    private final JLabel lblConteo = Estilos.texto("", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final JPanel zonaAviso = new JPanel(new BorderLayout());

    public PanelUsuarios() {
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);
        pantalla.add(Estilos.encabezado("USUARIOS",
                "Quién trabaja en la cenaduría. Los administradores entran con su contraseña."), BorderLayout.NORTH);

        BotonRedondeado agregar = Estilos.boton("+ Agregar usuario", Estilos.NARANJA);
        agregar.addActionListener(e -> abrirFormulario(null));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recargar(); }
            @Override public void removeUpdate(DocumentEvent e) { recargar(); }
            @Override public void changedUpdate(DocumentEvent e) { recargar(); }
        });
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(txtBuscar, BorderLayout.CENTER);
        barra.add(agregar, BorderLayout.EAST);

        zonaAviso.setOpaque(false);
        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(zonaAviso, BorderLayout.NORTH);
        arriba.add(barra, BorderLayout.CENTER);

        Estilos.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new RenderAcciones());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setPreferredWidth(200);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila < 0 || tabla.columnAtPoint(e.getPoint()) != COL_ACCIONES) {
                    return;
                }
                Rectangle celda = tabla.getCellRect(fila, COL_ACCIONES, false);
                UsuarioDTO u = modelo.get(fila);
                if (e.getX() < celda.x + 20 + 40) {
                    abrirFormulario(u);
                } else {
                    cambiarEstado(u);
                }
            }
        });
        tabla.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                tabla.setCursor(Cursor.getPredefinedCursor(tabla.columnAtPoint(e.getPoint()) == COL_ACCIONES
                        && tabla.rowAtPoint(e.getPoint()) >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(arriba, BorderLayout.NORTH);
        centro.add(Estilos.tarjetaTabla(tabla, lblConteo, paginador), BorderLayout.CENTER);
        pantalla.add(centro, BorderLayout.CENTER);
        pantalla.add(Estilos.texto("Sin iniciar sesión, la caja registra los pedidos como \""
                + ContextoApp.getUsuarioCaja().getNombre() + "\". Debe quedar al menos un administrador activo.",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO), BorderLayout.SOUTH);
        recargar();
    }

    @Override
    public void recargar() {
        try {
            String b = txtBuscar.getText().trim().toLowerCase();
            List<UsuarioDTO> lista = new ArrayList<>();
            for (UsuarioDTO u : ContextoApp.USUARIOS.listarUsuarios()) {
                if (b.isEmpty() || u.getNombre().toLowerCase().contains(b)
                        || u.getNombreUsuario().toLowerCase().contains(b)) {
                    lista.add(u);
                }
            }
            paginador.setDatos(lista);
            lblConteo.setText(lista.size() + " usuarios");
        } catch (NegocioException e) {
            paginador.setDatos(List.of());
            lblConteo.setText("No se pudo cargar la lista: " + e.getMessage());
        }
    }

    private void avisar(Estilos.TipoAviso tipo, String titulo, String detalle) {
        zonaAviso.removeAll();
        zonaAviso.add(Estilos.aviso(tipo, titulo, detalle), BorderLayout.CENTER);
        zonaAviso.revalidate();
        zonaAviso.repaint();
    }

    // ------------------------------------------------------------ Acciones
    private void cambiarEstado(UsuarioDTO u) {
        if (u.isActivo() && Objects.equals(u.getId(), ContextoApp.getUsuarioCaja().getId())) {
            Modal.error(this, "No se puede desactivar", "La caja usa este usuario para registrar pedidos.");
            return;
        }
        if (u.isActivo() && ContextoApp.getAdministrador() != null
                && Objects.equals(u.getId(), ContextoApp.getAdministrador().getId())) {
            Modal.error(this, "No se puede desactivar", "No puedes desactivar tu propia cuenta mientras la usas.");
            return;
        }
        if (u.isActivo()) {
            Modal m = new Modal(this, "¿Desactivar a " + u.getNombre() + "?", null, 480);
            m.parrafo("Ya no podrá usar el sistema. Sus pedidos y movimientos se conservan, "
                    + "y puedes reactivarlo cuando quieras.");
            m.boton("Conservar", null, false);
            m.boton("Desactivar", Estilos.GUINDA, true);
            if (!Boolean.TRUE.equals(m.mostrar())) {
                return;
            }
        }
        try {
            ContextoApp.USUARIOS.cambiarEstado(u.getId(), !u.isActivo());
            avisar(Estilos.TipoAviso.EXITO, u.isActivo() ? "Usuario desactivado" : "Usuario reactivado", u.getNombre());
            recargar();
        } catch (NegocioException e) {
            Modal.error(this, "No se pudo cambiar el estado", e.getMessage());
        }
    }

    /** Pop up para agregar (u == null) o editar un usuario. */
    private void abrirFormulario(UsuarioDTO u) {
        boolean edicion = u != null;
        Modal m = new Modal(this, edicion ? "Editar usuario" : "Agregar usuario",
                edicion ? "Cambia los datos de " + u.getNombre() + "." : "Añade a alguien del equipo.", 520);

        JTextField txtNombre = Estilos.campoTexto("Lupita Gómez");
        JTextField txtUsuario = Estilos.campoTexto("lupita");
        JComboBox<String> cbRol = Estilos.combo(ROLES, PanelUsuarios::textoRol);
        JPasswordField txtPass = new JPasswordField();
        JPasswordField txtPass2 = new JPasswordField();
        Estilos.estilizar(txtPass);
        Estilos.estilizar(txtPass2);
        if (edicion) {
            txtNombre.setText(u.getNombre());
            txtUsuario.setText(u.getNombreUsuario());
            cbRol.setSelectedItem(u.getRol());
        }
        JLabel ayuda = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
        Runnable actualizarAyuda = () -> ayuda.setText("ADMINISTRADOR".equals(cbRol.getSelectedItem())
                ? (edicion ? "Deja la contraseña vacía para no cambiarla." : "La contraseña sirve para entrar al modo administrador.")
                : "Cajeros y meseros no necesitan contraseña: usan el modo caja.");
        cbRol.addActionListener(e -> actualizarAyuda.run());
        actualizarAyuda.run();

        JPanel form = new JPanel(new GridLayout(3, 2, 12, 10));
        form.setOpaque(false);
        form.add(Estilos.campo("Nombre", txtNombre));
        form.add(Estilos.campo("Nombre de usuario", txtUsuario));
        form.add(Estilos.campo("Rol", cbRol));
        form.add(new JLabel());
        form.add(Estilos.campo(edicion ? "Nueva contraseña" : "Contraseña", txtPass));
        form.add(Estilos.campo("Confirmar contraseña", txtPass2));
        m.agregar(form);
        m.agregar(ayuda);
        JLabel error = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.ROJO);
        m.agregar(error);

        m.boton("Cancelar", null, null);
        m.botonAccion(edicion ? "Guardar cambios" : "Agregar usuario", Estilos.NARANJA, () -> {
            String pass = new String(txtPass.getPassword());
            if (!pass.equals(new String(txtPass2.getPassword()))) {
                error.setText("Las contraseñas no coinciden.");
                return;
            }
            UsuarioDTO dto = new UsuarioDTO(txtNombre.getText(), txtUsuario.getText(),
                    pass.isEmpty() ? null : pass, (String) cbRol.getSelectedItem());
            try {
                if (edicion) {
                    dto.setId(u.getId());
                    ContextoApp.USUARIOS.actualizarUsuario(dto);
                } else {
                    ContextoApp.USUARIOS.registrarUsuario(dto);
                }
                m.cerrar(true);
            } catch (NegocioException ex) {
                error.setText(ex.getMessage());
            }
        });
        if (Boolean.TRUE.equals(m.mostrar())) {
            avisar(Estilos.TipoAviso.EXITO, edicion ? "Usuario actualizado" : "Usuario agregado",
                    txtNombre.getText().trim() + " · " + textoRol((String) cbRol.getSelectedItem()));
            recargar();
        }
    }

    static String textoRol(String rol) {
        if (rol == null) {
            return "";
        }
        return switch (rol) {
            case "ADMINISTRADOR" -> "Administrador";
            case "CAJERO" -> "Cajero";
            case "MESERO" -> "Mesero";
            default -> rol;
        };
    }

    // ============================================================ Tabla
    private static class ModeloUsuarios extends AbstractTableModel {

        private final String[] columnas = {"Nombre", "Usuario", "Rol", "Estado", "Acciones"};
        private List<UsuarioDTO> datos = new ArrayList<>();

        void setDatos(List<UsuarioDTO> nuevos) {
            datos = new ArrayList<>(nuevos);
            fireTableDataChanged();
        }

        UsuarioDTO get(int fila) {
            return datos.get(fila);
        }

        @Override public int getRowCount() { return datos.size(); }
        @Override public int getColumnCount() { return columnas.length; }
        @Override public String getColumnName(int c) { return columnas[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            UsuarioDTO u = datos.get(fila);
            return switch (col) {
                case 0 -> u.getNombre();
                case 1 -> u.getNombreUsuario();
                case 2 -> textoRol(u.getRol());
                default -> u;
            };
        }
    }

    private static class RenderEstado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            UsuarioDTO u = (UsuarioDTO) v;
            super.getTableCellRendererComponent(t, u.isActivo() ? "Activo" : "Inactivo", false, false, r, c);
            return Estilos.pintarCelda(this, t, r, u.isActivo() ? Estilos.VERDE : Estilos.GRIS_TEXTO);
        }
    }

    /** "Editar   Desactivar" (o "Reactivar"). */
    private static class RenderAcciones implements javax.swing.table.TableCellRenderer {

        private final JPanel panel = new JPanel();
        private final JLabel editar = Estilos.texto("Editar", Estilos.ENLACE, Estilos.NARANJA);
        private final JLabel estado = Estilos.texto("", Estilos.ENLACE, Estilos.GUINDA);

        RenderAcciones() {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setBorder(new EmptyBorder(0, 20, 0, 8));
            panel.add(editar);
            panel.add(javax.swing.Box.createRigidArea(new Dimension(14, 0)));
            panel.add(estado);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            UsuarioDTO u = (UsuarioDTO) v;
            estado.setText(u.isActivo() ? "Desactivar" : "Reactivar");
            estado.setForeground(u.isActivo() ? Estilos.GUINDA : Estilos.VERDE);
            panel.setBackground(Estilos.fondoFila(t, r));
            return panel;
        }
    }
}
