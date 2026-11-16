package vista.ventas;

import dao.ComisionDAO;
import modelo.ComisionVendedorDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FrmComisionesCobranza extends JInternalFrame {

    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private JLabel lblVentaTotalGlobal;
    private JButton btnConsultar;
    private ComisionDAO comisionDAO;
    private List<ComisionVendedorDTO> listaUltimaConsulta = new ArrayList<>();

    public FrmComisionesCobranza() {
        super("COMISIONES X COBRANZA", true, false, false, false);
        this.comisionDAO = new ComisionDAO();

        initComponents();
        configurarAtajosTeclado();

        setSize(980, 620);
        setVisible(true);
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 168)); // Azul Clipper
        panelPrincipal.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Fechas dinámicas y Total Global)
        // ==========================================
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(new Color(0, 0, 168));
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelFiltros.setOpaque(false);

        JLabel lblTitulo = new JLabel("COBRANZA");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(255, 255, 85));

        JLabel lblFI = new JLabel("DESDE:");
        lblFI.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFI.setForeground(new Color(85, 255, 255));

        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        txtFechaInicio = new JTextField(fechaHoy, 10);
        estilizarCampoTexto(txtFechaInicio);

        JLabel lblFF = new JLabel("HASTA:");
        lblFF.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFF.setForeground(new Color(85, 255, 255));

        txtFechaFin = new JTextField(fechaHoy, 10);
        estilizarCampoTexto(txtFechaFin);

        btnConsultar = new JButton("Consultar [Enter]");
        estilizarBotonConsola(btnConsultar, new Color(0, 168, 168));
        btnConsultar.addActionListener(e -> cargarReporteCobranza());

        panelFiltros.add(lblTitulo);
        panelFiltros.add(lblFI);
        panelFiltros.add(txtFechaInicio);
        panelFiltros.add(lblFF);
        panelFiltros.add(txtFechaFin);
        panelFiltros.add(btnConsultar);

        panelNorte.add(panelFiltros, BorderLayout.WEST);

        lblVentaTotalGlobal = new JLabel("TOTAL: $ 0");
        lblVentaTotalGlobal.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblVentaTotalGlobal.setForeground(new Color(255, 255, 85));
        panelNorte.add(lblVentaTotalGlobal, BorderLayout.EAST);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL (Reporte Tabular)
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
        tablaReporte.setBackground(new Color(0, 0, 168));
        tablaReporte.setForeground(Color.WHITE);
        tablaReporte.setSelectionBackground(new Color(0, 168, 168));
        tablaReporte.setSelectionForeground(Color.WHITE);
        tablaReporte.setShowGrid(false);
        tablaReporte.setIntercellSpacing(new Dimension(0, 0));

        tablaReporte.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaReporte.getTableHeader().setBackground(new Color(0, 0, 168));
        tablaReporte.getTableHeader().setForeground(new Color(255, 255, 85));
        tablaReporte.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(0, 0, 168));
        centerRenderer.setForeground(Color.WHITE);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setBackground(new Color(0, 0, 168));
        rightRenderer.setForeground(Color.WHITE);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBackground(new Color(0, 0, 168));
        leftRenderer.setForeground(Color.WHITE);

        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tablaReporte.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tablaReporte.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        TableColumnModel colModel = tablaReporte.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(100);
        colModel.getColumn(1).setPreferredWidth(380);
        colModel.getColumn(2).setPreferredWidth(150);
        colModel.getColumn(3).setPreferredWidth(140);
        colModel.getColumn(4).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(tablaReporte);
        scrollPane.getViewport().setBackground(new Color(0, 0, 168));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Barra Roja de Comandos)
        // ==========================================
        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(new Color(168, 0, 0));
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar Ventana</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Consultar Reporte</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelComandos.add(lblAtajos);

        panelPrincipal.add(panelComandos, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void estilizarCampoTexto(JTextField txt) {
        txt.setFont(new Font("Monospaced", Font.BOLD, 13));
        txt.setBackground(new Color(0, 0, 120));
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255)));
    }

    private void estilizarBotonConsola(JButton btn, Color bg) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 85), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();

        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "CONSULTAR_ENTER");
        rootPane.getActionMap().put("CONSULTAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarReporteCobranza();
            }
        });
    }

    private void cargarReporteCobranza() {
        String fechaIni = txtFechaInicio.getText().trim();
        String fechaFin = txtFechaFin.getText().trim();

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
}
