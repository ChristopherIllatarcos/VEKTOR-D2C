package vista.ventas;

import modelo.VentaClienteDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.List;

public class FrmResultadoVentaCliente extends JInternalFrame {

    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter; // <-- Para filtrar la tabla
    private JTextField txtBuscar;
    private JLabel lblRegistros;
    private JLabel lblTotalGeneral;

    public FrmResultadoVentaCliente(List<VentaClienteDTO> datos, String fi, String ff) {
        super("INFORME DE VENTAS POR CLIENTE (" + fi + " al " + ff + ")", true, false, false, false );
        initComponents(datos, fi, ff);
        configurarAtajosTeclado();

        setSize(980, 620);
        setVisible(true);
    }

    private void initComponents(List<VentaClienteDTO> datos, String fi, String ff) {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 168)); // Azul Clipper
        panelPrincipal.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Título, Buscador y Registros)
        // ==========================================
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);
        panelSuperior.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblTitulo = new JLabel("INFORME VENTAS POR CLIENTE");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(255, 255, 85)); // Amarillo Clipper
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        // Subpanel para el Buscador estilo Consola
        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panelBuscador.setOpaque(false);

        JLabel lblBuscar = new JLabel("BUSCAR (RUT / Nombre):");
        lblBuscar.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblBuscar.setForeground(new Color(85, 255, 255));

        txtBuscar = new JTextField(18);
        txtBuscar.setFont(new Font("Monospaced", Font.BOLD, 13));
        txtBuscar.setBackground(new Color(0, 0, 120));
        txtBuscar.setForeground(Color.WHITE);
        txtBuscar.setCaretColor(Color.WHITE);
        txtBuscar.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255)));

        // Evento en tiempo real para filtrar la tabla mientras escribes
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrarTabla();
            }
        });

        panelBuscador.add(lblBuscar);
        panelBuscador.add(txtBuscar);
        panelSuperior.add(panelBuscador, BorderLayout.CENTER);

        int totalRegistros = datos != null ? datos.size() : 0;
        lblRegistros = new JLabel("Reg: " + totalRegistros);
        lblRegistros.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblRegistros.setForeground(new Color(85, 255, 85));
        panelSuperior.add(lblRegistros, BorderLayout.EAST);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

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
        }

        tablaResultados = new JTable(modeloTabla);
        tablaResultados.setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaResultados.setRowHeight(22);
        tablaResultados.setBackground(new Color(0, 0, 168));
        tablaResultados.setForeground(Color.WHITE);
        tablaResultados.setSelectionBackground(new Color(0, 168, 168));
        tablaResultados.setSelectionForeground(Color.WHITE);
        tablaResultados.setShowGrid(false);
        tablaResultados.setIntercellSpacing(new Dimension(0, 0));

        // Configuramos el sorter para habilitar el filtro dinámico
        sorter = new TableRowSorter<>(modeloTabla);
        tablaResultados.setRowSorter(sorter);

        // Cabecera de la tabla
        tablaResultados.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaResultados.getTableHeader().setBackground(new Color(0, 0, 168));
        tablaResultados.getTableHeader().setForeground(new Color(255, 255, 85));
        tablaResultados.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)));

        // Renderizadores
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

        lblTotalGeneral = new JLabel("TOTAL GENERAL VENTAS: " + df.format(granTotal));
        lblTotalGeneral.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTotalGeneral.setForeground(new Color(255, 255, 85));
        panelTotal.add(lblTotalGeneral, BorderLayout.EAST);

        panelSurGeneral.add(panelTotal, BorderLayout.NORTH);

        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(new Color(168, 0, 0)); // Rojo Clipper
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar Ventana</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Ir al Buscador</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelComandos.add(lblAtajos);

        panelSurGeneral.add(panelComandos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurGeneral, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    // Método para filtrar la tabla dinámicamente por RUT (Columna 0) o Nombre (Columna 1)
    private void filtrarTaskText(String text) {
        // Lógica delegada al sorter
    }

    private void filtrarTabla() {
        String texto = txtBuscar.getText().trim();
        if (texto.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // (?i) hace que la búsqueda no distinga entre mayúsculas y minúsculas
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0, 1));
        }
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();

        // Tecla ESC para cerrar
        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Tecla F3 para enfocar rápidamente el buscador (Muy estilo Clipper clásico)
        KeyStroke strokeF3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeF3, "ENFOQUE_BUSCAR");
        rootPane.getActionMap().put("ENFOQUE_BUSCAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtBuscar.requestFocusInWindow();
                txtBuscar.selectAll();
            }
        });
    }
}
