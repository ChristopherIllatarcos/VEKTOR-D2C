package vista.ventas;

import dao.ComisionDAO;
import modelo.DetalleVendedorDTO;

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

public class FrmDetalleVendedor extends JInternalFrame {

    private JTextField txtFolioBusqueda;
    private JTable tablaDetalle;
    private DefaultTableModel modeloTabla;
    private ComisionDAO comisionDAO;
    private int codigoVendedor;

    public FrmDetalleVendedor(String nombreVendedor, int codigoVendedor) {
        super("DETALLE: " + nombreVendedor, true, false, false, false);
        this.comisionDAO = new ComisionDAO();
        this.codigoVendedor = codigoVendedor;

        initComponents();
        // Asignar el icono Nexum a la ventana interna (JInternalFrame)
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
        configurarAtajosTeclado();
        cargarDatosDetalle(""); // Carga todos inicialmente ordenados del más nuevo al más antiguo

        setSize(900, 520);
        setVisible(true);
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 168)); // Azul Clipper
        panelPrincipal.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Buscador por Folio)
        // ==========================================
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(new Color(0, 0, 168));
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelFiltros.setOpaque(false);

        JLabel lblTitulo = new JLabel("DESGLOSE");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(255, 255, 85));

        JLabel lblFolio = new JLabel("N° FOLIO:");
        lblFolio.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblFolio.setForeground(new Color(85, 255, 255));

        txtFolioBusqueda = new JTextField(12);
        estilizarCampoTexto(txtFolioBusqueda);

        JButton btnBuscar = new JButton("Buscar [Enter]");
        estilizarBotonConsola(btnBuscar, new Color(0, 168, 168));
        btnBuscar.addActionListener(e -> ejecutarBusquedaFolio());

        JButton btnMostrarTodos = new JButton("Ver Todos");
        estilizarBotonConsola(btnMostrarTodos, new Color(0, 120, 120));
        btnMostrarTodos.addActionListener(e -> {
            txtFolioBusqueda.setText("");
            cargarDatosDetalle("");
        });

        panelFiltros.add(lblTitulo);
        panelFiltros.add(lblFolio);
        panelFiltros.add(txtFolioBusqueda);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnMostrarTodos);

        panelNorte.add(panelFiltros, BorderLayout.WEST);
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL
        // ==========================================
        String[] columnas = {"TIPO", "N° FOLIO", "FECHA", "CLIENTE", "TOTAL DOCUMENTO"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalle = new JTable(modeloTabla);
        tablaDetalle.setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaDetalle.setRowHeight(22);
        tablaDetalle.setBackground(new Color(0, 0, 168));
        tablaDetalle.setForeground(Color.WHITE);
        tablaDetalle.setSelectionBackground(new Color(0, 168, 168));
        tablaDetalle.setSelectionForeground(Color.WHITE);
        tablaDetalle.setShowGrid(false);

        tablaDetalle.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaDetalle.getTableHeader().setBackground(new Color(0, 0, 168));
        tablaDetalle.getTableHeader().setForeground(new Color(255, 255, 85));
        tablaDetalle.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)));

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

        tablaDetalle.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaDetalle.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tablaDetalle.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaDetalle.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);
        tablaDetalle.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        TableColumnModel colModel = tablaDetalle.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(80);
        colModel.getColumn(1).setPreferredWidth(100);
        colModel.getColumn(2).setPreferredWidth(120);
        colModel.getColumn(3).setPreferredWidth(350);
        colModel.getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(tablaDetalle);
        scrollPane.getViewport().setBackground(new Color(0, 0, 168));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. BARRA INFERIOR ROJA
        // ==========================================
        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(new Color(168, 0, 0));
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar Detalle</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Buscar Folio</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelComandos.add(lblAtajos);

        panelPrincipal.add(panelComandos, BorderLayout.SOUTH);
        add(panelPrincipal);
       SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });

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

    private void ejecutarBusquedaFolio() {
        String folioBuscado = txtFolioBusqueda.getText().trim();
        cargarDatosDetalle(folioBuscado);
    }

    private void cargarDatosDetalle(String filtroFolio) {
        modeloTabla.setRowCount(0);
        DecimalFormat dfDinero = new DecimalFormat("$ #,##0");

        try {
            List<DetalleVendedorDTO> lista = comisionDAO.obtenerDetallePorFolio(codigoVendedor, filtroFolio);
            for (DetalleVendedorDTO item : lista) {
                modeloTabla.addRow(new Object[]{
                    item.getTipoDocumento(),
                    item.getFolio(),
                    item.getFecha(),
                    item.getCliente(),
                    dfDinero.format(item.getTotal())
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al buscar el folio en la base de datos.", "Error SQL", JOptionPane.ERROR_MESSAGE);
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
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "BUSCAR_ENTER");
        rootPane.getActionMap().put("BUSCAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarBusquedaFolio();
            }
        });
    }
}
