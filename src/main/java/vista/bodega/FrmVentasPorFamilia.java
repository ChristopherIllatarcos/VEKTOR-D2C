package vista.bodega;

import dao.VentaFamiliaDAO;
import modelo.VentaFamiliaDTO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FrmVentasPorFamilia extends JInternalFrame {

    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private VentaFamiliaDAO ventaFamiliaDAO;
    private JLabel lblTotalGlobal;
    private JLabel lblAtajos;
    private JButton btnConsultar;
    private JButton btnGrafico;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelNorte, panelFiltros, panelComandos;
    private JScrollPane scrollPane;
    private JLabel lblTitulo, lblFI, lblFF;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelNorte, cTablaFondo, cBordeAcento, cTextoAmarillo, cTextoClaro, cBarraSur, cSeleccion;
    private final Font FUENTE_MONOSPACED = new Font("Monospaced", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Clásico por defecto

    // Lista en memoria para alimentar el gráfico ejecutivo con la última consulta
    private List<VentaFamiliaDTO> listaUltimaConsulta = new ArrayList<>();

    public FrmVentasPorFamilia() {
        super("8.- VENTAS POR FAMILIA", true, false, false, false);
        this.ventaFamiliaDAO = new VentaFamiliaDAO();

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents();
        configurarAtajosTeclado();

        setSize(950, 520);
        setVisible(true);
        // Asignar el icono de Nexum directamente a este JInternalFrame
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cPanelNorte = new Color(45, 10, 55);
                cTablaFondo = new Color(15, 10, 20);
                cBordeAcento = new Color(255, 110, 220);
                cTextoAmarillo = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cBarraSur = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelNorte = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cBordeAcento = new Color(0, 245, 255);
                cTextoAmarillo = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraSur = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelNorte = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cBordeAcento = new Color(0, 255, 128);
                cTextoAmarillo = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cBarraSur = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelNorte = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cBordeAcento = new Color(245, 73, 39);
                cTextoAmarillo = new Color(255, 180, 100);
                cTextoClaro = new Color(255, 235, 225);
                cBarraSur = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Clipper Clásico":
            default:
                cFondo = new Color(0, 0, 168);
                cPanelNorte = new Color(0, 0, 168);
                cTablaFondo = Color.BLACK;
                cBordeAcento = new Color(85, 255, 255);
                cTextoAmarillo = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cBarraSur = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
            panelPrincipal.setBorder(BorderFactory.createLineBorder(cBordeAcento, 2));
        }

        if (panelNorte != null) {
            panelNorte.setBackground(cPanelNorte);
            panelNorte.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, cBordeAcento),
                    new EmptyBorder(12, 15, 12, 15)
            ));
        }

        if (panelFiltros != null) {
            panelFiltros.setBackground(cPanelNorte);
        }
        if (lblTitulo != null) {
            lblTitulo.setForeground(cTextoAmarillo);
        }
        if (lblFI != null) {
            lblFI.setForeground(cBordeAcento);
        }
        if (lblFF != null) {
            lblFF.setForeground(cBordeAcento);
        }

        if (txtFechaInicio != null) {
            txtFechaInicio.setBackground(cTextoAmarillo);
            txtFechaInicio.setBorder(BorderFactory.createLineBorder(cBordeAcento));
        }
        if (txtFechaFin != null) {
            txtFechaFin.setBackground(cTextoAmarillo);
            txtFechaFin.setBorder(BorderFactory.createLineBorder(cBordeAcento));
        }

        if (btnConsultar != null) {
            estilizarBotonConsola(btnConsultar, cBarraSur);
        }
        if (btnGrafico != null) {
            estilizarBotonConsola(btnGrafico, new Color(Math.max(0, cBarraSur.getRed() - 40), Math.max(0, cBarraSur.getGreen() - 40), Math.max(0, cBarraSur.getBlue() - 40)));
        }

        if (lblTotalGlobal != null) {
            lblTotalGlobal.setForeground(cTextoAmarillo);
        }

        if (tablaReporte != null) {
            tablaReporte.setBackground(cTablaFondo);
            tablaReporte.setForeground(cTextoAmarillo);
            tablaReporte.setSelectionBackground(cSeleccion);
            tablaReporte.getTableHeader().setBackground(cFondo);
            tablaReporte.getTableHeader().setForeground(cTextoAmarillo);
            tablaReporte.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cBordeAcento));

            // Actualizar renderizadores de celdas
            DefaultTableCellRenderer centerRenderer = crearRendererCelda(JLabel.CENTER);
            DefaultTableCellRenderer leftRenderer = crearRendererCelda(JLabel.LEFT);
            DefaultTableCellRenderer rightRenderer = crearRendererCelda(JLabel.RIGHT);

            tablaReporte.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            tablaReporte.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
            tablaReporte.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            tablaReporte.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
            tablaReporte.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cTablaFondo);
        }

        if (panelComandos != null) {
            panelComandos.setBackground(cBarraSur);
            panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cTextoAmarillo));
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private DefaultTableCellRenderer crearRendererCelda(int alineacion) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(alineacion);
        renderer.setBackground(cTablaFondo);
        renderer.setForeground(cTextoAmarillo);
        return renderer;
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("<html><b style='color:" + colorToHex(cTextoAmarillo) + ";'>(Esc)</b> <span style='color:FFFFFF;'>Salir</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:" + colorToHex(cTextoAmarillo) + ";'>(Enter)</b> <span style='color:FFFFFF;'>Consultar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:" + colorToHex(cTextoAmarillo) + ";'>(F6)</b> <span style='color:FFFFFF;'>Tema: " + temaActual + "</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:" + colorToHex(cTextoAmarillo) + ";'>(F3)</b> <span style='color:FFFFFF;'>Gráfico</span></html>");
        }
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cBordeAcento, 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Fechas, Botones y Título)
        // ==========================================
        panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cBordeAcento),
                new EmptyBorder(12, 15, 12, 15)
        ));

        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelFiltros.setOpaque(false);

        lblTitulo = new JLabel("VENTAS POR FAMILIA");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTitulo.setForeground(cTextoAmarillo);

        lblFI = new JLabel("DESDE:");
        lblFI.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFI.setForeground(cBordeAcento);

        java.time.LocalDate fechaActualSys = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatoDdf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaHoyStr = fechaActualSys.format(formatoDdf);

        txtFechaInicio = new JTextField(fechaHoyStr, 12);
        estilizarCampoTextoAmarillo(txtFechaInicio);

        lblFF = new JLabel("HASTA:");
        lblFF.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFF.setForeground(cBordeAcento);

        txtFechaFin = new JTextField(fechaHoyStr, 12);
        estilizarCampoTextoAmarillo(txtFechaFin);

        btnConsultar = new JButton("Consultar [Enter]");
        estilizarBotonConsola(btnConsultar, cBarraSur);
        btnConsultar.addActionListener(e -> ejecutarConsulta());

        btnGrafico = new JButton("Ver Gráfico [F3]");
        estilizarBotonConsola(btnGrafico, new Color(0, 120, 120));
        btnGrafico.addActionListener(e -> abrirVentanaGrafico());

        panelFiltros.add(lblTitulo);
        panelFiltros.add(lblFI);
        panelFiltros.add(txtFechaInicio);
        panelFiltros.add(lblFF);
        panelFiltros.add(txtFechaFin);
        panelFiltros.add(btnConsultar);
        panelFiltros.add(btnGrafico);

        panelNorte.add(panelFiltros, BorderLayout.WEST);

        lblTotalGlobal = new JLabel("VENTA TOTAL: $ 0");
        lblTotalGlobal.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTotalGlobal.setForeground(cTextoAmarillo);
        panelNorte.add(lblTotalGlobal, BorderLayout.EAST);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL
        // ==========================================
        String[] columnas = {"CÓDIGO", "NOMBRE DE LA FAMILIA", "N° VENTAS", "VENTA TOTAL"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaReporte = new JTable(modeloTabla);
        tablaReporte.setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaReporte.setRowHeight(22);
        tablaReporte.setBackground(cTablaFondo);
        tablaReporte.setForeground(cTextoAmarillo);
        tablaReporte.setSelectionBackground(cSeleccion);
        tablaReporte.setSelectionForeground(Color.WHITE);
        tablaReporte.setShowGrid(false);

        tablaReporte.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaReporte.getTableHeader().setBackground(cFondo);
        tablaReporte.getTableHeader().setForeground(cTextoAmarillo);
        tablaReporte.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cBordeAcento));

        DefaultTableCellRenderer centerRenderer = crearRendererCelda(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = crearRendererCelda(JLabel.RIGHT);
        DefaultTableCellRenderer leftRenderer = crearRendererCelda(JLabel.LEFT);

        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaReporte.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        TableColumnModel colModel = tablaReporte.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(90);
        colModel.getColumn(1).setPreferredWidth(350);
        colModel.getColumn(2).setPreferredWidth(120);
        colModel.getColumn(3).setPreferredWidth(180);

        scrollPane = new JScrollPane(tablaReporte);
        scrollPane.getViewport().setBackground(cTablaFondo);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. BARRA INFERIOR (Atajos)
        // ==========================================
        panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(cBarraSur);
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cTextoAmarillo));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_MONOSPACED);
        actualizarTextoAtajos();
        panelComandos.add(lblAtajos);

        panelPrincipal.add(panelComandos, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void estilizarCampoTextoAmarillo(JTextField txt) {
        txt.setFont(new Font("Monospaced", Font.BOLD, 13));
        txt.setBackground(Color.YELLOW);
        txt.setForeground(Color.BLACK);
        txt.setCaretColor(Color.BLACK);
        txt.setBorder(BorderFactory.createLineBorder(cBordeAcento));
    }

    private void estilizarBotonConsola(JButton btn, Color bg) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(cBordeAcento, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void ejecutarConsulta() {
        String fechaIni = txtFechaInicio.getText().trim();
        String fechaFin = txtFechaFin.getText().trim();

        if (fechaIni.isEmpty() || fechaFin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar ambas fechas.", "VEKTOR D2C", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        DecimalFormat dfDinero = new DecimalFormat("$ #,##0");

        try {
            listaUltimaConsulta = ventaFamiliaDAO.obtenerVentasPorFamilia(fechaIni, fechaFin);
            double sumaGlobal = 0;

            for (VentaFamiliaDTO item : listaUltimaConsulta) {
                sumaGlobal += item.getVentaTotal();
                modeloTabla.addRow(new Object[]{
                    item.getCodigoFamilia(),
                    item.getNombreFamilia(),
                    item.getCantidadVentas(),
                    dfDinero.format(item.getVentaTotal())
                });
            }

            lblTotalGlobal.setText("VENTA TOTAL: " + dfDinero.format(sumaGlobal));

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al consultar las ventas por familia.", "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // 4. GENERACIÓN DE GRÁFICO (BARRAS HORIZONTALES)
    // ==========================================
    private void abrirVentanaGrafico() {
        if (listaUltimaConsulta == null || listaUltimaConsulta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe realizar una consulta con datos antes de ver el gráfico.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<VentaFamiliaDTO> listaOrdenada = new ArrayList<>(listaUltimaConsulta);
        listaOrdenada.sort((a, b) -> Double.compare(b.getVentaTotal(), a.getVentaTotal()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int limite = Math.min(10, listaOrdenada.size());

        for (int i = limite - 1; i >= 0; i--) {
            VentaFamiliaDTO item = listaOrdenada.get(i);
            String nombreFam = item.getNombreFamilia();
            if (nombreFam.length() > 22) {
                nombreFam = nombreFam.substring(0, 22) + "..";
            }
            dataset.addValue(item.getVentaTotal(), "Venta Total ($)", nombreFam);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "TOP " + limite + " - VENTAS POR FAMILIA ($)",
                "Familia",
                "Venta Total ($)",
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
        renderer.setSeriesPaint(0, cTextoAmarillo);

        barChart.setBackgroundPaint(cFondo);
        barChart.getTitle().setPaint(Color.YELLOW);
        barChart.getTitle().setFont(new Font("Monospaced", Font.BOLD, 15));

        JDialog dlgGrafico = new JDialog(JOptionPane.getFrameForComponent(this), "Gráfico Ejecutivo - Ventas por Familia", true);
        dlgGrafico.setSize(900, 600);
        dlgGrafico.setLocationRelativeTo(this);
        dlgGrafico.setUndecorated(true);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(cFondo);
        panelContenedor.setBorder(new LineBorder(cBordeAcento, 2));

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(cFondo);
        panelContenedor.add(chartPanel, BorderLayout.CENTER);

        JPanel panelSurDlg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSurDlg.setBackground(cBarraSur);
        JButton btnCerrarDlg = new JButton("Cerrar [Esc / Enter]");
        estilizarBotonConsola(btnCerrarDlg, cFondo);
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

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(cond).put(strokeEsc, "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(cond).put(strokeEnter, "CONSULTAR_ENTER");
        rootPane.getActionMap().put("CONSULTAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarConsulta();
            }
        });

        // [F6] Ciclar Tema en tiempo real
        KeyStroke strokeF6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        rootPane.getInputMap(cond).put(strokeF6, "CICLAR_TEMA");
        rootPane.getActionMap().put("CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        KeyStroke strokeF3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        rootPane.getInputMap(cond).put(strokeF3, "GRAFICO_F3");
        rootPane.getActionMap().put("GRAFICO_F3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaGrafico();
            }
        });
    }
}
