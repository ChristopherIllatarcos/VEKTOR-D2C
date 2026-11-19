package vista.ctacte;

import dao.ClienteDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
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
import javax.swing.table.DefaultTableModel;
import vista.ventas.DlgClientes;

/**
 * Módulo CTACTE - Cartola Interna de Cuenta Corriente por Cliente Estilo MS-DOS
 * / Clipper adaptado a MDI JInternalFrame
 *
 * @author caill
 */
public class FrmCartolaCliente extends JInternalFrame {

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
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public FrmCartolaCliente() {
        super("4.- Cartola Interna CtaCte Cliente", true, true, true, true);
        putClientProperty("JInternalFrame.isPalette", Boolean.FALSE);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        configurarTablaDetalle();
        modeloTablaCartola = (DefaultTableModel) tablaCartola.getModel();
        // Asignar el icono de Nexum directamente a este JInternalFrame
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
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
            lblAtajos.setText("  (Esc)apar    (F8): Buscar Cliente    (F2): Detalle    (F6) Tema: " + temaActual + "    (Print): Imprimir");
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
        modeloTablaCartola = new DefaultTableModel(columnasCartola, 0);

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
                    scrollCartola.setBounds(0, 110, w, h - 215);
                    panelInferior.setBounds(0, h - 105, w, 105);
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

        // F6 para Ciclar Tema en tiempo real
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");

        getRootPane().getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        // F7 o evento alternativo para Imprimir / Exportar Cartola (o se mantiene F6 si prefieres, pero F6 ahora cambia el tema, asignamos F7 para imprimir o combinamos)
        // Nota: El usuario usaba F6 para imprimir cartola previamente. Dado que F6 ahora es el estándar en todo el sistema para cambiar de tema, cambiamos Imprimir Cartola a F7 o dejamos F6 para tema y F7 para imprimir.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "imprimirCartolaF7");

        getRootPane().getActionMap().put("imprimirCartolaF7", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                imprimirCartolaPDF();
            }
        });

        // F2 para Ver Detalle del Documento seleccionado en la tabla
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
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaCartola.getModel();

        tablaCartola.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "verDetalleF2");

        tablaCartola.getActionMap().put("verDetalleF2", new javax.swing.AbstractAction() {
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
                    JOptionPane.showMessageDialog(FrmCartolaCliente.this,
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
                        lblNombreCliente.setText(datosCliente[1]);
                    }

                    cargarCartolaCliente(datosCliente[0]);
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

            boolean encontrado = false;
            for (Object[] cli : listaClientes) {
                String rutCli = cli[0] != null ? cli[0].toString() : "";
                if (rutCli.equalsIgnoreCase(rut) || rutCli.contains(rut)) {
                    txtRutCliente.setText(rutCli);
                    lblNombreCliente.setText(cli[1] != null ? cli[1].toString() : "Sin Nombre");
                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                cargarCartolaCliente(rut);
            } else {
                lblNombreCliente.setText("CLIENTE NO ENCONTRADO");
                modeloTablaCartola.setRowCount(0);
                lblTotalDeuda.setText("0");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar el cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCartolaCliente(String rutCliente) {
        modeloTablaCartola.setRowCount(0);
        double saldoTotalCalculado = 0.0;

        try {
            dao.CuentaCorrienteDAO ctaCteDao = new dao.CuentaCorrienteDAO();
            List<Object[]> movimientos = ctaCteDao.obtenerCartolaCliente(rutCliente);

            if (movimientos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El cliente no registra movimientos en su cuenta corriente.",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Object[] mov : movimientos) {
                    double cargo = 0.0;
                    double abono = 0.0;
                    double saldo = 0.0;

                    try {
                        if (mov[4] != null) {
                            cargo = Double.parseDouble(mov[4].toString());
                        }
                        if (mov[5] != null) {
                            abono = Double.parseDouble(mov[5].toString());
                        }
                        if (mov[6] != null) {
                            saldo = Double.parseDouble(mov[6].toString());
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar formato incorrecto
                    }

                    saldoTotalCalculado += saldo;

                    modeloTablaCartola.addRow(new Object[]{
                        mov[0],
                        mov[1],
                        mov[2],
                        mov[3] != null ? mov[3] : "-",
                        String.format("$%,.0f", cargo),
                        String.format("$%,.0f", abono),
                        String.format("$%,.0f", saldo)
                    });
                }
            }

            lblTotalDeuda.setText(String.format("%,.0f", saldoTotalCalculado));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar la cartola desde la base de datos: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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
            JOptionPane.showMessageDialog(this,
                    "Error al generar el PDF de la cartola: " + ex.getMessage(),
                    "Error de Impresión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verDetalleDocumentoF2() {
        int filaSeleccionada = tablaCartola.getSelectedRow();

        if (filaSeleccionada == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Por favor, seleccione un movimiento de la tabla para ver su detalle.",
                    "Aviso",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipoDoc = tablaCartola.getValueAt(filaSeleccionada, 0) != null ? tablaCartola.getValueAt(filaSeleccionada, 0).toString() : "";
        String nroDoc = tablaCartola.getValueAt(filaSeleccionada, 1) != null ? tablaCartola.getValueAt(filaSeleccionada, 1).toString() : "";
        String fechaEmision = tablaCartola.getValueAt(filaSeleccionada, 2) != null ? tablaCartola.getValueAt(filaSeleccionada, 2).toString() : "";
        String vencimiento = tablaCartola.getValueAt(filaSeleccionada, 3) != null ? tablaCartola.getValueAt(filaSeleccionada, 3).toString() : "";
        String cargo = tablaCartola.getValueAt(filaSeleccionada, 4) != null ? tablaCartola.getValueAt(filaSeleccionada, 4).toString() : "0";
        String abono = tablaCartola.getValueAt(filaSeleccionada, 5) != null ? tablaCartola.getValueAt(filaSeleccionada, 5).toString() : "0";
        String saldo = tablaCartola.getValueAt(filaSeleccionada, 6) != null ? tablaCartola.getValueAt(filaSeleccionada, 6).toString() : "0";

        javax.swing.JDialog dialogDetalle = new javax.swing.JDialog((java.awt.Frame) null, " Detalle de Documento [F2] ", true);
        dialogDetalle.setSize(450, 320);
        dialogDetalle.setLocationRelativeTo(this);
        dialogDetalle.setLayout(new java.awt.BorderLayout());

        javax.swing.JPanel panelFondo = new javax.swing.JPanel();
        panelFondo.setBackground(cFondo);
        panelFondo.setLayout(new java.awt.BorderLayout(10, 10));
        panelFondo.setBorder(javax.swing.BorderFactory.createLineBorder(cBordeAcento, 2));

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

        javax.swing.JTextArea areaTexto = new javax.swing.JTextArea(contenido);
        areaTexto.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 13));
        areaTexto.setForeground(cTextoAmarillo);
        areaTexto.setBackground(cFondo);
        areaTexto.setEditable(false);
        areaTexto.setMargin(new java.awt.Insets(15, 15, 15, 15));

        panelFondo.add(areaTexto, java.awt.BorderLayout.CENTER);

        dialogDetalle.getRootPane().registerKeyboardAction(e -> dialogDetalle.dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogDetalle.getRootPane().registerKeyboardAction(e -> dialogDetalle.dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogDetalle.add(panelFondo);
        dialogDetalle.setVisible(true);
    }
}
