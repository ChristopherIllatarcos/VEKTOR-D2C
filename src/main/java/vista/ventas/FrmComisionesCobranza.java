package vista.ventas;

import com.toedter.calendar.JDateChooser;
import dao.ComisionDAO;
import modelo.ComisionVendedorDTO;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FrmComisionesCobranza extends JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JDateChooser dcFechaInicio;
    private JDateChooser dcFechaFin;
    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private JLabel lblVentaTotalGlobal;
    private JLabel lblAtajos;
    private JButton btnConsultar;
    private JButton btnGrafico;
    private ComisionDAO comisionDAO;
    private List<ComisionVendedorDTO> listaUltimaConsulta = new ArrayList<>();

    // Componentes principales de la UI que cambiarán de color dinámicamente
    private JPanel panelPrincipal, panelNorte, panelComandos;
    private JScrollPane scrollPane;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelNorte, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 12);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmComisionesCobranza() {
        super("8.- COMISIONES X COBRANZA", true, false, false, false);
        this.comisionDAO = new ComisionDAO();

        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents();
        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
        configurarAtajosTeclado();

        setSize(1020, 620);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
            estilizarDateChooser(dcFechaInicio);
            estilizarDateChooser(dcFechaFin);
            dcFechaInicio.requestFocusInWindow();
        });
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondoNegro = new Color(10, 10, 15);
                cPanelNorte = new Color(45, 10, 55);
                cEncabezadoTabla = new Color(90, 10, 120);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cSeleccion = new Color(140, 20, 160);
                cBarraSur = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondoNegro = new Color(5, 15, 20);
                cPanelNorte = new Color(10, 40, 50);
                cEncabezadoTabla = new Color(0, 80, 95);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cSeleccion = new Color(0, 120, 140);
                cBarraSur = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondoNegro = new Color(5, 20, 15);
                cPanelNorte = new Color(10, 45, 30);
                cEncabezadoTabla = new Color(15, 80, 50);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cSeleccion = new Color(0, 140, 80);
                cBarraSur = new Color(0, 60, 35);
                break;
            case "Clipper Clásico":
                cFondoNegro = Color.BLACK;
                cPanelNorte = new Color(0, 0, 168);
                cEncabezadoTabla = new Color(0, 0, 168);
                cAcento = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cSeleccion = new Color(0, 168, 168);
                cBarraSur = new Color(168, 0, 0);
                break;
            case "Naranja Fuego":
            default:
                cFondoNegro = new Color(15, 10, 8);
                cPanelNorte = new Color(40, 18, 12);
                cEncabezadoTabla = new Color(80, 28, 18);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cSeleccion = new Color(130, 42, 22);
                cBarraSur = new Color(60, 20, 12);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        String nuevoTema = listaTemas[indiceTemaActual];
        aplicarTema(nuevoTema);

        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                new EmptyBorder(10, 15, 10, 15)
        ));

        lblVentaTotalGlobal.setForeground(cAcento);

        if (tablaReporte != null) {
            tablaReporte.setBackground(cFondoNegro);
            tablaReporte.setForeground(cTextoClaro);
            tablaReporte.setSelectionBackground(cSeleccion);
            tablaReporte.getTableHeader().setBackground(cEncabezadoTabla);
            tablaReporte.getTableHeader().setForeground(cAcento);
            tablaReporte.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
            tablaReporte.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondoNegro);
        }

        panelComandos.setBackground(cBarraSur);
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));

        estilizarDateChooser(dcFechaInicio);
        estilizarDateChooser(dcFechaFin);
        estilizarBotonConsola(btnConsultar, cEncabezadoTabla);
        estilizarBotonConsola(btnGrafico, cEncabezadoTabla);

        actualizarTextoAtajos();

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        lblAtajos.setText("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:#FFFFFF;'>Cerrar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(Enter)</b> <span style='color:#FFFFFF;'>Consultar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:#FFFFFF;'>Gráfico</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F5)</b> <span style='color:#00FFCC;'>Tema: " + temaActual + "</span></html>");
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
        Color fondoCampo = new Color(
                Math.max(0, cFondoNegro.getRed() + 10),
                Math.max(0, cFondoNegro.getGreen() + 10),
                Math.max(0, cFondoNegro.getBlue() + 10)
        );
        chooser.setBackground(cFondoNegro);
        chooser.setForeground(Color.WHITE);
        chooser.setFont(FUENTE_CONSOLA);

        Component editor = chooser.getDateEditor().getUiComponent();
        if (editor instanceof JComponent jcomp) {
            jcomp.setBackground(fondoCampo);
            jcomp.setForeground(Color.WHITE);
            jcomp.setFont(FUENTE_CONSOLA);
            jcomp.setBorder(BorderFactory.createLineBorder(cAcento));
        }
        if (editor instanceof JTextField tf) {
            tf.setBackground(fondoCampo);
            tf.setForeground(Color.WHITE);
            tf.setDisabledTextColor(Color.WHITE);
            tf.setCaretColor(cAcento);
            tf.repaint();
        }
    }

    private String obtenerTextoFecha(JDateChooser chooser) {
        if (chooser == null || chooser.getDate() == null) {
            return "";
        }
        LocalDate ld = Instant.ofEpochMilli(chooser.getDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return ld.format(FORMATO_FECHA);
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Filtros Limpios)
        // ==========================================
        panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelFiltros.setOpaque(false);

        JLabel lblTitulo = new JLabel("COBRANZA");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(cAcento);

        JLabel lblFI = new JLabel("DESDE:");
        lblFI.setFont(FUENTE_CONSOLA);
        lblFI.setForeground(cTextoClaro);

        LocalDate hoy = LocalDate.now();
        dcFechaInicio = crearDateChooserConsola(hoy, 145);

        JLabel lblFF = new JLabel("HASTA:");
        lblFF.setFont(FUENTE_CONSOLA);
        lblFF.setForeground(cTextoClaro);

        dcFechaFin = crearDateChooserConsola(hoy, 145);

        dcFechaInicio.addPropertyChangeListener("date", evt -> estilizarDateChooser(dcFechaInicio));
        dcFechaFin.addPropertyChangeListener("date", evt -> estilizarDateChooser(dcFechaFin));

        btnConsultar = new JButton("Consultar [Enter]");
        estilizarBotonConsola(btnConsultar, cEncabezadoTabla);
        btnConsultar.addActionListener(e -> cargarReporteCobranza());

        btnGrafico = new JButton("Ver Gráfico [F3]");
        estilizarBotonConsola(btnGrafico, cEncabezadoTabla);
        btnGrafico.addActionListener(e -> abrirVentanaGrafico());

        panelFiltros.add(lblTitulo);
        panelFiltros.add(lblFI);
        panelFiltros.add(dcFechaInicio);
        panelFiltros.add(lblFF);
        panelFiltros.add(dcFechaFin);
        panelFiltros.add(btnConsultar);
        panelFiltros.add(btnGrafico);

        panelNorte.add(panelFiltros, BorderLayout.WEST);

        lblVentaTotalGlobal = new JLabel("TOTAL: $ 0");
        lblVentaTotalGlobal.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblVentaTotalGlobal.setForeground(cAcento);
        panelNorte.add(lblVentaTotalGlobal, BorderLayout.EAST);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL CON RAYAS TRANSPARENTES
        // ==========================================
        String[] columnas = {"N° Ventas", "NOMBRE DEL VENDEDOR", "VENTA TOTAL", "COMISION", "PARTICIPACION"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaReporte = new JTable(modeloTabla);
        tablaReporte.setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaReporte.setRowHeight(22);
        tablaReporte.setBackground(cFondoNegro);
        tablaReporte.setForeground(cTextoClaro);
        tablaReporte.setSelectionBackground(cSeleccion);
        tablaReporte.setSelectionForeground(Color.WHITE);
        tablaReporte.setShowGrid(false);
        tablaReporte.setIntercellSpacing(new Dimension(0, 0));

        tablaReporte.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaReporte.getTableHeader().setBackground(cEncabezadoTabla);
        tablaReporte.getTableHeader().setForeground(cAcento);
        tablaReporte.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        DefaultTableCellRenderer rendererRayas = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(cFondoNegro);
                    } else {
                        c.setBackground(new Color(
                                Math.min(255, cFondoNegro.getRed() + 15),
                                Math.min(255, cFondoNegro.getGreen() + 15),
                                Math.min(255, cFondoNegro.getBlue() + 15)
                        ));
                    }
                    c.setForeground(cTextoClaro);
                } else {
                    c.setBackground(cSeleccion);
                    c.setForeground(Color.WHITE);
                }

                if (column == 2 || column == 3 || column == 4) {
                    setHorizontalAlignment(JLabel.RIGHT);
                } else if (column == 0) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tablaReporte.getColumnModel().getColumnCount(); i++) {
            tablaReporte.getColumnModel().getColumn(i).setCellRenderer(rendererRayas);
        }

        TableColumnModel colModel = tablaReporte.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(90);
        colModel.getColumn(1).setPreferredWidth(360);
        colModel.getColumn(2).setPreferredWidth(150);
        colModel.getColumn(3).setPreferredWidth(130);
        colModel.getColumn(4).setPreferredWidth(130);

        scrollPane = new JScrollPane(tablaReporte);
        scrollPane.getViewport().setBackground(cFondoNegro);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Barra de Comandos con F5)
        // ==========================================
        panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(cBarraSur);
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        actualizarTextoAtajos();
        panelComandos.add(lblAtajos);

        panelPrincipal.add(panelComandos, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void estilizarBotonConsola(JButton btn, Color bg) {
        btn.setFont(FUENTE_CONSOLA);
        btn.setBackground(bg);
        btn.setForeground(cTextoClaro);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CONSULTAR_ENTER");
        rootPane.getActionMap().put("CONSULTAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarReporteCobranza();
            }
        });

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "GRAFICO_F3");
        rootPane.getActionMap().put("GRAFICO_F3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaGrafico();
            }
        });

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "CAMBIAR_TEMA_F5");
        rootPane.getActionMap().put("CAMBIAR_TEMA_F5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }

    private void cargarReporteCobranza() {
        String fechaIni = obtenerTextoFecha(dcFechaInicio);
        String fechaFin = obtenerTextoFecha(dcFechaFin);

        if (fechaIni.isEmpty() || fechaFin.isEmpty()) {
            return;
        }

        modeloTabla.setRowCount(0);
        DecimalFormat dfDinero = new DecimalFormat("$ #,##0");
        DecimalFormat dfPorc = new DecimalFormat("#0.00'%'");

        try {
            listaUltimaConsulta = comisionDAO.obtenerCobranzasPorFecha(fechaIni, fechaFin);
            double sumaGlobalVentas = 0;
            for (ComisionVendedorDTO item : listaUltimaConsulta) {
                sumaGlobalVentas += item.getVentaTotal();
            }

            for (ComisionVendedorDTO item : listaUltimaConsulta) {
                double participacion = 0.0;
                if (sumaGlobalVentas > 0) {
                    participacion = (item.getVentaTotal() / sumaGlobalVentas) * 100.0;
                }
                item.setParticipacion(participacion);

                modeloTabla.addRow(new Object[]{
                    item.getNumeroVentas(),
                    item.getNombreVendedor(),
                    dfDinero.format(item.getVentaTotal()),
                    dfDinero.format(item.getComision()),
                    dfPorc.format(item.getParticipacion())
                });
            }

            lblVentaTotalGlobal.setText("TOTAL: " + dfDinero.format(sumaGlobalVentas));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void abrirVentanaGrafico() {
        if (listaUltimaConsulta == null || listaUltimaConsulta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe realizar una consulta con datos antes de ver el gráfico.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ComisionVendedorDTO item : listaUltimaConsulta) {
            String nombre = item.getNombreVendedor().trim();
            if (nombre.length() > 15) {
                nombre = nombre.substring(0, 15) + "..";
            }
            dataset.addValue(item.getVentaTotal(), "Venta Total", nombre);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "ESTADÍSTICA DE VENTAS POR VENDEDOR",
                "Vendedor",
                "Monto Total ($)",
                dataset
        );

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(cFondoNegro);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, cAcento);

        barChart.setBackgroundPaint(cFondoNegro);
        barChart.getTitle().setPaint(cAcento);
        barChart.getTitle().setFont(new Font("Monospaced", Font.BOLD, 15));

        JDialog dlgGrafico = new JDialog(JOptionPane.getFrameForComponent(this), "Gráfico Ejecutivo - Comisiones", true);
        dlgGrafico.setSize(850, 520);
        dlgGrafico.setLocationRelativeTo(this);
        dlgGrafico.setUndecorated(true);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(cFondoNegro);
        panelContenedor.setBorder(new LineBorder(cAcento, 2));

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(cFondoNegro);
        panelContenedor.add(chartPanel, BorderLayout.CENTER);

        JPanel panelSurDlg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSurDlg.setBackground(cBarraSur);
        JButton btnCerrarDlg = new JButton("Cerrar [Esc / Enter]");
        estilizarBotonConsola(btnCerrarDlg, cEncabezadoTabla);
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
}