package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import negocio.dtos.Formatos;
import negocio.dtos.ReporteDTO;
import negocio.excepciones.NegocioException;

/**
 * "Reportes" (solo administradores): ventas, platillos más vendidos y consumo de insumos
 * de un periodo, con exportación a CSV para abrirlo en Excel.
 */
public class PanelReportes extends JPanel implements Recargable {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TOP_PLATILLOS = 10;

    private final JTextField txtDesde = Estilos.campoTexto("dd/mm/aaaa");
    private final JTextField txtHasta = Estilos.campoTexto("dd/mm/aaaa");
    private final JPanel resultados = new JPanel(new BorderLayout());
    private ReporteDTO ultimo;

    public PanelReportes() {
        setLayout(new BorderLayout());
        JPanel pantalla = Estilos.pantalla();
        add(pantalla, BorderLayout.CENTER);
        pantalla.add(Estilos.encabezado("REPORTES", "Ventas, platillos y consumo de insumos por periodo"),
                BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        cuerpo.add(crearFiltros(), BorderLayout.NORTH);
        resultados.setOpaque(false);
        cuerpo.add(resultados, BorderLayout.CENTER);
        pantalla.add(cuerpo, BorderLayout.CENTER);

        periodo(LocalDate.now().minusDays(6), LocalDate.now());
    }

    @Override
    public void recargar() {
        generar();
    }

    // ------------------------------------------------------------ Filtros
    private JComponent crearFiltros() {
        txtDesde.setPreferredSize(new Dimension(130, 34));
        txtHasta.setPreferredSize(new Dimension(130, 34));
        txtDesde.addActionListener(e -> generar());
        txtHasta.addActionListener(e -> generar());

        BotonRedondeado generar = Estilos.boton("Generar", Estilos.NARANJA);
        generar.addActionListener(e -> generar());
        BotonRedondeado hoy = Estilos.botonSecundario("Hoy");
        hoy.addActionListener(e -> periodo(LocalDate.now(), LocalDate.now()));
        BotonRedondeado semana = Estilos.botonSecundario("Semana");
        semana.addActionListener(e -> periodo(LocalDate.now().minusDays(6), LocalDate.now()));
        BotonRedondeado mes = Estilos.botonSecundario("Mes");
        mes.addActionListener(e -> periodo(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        for (BotonRedondeado b : new BotonRedondeado[]{hoy, semana, mes}) {
            b.setPreferredSize(new Dimension(100, 36));
        }
        BotonRedondeado exportar = Estilos.botonSecundario("Exportar CSV");
        exportar.addActionListener(e -> exportar());

        JLabel lDesde = Estilos.texto("Desde", Estilos.ETIQUETA, Color.BLACK);
        JLabel lHasta = Estilos.texto("Hasta", Estilos.ETIQUETA, Color.BLACK);
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(Estilos.filaIzquierda(8, lDesde, txtDesde, lHasta, txtHasta, generar, hoy, semana, mes),
                BorderLayout.WEST);
        fila.add(exportar, BorderLayout.EAST);
        return fila;
    }

    private void periodo(LocalDate desde, LocalDate hasta) {
        txtDesde.setText(desde.format(FECHA));
        txtHasta.setText(hasta.format(FECHA));
        generar();
    }

    private void generar() {
        resultados.removeAll();
        Estilos.marcarError(txtDesde, false);
        Estilos.marcarError(txtHasta, false);
        LocalDate desde = leer(txtDesde);
        LocalDate hasta = leer(txtHasta);
        try {
            if (desde == null || hasta == null) {
                throw new NegocioException("Escribe las fechas como dd/mm/aaaa, por ejemplo "
                        + LocalDate.now().format(FECHA) + ".");
            }
            ultimo = ContextoApp.REPORTES.generar(desde, hasta);
            resultados.add(mostrar(ultimo), BorderLayout.NORTH);
        } catch (NegocioException e) {
            ultimo = null;
            resultados.add(Estilos.aviso(Estilos.TipoAviso.ERROR, "No se pudo generar el reporte", e.getMessage()),
                    BorderLayout.NORTH);
        }
        resultados.revalidate();
        resultados.repaint();
    }

    private LocalDate leer(JTextField campo) {
        try {
            return LocalDate.parse(campo.getText().trim(), FECHA);
        } catch (DateTimeParseException e) {
            Estilos.marcarError(campo, true);
            return null;
        }
    }

    // ------------------------------------------------------------ Resultados
    private JComponent mostrar(ReporteDTO r) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(kpi("Total vendido", Formatos.dinero(r.totalVendido()), Color.BLACK));
        kpis.add(kpi("Pedidos pagados", String.valueOf(r.pedidosPagados()), Color.BLACK));
        kpis.add(kpi("Cancelados", String.valueOf(r.pedidosCancelados()),
                r.pedidosCancelados() > 0 ? Estilos.ROJO : Color.BLACK));
        kpis.add(kpi("Ticket promedio", Formatos.dinero(r.ticketPromedio()), Color.BLACK));
        PanelPedidos.agregarIzq(col, kpis);

        JPanel medio = new JPanel(new GridLayout(1, 2, 14, 0));
        medio.setOpaque(false);
        medio.add(tarjeta("Ventas por método de pago", new Barras(r.ventasPorMetodo())));
        List<Object[]> filas = new ArrayList<>();
        for (ReporteDTO.VentaPlatillo v : r.platillos().subList(0, Math.min(TOP_PLATILLOS, r.platillos().size()))) {
            filas.add(new Object[]{v.nombre(), v.cantidad(), Formatos.dinero(v.importe())});
        }
        medio.add(tarjeta("Platillos más vendidos", Estilos.tablaFija(
                new String[]{"Platillo", "Cantidad", "Importe"}, filas, "Sin ventas en el periodo.")));
        PanelPedidos.agregarIzq(col, medio);

        List<Object[]> insumos = new ArrayList<>();
        for (ReporteDTO.ConsumoInsumo c : r.insumos()) {
            String u = " " + c.unidad().toLowerCase();
            insumos.add(new Object[]{c.nombre(), "+" + num(c.compras()) + u, "−" + num(c.consumo()) + u,
                    c.merma() > 0 ? "−" + num(c.merma()) + u : "0",
                    c.ajustes() == 0 ? "0" : (c.ajustes() > 0 ? "+" : "−") + num(Math.abs(c.ajustes())) + u});
        }
        PanelPedidos.agregarIzq(col, tarjeta("Consumo de insumos (cierres del día)", Estilos.tablaFija(
                new String[]{"Insumo", "Compras", "Consumo", "Merma", "Ajustes"}, insumos,
                "Sin movimientos de inventario en el periodo.")));
        return col;
    }

    private static JComponent kpi(String etiqueta, String valor, Color color) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(14, 18, 14, 18));
        PanelPedidos.agregarIzq(t, Estilos.texto(etiqueta, Estilos.TEXTO_CHICO, Estilos.GRIS_TEXTO));
        PanelPedidos.agregarIzq(t, Estilos.texto(valor, Estilos.TOTAL.deriveFont(24f), color));
        return t;
    }

    private static JComponent tarjeta(String titulo, JComponent contenido) {
        PanelRedondeado t = Estilos.tarjeta();
        t.setLayout(new BorderLayout(0, 10));
        t.add(Estilos.texto(titulo, Estilos.TITULO_TARJETA, Color.BLACK), BorderLayout.NORTH);
        t.add(contenido, BorderLayout.CENTER);
        return t;
    }

    /** Barras horizontales: cuánto se cobró con cada método. */
    private static class Barras extends JPanel {

        private static final Color[] COLORES = {Estilos.NARANJA, Estilos.GUINDA, new Color(0xC9A27E)};
        private final Map<String, BigDecimal> datos;

        Barras(Map<String, BigDecimal> datos) {
            this.datos = datos;
            setOpaque(false);
            setPreferredSize(new Dimension(300, datos.size() * 46));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            BigDecimal max = datos.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            if (max.signum() == 0) {
                max = BigDecimal.ONE;
            }
            int y = 0;
            int i = 0;
            g2.setFont(Estilos.TEXTO_CHICO);
            for (Map.Entry<String, BigDecimal> e : datos.entrySet()) {
                g2.setColor(Color.BLACK);
                g2.drawString(Estilos.textoMetodo(e.getKey()) + " · " + Formatos.dinero(e.getValue()), 0, y + 14);
                int ancho = (int) (getWidth() * e.getValue().doubleValue() / max.doubleValue());
                g2.setColor(Estilos.GRIS_LINEA);
                g2.fillRoundRect(0, y + 20, getWidth(), 10, 6, 6);
                g2.setColor(COLORES[i % COLORES.length]);
                g2.fillRoundRect(0, y + 20, Math.max(ancho, e.getValue().signum() > 0 ? 6 : 0), 10, 6, 6);
                y += 46;
                i++;
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------ Exportar
    private void exportar() {
        if (ultimo == null) {
            Modal.error(this, "No hay reporte", "Genera un reporte antes de exportarlo.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("CSV (Excel)", "csv"));
        selector.setSelectedFile(new File("reporte_" + ultimo.desde().format(DateTimeFormatter.ISO_DATE) + "_"
                + ultimo.hasta().format(DateTimeFormatter.ISO_DATE) + ".csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }
        try {
            escribirCsv(ultimo, archivo);
            Modal m = new Modal(this, "Reporte exportado", archivo.getAbsolutePath(), 520);
            m.boton("Aceptar", Estilos.NARANJA, null);
            m.mostrar();
        } catch (IOException e) {
            Modal.error(this, "No se pudo exportar", e.getMessage());
        }
    }

    /** CSV con BOM para que Excel respete acentos. */
    static void escribirCsv(ReporteDTO r, File archivo) throws IOException {
        try (PrintWriter w = new PrintWriter(archivo, StandardCharsets.UTF_8)) {
            w.print('﻿');
            w.println("Reporte," + r.desde().format(FECHA) + "," + r.hasta().format(FECHA));
            w.println();
            w.println("Total vendido,Pedidos pagados,Cancelados,Ticket promedio");
            w.println(r.totalVendido() + "," + r.pedidosPagados() + "," + r.pedidosCancelados() + "," + r.ticketPromedio());
            w.println();
            w.println("Método de pago,Importe");
            r.ventasPorMetodo().forEach((k, v) -> w.println(Estilos.textoMetodo(k) + "," + v));
            w.println();
            w.println("Platillo,Cantidad,Importe");
            r.platillos().forEach(p -> w.println(csv(p.nombre()) + "," + p.cantidad() + "," + p.importe()));
            w.println();
            w.println("Insumo,Unidad,Compras,Consumo,Merma,Ajustes");
            r.insumos().forEach(c -> w.println(csv(c.nombre()) + "," + c.unidad() + "," + c.compras() + ","
                    + c.consumo() + "," + c.merma() + "," + c.ajustes()));
        }
    }

    private static String csv(String s) {
        return s.contains(",") || s.contains("\"") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    private static String num(float n) {
        return n == (long) n ? String.valueOf((long) n) : String.valueOf(n);
    }
}
