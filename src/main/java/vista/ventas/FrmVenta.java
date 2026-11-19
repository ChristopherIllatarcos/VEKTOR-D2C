package vista.ventas;

import conexion.Conexion;
import dao.ClienteDAO;
import dao.ParametrosDAO;
import dao.ProductoVentaDAO;
import dao.VendedorDAO;
import dao.VentaDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.ItemVentaDTO;
import modelo.ParametrosDTO;

public class FrmVenta extends JInternalFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmVenta.class.getName());

    private JTextField txtVendedor;
    private JLabel lblNombreVendedor;
    private JLabel lblFecha;

    private JTextField txtIngresoRapido;
    private JTextField txtCantidad;

    private JTable tablaVenta;
    private DefaultTableModel modeloTablaVenta;
    private JScrollPane scrollTabla;

    private JTable tablaBuscadorFlotante;
    private DefaultTableModel modeloBuscadorFlotante;
    private JScrollPane scrollBuscadorFlotante;

    private JLabel lblTotalGeneral;
    private JLabel lblAtajos;

    // Componentes estructurales para actualización dinámica de colores
    private JLayeredPane layeredPane;
    private JPanel panelSuperior, panelInferior;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelSuperior, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur, cPanelTablas;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 15);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    private String codigoBarraSeleccionado = "";
    private String descripcionSeleccionada = "";
    private double precioUnitarioSeleccionado = 0.0;
    private String fechaProcesoSistema = "";
    private double porcentajeDescuentoGlobal = 0.0; // Descuento aplicado con F10

    private JTextField txtRutCliente = new JTextField(15);
    private JTextField txtNombreCliente = new JTextField(25);
    private JTextField txtDireccion = new JTextField(25);
    private JTextField txtComuna = new JTextField(15);
    private JTextField txtSector = new JTextField(15);
    private JTextField txtCelular = new JTextField(12);
    private JComboBox<String> cmbCondicionPago = new JComboBox<>(new String[]{"Contado", "Crédito"});
    private JTextField txtDias = new JTextField(5);
    private JTextField txtPlazo = new JTextField(10);
    private JTextField txtCiudad = new JTextField(15);
    private JTextField txtCodigoProducto = new JTextField(15);

    public FrmVenta() {
        super("1.- Ingreso de Venta", false, false, false, false);
        putClientProperty("JInternalFrame.isPalette", Boolean.FALSE);

        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        cargarFechaDesdeParametros();

        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
    }

    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondoNegro = new Color(10, 10, 15);
                cPanelSuperior = new Color(45, 10, 55);
                cPanelTablas = new Color(15, 10, 20);
                cEncabezadoTabla = new Color(90, 10, 120);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cSeleccion = new Color(140, 20, 160);
                cBarraSur = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondoNegro = new Color(5, 15, 20);
                cPanelSuperior = new Color(10, 40, 50);
                cPanelTablas = new Color(5, 20, 25);
                cEncabezadoTabla = new Color(0, 80, 95);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cSeleccion = new Color(0, 120, 140);
                cBarraSur = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondoNegro = new Color(5, 20, 15);
                cPanelSuperior = new Color(10, 45, 30);
                cPanelTablas = new Color(5, 25, 18);
                cEncabezadoTabla = new Color(15, 80, 50);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cSeleccion = new Color(0, 140, 80);
                cBarraSur = new Color(0, 60, 35);
                break;
            case "Clipper Clásico":
                cFondoNegro = Color.BLACK;
                cPanelSuperior = new Color(0, 0, 168);
                cPanelTablas = new Color(0, 0, 120);
                cEncabezadoTabla = new Color(0, 0, 168);
                cAcento = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cSeleccion = new Color(0, 168, 168);
                cBarraSur = new Color(168, 0, 0);
                break;
            case "Naranja Fuego":
            default:
                cFondoNegro = new Color(15, 10, 8);
                cPanelSuperior = new Color(40, 18, 12);
                cPanelTablas = new Color(25, 12, 10);
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

        if (layeredPane != null) {
            layeredPane.setBackground(cFondoNegro);
        }
        if (panelSuperior != null) {
            panelSuperior.setBackground(cPanelSuperior);
            panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
        }
        if (panelInferior != null) {
            panelInferior.setBackground(cPanelSuperior);
            panelInferior.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, cAcento));
        }
        if (lblNombreVendedor != null) {
            lblNombreVendedor.setForeground(cAcento);
        }
        if (lblFecha != null) {
            lblFecha.setForeground(cAcento);
        }
        if (lblTotalGeneral != null) {
            lblTotalGeneral.setForeground(cAcento);
        }

        if (tablaVenta != null) {
            tablaVenta.setBackground(cPanelTablas);
            tablaVenta.setForeground(cTextoClaro);
            tablaVenta.setSelectionBackground(cSeleccion);
            tablaVenta.getTableHeader().setBackground(cEncabezadoTabla);
            tablaVenta.getTableHeader().setForeground(cAcento);
            tablaVenta.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cPanelTablas);
            scrollTabla.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        }

        if (tablaBuscadorFlotante != null) {
            tablaBuscadorFlotante.setBackground(cPanelTablas);
            tablaBuscadorFlotante.setForeground(cTextoClaro);
            tablaBuscadorFlotante.setSelectionBackground(cSeleccion);
            tablaBuscadorFlotante.getTableHeader().setBackground(cEncabezadoTabla);
            tablaBuscadorFlotante.getTableHeader().setForeground(cAcento);
            tablaBuscadorFlotante.repaint();
        }

        if (scrollBuscadorFlotante != null) {
            scrollBuscadorFlotante.getViewport().setBackground(cPanelTablas);
            scrollBuscadorFlotante.setBorder(BorderFactory.createLineBorder(cAcento, 2));
        }

        estilizarCampoTexto(txtVendedor);
        estilizarCampoTexto(txtIngresoRapido);
        estilizarCampoTexto(txtCantidad);

        actualizarTextoAtajos();

        if (layeredPane != null) {
            layeredPane.revalidate();
            layeredPane.repaint();
        }
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        String descTexto = porcentajeDescuentoGlobal > 0 ? " (" + porcentajeDescuentoGlobal + "%)" : "";
        if (lblAtajos != null) {
            lblAtajos.setText("<html>(Esc)apar &nbsp;&nbsp; (F4): Eliminar &nbsp;&nbsp; (F8): Cliente &nbsp;&nbsp; (F10): Descto" + descTexto + " &nbsp;&nbsp; (F11): PDF &nbsp;&nbsp; (F12): Guardar &nbsp;&nbsp;|&nbsp;&nbsp; <b style='color:#FFFF55;'>(F5): Tema: " + temaActual + "</b></html>");
        }
    }

    private void cargarFechaDesdeParametros() {
        try (Connection con = Conexion.getConnection()) {
            ParametrosDAO pDao = new ParametrosDAO();
            ParametrosDTO param = pDao.obtenerParametros(con);
            if (param.getFecha() != null && !param.getFecha().trim().isEmpty()) {
                fechaProcesoSistema = param.getFecha().trim();
            } else {
                fechaProcesoSistema = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (Exception e) {
            fechaProcesoSistema = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        lblFecha.setText("Fecha: " + fechaProcesoSistema);
    }

    private void initComponentsManual() {
        setSize(1100, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);
        layeredPane.setBackground(cFondoNegro);

        Font fuenteConsola = FUENTE_CONSOLA;

        // 1. PANEL SUPERIOR
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBounds(0, 0, 1100, 80);
        panelSuperior.setBackground(cPanelSuperior);
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        JPanel panelVendedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        panelVendedor.setOpaque(false);

        JLabel lblVende = new JLabel("Vende:");
        lblVende.setForeground(cTextoClaro);
        lblVende.setFont(fuenteConsola);

        txtVendedor = new JTextField(6);
        estilizarCampoTexto(txtVendedor);
        txtVendedor.addActionListener(e -> {
            buscarVendedor();
            txtIngresoRapido.requestFocusInWindow();
            txtIngresoRapido.selectAll();
        });

        lblNombreVendedor = new JLabel("");
        lblNombreVendedor.setForeground(cAcento);
        lblNombreVendedor.setFont(fuenteConsola);

        panelVendedor.add(lblVende);
        panelVendedor.add(txtVendedor);
        panelVendedor.add(lblNombreVendedor);

        JPanel panelTitulo = new JPanel();
        panelTitulo.setOpaque(false);
        panelTitulo.setLayout(new BoxLayout(panelTitulo, BoxLayout.Y_AXIS));
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel lblTituloPrincipal = new JLabel("INGRESO DE VENTA");
        lblTituloPrincipal.setForeground(cAcento);
        lblTituloPrincipal.setFont(new Font(fuenteConsola.getFamily(), Font.BOLD, 17));
        lblTituloPrincipal.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmpresa = new JLabel("— CASA VICTORIA —");
        lblEmpresa.setForeground(cTextoClaro);
        lblEmpresa.setFont(fuenteConsola);
        lblEmpresa.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelTitulo.add(lblTituloPrincipal);
        panelTitulo.add(lblEmpresa);

        JPanel panelDerecha = new JPanel(new GridLayout(2, 1, 0, 2));
        panelDerecha.setOpaque(false);
        panelDerecha.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 25));

        lblFecha = new JLabel("Fecha: --/--/----", JLabel.RIGHT);
        lblFecha.setForeground(cAcento);
        lblFecha.setFont(fuenteConsola);

        JPanel panelTotalBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelTotalBox.setOpaque(false);

        JLabel lblTextoTotal = new JLabel("TOTAL: $");
        lblTextoTotal.setForeground(cTextoClaro);
        lblTextoTotal.setFont(fuenteConsola);

        lblTotalGeneral = new JLabel("0");
        lblTotalGeneral.setForeground(cAcento);
        lblTotalGeneral.setFont(new Font(fuenteConsola.getFamily(), Font.BOLD, 18));

        panelTotalBox.add(lblTextoTotal);
        panelTotalBox.add(lblTotalGeneral);

        panelDerecha.add(lblFecha);
        panelDerecha.add(panelTotalBox);

        panelSuperior.add(panelVendedor, BorderLayout.WEST);
        panelSuperior.add(panelTitulo, BorderLayout.CENTER);
        panelSuperior.add(panelDerecha, BorderLayout.EAST);

        layeredPane.add(panelSuperior, JLayeredPane.DEFAULT_LAYER);

        // 2. CENTRO (Tabla de Ventas)
        String[] columnasVenta = {"N°", "#Barra", "Descripción", "Cantidad", "Neto", "%", "Total"};
        modeloTablaVenta = new DefaultTableModel(columnasVenta, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVenta = new JTable(modeloTablaVenta);
        tablaVenta.setFont(fuenteConsola);
        tablaVenta.setBackground(cPanelTablas);
        tablaVenta.setForeground(cTextoClaro);
        tablaVenta.setGridColor(cAcento);
        tablaVenta.setRowHeight(26);
        tablaVenta.setShowGrid(false);
        tablaVenta.setIntercellSpacing(new Dimension(0, 0));
        tablaVenta.setSelectionBackground(cSeleccion);
        tablaVenta.setSelectionForeground(Color.WHITE);

        tablaVenta.getTableHeader().setFont(fuenteConsola);
        tablaVenta.getTableHeader().setBackground(cEncabezadoTabla);
        tablaVenta.getTableHeader().setForeground(cAcento);
        tablaVenta.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        DefaultTableCellRenderer rendererRayasVenta = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(cPanelTablas);
                    } else {
                        c.setBackground(new Color(
                                Math.min(255, cPanelTablas.getRed() + 15),
                                Math.min(255, cPanelTablas.getGreen() + 15),
                                Math.min(255, cPanelTablas.getBlue() + 15)
                        ));
                    }
                    c.setForeground(cTextoClaro);
                } else {
                    c.setBackground(cSeleccion);
                    c.setForeground(Color.WHITE);
                }

                switch (column) {
                    case 3, 4, 5, 6 ->
                        setHorizontalAlignment(JLabel.RIGHT);
                    case 0 ->
                        setHorizontalAlignment(JLabel.CENTER);
                    default ->
                        setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tablaVenta.getColumnCount(); i++) {
            tablaVenta.getColumnModel().getColumn(i).setCellRenderer(rendererRayasVenta);
        }

        scrollTabla = new JScrollPane(tablaVenta);
        scrollTabla.setBounds(0, 80, 1100, 515);
        scrollTabla.getViewport().setBackground(cPanelTablas);
        scrollTabla.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        layeredPane.add(scrollTabla, JLayeredPane.DEFAULT_LAYER);

        // 3. TABLA FLOTANTE DE BÚSQUEDA
        String[] columnasBuscador = {"Barra", "Descripción del Producto", "Precio Público"};
        modeloBuscadorFlotante = new DefaultTableModel(columnasBuscador, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaBuscadorFlotante = new JTable(modeloBuscadorFlotante);
        tablaBuscadorFlotante.setFont(fuenteConsola);
        tablaBuscadorFlotante.setBackground(cPanelTablas);
        tablaBuscadorFlotante.setForeground(cTextoClaro);
        tablaBuscadorFlotante.setGridColor(cAcento);
        tablaBuscadorFlotante.setRowHeight(26);
        tablaBuscadorFlotante.setSelectionBackground(cSeleccion);
        tablaBuscadorFlotante.setSelectionForeground(Color.WHITE);

        tablaBuscadorFlotante.getTableHeader().setFont(fuenteConsola);
        tablaBuscadorFlotante.getTableHeader().setBackground(cEncabezadoTabla);
        tablaBuscadorFlotante.getTableHeader().setForeground(cAcento);
        tablaBuscadorFlotante.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        scrollBuscadorFlotante = new JScrollPane(tablaBuscadorFlotante);
        scrollBuscadorFlotante.setBounds(150, 120, 800, 350);
        scrollBuscadorFlotante.getViewport().setBackground(cPanelTablas);
        scrollBuscadorFlotante.setBorder(BorderFactory.createLineBorder(cAcento, 2));
        scrollBuscadorFlotante.setVisible(false);
        layeredPane.add(scrollBuscadorFlotante, JLayeredPane.PALETTE_LAYER);

        tablaBuscadorFlotante.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    seleccionarProductoFlotante();
                }
            }
        });

        // 4. PANEL INFERIOR
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBounds(0, 595, 1100, 105);
        panelInferior.setBackground(cPanelSuperior);
        panelInferior.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, cAcento));

        JPanel panelInputsLinea = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelInputsLinea.setOpaque(false);

        JLabel lblIngreso = new JLabel("Barra/Prod:");
        lblIngreso.setForeground(cTextoClaro);
        lblIngreso.setFont(fuenteConsola);

        txtIngresoRapido = new JTextField(20);
        estilizarCampoTexto(txtIngresoRapido);
        txtIngresoRapido.addActionListener(e -> filtrarYMostrarBuscador());

        JLabel lblCant = new JLabel("uni:");
        lblCant.setForeground(cTextoClaro);
        lblCant.setFont(fuenteConsola);

        txtCantidad = new JTextField(5);
        estilizarCampoTexto(txtCantidad);
        txtCantidad.setText("1");
        txtCantidad.setEnabled(false);
        txtCantidad.addActionListener(e -> confirmarYAgregarItemTabla());

        panelInputsLinea.add(lblIngreso);
        panelInputsLinea.add(txtIngresoRapido);
        panelInputsLinea.add(lblCant);
        panelInputsLinea.add(txtCantidad);

        lblAtajos = new JLabel();
        lblAtajos.setForeground(cAcento);
        lblAtajos.setFont(new Font(fuenteConsola.getFamily(), Font.BOLD, 13));
        lblAtajos.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        actualizarTextoAtajos();

        panelInferior.add(panelInputsLinea, BorderLayout.NORTH);
        panelInferior.add(lblAtajos, BorderLayout.SOUTH);

        layeredPane.add(panelInferior, JLayeredPane.DEFAULT_LAYER);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();

                if (w > 0 && h > 0) {
                    panelSuperior.setBounds(0, 0, w, 80);
                    scrollTabla.setBounds(0, 80, w, h - 185);
                    panelInferior.setBounds(0, h - 105, w, 105);

                    int buscadorW = Math.min(850, w - 150);
                    scrollBuscadorFlotante.setBounds((w - buscadorW) / 2, 100, buscadorW, Math.min(400, h - 180));
                }
            }
        });

        getRootPane().registerKeyboardAction(ev -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        atajof8();
        atajof4();
        atajof10Descuento(); // NUEVO: Atajo F10 activado

        // Atajos de teclado generales (F5, F11, F12)
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "cambiarTemaF5");
        getRootPane().getActionMap().put("cambiarTemaF5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "guardarVentaF12");
        getRootPane().getActionMap().put("guardarVentaF12", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                finalizarYGuardarVenta();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "exportarPDFF11");
        getRootPane().getActionMap().put("exportarPDFF11", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportarVentaPDF();
            }
        });

        buscarVendedor();
        txtIngresoRapido.requestFocusInWindow();

        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });
    }

    private void estilizarCampoTexto(JTextField txt) {
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(cPanelTablas);
        txt.setForeground(cAcento);
        txt.setCaretColor(cAcento);
        txt.setBorder(BorderFactory.createLineBorder(cAcento, 1));
    }

    private void atajof8() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "abrirBuscarCliente");

        getRootPane().getActionMap().put("abrirBuscarCliente", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(FrmVenta.this);
                DlgClientes buscador = new DlgClientes(parentWindow);
                try {
                    ClienteDAO dao = new ClienteDAO();
                    List<Object[]> listaClientes = dao.cargarClientesDAO("");
                    buscador.cargarClientesEnTabla(listaClientes);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FrmVenta.this,
                            "Error al cargar los clientes: " + ex.getMessage(),
                            "Error de Base de Datos",
                            JOptionPane.ERROR_MESSAGE);
                }

                buscador.setVisible(true);

                String[] datosCliente = buscador.getClienteSeleccionado();
                if (datosCliente != null) {
                    if (datosCliente.length > 0) {
                        txtRutCliente.setText(datosCliente[0]);
                    }
                    if (datosCliente.length > 1) {
                        txtNombreCliente.setText(datosCliente[1]);
                    }
                    if (datosCliente.length > 2) {
                        txtDireccion.setText(datosCliente[2]);
                    }
                    if (datosCliente.length > 3) {
                        txtComuna.setText(datosCliente[3]);
                    }
                    if (datosCliente.length > 4) {
                        txtSector.setText(datosCliente[4]);
                    }
                    if (datosCliente.length > 5) {
                        txtCelular.setText(datosCliente[5]);
                    }
                    if (datosCliente.length > 6) {
                        txtCiudad.setText(datosCliente[6]);
                    }
                    txtIngresoRapido.requestFocusInWindow();
                }
            }
        });
    }

    private void atajof4() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "eliminarItemF4");

        getRootPane().getActionMap().put("eliminarItemF4", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int filaSeleccionada = tablaVenta.getSelectedRow();
                if (filaSeleccionada != -1) {
                    modeloTablaVenta.removeRow(filaSeleccionada);
                    for (int i = 0; i < modeloTablaVenta.getRowCount(); i++) {
                        modeloTablaVenta.setValueAt(i + 1, i, 0);
                    }
                    actualizarTotalGeneral();
                    JOptionPane.showMessageDialog(FrmVenta.this, "Ítem eliminado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(FrmVenta.this, "Seleccione un ítem de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    private void atajof10Descuento() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "aplicarDescuentoF10");

        getRootPane().getActionMap().put("aplicarDescuentoF10", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Window ventanaPadre = SwingUtilities.getWindowAncestor(FrmVenta.this);

                // Instanciamos la clase interna directamente
                DlgDescuentoGlobal dlg = new DlgDescuentoGlobal(ventanaPadre);
                dlg.setVisible(true);

                double resultado = dlg.getDescuentoSeleccionado();
                if (resultado >= 0) {
                    porcentajeDescuentoGlobal = resultado;
                    actualizarTotalGeneral();
                    actualizarTextoAtajos();
                    JOptionPane.showMessageDialog(FrmVenta.this, "Descuento de " + resultado + "% aplicado con éxito.");
                }
            }
        });
    }

    private void exportarVentaPDF() {
        if (modeloTablaVenta.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay productos en la venta actual para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String vendedor = lblNombreVendedor.getText() != null ? lblNombreVendedor.getText() : "Desconocido";
        String total = lblTotalGeneral.getText();
        utilidades.PDFUtils.generarComprobanteVenta(vendedor, total, modeloTablaVenta);
    }

    private void buscarVendedor() {
        try {
            String textoCodigo = txtVendedor.getText().trim();
            if (textoCodigo.isEmpty()) {
                lblNombreVendedor.setText("INGRESE CÓDIGO");
                return;
            }
            int codigoVendedor = Integer.parseInt(textoCodigo);
            VendedorDAO dao = new VendedorDAO();
            String nombreVendedor = dao.obtenerNombreVendedor(codigoVendedor);

            if (nombreVendedor != null && !nombreVendedor.isEmpty()) {
                lblNombreVendedor.setText(nombreVendedor);
                txtIngresoRapido.requestFocusInWindow();
                txtIngresoRapido.selectAll();
            } else {
                lblNombreVendedor.setText("VENDEDOR NO ENCONTRADO");
                txtVendedor.requestFocusInWindow();
                txtVendedor.selectAll();
            }
        } catch (NumberFormatException e) {
            lblNombreVendedor.setText("CÓDIGO INVÁLIDO");
        } catch (Exception e) {
            lblNombreVendedor.setText("ERROR AL BUSCAR");
            System.err.println("Error en buscarVendedor: " + e.getMessage());
        }
    }

    private void confirmarYAgregarItemTabla() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                cantidad = 1;
            }

            double totalItem = precioUnitarioSeleccionado * cantidad;

            // Aplicar descuento global si está activo
            if (porcentajeDescuentoGlobal > 0) {
                totalItem = totalItem * (1 - (porcentajeDescuentoGlobal / 100.0));
            }

            double netoItem = totalItem / 1.19;

            modeloTablaVenta.addRow(new Object[]{
                modeloTablaVenta.getRowCount() + 1,
                codigoBarraSeleccionado,
                descripcionSeleccionada,
                cantidad,
                String.format("$%,.0f", netoItem),
                porcentajeDescuentoGlobal > 0 ? porcentajeDescuentoGlobal + "%" : "19%",
                String.format("$%,.0f", totalItem)
            });

            actualizarTotalGeneral();

            txtIngresoRapido.setText("");
            txtCantidad.setText("1");
            txtCantidad.setEnabled(false);
            txtIngresoRapido.requestFocusInWindow();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad válida.", "Error", JOptionPane.ERROR_MESSAGE);
            txtCantidad.requestFocusInWindow();
        }
    }

    private void filtrarYMostrarBuscador() {
        String texto = txtIngresoRapido.getText().trim();
        modeloBuscadorFlotante.setRowCount(0);

        ProductoVentaDAO dao = new ProductoVentaDAO();
        List<Object[]> listaProductos = dao.buscarProductosYVendedorDAO(texto);

        for (Object[] prod : listaProductos) {
            String barra = prod[0] != null ? prod[0].toString() : "";
            String desc = prod[1] != null ? prod[1].toString() : "";
            String color = prod[2] != null ? prod[2].toString() : "";

            if (!color.isEmpty() && !color.equalsIgnoreCase("NULL")) {
                desc = desc + " (" + color + ")";
            }

            double precio = 0.0;
            try {
                if (prod[3] != null) {
                    precio = Double.parseDouble(prod[3].toString());
                }
            } catch (NumberFormatException e) {
                precio = 0.0;
            }

            modeloBuscadorFlotante.addRow(new Object[]{barra, desc, String.format("$%,.2f", precio)});
        }

        if (modeloBuscadorFlotante.getRowCount() > 0) {
            tablaBuscadorFlotante.setRowSelectionInterval(0, 0);
            scrollBuscadorFlotante.setVisible(true);
            tablaBuscadorFlotante.requestFocusInWindow();
        } else {
            scrollBuscadorFlotante.setVisible(false);
            JOptionPane.showMessageDialog(this, "No se encontraron productos en la base de datos.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void seleccionarProductoFlotante() {
        int fila = tablaBuscadorFlotante.getSelectedRow();
        if (fila != -1) {
            codigoBarraSeleccionado = modeloBuscadorFlotante.getValueAt(fila, 0).toString();
            descripcionSeleccionada = modeloBuscadorFlotante.getValueAt(fila, 1).toString();

            String precioStr = modeloBuscadorFlotante.getValueAt(fila, 2).toString()
                    .replace("$", "").replace(".", "").replace(",", ".").trim();
            precioUnitarioSeleccionado = Double.parseDouble(precioStr);

            scrollBuscadorFlotante.setVisible(false);

            txtIngresoRapido.setText(codigoBarraSeleccionado + " - " + descripcionSeleccionada);
            txtCantidad.setEnabled(true);
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
        }
    }

    private void actualizarTotalGeneral() {
        double sumaTotal = 0;
        for (int i = 0; i < modeloTablaVenta.getRowCount(); i++) {
            String valStr = modeloTablaVenta.getValueAt(i, 6).toString()
                    .replace("$", "").replace(".", "").replace(",", "").trim();
            try {
                sumaTotal += Double.parseDouble(valStr);
            } catch (NumberFormatException ignored) {
            }
        }
        lblTotalGeneral.setText(String.format("%,.0f", sumaTotal));
    }

    private String convertirFechaParaBD(String fechaDdMmYyyy) {
        try {
            DateTimeFormatter entrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter salida = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(fechaDdMmYyyy, entrada).format(salida);
        } catch (Exception e) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    private void finalizarYGuardarVenta() {
        if (modeloTablaVenta.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay productos en la venta actual.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String codigoVendedorStr = txtVendedor.getText().trim();
        if (codigoVendedorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un código de vendedor válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection con = null;
        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false); // <--- Iniciamos transacción segura

            int codigoVendedor = Integer.parseInt(codigoVendedorStr);
            String rutClienteStr = txtRutCliente.getText().trim();
            int codigoCliente = 0;
            if (!rutClienteStr.isEmpty()) {
                try {
                    codigoCliente = Integer.parseInt(rutClienteStr.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {
                }
            }

            ParametrosDAO paramDAO = new ParametrosDAO();
            ParametrosDTO param = paramDAO.obtenerParametros(con);

            String fechaParaBD = convertirFechaParaBD(fechaProcesoSistema);
            int folioVenta = (int) (param.getBoleta() + 1);

            List<ItemVentaDTO> listaItems = new ArrayList<>();
            for (int i = 0; i < modeloTablaVenta.getRowCount(); i++) {
                String barra = modeloTablaVenta.getValueAt(i, 1).toString();
                int cant = Integer.parseInt(modeloTablaVenta.getValueAt(i, 3).toString());

                String totalStr = modeloTablaVenta.getValueAt(i, 6).toString()
                        .replace("$", "").replace(".", "").replace(",", "").trim();
                double totalItem = Double.parseDouble(totalStr);
                double precioUnit = totalItem / cant;

                listaItems.add(new ItemVentaDTO(barra, cant, precioUnit, totalItem));
            }

            VentaDAO ventaDAO = new VendedorDAO() != null ? new VentaDAO() : new VentaDAO();
            boolean exitoVenta = ventaDAO.guardarVenta(folioVenta, codigoCliente, fechaParaBD, codigoVendedor, listaItems);

            if (exitoVenta) {
                param.setBoleta(folioVenta);
                paramDAO.guardarParametros(con, param);

                con.commit(); // <--- Confirmamos cambios si todo sale bien

                JOptionPane.showMessageDialog(this,
                        "¡Venta registrada con éxito!\nBoleta N°: " + folioVenta + "\nFecha Proceso: " + fechaProcesoSistema);

                modeloTablaVenta.setRowCount(0);
                lblTotalGeneral.setText("0");
                txtIngresoRapido.setText("");
                txtVendedor.setText("");
                lblNombreVendedor.setText("");
                porcentajeDescuentoGlobal = 0.0;
                actualizarTextoAtajos();
                txtVendedor.requestFocusInWindow();
            } else {
                con.rollback(); // <--- Revertimos si falló el guardado
                JOptionPane.showMessageDialog(this, "No se pudo guardar la venta en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            if (con != null) {
                try {
                    con.rollback(); // <--- Revertimos ante cualquier excepción inesperada
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error crítico en la transacción: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
    
    private class DlgDescuentoGlobal extends JDialog {
        private JTextField txtDescuento;
        private double descuentoSeleccionado = -1.0;

        public DlgDescuentoGlobal(Window parent) {
            super(parent, " Descuento Global ", ModalityType.APPLICATION_MODAL);
            setSize(400, 180);
            setLocationRelativeTo(parent);
            setUndecorated(true); // Estilo ventana limpia / retro

            JPanel panelContenedor = new JPanel(new BorderLayout());
            panelContenedor.setBackground(cFondoNegro);
            panelContenedor.setBorder(BorderFactory.createLineBorder(cAcento, 2));

            // Título superior estilo Clipper
            JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelNorte.setBackground(cPanelSuperior);
            panelNorte.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            
            JLabel lblTitulo = new JLabel("=== APLICAR DESCUENTO GLOBAL ===");
            lblTitulo.setForeground(cAcento);
            lblTitulo.setFont(new Font(FUENTE_CONSOLA.getFamily(), Font.BOLD, 14));
            panelNorte.add(lblTitulo);
            panelContenedor.add(panelNorte, BorderLayout.NORTH);

            // Centro con el campo de texto
            JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
            panelCentro.setBackground(cFondoNegro);

            JLabel lblEtiqueta = new JLabel("Porcentaje (%):");
            lblEtiqueta.setForeground(cTextoClaro);
            lblEtiqueta.setFont(FUENTE_CONSOLA);

            txtDescuento = new JTextField(8);
            txtDescuento.setFont(FUENTE_CONSOLA);
            txtDescuento.setBackground(cPanelSuperior);
            txtDescuento.setForeground(cAcento);
            txtDescuento.setCaretColor(cAcento);
            txtDescuento.setBorder(BorderFactory.createLineBorder(cAcento, 1));
            txtDescuento.addActionListener(e -> confirmarDescuento());

            panelCentro.add(lblEtiqueta);
            panelCentro.add(txtDescuento);
            panelContenedor.add(panelCentro, BorderLayout.CENTER);

            // Barra inferior de atajos estilo Clipper
            JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelSur.setBackground(cPanelSuperior);
            panelSur.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            JLabel lblAtajos = new JLabel("[ENTER] Aceptar   |   [ESC] Cancelar");
            lblAtajos.setForeground(cTextoClaro);
            lblAtajos.setFont(new Font(FUENTE_CONSOLA.getFamily(), Font.BOLD, 12));
            panelSur.add(lblAtajos);
            panelContenedor.add(panelSur, BorderLayout.SOUTH);

            add(panelContenedor);

            // Atajos de teclado (Escape para cerrar)
            getRootPane().registerKeyboardAction(ev -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            SwingUtilities.invokeLater(() -> txtDescuento.requestFocusInWindow());
        }

        private void confirmarDescuento() {
            try {
                double valor = Double.parseDouble(txtDescuento.getText().trim());
                if (valor < 0 || valor > 100) {
                    JOptionPane.showMessageDialog(this, "El descuento debe estar entre 0% y 100%.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                descuentoSeleccionado = valor;
                dispose();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese un valor numérico válido.", "Error", JOptionPane.ERROR_MESSAGE);
                txtDescuento.requestFocusInWindow();
                txtDescuento.selectAll();
            }
        }

        public double getDescuentoSeleccionado() {
            return descuentoSeleccionado;
        }
    }
    
    
}
