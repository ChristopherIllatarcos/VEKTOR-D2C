package vista.ventas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import dao.DocumentoDAO;
import java.sql.SQLException;
import modelo.DetalleDocumentoDTO;

public class FrmConsultaDocumento extends JInternalFrame {

    private JComboBox<String> cmbTipoDocumento;
    private JTextField txtFolio;
    private JTable tablaDetalle;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalDocumento;
    private JButton btnBuscar;
    private DocumentoDAO documentoDAO;

    public FrmConsultaDocumento() {
        // Parámetros MDI: (Título, resizable, closable, maximizable, iconifiable)
        // Sin botones de minimizar, maximizar ni cerrar (se sale con ESC)
        super("VEKTOR D2C - CONSULTA DE DOCUMENTOS", true, false, false, false);

        initComponents();
        configurarAtajosTeclado();
        this.documentoDAO = new DocumentoDAO();

        setSize(980, 620);
        setVisible(true);
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 168)); // Azul Clipper
        panelPrincipal.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Filtros: Documento y Folio) + Referencia visual tipo la foto
        // ==========================================
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(new Color(0, 0, 168));
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitulo = new JLabel("CONSULTA DE DOCUMENTOS");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(255, 255, 85)); // Amarillo Clipper
        panelNorte.add(lblTitulo, BorderLayout.WEST);

        // Panel central de parámetros (DOCUMENTO y FOLIO) tal como la foto
        JPanel panelParametros = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelParametros.setOpaque(false);

        JLabel lblDoc = new JLabel("DOCUMENTO:");
        lblDoc.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblDoc.setForeground(new Color(85, 255, 255));

        listaClipper();

        JLabel lblFolio = new JLabel("FOLIO:");
        lblFolio.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblFolio.setForeground(new Color(85, 255, 255));

        txtFolio = new JTextField(10);
        txtFolio.setFont(new Font("Monospaced", Font.BOLD, 14));
        txtFolio.setBackground(new Color(0, 0, 120));
        txtFolio.setForeground(Color.WHITE);
        txtFolio.setCaretColor(Color.WHITE);
        txtFolio.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255)));

        btnBuscar = new JButton("Consultar [F3]");
        estilizarBotonConsola(btnBuscar, new Color(0, 168, 168));
        btnBuscar.addActionListener(e -> {
            try {
                buscarDocumento();
            } catch (SQLException ex) {
                System.getLogger(FrmConsultaDocumento.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });

        panelParametros.add(lblDoc);
        panelParametros.add(cmbTipoDocumento);
        panelParametros.add(lblFolio);
        panelParametros.add(txtFolio);
        panelParametros.add(btnBuscar);

        panelNorte.add(panelParametros, BorderLayout.CENTER);
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL (Detalle de ítems del documento)
        // ==========================================
        String[] columnas = {"Código", "Descripción del Artículo / Producto", "Cantidad", "Precio Unit.", "Total Línea"};
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
        tablaDetalle.setIntercellSpacing(new Dimension(0, 0));

        // Cabecera de la tabla estilo Clipper
        tablaDetalle.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaDetalle.getTableHeader().setBackground(new Color(0, 0, 168));
        tablaDetalle.getTableHeader().setForeground(new Color(255, 255, 85));
        tablaDetalle.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 255, 255)));

        // Renderizadores de alineación
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

        tablaDetalle.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        tablaDetalle.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tablaDetalle.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaDetalle.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tablaDetalle.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        TableColumnModel colModel = tablaDetalle.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(120);
        colModel.getColumn(1).setPreferredWidth(420);
        colModel.getColumn(2).setPreferredWidth(100);
        colModel.getColumn(3).setPreferredWidth(140);
        colModel.getColumn(4).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(tablaDetalle);
        scrollPane.getViewport().setBackground(new Color(0, 0, 168));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Totales y Barra Roja de Comandos)
        // ==========================================
        JPanel panelSurGeneral = new JPanel(new BorderLayout());
        panelSurGeneral.setBackground(new Color(0, 0, 168));

        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setOpaque(false);
        panelTotal.setBorder(new EmptyBorder(8, 15, 8, 15));

        lblTotalDocumento = new JLabel("TOTAL DOCUMENTO: $ 0.00");
        lblTotalDocumento.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTotalDocumento.setForeground(new Color(255, 255, 85));
        panelTotal.add(lblTotalDocumento, BorderLayout.EAST);

        panelSurGeneral.add(panelTotal, BorderLayout.NORTH);

        // Barra Roja inferior de comandos tipo Clipper
        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(new Color(168, 0, 0)); // Rojo Clipper
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar Ventana</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Consultar Folio</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Aceptar / Buscar</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelComandos.add(lblAtajos);

        panelSurGeneral.add(panelComandos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurGeneral, BorderLayout.SOUTH);

        add(panelPrincipal);
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

        // Tecla ESC para cerrar ventana sin necesidad de botón superior
        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Tecla Enter o F3 para disparar la consulta rápida
        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "BUSCAR_ENTER");
        rootPane.getActionMap().put("BUSCAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    buscarDocumento();
                } catch (SQLException ex) {
                    System.getLogger(FrmConsultaDocumento.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        });
    }

    private void buscarDocumento() throws SQLException {
        String seleccion = (String) cmbTipoDocumento.getSelectedItem();
        String tipoDoc = seleccion.substring(0, 3); // Extrae "FAC", "BOL", etc.
        String folioStr = txtFolio.getText().trim();

        if (folioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un número de folio válido.", "VEKTOR D2C", JOptionPane.WARNING_MESSAGE);
            txtFolio.requestFocusInWindow();
            return;
        }

        modeloTabla.setRowCount(0); // Limpiamos la tabla antes de cargar
        double totalDoc = 0;
        DecimalFormat df = new DecimalFormat("$ #,##0.00");

        try {
            int folio = Integer.parseInt(folioStr);

            // Invocamos estrictamente al DAO (Cero SQL ni conexiones en la interfaz)
            java.util.List<DetalleDocumentoDTO> detalles = documentoDAO.obtenerDetalleDocumento(tipoDoc, folio);
            if (detalles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontró el documento " + tipoDoc + " N° " + folioStr, "VEKTOR D2C", JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (DetalleDocumentoDTO item : detalles) {
                    double subtotal = item.getTotalLinea();
                    totalDoc += subtotal;

                    modeloTabla.addRow(new Object[]{
                        item.getCodigoProducto(),
                        item.getDescripcion(),
                        item.getCantidad(),
                        df.format(item.getPrecioUnitario()),
                        df.format(subtotal)
                    });
                }
            }

            lblTotalDocumento.setText("TOTAL DOCUMENTO: " + df.format(totalDoc));

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "El folio debe ser un número entero válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            txtFolio.requestFocusInWindow();
        }
    }

    private void listaClipper() {
        String[] opcionesDocs = {
            "FAC - (FAC)ura",
            "BOL - (BOL)eta",
            "GUI - (GUI)a",
            "VAL - (VAL)e",
            "COT - (COT)iza"
        };
        cmbTipoDocumento = new JComboBox<>(opcionesDocs);
        cmbTipoDocumento.setFont(new Font("Monospaced", Font.BOLD, 13));
        cmbTipoDocumento.setBackground(new Color(0, 0, 120));
        cmbTipoDocumento.setForeground(Color.WHITE);
        cmbTipoDocumento.setBorder(BorderFactory.createLineBorder(new Color(85, 255, 255), 1));

        // Renderizador personalizado para darle el look & feel Clipper MS-DOS
        cmbTipoDocumento.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(new Font("Monospaced", Font.BOLD, 13));
                if (isSelected) {
                    c.setBackground(new Color(0, 168, 168)); // Cian oscuro clásico de selección
                    c.setForeground(new Color(255, 255, 85)); // Amarillo Clipper al seleccionar
                } else {
                    c.setBackground(new Color(0, 0, 120));    // Fondo azul de consola
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });
    }

}
