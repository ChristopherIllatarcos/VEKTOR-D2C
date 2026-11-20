package vista.ctacte;

import dao.ClienteDAO;
import dao.CuentaCorrienteDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import vista.ventas.DlgClientes;

/**
 * Módulo CTACTE - Cartola Interna de Cuenta Corriente por Cliente Estilo MS-DOS
 * / Clipper adaptado a MDI JInternalFrame
 *
 * @author caill
 */
public class FrmCartolaCliente extends JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(FrmCartolaCliente.class.getName());

    private JTextField txtRutCliente;
    private JLabel lblNombreCliente;
    private JLabel lblTotalDeuda;
    private JLabel lblAtajos;
    private JLabel lblSubTitulo;
    private JLabel lblRut;
    private JLabel lblAyudaF8;
    private JLabel lblNom;
    private JLabel lblTextoDeuda;

    private JTable tablaCartola;
    private DefaultTableModel modeloTablaCartola;

    // Componentes estructurales para actualización dinámica de colores
    private JLayeredPane layeredPane;
    private JPanel panelSuperior;
    private JPanel panelInferior;
    private JScrollPane scrollCartola;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cTablaFondo, cBordeAcento, cTextoAmarillo, cTextoBlanco, cBarraInferior;
    private final Font fuenteConsola = new Font("Consolas", Font.BOLD, 15);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmCartolaCliente() {
        super("4.- Cartola Interna CtaCte Cliente", false, false, false, false);
        putClientProperty("JInternalFrame.isPalette", Boolean.FALSE);

        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        configurarTablaDetalle();
        modeloTablaCartola = (DefaultTableModel) tablaCartola.getModel();

        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new javax.swing.ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono interno", e);
        }
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cEncabezado = new Color(45, 10, 55);
                cTablaFondo = new Color(15, 10, 20);
                cBordeAcento = new Color(255, 110, 220);
                cTextoAmarillo = new Color(255, 110, 220);
                cTextoBlanco = new Color(255, 230, 255);
                cBarraInferior = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cEncabezado = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cBordeAcento = new Color(0, 245, 255);
                cTextoAmarillo = new Color(0, 245, 255);
                cTextoBlanco = new Color(220, 255, 255);
                cBarraInferior = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cEncabezado = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cBordeAcento = new Color(0, 255, 128);
                cTextoAmarillo = new Color(0, 255, 128);
                cTextoBlanco = new Color(220, 255, 240);
                cBarraInferior = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cEncabezado = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cBordeAcento = new Color(245, 73, 39);
                cTextoAmarillo = new Color(255, 180, 100);
                cTextoBlanco = new Color(255, 235, 225);
                cBarraInferior = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 128);
                cEncabezado = new Color(0, 0, 128);
                cTablaFondo = new Color(16, 16, 16);
                cBordeAcento = Color.WHITE;
                cTextoAmarillo = Color.YELLOW;
                cTextoBlanco = Color.WHITE;
                cBarraInferior = new Color(0, 0, 128);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (layeredPane != null) {
            layeredPane.setBackground(cFondo);
        }

        if (panelSuperior != null) {
            panelSuperior.setBackground(cEncabezado);
            panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cBordeAcento));
        }

        if (lblSubTitulo != null) {
            lblSubTitulo.setForeground(cTextoAmarillo);
        }
        if (lblRut != null) {
            lblRut.setForeground(cTextoBlanco);
        }
        if (lblAyudaF8 != null) {
            lblAyudaF8.setForeground(cTextoAmarillo);
        }
        if (lblNom != null) {
            lblNom.setForeground(cTextoBlanco);
        }
        if (lblNombreCliente != null) {
            lblNombreCliente.setForeground(cTextoAmarillo);
        }

        if (txtRutCliente != null) {
            txtRutCliente.setBackground(cTablaFondo);
            txtRutCliente.setForeground(cTextoAmarillo);
            txtRutCliente.setCaretColor(cTextoAmarillo);
            txtRutCliente.setBorder(BorderFactory.createLineBorder(cBordeAcento, 1));
        }

        if (tablaCartola != null) {
            tablaCartola.setBackground(cTablaFondo);
            tablaCartola.setForeground(cTextoBlanco);
            tablaCartola.getTableHeader().setBackground(cEncabezado);
            tablaCartola.getTableHeader().setForeground(cTextoAmarillo);
            tablaCartola.repaint();
        }

        if (scrollCartola != null) {
            scrollCartola.getViewport().setBackground(cTablaFondo);
            scrollCartola.setBorder(BorderFactory.createLineBorder(cBordeAcento, 1));
        }

        if (panelInferior != null) {
            panelInferior.setBackground(cBarraInferior);
            panelInferior.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, cBordeAcento));
        }

        if (lblTextoDeuda != null) {
            lblTextoDeuda.setForeground(cTextoBlanco);
        }
        if (lblTotalDeuda != null) {
            lblTotalDeuda.setForeground(cTextoAmarillo);
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("  (Esc)apar    (F8): Buscar Cliente    (F2): Detalle    (F6) Tema: " + temaActual + "    (F7): Imprimir PDF");
        }
    }

    private void initComponentsManual() {
        setSize(1100, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Font fConsola = fuenteConsola;
        if (fConsola.getFamily().equals("Dialog")) {
            fConsola = new Font("Monospaced", Font.BOLD, 15);
        }

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);
        layeredPane.setBackground(cFondo);

        // ==========================================
        // 1. PANEL SUPERIOR (Cabecera y Buscador de Cliente)
        // ==========================================
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBounds(0, 0, 1100, 110);
        panelSuperior.setBackground(cEncabezado);
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cBordeAcento));

        JPanel panelTituloBox = new JPanel();
        panelTituloBox.setOpaque(false);
        panelTituloBox.setLayout(new BoxLayout(panelTituloBox, BoxLayout.Y_AXIS));
        panelTituloBox.setBorder(BorderFactory.createEmptyBorder(8, 15, 5, 15));

        lblSubTitulo = new JLabel(" CARTOLA INTERNA CTACTE CLIENTE ");
        lblSubTitulo.setForeground(cTextoAmarillo);
        lblSubTitulo.setFont(new Font(fConsola.getFamily(), Font.BOLD, 16));
        panelTituloBox.add(lblSubTitulo);

        JPanel panelDatosFiltro = new JPanel(new GridLayout(2, 1, 0, 5));
        panelDatosFiltro.setOpaque(false);
        panelDatosFiltro.setBorder(BorderFactory.createEmptyBorder(0, 15, 8, 15));

        // Fila 1: RUT
        JPanel filaRut = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaRut.setOpaque(false);

        lblRut = new JLabel("Ingrese R.U.T.:");
        lblRut.setForeground(cTextoBlanco);
        lblRut.setFont(fConsola);

        txtRutCliente = new JTextField(15);
        txtRutCliente.setFont(fConsola);
        txtRutCliente.setBackground(cTablaFondo);
        txtRutCliente.setForeground(cTextoAmarillo);
        txtRutCliente.setCaretColor(cTextoAmarillo);
        txtRutCliente.setBorder(BorderFactory.createLineBorder(cBordeAcento, 1));
        txtRutCliente.addActionListener(e -> buscarClienteYCartola());

        lblAyudaF8 = new JLabel("(Presione F8 para buscar cliente)");
        lblAyudaF8.setForeground(cTextoAmarillo);
        lblAyudaF8.setFont(new Font(fConsola.getFamily(), Font.ITALIC, 12));

        filaRut.add(lblRut);
        filaRut.add(txtRutCliente);
        filaRut.add(lblAyudaF8);

        // Fila 2: Nombre
        JPanel filaNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaNombre.setOpaque(false);

        lblNom = new JLabel("Nombre del Cliente:");
        lblNom.setForeground(cTextoBlanco);
        lblNom.setFont(fConsola);

        lblNombreCliente = new JLabel("[ Seleccione o ingrese un RUT válido ]");
        lblNombreCliente.setForeground(cTextoAmarillo);
        lblNombreCliente.setFont(fConsola);

        filaNombre.add(lblNom);
        filaNombre.add(lblNombreCliente);

        panelDatosFiltro.add(filaRut);
        panelDatosFiltro.add(filaNombre);

        panelSuperior.add(panelTituloBox, BorderLayout.NORTH);
        panelSuperior.add(panelDatosFiltro, BorderLayout.CENTER);
        layeredPane.add(panelSuperior, JLayeredPane.DEFAULT_LAYER);

        // ==========================================
        // 2. CENTRO (Tabla de Detalle / Cartola de Movimientos)
        // ==========================================
        String[] columnasCartola = {"Tipo Doc", "N° Doc", "Fecha Emisión", "Vencimiento", "Cargo ($)", "Abono ($)", "Saldo ($)"};
        modeloTablaCartola = new DefaultTableModel(columnasCartola, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCartola = new JTable(modeloTablaCartola);
        tablaCartola.setFont(fConsola);
        tablaCartola.setBackground(cTablaFondo);
        tablaCartola.setForeground(cTextoBlanco);
        tablaCartola.setGridColor(Color.DARK_GRAY);
        tablaCartola.setRowHeight(26);

        tablaCartola.getTableHeader().setFont(fConsola);
        tablaCartola.getTableHeader().setBackground(cEncabezado);
        tablaCartola.getTableHeader().setForeground(cTextoAmarillo);

        scrollCartola = new JScrollPane(tablaCartola);
        scrollCartola.setBounds(0, 110, 1100, 485);
        scrollCartola.getViewport().setBackground(cTablaFondo);
        scrollCartola.setBorder(BorderFactory.createLineBorder(cBordeAcento, 1));
        layeredPane.add(scrollCartola, JLayeredPane.DEFAULT_LAYER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales y Barra de Comandos)
        // ==========================================
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBounds(0, 595, 1100, 105);
        panelInferior.setBackground(cBarraInferior);
        panelInferior.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, cBordeAcento));

        JPanel panelTotalBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        panelTotalBox.setOpaque(false);

        lblTextoDeuda = new JLabel("SALDO TOTAL CARTERA: $");
        lblTextoDeuda.setForeground(cTextoBlanco);
        lblTextoDeuda.setFont(fConsola);

        lblTotalDeuda = new JLabel("0");
        lblTotalDeuda.setForeground(cTextoAmarillo);
        lblTotalDeuda.setFont(new Font(fConsola.getFamily(), Font.BOLD, 18));

        panelTotalBox.add(lblTextoDeuda);
        panelTotalBox.add(lblTotalDeuda);

        lblAtajos = new JLabel();
        lblAtajos.setForeground(cTextoAmarillo);
        lblAtajos.setFont(new Font(fConsola.getFamily(), Font.BOLD, 13));
        lblAtajos.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        actualizarTextoAtajos();

        panelInferior.add(panelTotalBox, BorderLayout.NORTH);
        panelInferior.add(lblAtajos, BorderLayout.SOUTH);
        layeredPane.add(panelInferior, JLayeredPane.DEFAULT_LAYER);

        // ==========================================
        // LISTENER RESPONSIVO
        // ==========================================
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();

                if (w > 0 && h > 0) {
                    panelSuperior.setBounds(0, 0, w, 110);
                    scrollCartola.setBounds(0, 110, w, Math.max(0, h - 215));
                    panelInferior.setBounds(0, Math.max(0, h - 105), w, 105);
                }
            }
        });

        // ==========================================
        // ATAJOS DE TECLADO GLOBALES
        // ==========================================
        getRootPane().registerKeyboardAction(ev -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        atajarF8Cliente();

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");
        getRootPane().getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "imprimirCartolaF7");
        getRootPane().getActionMap().put("imprimirCartolaF7", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                imprimirCartolaPDF();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "verDetalleF2");
        getRootPane().getActionMap().put("verDetalleF2", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                verDetalleDocumentoF2();
            }
        });

        txtRutCliente.requestFocusInWindow();
        revalidate();
        repaint();
    }

    private void configurarTablaDetalle() {
        tablaCartola.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "verDetalleF2");

        tablaCartola.getActionMap().put("verDetalleF2", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                verDetalleDocumentoF2();
            }
        });

        tablaCartola.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tablaCartola.getSelectedRow() != -1) {
                    verDetalleDocumentoF2();
                }
            }
        });
    }

    private void atajarF8Cliente() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "abrirBuscarClienteCartola");

        getRootPane().getActionMap().put("abrirBuscarClienteCartola", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(FrmCartolaCliente.this);
                DlgClientes buscador = new DlgClientes(parentWindow);
                try {
                    ClienteDAO dao = new ClienteDAO();
                    List<Object[]> listaClientes = dao.cargarClientesDAO("");
                    buscador.cargarClientesEnTabla(listaClientes);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al cargar clientes desde buscador F8", ex);
                    JOptionPane.showMessageDialog(FrmCartolaCliente.this,
                            "No se pudo cargar el listado de clientes.",
                            "Error de Base de Datos",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                buscador.setVisible(true);

                String[] datosCliente = buscador.getClienteSeleccionado();
                if (datosCliente != null && datosCliente.length > 0) {
                    String rutSeleccionado = datosCliente[0] != null ? datosCliente[0].trim() : "";
                    txtRutCliente.setText(rutSeleccionado);
                    if (datosCliente.length > 1 && datosCliente[1] != null) {
                        lblNombreCliente.setText(datosCliente[1].trim());
                    }
                    cargarCartolaCliente(rutSeleccionado);
                }
            }
        });
    }

    private void buscarClienteYCartola() {
        String rut = txtRutCliente.getText().trim();
        if (rut.isEmpty()) {
            lblNombreCliente.setText("[ Ingrese un RUT válido ]");
            return;
        }

        try {
            ClienteDAO dao = new ClienteDAO();
            List<Object[]> listaClientes = dao.cargarClientesDAO(rut);

            String rutEncontrado = null;
            String nombreEncontrado = "Sin Nombre";

            for (Object[] cli : listaClientes) {
                String rutCli = cli != null && cli.length > 0 && cli[0] != null ? cli[0].toString().trim() : "";
                if (rutCli.equalsIgnoreCase(rut) || (!rutCli.isEmpty() && rutCli.contains(rut))) {
                    rutEncontrado = rutCli;
                    if (cli.length > 1 && cli[1] != null && !cli[1].toString().trim().isEmpty()) {
                        nombreEncontrado = cli[1].toString().trim();
                    }
                    break;
                }
            }

            if (rutEncontrado != null) {
                txtRutCliente.setText(rutEncontrado);
                lblNombreCliente.setText(nombreEncontrado);
                cargarCartolaCliente(rutEncontrado);
            } else {
                lblNombreCliente.setText("CLIENTE NO ENCONTRADO");
                modeloTablaCartola.setRowCount(0);
                lblTotalDeuda.setText("0");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar el cliente en cartola", ex);
            JOptionPane.showMessageDialog(this, "No se pudo consultar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCartolaCliente(String rutCliente) {
        modeloTablaCartola.setRowCount(0);
        double saldoTotalCalculado = 0.0;

        if (rutCliente == null || rutCliente.trim().isEmpty()) {
            lblTotalDeuda.setText("0");
            return;
        }

        try {
            CuentaCorrienteDAO ctaCteDao = new CuentaCorrienteDAO();
            List<Object[]> movimientos = ctaCteDao.obtenerCartolaCliente(rutCliente.trim());

            if (movimientos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El cliente no registra movimientos en su cuenta corriente.",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Object[] mov : movimientos) {
                    if (mov == null || mov.length < 7) {
                        continue;
                    }

                    double cargo = parsearMontoTabla(mov[4]);
                    double abono = parsearMontoTabla(mov[5]);
                    double saldo = parsearMontoTabla(mov[6]);

                    saldoTotalCalculado += saldo;

                    modeloTablaCartola.addRow(new Object[]{
                        mov[0] != null ? mov[0] : "",
                        mov[1] != null ? mov[1] : "",
                        mov[2] != null ? mov[2] : "",
                        mov[3] != null ? mov[3] : "-",
                        String.format("$%,.0f", cargo),
                        String.format("$%,.0f", abono),
                        String.format("$%,.0f", saldo)
                    });
                }
            }

            lblTotalDeuda.setText(String.format("%,.0f", saldoTotalCalculado));

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar la cartola desde la base de datos", ex);
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la cartola desde la base de datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parsearMontoTabla(Object valor) {
        if (valor == null) {
            return 0.0;
        }
        if (valor instanceof Number num) {
            return num.doubleValue();
        }
        try {
            return Double.parseDouble(valor.toString().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void imprimirCartolaPDF() {
        if (modeloTablaCartola.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay movimientos en la cartola para imprimir.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rutCliente = txtRutCliente.getText().trim();
        String nombreCliente = lblNombreCliente.getText().trim();
        String totalDeuda = lblTotalDeuda.getText().trim();

        try {
            utilidades.PDFUtils.generarCartolaClientePDF(rutCliente, nombreCliente, totalDeuda, modeloTablaCartola);
            JOptionPane.showMessageDialog(this, "¡Cartola exportada a PDF con éxito!");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al generar el PDF de la cartola", ex);
            JOptionPane.showMessageDialog(this,
                    "No se pudo generar el PDF de la cartola.",
                    "Error de Impresión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verDetalleDocumentoF2() {
        int filaSeleccionada = tablaCartola.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un movimiento de la tabla para ver su detalle.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaCartola.convertRowIndexToModel(filaSeleccionada);

        String tipoDoc = modeloTablaCartola.getValueAt(filaModelo, 0) != null ? modeloTablaCartola.getValueAt(filaModelo, 0).toString() : "";
        String nroDoc = modeloTablaCartola.getValueAt(filaModelo, 1) != null ? modeloTablaCartola.getValueAt(filaModelo, 1).toString() : "";
        String fechaEmision = modeloTablaCartola.getValueAt(filaModelo, 2) != null ? modeloTablaCartola.getValueAt(filaModelo, 2).toString() : "";
        String vencimiento = modeloTablaCartola.getValueAt(filaModelo, 3) != null ? modeloTablaCartola.getValueAt(filaModelo, 3).toString() : "";
        String cargo = modeloTablaCartola.getValueAt(filaModelo, 4) != null ? modeloTablaCartola.getValueAt(filaModelo, 4).toString() : "0";
        String abono = modeloTablaCartola.getValueAt(filaModelo, 5) != null ? modeloTablaCartola.getValueAt(filaModelo, 5).toString() : "0";
        String saldo = modeloTablaCartola.getValueAt(filaModelo, 6) != null ? modeloTablaCartola.getValueAt(filaModelo, 6).toString() : "0";

        Window padre = SwingUtilities.getWindowAncestor(this);
        JDialog dialogDetalle = new JDialog(padre, " Detalle de Documento [F2] ", Dialog.ModalityType.APPLICATION_MODAL);
        dialogDetalle.setSize(450, 320);
        dialogDetalle.setLocationRelativeTo(this);
        dialogDetalle.setLayout(new BorderLayout());

        JPanel panelFondo = new JPanel();
        panelFondo.setBackground(cFondo);
        panelFondo.setLayout(new BorderLayout(10, 10));
        panelFondo.setBorder(BorderFactory.createLineBorder(cBordeAcento, 2));

        String contenido
                = """
                   ═══════════════════════════════════════════
                              DETALLE DE MOVIMIENTO        
                   ═══════════════════════════════════════════
                  
                   Tipo de Documento : """ + tipoDoc + "\n"
                + " N° de Documento   : " + nroDoc + "\n"
                + " Fecha de Emisión  : " + fechaEmision + "\n"
                + " Fecha Vencimiento : " + vencimiento + "\n\n"
                + " ─────────────────────────────────────────\n"
                + " Cargo ($)         : " + cargo + "\n"
                + " Abono ($)         : " + abono + "\n"
                + " Saldo Parcial ($) : " + saldo + "\n"
                + " ═════════════════════════════════════════";

        JTextArea areaTexto = new JTextArea(contenido);
        areaTexto.setFont(new Font("Monospaced", Font.BOLD, 13));
        areaTexto.setForeground(cTextoAmarillo);
        areaTexto.setBackground(cFondo);
        areaTexto.setEditable(false);
        areaTexto.setMargin(new java.awt.Insets(15, 15, 15, 15));

        panelFondo.add(areaTexto, BorderLayout.CENTER);

        dialogDetalle.getRootPane().registerKeyboardAction(e -> dialogDetalle.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogDetalle.getRootPane().registerKeyboardAction(e -> dialogDetalle.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogDetalle.add(panelFondo);
        dialogDetalle.setVisible(true);
    }
}
