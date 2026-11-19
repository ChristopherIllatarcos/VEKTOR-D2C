package vista.ctacte;

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
import java.time.LocalDate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class FrmCarteraCheques extends JInternalFrame {

    private JTextField txtCliente;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
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
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

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

        // Evento para cerrar la ventana con la tecla ESC
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, false);
        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        panelPrincipal.getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // Evento para ciclar temas con F6
        KeyStroke f6KeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0, false);
        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f6KeyStroke, "CICLAR_TEMA");
        panelPrincipal.getActionMap().put("CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

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
        String fechaInicioPorDefecto = "01-01-" + anioActual;
        String fechaFinPorDefecto = "31-12-" + anioActual;

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
        txtFechaInicio = new JTextField(fechaInicioPorDefecto);
        estilarCampoTexto(txtFechaInicio, 140);
        panelFiltros.add(txtFechaInicio, gbc);

        gbc.gridx = 2;
        JLabel lblFecFin = new JLabel(" Fecha Final   :");
        lblFecFin.setFont(FUENTE_CONSOLA);
        lblFecFin.setForeground(cTextoBlanco);
        panelFiltros.add(lblFecFin, gbc);

        gbc.gridx = 3;
        txtFechaFin = new JTextField(fechaFinPorDefecto);
        estilarCampoTexto(txtFechaFin, 140);
        panelFiltros.add(txtFechaFin, gbc);

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
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setForeground(cTextoBlanco);
                }
            }
        }

        if (panelBotones != null) {
            panelBotones.setBackground(cFondo);
        }

        if (txtCliente != null) {
            estilarCampoTexto(txtCliente, txtCliente.getPreferredSize().width);
        }
        if (txtFechaInicio != null) {
            estilarCampoTexto(txtFechaInicio, txtFechaInicio.getPreferredSize().width);
        }
        if (txtFechaFin != null) {
            estilarCampoTexto(txtFechaFin, txtFechaFin.getPreferredSize().width);
        }

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
        btn.setFont(FUENTE_CONSOLA);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
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
        String cliente = txtCliente.getText().trim();
        String fecIni = txtFechaInicio.getText().trim();
        String fecFin = txtFechaFin.getText().trim();

        modeloTabla.setRowCount(0);

        try (Connection con = Conexion.getConnection()) {
            listaUltimaConsulta = dao.listarDocumentosCartera(con, cliente, fecIni, fecFin);

            for (DocumentoCarteraDTO d : listaUltimaConsulta) {
                modeloTabla.addRow(new Object[]{
                    d.getFolio(),
                    d.getFecha(),
                    d.getCliente(),
                    String.format("$ %,.0f", d.getMonto())
                });
            }

            if (listaUltimaConsulta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron documentos en el rango indicado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        // Ordenamos de mayor a menor según el monto
        List<DocumentoCarteraDTO> listaOrdenada = new ArrayList<>(listaUltimaConsulta);
        listaOrdenada.sort((a, b) -> Double.compare(b.getMonto(), a.getMonto()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int limite = Math.min(10, listaOrdenada.size()); // Top 10

        for (int i = limite - 1; i >= 0; i--) {
            DocumentoCarteraDTO d = listaOrdenada.get(i);
            String etiqueta = d.getFolio() + " - " + d.getCliente();
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
        renderer.setSeriesPaint(0, cTextoVerde); // Barras color acorde al tema

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
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // F2: Foco al cliente
        getRootPane().getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2_BUSCAR");
        getRootPane().getActionMap().put("F2_BUSCAR", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtCliente.requestFocus();
                txtCliente.selectAll();
            }
        });

        // F3: Abrir Gráfico Ejecutivo
        getRootPane().getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "F3_GRAFICO");
        getRootPane().getActionMap().put("F3_GRAFICO", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirVentanaGrafico();
            }
        });

        // F6: Ciclar Temas
        getRootPane().getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "F6_CICLAR_TEMA");
        getRootPane().getActionMap().put("F6_CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        // ENTER en campos de texto
        txtCliente.addActionListener(e -> buscarDocumentos());
        txtFechaInicio.addActionListener(e -> buscarDocumentos());
        txtFechaFin.addActionListener(e -> buscarDocumentos());

        // F8: Buscador de clientes
        getRootPane().getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "F8_BUSCAR_CLIENTE");
        getRootPane().getActionMap().put("F8_BUSCAR_CLIENTE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
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
