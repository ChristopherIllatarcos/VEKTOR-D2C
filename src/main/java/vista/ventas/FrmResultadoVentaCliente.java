package vista.ventas;

import modelo.VentaClienteDTO;
import dao.VentaDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    private JLabel lblAtajos;
    private JButton btnConsultar;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelNorteContenedor, panelFiltros, panelSurGeneral, panelComandos;
    private JScrollPane scrollPane;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelNorte, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur;
    private final Font FUENTE_CONSOLA = new Font("Monospaced", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Naranja Fuego por defecto

    private VentaDAO ventaDAO;

    public FrmResultadoVentaCliente() {
        super("5.- INFORME DE VENTAS POR CLIENTE", false, false, false, false);
        this.ventaDAO = new VentaDAO();

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents();
        cargarFechasAutomaticas();
        configurarAtajosTeclado();

        consultarDatos();

        setSize(980, 620);
        setVisible(true);

        // Asignar el icono de Nexum directamente a este JInternalFrame
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }

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

        if (panelFiltros != null) {
            panelFiltros.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cAcento));
        }

        if (lblRegistros != null) {
            lblRegistros.setForeground(cAcento);
        }
        if (lblTotalGeneral != null) {
            lblTotalGeneral.setForeground(cAcento);
        }

        if (tablaResultados != null) {
            tablaResultados.setBackground(cFondoNegro);
            tablaResultados.setForeground(cTextoClaro);
            tablaResultados.setSelectionBackground(cSeleccion);
            tablaResultados.getTableHeader().setBackground(cEncabezadoTabla);
            tablaResultados.getTableHeader().setForeground(cAcento);
            tablaResultados.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
            tablaResultados.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondoNegro);
        }

        if (panelComandos != null) {
            panelComandos.setBackground(cBarraSur);
            panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));
        }

        estilizarCampo(txtFechaIni);
        estilizarCampo(txtFechaFin);
        estilizarCampo(txtBuscar);
        estilizarBotonConsola(btnConsultar, cEncabezadoTabla);

        actualizarTextoAtajos();

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cerrar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Filtrar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F4)</b> <span style='color:FFFFFF;'>PDF</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F5)</b> <span style='color:#00FFCC;'>Tema: " + temaActual + "</span></html>");
        }
    }

    private void cargarFechasAutomaticas() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoy = LocalDate.now();
        LocalDate primerDiaMes = hoy.withDayOfMonth(1);

        txtFechaIni.setText(primerDiaMes.format(formato));
        txtFechaFin.setText(hoy.format(formato));
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        // ==========================================
        // 1. ZONA SUPERIOR
        // ==========================================
        panelNorteContenedor = new JPanel(new BorderLayout());
        panelNorteContenedor.setOpaque(false);
        panelNorteContenedor.setBorder(new EmptyBorder(10, 15, 8, 15));

        JPanel panelFila1 = new JPanel(new BorderLayout());
        panelFila1.setOpaque(false);

        JLabel lblTitulo = new JLabel("INFORME VENTAS POR CLIENTE");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTitulo.setForeground(cAcento);
        panelFila1.add(lblTitulo, BorderLayout.WEST);

        lblRegistros = new JLabel("Reg: 0");
        lblRegistros.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblRegistros.setForeground(cAcento);
        panelFila1.add(lblRegistros, BorderLayout.EAST);

        panelNorteContenedor.add(panelFila1, BorderLayout.NORTH);

        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFiltros.setOpaque(false);
        panelFiltros.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cAcento));

        JLabel lblF1 = new JLabel("DESDE:");
        lblF1.setFont(FUENTE_CONSOLA);
        lblF1.setForeground(cAcento);

        txtFechaIni = new JTextField(10);
        estilizarCampo(txtFechaIni);

        JLabel lblF2 = new JLabel("HASTA:");
        lblF2.setFont(FUENTE_CONSOLA);
        lblF2.setForeground(cAcento);

        txtFechaFin = new JTextField(10);
        estilizarCampo(txtFechaFin);

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

        btnConsultar = new JButton("Consultar");
        estilizarBotonConsola(btnConsultar, cEncabezadoTabla);
        btnConsultar.addActionListener(e -> consultarDatos());

        JLabel lblBuscar = new JLabel("  |  FILTRAR (RUT/Nombre):");
        lblBuscar.setFont(FUENTE_CONSOLA);
        lblBuscar.setForeground(cTextoClaro);

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
        // 2. TABLA CENTRAL CON RAYAS TRANSPARENTES
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
        tablaResultados.setBackground(cFondoNegro);
        tablaResultados.setForeground(cTextoClaro);
        tablaResultados.setSelectionBackground(cSeleccion);
        tablaResultados.setSelectionForeground(Color.WHITE);
        tablaResultados.setShowGrid(false);
        tablaResultados.setIntercellSpacing(new Dimension(0, 0));

        sorter = new TableRowSorter<>(modeloTabla);
        tablaResultados.setRowSorter(sorter);

        tablaResultados.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 13));
        tablaResultados.getTableHeader().setBackground(cEncabezadoTabla);
        tablaResultados.getTableHeader().setForeground(cAcento);
        tablaResultados.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

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

                switch (column) {
                    case 2:
                        setHorizontalAlignment(JLabel.CENTER);
                        break;
                    case 3:
                        setHorizontalAlignment(JLabel.RIGHT);
                        break;
                    default:
                        setHorizontalAlignment(JLabel.LEFT);
                        break;
                }
                return c;
            }
        };

        for (int i = 0; i < tablaResultados.getColumnCount(); i++) {
            tablaResultados.getColumnModel().getColumn(i).setCellRenderer(rendererRayas);
        }

        TableColumnModel colModel = tablaResultados.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(140);
        colModel.getColumn(1).setPreferredWidth(440);
        colModel.getColumn(2).setPreferredWidth(100);
        colModel.getColumn(3).setPreferredWidth(160);

        scrollPane = new JScrollPane(tablaResultados);
        scrollPane.getViewport().setBackground(cFondoNegro);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Totales y Barra de Comandos con F5)
        // ==========================================
        panelSurGeneral = new JPanel(new BorderLayout());
        panelSurGeneral.setBackground(cFondoNegro);

        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setOpaque(false);
        panelTotal.setBorder(new EmptyBorder(6, 15, 6, 15));

        lblTotalGeneral = new JLabel("TOTAL GENERAL VENTAS: $ 0.00");
        lblTotalGeneral.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTotalGeneral.setForeground(cAcento);
        panelTotal.add(lblTotalGeneral, BorderLayout.EAST);

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

    private void estilizarCampo(JTextField txt) {
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(new Color(Math.max(0, cFondoNegro.getRed() + 10), Math.max(0, cFondoNegro.getGreen() + 10), Math.max(0, cFondoNegro.getBlue() + 10)));
        txt.setForeground(cTextoClaro);
        txt.setCaretColor(cAcento);
        txt.setHorizontalAlignment(JTextField.CENTER);
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

    private void consultarDatos() {
        String fi = txtFechaIni.getText().trim();
        String ff = txtFechaFin.getText().trim();

        setTitle("INFORME DE VENTAS POR CLIENTE (" + fi + " al " + ff + ")");

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
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "ENFOQUE_BUSCAR");
        rootPane.getActionMap().put("ENFOQUE_BUSCAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtBuscar.requestFocusInWindow();
                txtBuscar.selectAll();
            }
        });

        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "EXPORTAR_PDF");
        rootPane.getActionMap().put("EXPORTAR_PDF", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarPDF();
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
