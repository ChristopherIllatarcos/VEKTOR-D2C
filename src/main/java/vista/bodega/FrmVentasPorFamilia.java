package vista.bodega;

import dao.VentaFamiliaDAO;
import modelo.VentaFamiliaDTO;

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
import java.util.List;

public class FrmVentasPorFamilia extends JInternalFrame {

    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private VentaFamiliaDAO ventaFamiliaDAO;
    private JLabel lblTotalGlobal;

    public FrmVentasPorFamilia() {
        super("VENTAS POR FAMILIA", true, false, false, false);
        this.ventaFamiliaDAO = new VentaFamiliaDAO();

        initComponents();
        configurarAtajosTeclado();

        setSize(850, 500);
        setVisible(true);
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 168)); // Azul marino Clipper
        panelPrincipal.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Fechas y Título)
        // ==========================================
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(new Color(0, 0, 168));
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelFiltros.setOpaque(false);

        JLabel lblTitulo = new JLabel("VENTAS POR FAMILIA");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(255, 255, 85));

        JLabel lblFI = new JLabel("DESDE:");
        lblFI.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFI.setForeground(new Color(85, 255, 255));

        
        // Obtenemos la fecha actual del sistema de forma automática
        java.time.LocalDate fechaActualSys = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatoDdf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaHoyStr = fechaActualSys.format(formatoDdf);

        txtFechaInicio = new JTextField(fechaHoyStr, 10);
        estilizarCampoTextoAmarillo(txtFechaInicio);

        JLabel lblFF = new JLabel("HASTA:");
        lblFF.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFF.setForeground(new Color(85, 255, 255));

        txtFechaFin = new JTextField(fechaHoyStr, 10);
        estilizarCampoTextoAmarillo(txtFechaFin);
    

        JButton btnConsultar = new JButton("Consultar [Enter]");
        estilizarBotonConsola(btnConsultar, new Color(0, 168, 168));
        btnConsultar.addActionListener(e -> ejecutarConsulta());

        panelFiltros.add(lblTitulo);
        panelFiltros.add(lblFI);
        panelFiltros.add(txtFechaInicio);
        panelFiltros.add(lblFF);
        panelFiltros.add(txtFechaFin);
        panelFiltros.add(btnConsultar);

        panelNorte.add(panelFiltros, BorderLayout.WEST);

        lblTotalGlobal = new JLabel("VENTA TOTAL: $ 0");
        lblTotalGlobal.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTotalGlobal.setForeground(new Color(255, 255, 85));
        panelNorte.add(lblTotalGlobal, BorderLayout.EAST);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL (Fondo negro estilo imagen 2)
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
        tablaReporte.setBackground(Color.BLACK); // Fondo negro pedido
        tablaReporte.setForeground(new Color(255, 255, 85)); // Texto amarillo
        tablaReporte.setSelectionBackground(new Color(0, 168, 168));
        tablaReporte.setSelectionForeground(Color.WHITE);
        tablaReporte.setShowGrid(false);

        tablaReporte.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaReporte.getTableHeader().setBackground(new Color(0, 0, 168));
        tablaReporte.getTableHeader().setForeground(new Color(255, 255, 85));
        tablaReporte.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(Color.BLACK);
        centerRenderer.setForeground(new Color(255, 255, 85));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setBackground(Color.BLACK);
        rightRenderer.setForeground(new Color(255, 255, 85));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBackground(Color.BLACK);
        leftRenderer.setForeground(new Color(255, 255, 85));

        tablaReporte.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaReporte.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tablaReporte.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaReporte.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        TableColumnModel colModel = tablaReporte.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(90);
        colModel.getColumn(1).setPreferredWidth(350);
        colModel.getColumn(2).setPreferredWidth(120);
        colModel.getColumn(3).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(tablaReporte);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. BARRA INFERIOR ROJA (Atajos)
        // ==========================================
        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(new Color(168, 0, 0)); // Rojo Clipper
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Salir</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Consultar</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelComandos.add(lblAtajos);

        panelPrincipal.add(panelComandos, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void estilizarCampoTextoAmarillo(JTextField txt) {
        txt.setFont(new Font("Monospaced", Font.BOLD, 13));
        txt.setBackground(Color.YELLOW); // Fondo amarillo solicitado
        txt.setForeground(Color.BLACK);  // Texto oscuro legible sobre amarillo
        txt.setCaretColor(Color.BLACK);
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
            List<VentaFamiliaDTO> lista = ventaFamiliaDAO.obtenerVentasPorFamilia(fechaIni, fechaFin);
            double sumaGlobal = 0;

            for (VentaFamiliaDTO item : lista) {
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
                ejecutarConsulta();
            }
        });
    }
}
