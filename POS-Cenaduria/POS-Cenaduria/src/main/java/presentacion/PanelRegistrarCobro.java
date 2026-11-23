package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import negocio.dtos.DetallePedidoDTO;
import negocio.dtos.Formatos;
import negocio.dtos.PagoDTO;
import negocio.dtos.PartidaCobroDTO;
import negocio.dtos.PedidoDTO;
import negocio.excepciones.NegocioException;
import negocio.service.IPedidoService;

/**
 * CU Cobrar pedido · paso 3 "REGISTRAR COBRO": método de pago e importes,
 * para la cuenta completa o para la parte seleccionada al separar la cuenta.
 * Las reglas (monto suficiente, tarjeta exacta, mixto) las valida PedidoService
 * mientras se escribe; "Confirmar cobro" solo se habilita si el cobro es válido.
 * El registro corre fuera del hilo de la interfaz y no se puede confirmar dos veces.
 */
public class PanelRegistrarCobro extends JPanel {

    private static final String[] METODOS = {"EFECTIVO", "TARJETA", "MIXTO"};

    private final PuntoDeVentaView nav;
    private final IPedidoService servicio = ContextoApp.PEDIDOS;
    private final PedidoDTO pedido;
    // null = cuenta completa (todo lo que falta); si no, solo estas unidades (cuenta separada)
    private final List<PartidaCobroDTO> partidas;
    private final BigDecimal monto;
    // Líneas a mostrar en el resumen: {cantidad, descripción, precio unitario}
    private final List<Object[]> lineas = new ArrayList<>();

    private final JComboBox<String> cbMetodo = Estilos.combo(METODOS, Estilos::textoMetodo);
    private final BotonRedondeado[] botonesMetodo = new BotonRedondeado[METODOS.length];
    private final JTextField txtTarjeta = Estilos.campoTexto("$0.00");
    private final JTextField txtEfectivo = Estilos.campoTexto("$0.00");
    private final JTextField txtReferencia = Estilos.campoTexto("Ej. Aprobada · 0142");
    private final JTextField txtCambio = Estilos.campoLectura("");
    private final JPanel zonaCampos = new JPanel();
    private final JPanel zonaError = new JPanel(new BorderLayout());
    private final JLabel lblAyuda = Estilos.texto(" ", Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO);
    private final BotonRedondeado btnConfirmar = Estilos.boton("Confirmar cobro", Estilos.NARANJA);

    public PanelRegistrarCobro(PuntoDeVentaView nav, PedidoDTO pedido, List<PartidaCobroDTO> partidas) {
        this.nav = nav;
        this.pedido = pedido;
        this.partidas = partidas;
        BigDecimal suma = BigDecimal.ZERO;
        for (DetallePedidoDTO d : pedido.getDetalles()) {
            int cantidad = d.getPendiente();
            if (partidas != null) {
                cantidad = partidas.stream().filter(x -> x.idDetalle().equals(d.getId()))
                        .mapToInt(PartidaCobroDTO::cantidad).sum();
            }
            if (cantidad > 0) {
                lineas.add(new Object[]{cantidad, d.descripcion(), d.getPrecioUnitario()});
                suma = suma.add(d.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad)));
            }
        }
        this.monto = suma;
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);
        pantalla.add(Estilos.encabezado("REGISTRAR COBRO", pedido.getFolioTexto() + " · "
                + pedido.getServicioTexto() + " · Revisa los importes antes de confirmar."), BorderLayout.NORTH);

        JPanel columnas = new JPanel(new GridLayout(1, 2, 24, 0));
        columnas.setOpaque(false);
        columnas.add(crearResumen());
        columnas.add(crearFormulario());
        pantalla.add(PanelDetallePedido.envolver(columnas), BorderLayout.CENTER);

        seleccionarMetodo("EFECTIVO");
    }

    // ------------------------------------------------------------ Columna izquierda
    private JComponent crearResumen() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        boolean separada = partidas != null || !pedido.getPagos().isEmpty();
        PanelPedidos.agregarIzq(t, Estilos.texto(separada ? "Resumen del cobro · cuenta separada"
                : "Resumen del pedido", Estilos.TITULO_TARJETA, Color.BLACK));
        JPanel estados = new JPanel(new GridLayout(1, 2, 20, 0));
        estados.setOpaque(false);
        estados.add(Estilos.dato("Total de la cuenta", Formatos.dinero(pedido.getTotal()), Color.BLACK));
        estados.add(Estilos.dato("Saldo antes de este cobro", Formatos.dinero(pedido.getSaldo()), Estilos.GUINDA));
        PanelPedidos.agregarIzq(t, estados);
        for (Object[] l : lineas) {
            BigDecimal precio = (BigDecimal) l[2];
            int cantidad = (Integer) l[0];
            PanelPedidos.agregarIzq(t, Estilos.texto(cantidad + " " + l[1] + " × " + Formatos.dinero(precio) + " = "
                    + Formatos.dinero(precio.multiply(BigDecimal.valueOf(cantidad))), Estilos.TEXTO_CHICO, Color.BLACK));
        }
        PanelPedidos.agregarIzq(t, Estilos.texto("A cobrar: " + Formatos.dinero(monto) + " MXN",
                Estilos.TOTAL, Color.BLACK));
        if (separada) {
            PanelPedidos.agregarIzq(t, Estilos.texto("Después de este cobro quedarán "
                    + Formatos.dinero(pedido.getSaldo().subtract(monto)) + " por cobrar.",
                    Estilos.TEXTO_CHICO, Estilos.GUINDA));
        }
        PanelPedidos.agregarIzq(col, t);
        PanelPedidos.agregarIzq(col, Estilos.texto("El pedido permanece Activo hasta completar el cobro.",
                Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        return col;
    }

    // ------------------------------------------------------------ Columna derecha
    private JComponent crearFormulario() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        cbMetodo.addActionListener(e -> seleccionarMetodo((String) cbMetodo.getSelectedItem()));
        JPanel campoMetodo = Estilos.campo("Método de pago", cbMetodo);
        limitarAlto(campoMetodo);
        PanelPedidos.agregarIzq(col, campoMetodo);

        for (int i = 0; i < METODOS.length; i++) {
            String metodo = METODOS[i];
            BotonRedondeado b = Estilos.botonSecundario(Estilos.textoMetodo(metodo));
            b.setPreferredSize(new Dimension(96, 32));
            b.addActionListener(e -> cbMetodo.setSelectedItem(metodo));
            botonesMetodo[i] = b;
        }
        PanelPedidos.agregarIzq(col, Estilos.filaIzquierda(8, botonesMetodo));

        zonaCampos.setLayout(new BoxLayout(zonaCampos, BoxLayout.Y_AXIS));
        zonaCampos.setOpaque(false);
        zonaCampos.putClientProperty("altoLibre", true);
        PanelPedidos.agregarIzq(col, zonaCampos);

        zonaError.setOpaque(false);
        zonaError.putClientProperty("altoLibre", true);
        PanelPedidos.agregarIzq(col, zonaError);
        PanelPedidos.agregarIzq(col, lblAyuda);

        DocumentListener alEscribir = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validar(); }
            @Override public void removeUpdate(DocumentEvent e) { validar(); }
            @Override public void changedUpdate(DocumentEvent e) { validar(); }
        };
        txtTarjeta.getDocument().addDocumentListener(alEscribir);
        txtEfectivo.getDocument().addDocumentListener(alEscribir);
        txtReferencia.getDocument().addDocumentListener(alEscribir);

        BotonRedondeado btnCancelar = Estilos.boton("Cancelar cobro", Estilos.GUINDA);
        btnCancelar.addActionListener(e -> nav.irACobroCancelado(pedido.getId()));
        btnConfirmar.addActionListener(e -> confirmar());
        PanelPedidos.agregarIzq(col, Estilos.filaIzquierda(10, btnCancelar, btnConfirmar));
        return col;
    }

    private void seleccionarMetodo(String metodo) {
        if (!metodo.equals(cbMetodo.getSelectedItem())) {
            cbMetodo.setSelectedItem(metodo); // vuelve a entrar por el listener del combo
            return;
        }
        for (int i = 0; i < METODOS.length; i++) {
            boolean activo = METODOS[i].equals(metodo);
            botonesMetodo[i].setRelleno(activo ? Estilos.NARANJA : Color.WHITE);
            botonesMetodo[i].setForeground(activo ? Color.WHITE : Color.BLACK);
        }

        zonaCampos.removeAll();
        switch (metodo) {
            case "EFECTIVO" -> {
                agregarCampo("Efectivo recibido (MXN)", txtEfectivo);
                agregarCampo("Cambio a entregar (MXN)", txtCambio);
            }
            case "TARJETA" -> {
                txtTarjeta.setText(sinSigno(monto));
                txtTarjeta.setEditable(false);
                agregarCampo("Monto en tarjeta (MXN)", txtTarjeta);
                agregarCampo("Referencia de terminal", txtReferencia);
                agregarCampo("Cambio", txtCambio);
            }
            default -> {
                if (!txtTarjeta.isEditable()) {
                    txtTarjeta.setText("");
                }
                agregarCampo("Monto en tarjeta (MXN)", txtTarjeta);
                agregarCampo("Efectivo recibido (MXN)", txtEfectivo);
                agregarCampo("Cambio a entregar (MXN)", txtCambio);
            }
        }
        txtTarjeta.setEditable(!"TARJETA".equals(metodo));
        zonaCampos.revalidate();
        zonaCampos.repaint();
        validar();
    }

    private void agregarCampo(String etiqueta, JTextField campo) {
        JPanel c = Estilos.campo(etiqueta, campo);
        limitarAlto(c);
        PanelPedidos.agregarIzq(zonaCampos, c);
    }

    // ------------------------------------------------------------ Validación en vivo
    private void validar() {
        String metodo = (String) cbMetodo.getSelectedItem();
        zonaError.removeAll();
        Estilos.marcarError(txtEfectivo, false);
        Estilos.marcarError(txtTarjeta, false);

        BigDecimal tarjeta = leer(txtTarjeta);
        BigDecimal efectivo = leer(txtEfectivo);
        JTextField[] editables = switch (metodo) {
            case "EFECTIVO" -> new JTextField[]{txtEfectivo};
            case "MIXTO" -> new JTextField[]{txtTarjeta, txtEfectivo};
            default -> new JTextField[0];
        };
        for (JTextField campo : editables) {
            if (!campo.getText().isBlank() && leer(campo) == null) {
                Estilos.habilitar(btnConfirmar, false, Estilos.NARANJA);
                txtCambio.setText("No disponible");
                Estilos.marcarError(campo, true);
                zonaError.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "Importe no válido",
                        "Escribe solo números, por ejemplo 300 o 300.50."), BorderLayout.CENTER);
                zonaError.revalidate();
                zonaError.repaint();
                return;
            }
        }
        boolean capturado = switch (metodo) {
            case "EFECTIVO" -> efectivo != null;
            case "TARJETA" -> true;
            default -> tarjeta != null && efectivo != null;
        };
        try {
            if (!capturado || ("TARJETA".equals(metodo) && txtReferencia.getText().isBlank())) {
                throw new NegocioException((String) null);
            }
            PagoDTO r = servicio.previsualizarCobro(monto,
                    new PagoDTO(metodo, tarjeta, efectivo, txtReferencia.getText()));
            txtCambio.setText("TARJETA".equals(metodo) ? "No aplica" : Formatos.dinero(r.getCambio()));
            lblAyuda.setText(switch (metodo) {
                case "TARJETA" -> "Registra la aprobación de la terminal antes de confirmar. No se recibe efectivo.";
                case "MIXTO" -> "Aplicado: " + Formatos.dinero(r.getMonto()) + " = tarjeta "
                        + Formatos.dinero(r.getMontoTarjeta()) + " + efectivo " + Formatos.dinero(r.getEfectivoAplicado())
                        + ". Cambio: " + Formatos.dinero(r.getCambio()) + ".";
                default -> "Efectivo: " + Formatos.dinero(r.getEfectivoRecibido()) + " recibido − "
                        + Formatos.dinero(r.getMonto()) + " total = " + Formatos.dinero(r.getCambio()) + " de cambio.";
            });
            Estilos.habilitar(btnConfirmar, true, Estilos.NARANJA);
        } catch (NegocioException e) {
            Estilos.habilitar(btnConfirmar, false, Estilos.NARANJA);
            txtCambio.setText("TARJETA".equals(metodo) ? "No aplica" : (capturado ? "No disponible" : ""));
            lblAyuda.setText("TARJETA".equals(metodo)
                    ? "Para tarjeta: registra la referencia de la terminal; no se calcula cambio."
                    : "El pedido permanece Activo hasta completar el cobro.");
            if (capturado && e.getMessage() != null) {
                mostrarError(e.getMessage(), metodo);
            }
        }
        zonaError.revalidate();
        zonaError.repaint();
    }

    private void mostrarError(String mensaje, String metodo) {
        int punto = mensaje.indexOf(". ");
        String titulo = punto > 0 ? mensaje.substring(0, punto) : mensaje;
        String detalle = punto > 0 ? mensaje.substring(punto + 2) : null;
        zonaError.add(Estilos.aviso(Estilos.TipoAviso.ERROR, titulo, detalle), BorderLayout.CENTER);
        if (titulo.contains("tarjeta")) {
            Estilos.marcarError(txtTarjeta, true);
        } else if (!"TARJETA".equals(metodo)) {
            Estilos.marcarError(txtEfectivo, true);
        }
    }

    // ------------------------------------------------------------ Confirmar
    private void confirmar() {
        if (!btnConfirmar.isEnabled()) {
            return;
        }
        Estilos.habilitar(btnConfirmar, false, Estilos.NARANJA); // evita doble confirmación
        PagoDTO datos = new PagoDTO((String) cbMetodo.getSelectedItem(), leer(txtTarjeta), leer(txtEfectivo),
                txtReferencia.getText());
        Long idUsuario = ContextoApp.getUsuario().getId();

        Modal procesando = new Modal(this, "Procesando cobro", "Se está registrando el pago del pedido "
                + pedido.getFolioTexto() + ".", 560);
        procesando.setCerrable(false);
        procesando.agregar(Estilos.aviso(Estilos.TipoAviso.INFO, "Cobro en curso · no volver a confirmar",
                "La operación corre en segundo plano sin bloquear la interfaz."));
        procesando.agregar(datosEstado("Activo"));
        procesando.parrafo("Todavía no se registra el cobro ni se emite el ticket.");
        BotonRedondeado enCurso = Estilos.boton("Cobro en curso...", Estilos.NARANJA);
        Estilos.habilitar(enCurso, false, Estilos.NARANJA);
        procesando.agregar(enCurso);

        SwingWorker<PedidoDTO, Void> trabajo = new SwingWorker<>() {
            @Override
            protected PedidoDTO doInBackground() throws Exception {
                return partidas == null
                        ? servicio.cobrarPedido(pedido.getId(), datos, idUsuario)
                        : servicio.cobrarPartidas(pedido.getId(), partidas, datos, idUsuario);
            }

            @Override
            protected void done() {
                procesando.cerrar(null);
            }
        };
        trabajo.execute();
        if (!trabajo.isDone()) {
            procesando.mostrar(); // se cierra solo cuando termina el cobro
        }

        try {
            mostrarExito(trabajo.get());
        } catch (ExecutionException e) {
            mostrarErrorCobro(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            mostrarErrorCobro(e);
        }
    }

    /** Paso 4: "Pago registrado". Si la cuenta se separó y aún hay saldo, invita a seguir cobrando. */
    private void mostrarExito(PedidoDTO pagado) {
        PagoDTO pago = pagado.getPagos().get(pagado.getPagos().size() - 1);
        boolean liquidado = pagado.esPagado();
        Modal m = new Modal(this, liquidado ? "Pago registrado" : "Pago parcial registrado",
                liquidado ? "La cuenta del pedido " + pagado.getFolioTexto() + " quedó liquidada."
                        : "Se cobró una parte de la cuenta del pedido " + pagado.getFolioTexto() + ".", 600);
        m.agregar(Estilos.aviso(Estilos.TipoAviso.EXITO, "Cobro por " + Formatos.dinero(pago.getMonto()) + " MXN",
                PanelDetallePedido.textoImportes(pago)));
        JPanel datos = new JPanel(new GridLayout(1, 2, 20, 0));
        datos.setOpaque(false);
        datos.add(Estilos.dato("Estado del pedido", Estilos.textoEstadoPedido(pagado.getEstado()),
                Estilos.colorEstadoPedido(pagado.getEstado())));
        datos.add(Estilos.dato("Saldo por cobrar", Formatos.dinero(pagado.getSaldo()),
                liquidado ? Estilos.VERDE : Estilos.GUINDA));
        m.agregar(datos);
        m.parrafo("Comprobante " + pago.getFolioComprobante() + " · " + Formatos.fechaHora(pago.getFechaHora())
                + " · " + ContextoApp.nombreCaja());
        if (liquidado) {
            m.boton("Volver a pedidos", null, "volver");
        } else {
            m.boton("Seguir cobrando", Estilos.NARANJA, "seguir");
        }
        m.boton("Ver comprobante", liquidado ? Estilos.NARANJA : null, "ticket");
        Object r = m.mostrar();
        if ("ticket".equals(r)) {
            nav.irATicket(pagado.getId(), pago.getNumero());
        } else if ("seguir".equals(r) || (r == null && !liquidado)) {
            nav.irACobrar(pagado.getId());
        } else {
            nav.irAConsultar();
        }
    }

    /** Paso 4F: "Error de cobro". Nada cambió: el pedido sigue Activo y se puede reintentar. */
    private void mostrarErrorCobro(Throwable causa) {
        String detalle = causa instanceof NegocioException ne ? ne.getMessage() : "Error inesperado: " + causa;
        Modal m = new Modal(this, "Error de cobro", "No se pudo registrar el pago.", 600);
        m.agregar(Estilos.aviso(Estilos.TipoAviso.ERROR, "Sin cambios en el pedido",
                "No se registró el cobro ni se emitió el ticket. " + detalle));
        m.agregar(datosEstado("Activo"));
        m.parrafo("Comprobante no emitido. El pedido " + pedido.getFolioTexto() + " sigue disponible para cobrar.");
        m.boton("Volver a pedidos", null, "volver");
        m.boton("Intentar nuevamente", Estilos.NARANJA, "reintentar");
        Object r = m.mostrar();
        if ("reintentar".equals(r)) {
            validar(); // vuelve a habilitar Confirmar si los datos siguen siendo válidos
        } else {
            nav.irAConsultar();
        }
    }

    private JPanel datosEstado(String estadoPedido) {
        JPanel datos = new JPanel(new GridLayout(1, 2, 20, 0));
        datos.setOpaque(false);
        datos.add(Estilos.dato("Estado del pedido", estadoPedido,
                "Pagado".equals(estadoPedido) ? Estilos.VERDE : Estilos.NARANJA));
        datos.add(Estilos.dato("Este cobro", Formatos.dinero(monto), Color.BLACK));
        return datos;
    }

    // ------------------------------------------------------------ Utilidades
    /** Lee "$1,200.50" o "1200.5"; vacío o inválido = null. */
    private static BigDecimal leer(JTextField campo) {
        String t = campo.getText().replace("$", "").replace(",", "").trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String sinSigno(BigDecimal monto) {
        return Formatos.dinero(monto).replace("$", "").replace(",", "");
    }

    private static void limitarAlto(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }
}
