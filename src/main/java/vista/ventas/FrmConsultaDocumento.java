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
    private JLabel lblAtajos;
    private JButton btnBuscar;
    private DocumentoDAO documentoDAO;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelNorte, panelComandos, panelTotal;
    private JScrollPane scrollPane;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelNorte, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur;
    private final Font FUENTE_CONSOLA = new Font("Monospaced", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Naranja Fuego por defecto

    public FrmConsultaDocumento() {
        super("2.- CONSULTA DE DOCUMENTOS", true, false, false, false);
        this.documentoDAO = new DocumentoDAO();

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents();
        // Asignar el icono Nexum a la ventana interna (JInternalFrame)
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
        configurarAtajosTeclado();

        setSize(980, 620);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });
        txtFolio.requestFocusInWindow();

    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F5]
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

        // Refrescar componentes visuales en tiempo de ejecución
        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                new EmptyBorder(12, 15, 12, 15)
        ));

        lblTotalDocumento.setForeground(cAcento);

        if (tablaDetalle != null) {
            tablaDetalle.setBackground(cFondoNegro);
            tablaDetalle.setForeground(cTextoClaro);
            tablaDetalle.setSelectionBackground(cSeleccion);
            tablaDetalle.getTableHeader().setBackground(cEncabezadoTabla);
            tablaDetalle.getTableHeader().setForeground(cAcento);
            tablaDetalle.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
            tablaDetalle.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondoNegro);
        }

        panelComandos.setBackground(cBarraSur);
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));

        estilizarCampoTexto(txtFolio);
        estilizarBotonConsola(btnBuscar, cEncabezadoTabla);

        if (cmbTipoDocumento != null) {
            cmbTipoDocumento.setBackground(cEncabezadoTabla);
            cmbTipoDocumento.setForeground(cTextoClaro);
            cmbTipoDocumento.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        }

        actualizarTextoAtajos();

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Consultar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Buscar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F5)</b> <span style='color:#00FFCC;'>Tema: " + temaActual + "</span></html>");
        }
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        // ==========================================
        // 1. ZONA SUPERIOR (Filtros)
        // ==========================================
        panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitulo = new JLabel("CONSULTA DE DOCUMENTOS");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(cAcento);
        panelNorte.add(lblTitulo, BorderLayout.WEST);

        JPanel panelParametros = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelParametros.setOpaque(false);

        JLabel lblDoc = new JLabel("DOCUMENTO:");
        lblDoc.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblDoc.setForeground(cAcento);

        listaClipper();

        JLabel lblFolio = new JLabel("FOLIO:");
        lblFolio.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblFolio.setForeground(cAcento);

        txtFolio = new JTextField(10);
        estilizarCampoTexto(txtFolio);

        btnBuscar = new JButton("Consultar [F3]");
        estilizarBotonConsola(btnBuscar, cEncabezadoTabla);
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
        // 2. TABLA CENTRAL CON RAYAS TRANSPARENTES
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
        tablaDetalle.setBackground(cFondoNegro);
        tablaDetalle.setForeground(cTextoClaro);
        tablaDetalle.setSelectionBackground(cSeleccion);
        tablaDetalle.setSelectionForeground(Color.WHITE);
        tablaDetalle.setShowGrid(false);
        tablaDetalle.setIntercellSpacing(new Dimension(0, 0));

        tablaDetalle.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaDetalle.getTableHeader().setBackground(cEncabezadoTabla);
        tablaDetalle.getTableHeader().setForeground(cAcento);
        tablaDetalle.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

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

                if (column == 2) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else if (column == 3 || column == 4) {
                    setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tablaDetalle.getColumnCount(); i++) {
            tablaDetalle.getColumnModel().getColumn(i).setCellRenderer(rendererRayas);
        }

        TableColumnModel colModel = tablaDetalle.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(120);
        colModel.getColumn(1).setPreferredWidth(420);
        colModel.getColumn(2).setPreferredWidth(100);
        colModel.getColumn(3).setPreferredWidth(140);
        colModel.getColumn(4).setPreferredWidth(160);

        scrollPane = new JScrollPane(tablaDetalle);
        scrollPane.getViewport().setBackground(cFondoNegro);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Totales y Barra de Comandos con F5)
        // ==========================================
        JPanel panelSurGeneral = new JPanel(new BorderLayout());
        panelSurGeneral.setBackground(cFondoNegro);

        panelTotal = new JPanel(new BorderLayout());
        panelTotal.setOpaque(false);
        panelTotal.setBorder(new EmptyBorder(8, 15, 8, 15));

        lblTotalDocumento = new JLabel("TOTAL DOCUMENTO: $ 0.00");
        lblTotalDocumento.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTotalDocumento.setForeground(cAcento);
        panelTotal.add(lblTotalDocumento, BorderLayout.EAST);

        panelSurGeneral.add(panelTotal, BorderLayout.NORTH);

        panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelComandos.setBackground(cBarraSur);
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        actualizarTextoAtajos();
        panelComandos.add(lblAtajos);

        panelSurGeneral.add(panelComandos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurGeneral, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void estilizarCampoTexto(JTextField txt) {
        txt.setFont(new Font("Monospaced", Font.BOLD, 14));
        txt.setBackground(new Color(Math.max(0, cFondoNegro.getRed() + 10), Math.max(0, cFondoNegro.getGreen() + 10), Math.max(0, cFondoNegro.getBlue() + 10)));
        txt.setForeground(cTextoClaro);
        txt.setCaretColor(cAcento);
        txt.setBorder(BorderFactory.createLineBorder(cAcento));
    }

    private void estilizarBotonConsola(JButton btn, Color bg) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
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

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "BUSCAR_ENTER");
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

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "BUSCAR_F3");
        rootPane.getActionMap().put("BUSCAR_F3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    buscarDocumento();
                } catch (SQLException ex) {
                    System.getLogger(FrmConsultaDocumento.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        });

        // [F5] Ciclar Tema
        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "CAMBIAR_TEMA_F5");
        rootPane.getActionMap().put("CAMBIAR_TEMA_F5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }

    private void buscarDocumento() throws SQLException {
        String seleccion = (String) cmbTipoDocumento.getSelectedItem();
        String tipoDoc = seleccion.substring(0, 3);
        String folioStr = txtFolio.getText().trim();

        if (folioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un número de folio válido.", "VEKTOR D2C", JOptionPane.WARNING_MESSAGE);
            txtFolio.requestFocusInWindow();
            return;
        }

        modeloTabla.setRowCount(0);
        double totalDoc = 0;
        DecimalFormat df = new DecimalFormat("$ #,##0.00");

        try {
            int folio = Integer.parseInt(folioStr);
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
        cmbTipoDocumento.setBackground(cEncabezadoTabla);
        cmbTipoDocumento.setForeground(cTextoClaro);
        cmbTipoDocumento.setBorder(BorderFactory.createLineBorder(cAcento, 1));

        cmbTipoDocumento.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(new Font("Monospaced", Font.BOLD, 13));
                if (isSelected) {
                    c.setBackground(cSeleccion);
                    c.setForeground(cAcento);
                } else {
                    c.setBackground(cFondoNegro);
                    c.setForeground(cTextoClaro);
                }
                return c;
            }
        });
    }
}
