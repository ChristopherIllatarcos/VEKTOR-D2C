package vista.bodega;

import conexion.Conexion;
import dao.ParametrosDAO;
import dao.RotacionVentasDAO;
import modelo.ParametrosDTO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FrmRotacionVentas extends JInternalFrame {

    private JTextField txtFechaInicial;
    private JTextField txtFechaFinal;
    private JTextField txtBuscarProducto;

    private JTable tablaRotacion;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalUnidades;
    private JLabel lblTotalMonto;
    private JLabel lblAtajos;
    private JButton btnConsultar;
    private JButton btnGrafico;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelSuperior, panelInferiorContenedor, panelTotales, panelAtajosContainer;
    private JScrollPane scrollTabla;

    // Lista en memoria para alimentar el gráfico ejecutivo con la última consulta
    private List<Object[]> listaUltimaConsulta = new ArrayList<>();

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTabla, cZebra1, cZebra2, cBordeAmbar, cTextoCrema, cBarraInf, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Turquesa Pro", "Esmeralda Terminal","Terracota Cobre" , "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Terracota Cobre por defecto

    public FrmRotacionVentas() {
        super("7.- ROTACIÓN DE MERCADERÍA POR VENTAS", false, false, false, false);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        cargarFechaDesdeParametros();
        ejecutarBusquedaInicial();
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
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTabla = new Color(5, 20, 25);
                cZebra1 = new Color(5, 20, 25);
                cZebra2 = new Color(10, 35, 45);
                cBordeAmbar = new Color(0, 245, 255);
                cTextoCrema = new Color(220, 255, 255);
                cBarraInf = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelTabla = new Color(5, 25, 18);
                cZebra1 = new Color(5, 25, 18);
                cZebra2 = new Color(12, 45, 30);
                cBordeAmbar = new Color(0, 255, 128);
                cTextoCrema = new Color(220, 255, 240);
                cBarraInf = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Clipper Clásico":
                cFondo = Color.BLACK;
                cPanelTabla = new Color(0, 0, 120);
                cZebra1 = new Color(0, 0, 120);
                cZebra2 = new Color(0, 0, 168);
                cBordeAmbar = new Color(255, 255, 85);
                cTextoCrema = Color.WHITE;
                cBarraInf = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelTabla = new Color(25, 12, 10);
                cZebra1 = new Color(25, 12, 10);
                cZebra2 = new Color(50, 22, 15);
                cBordeAmbar = new Color(245, 73, 39);
                cTextoCrema = new Color(255, 235, 225);
                cBarraInf = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Terracota Cobre":
            default:
                cFondo = new Color(61, 28, 2);
                cPanelTabla = new Color(26, 14, 5);
                cZebra1 = new Color(26, 14, 5);
                cZebra2 = new Color(36, 20, 8);
                cBordeAmbar = new Color(255, 170, 51);
                cTextoCrema = new Color(255, 240, 204);
                cBarraInf = new Color(115, 50, 5);
                cSeleccion = new Color(140, 60, 10);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        String nuevoTema = listaTemas[indiceTemaActual];
        aplicarTema(nuevoTema);

        getContentPane().setBackground(cFondo);

        if (panelSuperior != null) {
            panelSuperior.setBackground(cFondo);
            panelSuperior.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAmbar, 2),
                    " ROTACIÓN DE MERCADERÍA - FILTROS DE CONSULTA ",
                    0, 0, FUENTE_CONSOLA, cBordeAmbar
            ));
        }

        if (tablaRotacion != null) {
            tablaRotacion.setBackground(cPanelTabla);
            tablaRotacion.setForeground(cTextoCrema);
            tablaRotacion.setGridColor(cBordeAmbar);
            tablaRotacion.setSelectionBackground(cBarraInf);
            tablaRotacion.setSelectionForeground(Color.YELLOW);
            tablaRotacion.getTableHeader().setBackground(cFondo);
            tablaRotacion.getTableHeader().setForeground(cBordeAmbar);
            tablaRotacion.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cPanelTabla);
            scrollTabla.setBorder(new LineBorder(cBordeAmbar, 1));
        }

        if (panelInferiorContenedor != null) {
            panelInferiorContenedor.setBackground(cFondo);
        }

        if (panelTotales != null) {
            panelTotales.setBackground(cFondo);
            panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeAmbar));
        }

        if (lblTotalMonto != null) {
            lblTotalMonto.setForeground(cBordeAmbar);
        }

        if (panelAtajosContainer != null) {
            panelAtajosContainer.setBackground(cBarraInf);
            panelAtajosContainer.setBorder(new LineBorder(cBordeAmbar, 1));
        }

        estilizarCampoDinamico(txtFechaInicial);
        estilizarCampoDinamico(txtFechaFinal);
        estilizarCampoDinamico(txtBuscarProducto);

        if (btnConsultar != null) {
            estilizarBotonConsola(btnConsultar, cBarraInf);
        }
        if (btnGrafico != null) {
            estilizarBotonConsola(btnGrafico, new Color(Math.min(255, cBarraInf.getRed() + 65), Math.max(0, cBarraInf.getGreen() + 20), Math.max(0, cBarraInf.getBlue())));
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void estilizarCampoDinamico(JTextField txt) {
        if (txt != null) {
            txt.setBackground(cPanelTabla);
            txt.setForeground(cBordeAmbar);
            txt.setCaretColor(cBordeAmbar);
            txt.setBorder(new LineBorder(cBordeAmbar, 1));
        }
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("(Esc) Salir    |    (Enter) Buscar    |    (F5) Ver Todo    |    (F6) Tema: " + temaActual + "    |    (F3) Gráfico");
        }
    }

    private void initComponentsManual() {
        setSize(1150, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(cFondo);
        setLayout(new BorderLayout(0, 6));

        // ==========================================
        // 1. PANEL SUPERIOR (Filtros y Botón Gráfico)
        // ==========================================
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(cFondo);
        panelSuperior.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAmbar, 2),
                " ROTACIÓN DE MERCADERÍA - FILTROS DE CONSULTA ",
                0, 0, FUENTE_CONSOLA, cBordeAmbar
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelCampos.setOpaque(false);

        txtFechaInicial = crearCampoTexto(12);
        txtFechaFinal = crearCampoTexto(12);
        txtBuscarProducto = crearCampoTexto(18);

        btnConsultar = new JButton("Consultar [Enter]");
        estilizarBotonConsola(btnConsultar, cBarraInf);
        btnConsultar.addActionListener(e -> ejecutarBusqueda());

        btnGrafico = new JButton("Ver Gráfico [F3]");
        estilizarBotonConsola(btnGrafico, new Color(180, 90, 0));
        btnGrafico.addActionListener(e -> abrirVentanaGrafico());

        panelCampos.add(crearEtiqueta("F.Ini:"));
        panelCampos.add(txtFechaInicial);
        panelCampos.add(crearEtiqueta("F.Fin:"));
        panelCampos.add(txtFechaFinal);
        panelCampos.add(crearEtiqueta("Barra/Prod:"));
        panelCampos.add(txtBuscarProducto);
        panelCampos.add(btnConsultar);
        panelCampos.add(btnGrafico);

        txtFechaInicial.addActionListener(e -> {
            ejecutarBusqueda();
            txtFechaFinal.requestFocusInWindow();
            txtFechaFinal.selectAll();
        });
        txtFechaFinal.addActionListener(e -> {
            ejecutarBusqueda();
            txtBuscarProducto.requestFocusInWindow();
            txtBuscarProducto.selectAll();
        });
        txtBuscarProducto.addActionListener(e -> ejecutarBusqueda());

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla de Resultados Cebra)
        // ==========================================
        String[] columnas = {"# Barra", "Fam.", "Fecha", "Descripción del Producto", "Costo", "Rotac. Cant.", "Rotac. Total ($)", "Media ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRotacion = new JTable(modeloTabla);
        tablaRotacion.setFont(FUENTE_CONSOLA);
        tablaRotacion.setBackground(cPanelTabla);
        tablaRotacion.setForeground(cTextoCrema);
        tablaRotacion.setGridColor(cBordeAmbar);
        tablaRotacion.setRowHeight(28);
        tablaRotacion.setSelectionBackground(cBarraInf);
        tablaRotacion.setSelectionForeground(Color.YELLOW);

        tablaRotacion.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaRotacion.getTableHeader().setBackground(cFondo);
        tablaRotacion.getTableHeader().setForeground(cBordeAmbar);

        DefaultTableCellRenderer renderizadorCebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(cBarraInf);
                    c.setForeground(Color.YELLOW);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(cZebra1);
                    } else {
                        c.setBackground(cZebra2);
                    }
                    c.setForeground(cTextoCrema);
                }

                if (column == 1 || column >= 4) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tablaRotacion.getColumnCount(); i++) {
            tablaRotacion.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaRotacion.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaRotacion.getColumnModel().getColumn(1).setPreferredWidth(55);
        tablaRotacion.getColumnModel().getColumn(2).setPreferredWidth(105);
        tablaRotacion.getColumnModel().getColumn(3).setPreferredWidth(300);
        tablaRotacion.getColumnModel().getColumn(4).setPreferredWidth(95);
        tablaRotacion.getColumnModel().getColumn(5).setPreferredWidth(100);
        tablaRotacion.getColumnModel().getColumn(6).setPreferredWidth(130);
        tablaRotacion.getColumnModel().getColumn(7).setPreferredWidth(100);

        scrollTabla = new JScrollPane(tablaRotacion);
        scrollTabla.getViewport().setBackground(cPanelTabla);
        scrollTabla.setBorder(new LineBorder(cBordeAmbar, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Barra Atajos)
        // ==========================================
        panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(cFondo);

        panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(cFondo);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeAmbar));

        lblTotalUnidades = crearEtiqueta("UNIDADES ROTADAS: 0");
        lblTotalMonto = new JLabel("TOTAL ROTACIÓN: $0");
        lblTotalMonto.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalMonto.setForeground(cBordeAmbar);

        panelTotales.add(lblTotalUnidades);
        panelTotales.add(lblTotalMonto);

        panelAtajosContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajosContainer.setBackground(cBarraInf);
        panelAtajosContainer.setBorder(new LineBorder(cBordeAmbar, 1));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(cTextoCrema);
        actualizarTextoAtajos();
        panelAtajosContainer.add(lblAtajos);

        panelInferiorContenedor.add(panelTotales, BorderLayout.NORTH);
        panelInferiorContenedor.add(panelAtajosContainer, BorderLayout.SOUTH);
        add(panelInferiorContenedor, BorderLayout.SOUTH);

        configurarAtajos();
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(cTextoCrema);
        return lbl;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(cPanelTabla);
        txt.setForeground(cBordeAmbar);
        txt.setCaretColor(cBordeAmbar);
        txt.setBorder(new LineBorder(cBordeAmbar, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(Math.min(255, cPanelTabla.getRed() + 30), Math.min(255, cPanelTabla.getGreen() + 20), Math.min(255, cPanelTabla.getBlue() + 15)));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(cPanelTabla);
                txt.setBorder(new LineBorder(cBordeAmbar, 1));
            }
        });

        return txt;
    }

    private void estilizarBotonConsola(JButton btn, Color bg) {
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(cBordeAmbar, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void cargarFechaDesdeParametros() {
        String fechaDefecto = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection con = Conexion.getConnection()) {
            ParametrosDAO pDao = new ParametrosDAO();
            ParametrosDTO param = pDao.obtenerParametros(con);
            if (param.getFecha() != null && !param.getFecha().trim().isEmpty()) {
                fechaDefecto = param.getFecha().trim();
            }
        } catch (Exception ignored) {
        }
        txtFechaInicial.setText(fechaDefecto);
        txtFechaFinal.setText(fechaDefecto);
    }

    private void ejecutarBusquedaInicial() {
        cargarTabla("", "", "");
    }

    private void ejecutarBusqueda() {
        cargarTabla(txtFechaInicial.getText(), txtFechaFinal.getText(), txtBuscarProducto.getText());
    }

    private void cargarTabla(String fIni, String fFin, String prod) {
        modeloTabla.setRowCount(0);
        RotacionVentasDAO dao = new RotacionVentasDAO();
        listaUltimaConsulta = dao.buscarRotacionVentas(fIni, fFin, prod);

        double sumaUnidades = 0;
        double sumaMonto = 0;

        for (Object[] fila : listaUltimaConsulta) {
            double costo = (double) fila[4];
            double cant = (double) fila[5];
            double total = (double) fila[6];
            double media = (double) fila[7];

            sumaUnidades += cant;
            sumaMonto += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                fila[3],
                String.format("$%,.0f", costo),
                String.format("%,.0f", cant),
                String.format("$%,.0f", total),
                String.format("$%,.0f", media)
            });
        }

        lblTotalUnidades.setText(String.format("UNIDADES ROTADAS: %,.0f", sumaUnidades));
        lblTotalMonto.setText(String.format("TOTAL ROTACIÓN: $%,.0f", sumaMonto));
    }

    private void abrirVentanaGrafico() {
        if (listaUltimaConsulta == null || listaUltimaConsulta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe realizar una consulta con datos antes de ver el gráfico.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Object[]> listaOrdenada = new ArrayList<>(listaUltimaConsulta);
        listaOrdenada.sort((a, b) -> Double.compare((double) b[6], (double) a[6]));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int limite = Math.min(10, listaOrdenada.size());

        for (int i = limite - 1; i >= 0; i--) {
            Object[] fila = listaOrdenada.get(i);
            String descripcion = (String) fila[3];
            if (descripcion.length() > 22) {
                descripcion = descripcion.substring(0, 22) + "..";
            }
            double rotacionTotal = (double) fila[6];
            dataset.addValue(rotacionTotal, "Rotación Total ($)", descripcion);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "TOP " + limite + " - ROTACIÓN DE MERCADERÍA ($)",
                "Producto",
                "Rotación Total ($)",
                dataset,
                org.jfree.chart.plot.PlotOrientation.HORIZONTAL,
                true, true, false
        );

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(cPanelTabla);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, cBordeAmbar);

        barChart.setBackgroundPaint(cFondo);
        barChart.getTitle().setPaint(Color.YELLOW);
        barChart.getTitle().setFont(new Font("Consolas", Font.BOLD, 15));

        JDialog dlgGrafico = new JDialog(JOptionPane.getFrameForComponent(this), "Gráfico Ejecutivo - Top Rotación", true);
        dlgGrafico.setSize(900, 600);
        dlgGrafico.setLocationRelativeTo(this);
        dlgGrafico.setUndecorated(true);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(cFondo);
        panelContenedor.setBorder(new LineBorder(cBordeAmbar, 2));

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(cFondo);
        panelContenedor.add(chartPanel, BorderLayout.CENTER);

        JPanel panelSurDlg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSurDlg.setBackground(cBarraInf);
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

    private void configurarAtajos() {
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtFechaInicial.setText("");
                txtFechaFinal.setText("");
                txtBuscarProducto.setText("");
                ejecutarBusqueda();
                txtFechaInicial.requestFocusInWindow();
            }
        });

        // [F6] Ciclar Tema en tiempo real
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclar_tema");
        root.getActionMap().put("ciclar_tema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "grafico");
        root.getActionMap().put("grafico", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirVentanaGrafico();
            }
        });
    }
}
