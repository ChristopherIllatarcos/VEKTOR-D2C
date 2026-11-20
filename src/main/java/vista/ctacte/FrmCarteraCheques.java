package vista.ctacte;

import com.toedter.calendar.JDateChooser;
import modelo.DocumentoCarteraDTO;
import conexion.Conexion;
import dao.CuentaCorrienteDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FrmCarteraCheques extends JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(FrmCarteraCheques.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField txtCliente;
    private JDateChooser dcFechaInicio;
    private JDateChooser dcFechaFin;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JButton btnGrafico;
    private JButton btnBuscar;
    private JLabel lblAyuda;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelFiltros, panelBotones, panelAyudaContainer;
    private JScrollPane scrollTabla;

    private final CuentaCorrienteDAO dao = new CuentaCorrienteDAO();

    // Lista en memoria para alimentar el gráfico ejecutivo con la última consulta
    private List<DocumentoCarteraDTO> listaUltimaConsulta = new ArrayList<>();

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelSup, cTablaFondo, cBordeAcento, cTextoVerde, cTextoBlanco, cBarraAyuda;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmCarteraCheques() {
        setTitle("2.- MODULO CARTERA DE CHEQUES Y DOCUMENTOS");
        setSize(1050, 600);

        // Ocultar botones de control moderno y permitir cierre con ESC
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        setClosable(false);
        setResizable(false);
        setIconifiable(false);
        setMaximizable(false);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        // Panel principal con diseño de bordes y márgenes limpios
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ---------------------------------------------------------
        // 1. PANEL SUPERIOR: Filtros y Botones
        // ---------------------------------------------------------
        panelFiltros = new JPanel(new GridBagLayout());
        panelFiltros.setBackground(cFondo);
        panelFiltros.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2),
                " CRITERIOS DE BUSQUEDA ",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.TOP,
                FUENTE_CONSOLA,
                cBordeAcento
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 0: Cliente
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblCliente = new JLabel("Run: Cliente :");
        lblCliente.setFont(FUENTE_CONSOLA);
        lblCliente.setForeground(cTextoBlanco);
        panelFiltros.add(lblCliente, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCliente = new JTextField();
        estilarCampoTexto(txtCliente, 340);
        panelFiltros.add(txtCliente, gbc);

        int anioActual = LocalDate.now().getYear();
        LocalDate fechaInicioPorDefecto = LocalDate.of(anioActual, 1, 1);
        LocalDate fechaFinPorDefecto = LocalDate.of(anioActual, 12, 31);

        // Fila 1: Fechas
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblFecIni = new JLabel("Fecha Inicial   :");
        lblFecIni.setFont(FUENTE_CONSOLA);
        lblFecIni.setForeground(cTextoBlanco);
        panelFiltros.add(lblFecIni, gbc);

        gbc.gridx = 1;
        dcFechaInicio = crearDateChooserConsola(fechaInicioPorDefecto, 145);
        panelFiltros.add(dcFechaInicio, gbc);

        gbc.gridx = 2;
        JLabel lblFecFin = new JLabel(" Fecha Final   :");
        lblFecFin.setFont(FUENTE_CONSOLA);
        lblFecFin.setForeground(cTextoBlanco);
        panelFiltros.add(lblFecFin, gbc);

        gbc.gridx = 3;
        dcFechaFin = crearDateChooserConsola(fechaFinPorDefecto, 145);
        panelFiltros.add(dcFechaFin, gbc);

        dcFechaInicio.addPropertyChangeListener("date", evt -> estilizarDateChooser(dcFechaInicio));
        dcFechaFin.addPropertyChangeListener("date", evt -> estilizarDateChooser(dcFechaFin));

        // Panel de Botones (Consultar y Gráfico)
        panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBotones.setBackground(cFondo);

        btnBuscar = new JButton("[ CONSULTAR ]");
        estilizarBoton(btnBuscar, new Color(192, 192, 192), Color.BLACK);
        btnBuscar.addActionListener(e -> buscarDocumentos());

        btnGrafico = new JButton("Gráfico [F3]");
        estilizarBoton(btnGrafico, new Color(0, 120, 120), Color.WHITE);
        btnGrafico.addActionListener(e -> abrirVentanaGrafico());

        panelBotones.add(btnBuscar);
        panelBotones.add(btnGrafico);

        gbc.gridx = 4;
        gbc.insets = new Insets(6, 15, 6, 10);
        panelFiltros.add(panelBotones, gbc);

        panelPrincipal.add(panelFiltros, BorderLayout.NORTH);

        // ---------------------------------------------------------
        // 2. PANEL CENTRAL: Tabla de Resultados
        // ---------------------------------------------------------
        String[] columnas = {"Folio", "Fecha", "Cliente/RUT", "Monto"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResultados = new JTable(modeloTabla);
        estilarTabla(tablaResultados);

        scrollTabla = new JScrollPane(tablaResultados);
        scrollTabla.getViewport().setBackground(cTablaFondo);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2),
                " LISTADO DE DOCUMENTOS EN CARTERA ",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.TOP,
                FUENTE_CONSOLA,
                cBordeAcento
        ));

        panelPrincipal.add(scrollTabla, BorderLayout.CENTER);

        // ---------------------------------------------------------
        // 3. PANEL INFERIOR: Barra de Ayuda Teclado
        // ---------------------------------------------------------
        panelAyudaContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        panelAyudaContainer.setBackground(cBarraAyuda);

        lblAyuda = new JLabel();
        lblAyuda.setFont(new Font("Courier New", Font.BOLD, 12));
        lblAyuda.setForeground(cBordeAcento);
        actualizarTextoAyuda();
        panelAyudaContainer.add(lblAyuda);

        panelPrincipal.add(panelAyudaContainer, BorderLayout.SOUTH);

        add(panelPrincipal);
        atajoTeclado();

        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono interno", e);
        }

        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
            estilizarDateChooser(dcFechaInicio);
            estilizarDateChooser(dcFechaFin);
        });
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cPanelSup = new Color(45, 10, 55);
                cTablaFondo = new Color(15, 10, 20);
                cBordeAcento = new Color(255, 110, 220);
                cTextoVerde = new Color(255, 180, 240);
                cTextoBlanco = new Color(255, 230, 255);
                cBarraAyuda = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelSup = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cBordeAcento = new Color(0, 245, 255);
                cTextoVerde = new Color(180, 255, 255);
                cTextoBlanco = new Color(220, 255, 255);
                cBarraAyuda = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelSup = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cBordeAcento = new Color(0, 255, 128);
                cTextoVerde = new Color(180, 255, 200);
                cTextoBlanco = new Color(220, 255, 240);
                cBarraAyuda = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelSup = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cBordeAcento = new Color(245, 73, 39);
                cTextoVerde = new Color(255, 200, 150);
                cTextoBlanco = new Color(255, 235, 225);
                cBarraAyuda = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 132);
                cPanelSup = new Color(0, 0, 80);
                cTablaFondo = Color.BLACK;
                cBordeAcento = Color.CYAN;
                cTextoVerde = Color.GREEN;
                cTextoBlanco = Color.WHITE;
                cBarraAyuda = new Color(0, 0, 80);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
        }

        if (panelFiltros != null) {
            panelFiltros.setBackground(cFondo);
            panelFiltros.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2),
                    " CRITERIOS DE BUSQUEDA ",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    FUENTE_CONSOLA,
                    cBordeAcento
            ));
            for (Component comp : panelFiltros.getComponents()) {
                if (comp instanceof JLabel jLabel) {
                    jLabel.setForeground(cTextoBlanco);
                }
            }
        }

        if (panelBotones != null) {
            panelBotones.setBackground(cFondo);
        }

        if (txtCliente != null) {
            int anchoActual = txtCliente.getWidth() > 0 ? txtCliente.getWidth() : 340;
            estilarCampoTexto(txtCliente, anchoActual);
        }
        estilizarDateChooser(dcFechaInicio);
        estilizarDateChooser(dcFechaFin);

        if (tablaResultados != null) {
            estilarTabla(tablaResultados);
            tablaResultados.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cTablaFondo);
            scrollTabla.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2),
                    " LISTADO DE DOCUMENTOS EN CARTERA ",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    FUENTE_CONSOLA,
                    cBordeAcento
            ));
        }

        if (panelAyudaContainer != null) {
            panelAyudaContainer.setBackground(cBarraAyuda);
        }
        actualizarTextoAyuda();

        revalidate();
        repaint();
    }

    private JDateChooser crearDateChooserConsola(LocalDate fechaInicial, int anchoPx) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("dd/MM/yyyy");
        chooser.setFont(FUENTE_CONSOLA);
        chooser.setPreferredSize(new Dimension(anchoPx, 26));
        if (fechaInicial != null) {
            chooser.setDate(Date.from(fechaInicial.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        estilizarDateChooser(chooser);
        return chooser;
    }

    private void estilizarDateChooser(JDateChooser chooser) {
        if (chooser == null) {
            return;
        }
        chooser.setBackground(cFondo);
        chooser.setForeground(Color.WHITE);
        chooser.setFont(FUENTE_CONSOLA);

        Component editor = chooser.getDateEditor().getUiComponent();
        if (editor instanceof JComponent jcomp) {
            jcomp.setBackground(cTablaFondo);
            jcomp.setForeground(Color.WHITE);
            jcomp.setFont(FUENTE_CONSOLA);
            jcomp.setBorder(new LineBorder(cBordeAcento, 1));
        }
        if (editor instanceof JTextField tf) {
            tf.setBackground(cTablaFondo);
            tf.setForeground(Color.WHITE);
            tf.setDisabledTextColor(Color.WHITE);
            tf.setCaretColor(cTextoVerde);
            tf.repaint();
        }
    }

    private LocalDate obtenerLocalDate(JDateChooser chooser) {
        if (chooser == null || chooser.getDate() == null) {
            return null;
        }
        return Instant.ofEpochMilli(chooser.getDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void actualizarTextoAyuda() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAyuda != null) {
            lblAyuda.setText(" [ESC] Salir    [ENTER] Seleccionar    [F2] Foco RUT    [F8] Cliente    [F6] Tema: " + temaActual + "    [F3] Gráfico ");
        }
    }

    private void estilarCampoTexto(JTextField txt, int ancho) {
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(cTablaFondo);
        txt.setForeground(cTextoVerde);
        txt.setCaretColor(cTextoVerde);
        txt.setBorder(new LineBorder(cBordeAcento, 1));
        txt.setPreferredSize(new Dimension(ancho, 26));
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        if (btn == null) {
            return;
        }
        btn.setFont(FUENTE_CONSOLA);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(cBordeAcento, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void estilarTabla(JTable tabla) {
        tabla.setFont(FUENTE_CONSOLA);
        tabla.setBackground(cTablaFondo);
        tabla.setForeground(cTextoVerde);
        tabla.setGridColor(cBordeAcento);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setFont(FUENTE_CONSOLA);
        tabla.getTableHeader().setBackground(cPanelSup);
        tabla.getTableHeader().setForeground(cTextoBlanco);
    }

    private void buscarDocumentos() {
        LocalDate dIni = obtenerLocalDate(dcFechaInicio);
        LocalDate dFin = obtenerLocalDate(dcFechaFin);
        if (dIni != null && dFin != null && dIni.isAfter(dFin)) {
            JOptionPane.showMessageDialog(this, "La Fecha Inicial no puede ser posterior a la Fecha Final.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cliente = txtCliente != null ? txtCliente.getText().trim() : "";
        String fecIni = dIni != null ? dIni.format(FORMATO_FECHA) : "";
        String fecFin = dFin != null ? dFin.format(FORMATO_FECHA) : "";

        modeloTabla.setRowCount(0);

        try (Connection con = Conexion.getConnection()) {
            listaUltimaConsulta = dao.listarDocumentosCarteraNormalizado(con, cliente, fecIni, fecFin);
            for (DocumentoCarteraDTO d : listaUltimaConsulta) {
                modeloTabla.addRow(new Object[]{
                    d.getFolio() != null ? d.getFolio() : "",
                    d.getFecha() != null ? d.getFecha() : "",
                    d.getCliente() != null ? d.getCliente() : "",
                    String.format("$ %,.0f", d.getMonto())
                });
            }

            if (listaUltimaConsulta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron documentos en el rango indicado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar documentos en cartera", ex);
            JOptionPane.showMessageDialog(this, "No se pudo consultar la base de datos. Intente nuevamente.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // 4. GENERACIÓN DE GRÁFICO (TOP 10 HORIZONTAL)
    // ==========================================
    private void abrirVentanaGrafico() {
        if (listaUltimaConsulta == null || listaUltimaConsulta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe realizar una consulta con datos antes de ver el gráfico.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<DocumentoCarteraDTO> listaOrdenada = new ArrayList<>(listaUltimaConsulta);
        listaOrdenada.sort((a, b) -> Double.compare(b.getMonto(), a.getMonto()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int limite = Math.min(10, listaOrdenada.size());

        for (int i = limite - 1; i >= 0; i--) {
            DocumentoCarteraDTO d = listaOrdenada.get(i);
            String folioStr = d.getFolio() != null ? d.getFolio() : "";
            String clienteStr = d.getCliente() != null ? d.getCliente() : "";
            String etiqueta = folioStr + " - " + clienteStr;
            if (etiqueta.length() > 24) {
                etiqueta = etiqueta.substring(0, 24) + "..";
            }
            dataset.addValue(d.getMonto(), "Monto ($)", etiqueta);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "TOP " + limite + " - CARTERA DE DOCUMENTOS ($)",
                "Documento / Cliente",
                "Monto ($)",
                dataset,
                org.jfree.chart.plot.PlotOrientation.HORIZONTAL,
                true, true, false
        );

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(cTablaFondo);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, cTextoVerde);

        barChart.setBackgroundPaint(cFondo);
        barChart.getTitle().setPaint(cBordeAcento);
        barChart.getTitle().setFont(FUENTE_CONSOLA);

        JDialog dlgGrafico = new JDialog(JOptionPane.getFrameForComponent(this), "Gráfico Ejecutivo - Cartera de Cheques", true);
        dlgGrafico.setSize(950, 600);
        dlgGrafico.setLocationRelativeTo(this);
        dlgGrafico.setUndecorated(true);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(cFondo);
        panelContenedor.setBorder(new LineBorder(cBordeAcento, 2));

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(cFondo);
        panelContenedor.add(chartPanel, BorderLayout.CENTER);

        JPanel panelSurDlg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSurDlg.setBackground(cBarraAyuda);
        JButton btnCerrarDlg = new JButton("Cerrar [Esc / Enter]");
        estilizarBoton(btnCerrarDlg, cFondo, cBordeAcento);
        btnCerrarDlg.addActionListener(e -> dlgGrafico.dispose());
        panelSurDlg.add(btnCerrarDlg);
        panelContenedor.add(panelSurDlg, BorderLayout.SOUTH);

        dlgGrafico.setContentPane(panelContenedor);

        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        dlgGrafico.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "CERRAR");
        dlgGrafico.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "CERRAR");
        dlgGrafico.getRootPane().getActionMap().put("CERRAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlgGrafico.dispose();
            }
        });

        dlgGrafico.setVisible(true);
    }

    private void atajoTeclado() {
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // ESC: Cerrar ventana interna
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        root.getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // F2: Foco al cliente
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2_BUSCAR");
        root.getActionMap().put("F2_BUSCAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtCliente.requestFocus();
                txtCliente.selectAll();
            }
        });

        // F3: Abrir Gráfico Ejecutivo
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "F3_GRAFICO");
        root.getActionMap().put("F3_GRAFICO", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaGrafico();
            }
        });

        // F6: Ciclar Temas
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "F6_CICLAR_TEMA");
        root.getActionMap().put("F6_CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        // ENTER en campo cliente
        txtCliente.addActionListener(e -> buscarDocumentos());

        // F8: Buscador de clientes
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "F8_BUSCAR_CLIENTE");
        root.getActionMap().put("F8_BUSCAR_CLIENTE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirBuscadorClientesF8();
            }
        });
    }

    private void abrirBuscadorClientesF8() {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        FrmBuscarCliente buscador = new FrmBuscarCliente(ventanaPadre);
        buscador.setVisible(true);

        String rutElegido = buscador.getRutSeleccionado();
        if (rutElegido != null && !rutElegido.isEmpty()) {
            txtCliente.setText(rutElegido);
            buscarDocumentos();
        }
    }
}
