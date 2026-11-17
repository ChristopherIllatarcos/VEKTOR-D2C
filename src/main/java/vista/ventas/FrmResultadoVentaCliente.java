package vista.ventas;

import modelo.VentaClienteDTO;
// IMPORTANTE: Cambia esto por el paquete y nombre real de tu DAO de ventas
import dao.VentaDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import utilidades.PDFUtils;

public class FrmResultadoVentaCliente extends JInternalFrame {

    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtFechaIni;
    private JTextField txtFechaFin;
    private JTextField txtBuscar;
    private JLabel lblRegistros;
    private JLabel lblTotalGeneral;

    // Ajusta el tipo de tu DAO aquí
    private VentaDAO ventaDAO;

    // Constructor sin parámetros: abre directo y calcula fechas automáticas
    public FrmResultadoVentaCliente() {
        super("INFORME DE VENTAS POR CLIENTE", true, false, false, false);

        // Inicializa tu DAO
        this.ventaDAO = new VentaDAO();

        initComponents();
        cargarFechasAutomaticas();
        configurarAtajosTeclado();

        // Carga inicial automática apenas se abre la ventana
        consultarDatos();

        setSize(980, 620);
        setVisible(true);
    }

    private void cargarFechasAutomaticas() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoy = LocalDate.now();
        LocalDate primerDiaMes = hoy.withDayOfMonth(1);

        txtFechaIni.setText(primerDiaMes.format(formato));
        txtFechaFin.setText(hoy.format(formato));
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 168)); // Azul Clipper
        panelPrincipal.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Título, Fechas, Buscador y Registros)
        // ==========================================
        JPanel panelNorteContenedor = new JPanel(new BorderLayout());
        panelNorteContenedor.setOpaque(false);
        panelNorteContenedor.setBorder(new EmptyBorder(10, 15, 8, 15));

        // Fila 1: Título y Contador de Registros
        JPanel panelFila1 = new JPanel(new BorderLayout());
        panelFila1.setOpaque(false);

        JLabel lblTitulo = new JLabel("INFORME VENTAS POR CLIENTE");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(255, 255, 85)); // Amarillo Clipper
        panelFila1.add(lblTitulo, BorderLayout.WEST);

        lblRegistros = new JLabel("Reg: 0");
        lblRegistros.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblRegistros.setForeground(new Color(85, 255, 85));
        panelFila1.add(lblRegistros, BorderLayout.EAST);

        panelNorteContenedor.add(panelFila1, BorderLayout.NORTH);

        // Fila 2: Filtros de Fecha Automáticos + Buscador Rápido
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFiltros.setOpaque(false);
        panelFiltros.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(85, 255, 255)));

        JLabel lblF1 = new JLabel("DESDE:");
        lblF1.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblF1.setForeground(new Color(255, 255, 85));

        txtFechaIni = new JTextField(10);
        estilizarCampo(txtFechaIni);

        JLabel lblF2 = new JLabel("HASTA:");
        lblF2.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblF2.setForeground(new Color(255, 255, 85));

        txtFechaFin = new JTextField(10);
        estilizarCampo(txtFechaFin);

        // Al presionar ENTER en cualquiera de las fechas, consulta a la BD
        KeyAdapter enterConsulta = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    consultarDatos();
                }
            }
        };
        txtFechaIni.addKeyListener(enterConsulta);
        txtFechaFin.addKeyListener(enterConsulta);

        JButton btnConsultar = new JButton("[F5] Consultar");
        btnConsultar.setFont(new Font("Monospaced", Font.BOLD, 12));
        btnConsultar.setBackground(new Color(0, 168, 168));
        btnConsultar.setForeground(Color.BLACK);
        btnConsultar.setFocusPainted(false);
        btnConsultar.addActionListener(e -> consultarDatos());

        JLabel lblBuscar = new JLabel("  |  FILTRAR (RUT/Nombre):");
        lblBuscar.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblBuscar.setForeground(new Color(85, 255, 255));

        txtBuscar = new JTextField(16);
        estilizarCampo(txtBuscar);
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                filtrarTabla();
            }
        });

        panelFiltros.add(lblF1);
        panelFiltros.add(txtFechaIni);
        panelFiltros.add(lblF2);
        panelFiltros.add(txtFechaFin);
        panelFiltros.add(btnConsultar);
        panelFiltros.add(lblBuscar);
        panelFiltros.add(txtBuscar);

        panelNorteContenedor.add(panelFiltros, BorderLayout.SOUTH);
        panelPrincipal.add(panelNorteContenedor, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL
        // ==========================================
        String[] columnas = {"R.U.T. / Código", "NOMBRE DEL CLIENTE", "Cant. Docs", "Total Ventas"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResultados = new JTable(modeloTabla);
        tablaResultados.setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaResultados.setRowHeight(22);
        tablaResultados.setBackground(new Color(0, 0, 168));
        tablaResultados.setForeground(Color.WHITE);
        tablaResultados.setSelectionBackground(new Color(0, 168, 168));
        tablaResultados.setSelectionForeground(Color.WHITE);
        tablaResultados.setShowGrid(false);
        tablaResultados.setIntercellSpacing(new Dimension(0, 0));

        sorter = new TableRowSorter<>(modeloTabla);
        tablaResultados.setRowSorter(sorter);

        tablaResultados.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaResultados.getTableHeader().setBackground(new Color(0, 0, 168));
        tablaResultados.getTableHeader().setForeground(new Color(255, 255, 85));
        tablaResultados.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setBackground(new Color(0, 0, 168));
        rightRenderer.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(0, 0, 168));
        centerRenderer.setForeground(Color.WHITE);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBackground(new Color(0, 0, 168));
        leftRenderer.setForeground(Color.WHITE);

        tablaResultados.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        tablaResultados.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tablaResultados.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaResultados.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        TableColumnModel colModel = tablaResultados.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(140);
        colModel.getColumn(1).setPreferredWidth(440);
        colModel.getColumn(2).setPreferredWidth(100);
        colModel.getColumn(3).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(tablaResultados);
        scrollPane.getViewport().setBackground(new Color(0, 0, 168));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Totales y Barra Roja)
        // ==========================================
        JPanel panelSurGeneral = new JPanel(new BorderLayout());
        panelSurGeneral.setBackground(new Color(0, 0, 168));

        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setOpaque(false);
        panelTotal.setBorder(new EmptyBorder(6, 15, 6, 15));

        lblTotalGeneral = new JLabel("TOTAL GENERAL VENTAS: $ 0.00");
        lblTotalGeneral.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTotalGeneral.setForeground(new Color(255, 255, 85));
        panelTotal.add(lblTotalGeneral, BorderLayout.EAST);

        panelSurGeneral.add(panelTotal, BorderLayout.NORTH);

        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(new Color(168, 0, 0)); // Rojo Clipper
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

       JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Filtrar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F4)</b> <span style='color:FFFFFF;'>Exportar PDF</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F5 / Enter)</b> <span style='color:FFFFFF;'>Consultar Fechas</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelComandos.add(lblAtajos);

        panelSurGeneral.add(panelComandos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurGeneral, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void estilizarCampo(JTextField txt) {
        txt.setFont(new Font("Monospaced", Font.BOLD, 13));
        txt.setBackground(new Color(0, 0, 120));
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setHorizontalAlignment(JTextField.CENTER);
        txt.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255)));
    }

    // ==========================================
    // CONSULTA DIRECTA A BASE DE DATOS
    // ==========================================
    private void consultarDatos() {
        String fi = txtFechaIni.getText().trim();
        String ff = txtFechaFin.getText().trim();

        // Actualiza el título de la ventana dinámicamente
        setTitle("INFORME DE VENTAS POR CLIENTE (" + fi + " al " + ff + ")");

        // IMPORTANTE: Reemplaza 'obtenerVentasPorCliente(fi, ff)' por el método exacto de tu DAO
        List<VentaClienteDTO> datos = ventaDAO.obtenerVentasPorCliente(fi, ff);
        modeloTabla.setRowCount(0);
        double granTotal = 0;
        DecimalFormat df = new DecimalFormat("$ #,##0.00");

        if (datos != null) {
            for (VentaClienteDTO dto : datos) {
                modeloTabla.addRow(new Object[]{
                    dto.getCodigoCliente(),
                    dto.getNombreCliente(),
                    dto.getCantidadDocumentos(),
                    df.format(dto.getTotalVentas())
                });
                granTotal += dto.getTotalVentas();
            }
            lblRegistros.setText("Reg: " + datos.size());
        } else {
            lblRegistros.setText("Reg: 0");
        }

        lblTotalGeneral.setText("TOTAL GENERAL VENTAS: " + df.format(granTotal));

        // Si había algo escrito en el filtro, lo vuelve a aplicar sobre los nuevos datos
        filtrarTabla();
    }

    private void filtrarTabla() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 0, 1));
        }
        lblRegistros.setText("Reg: " + tablaResultados.getRowCount());
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();

        // ESC para cerrar
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // F3 para ir al buscador
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "ENFOQUE_BUSCAR");
        rootPane.getActionMap().put("ENFOQUE_BUSCAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtBuscar.requestFocusInWindow();
                txtBuscar.selectAll();
            }
        });
        
         // Tecla F4 para Exportar a PDF
        KeyStroke strokeF4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeF4, "EXPORTAR_PDF");
        rootPane.getActionMap().put("EXPORTAR_PDF", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarPDF();
            }
        });

        // F5 para recargar consulta de fechas
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "CONSULTAR_F5");
        rootPane.getActionMap().put("CONSULTAR_F5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consultarDatos();
            }
        });
        
       
    }
    
    private void exportarPDF() {
    String subtitulo = "Período: " + txtFechaIni.getText().trim() + " al " + txtFechaFin.getText().trim();
    PDFUtils.exportarTablaPDF(
            tablaResultados,
            "INFORME DE VENTAS POR CLIENTE",
            subtitulo,
            lblTotalGeneral.getText()
    );
}
}
