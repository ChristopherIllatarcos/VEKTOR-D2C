package vista.ctacte;

import dao.ClienteDAO;
import modelo.EstadisticasClienteDTO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FrmEstadisticasClientes extends javax.swing.JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(FrmEstadisticasClientes.class.getName());
    private static final String TEXTO_INICIAL_NOMBRE = "INGRESE UN RUT Y PRESIONE BUSCAR...";

    private JTextField txtRut;
    private JLabel lblNombre, lblSaldo;
    private JLabel lblVecesCompro, lblTotalCompro, lblPromCompro, lblUltimaCompra, lblMaxCompro;
    private JLabel lblVecesPago, lblTotalPago, lblPromPago, lblUltimoPago, lblMaxPago;
    private JLabel lblInfo;
    private JButton btnBuscar;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelTop, panelCentral, panelNombre, panelSaldo, panelCompras, panelPagos, panelInferior;
    private JLabel lblTitulo, lblRutTitle, lblNomTitle, lblSalTitle;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cAcento, cTextoClaro, cTextoDato, cBarraSur;
    private final Font FUENTE_MONO = new Font("Monospaced", Font.BOLD, 15);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmEstadisticasClientes() {
        super("3.- ESTADÍSTICAS Y CUENTA CORRIENTE - CLIENTES");
        setSize(800, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        // --- ENCABEZADO Y BUSCADOR DE RUT ---
        panelTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelTop.setBackground(cEncabezado);

        lblTitulo = new JLabel("ESTADÍSTICAS DEL CLIENTE");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setForeground(cAcento);
        panelTop.add(lblTitulo);

        lblRutTitle = new JLabel(" | RUT:");
        lblRutTitle.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblRutTitle.setForeground(cTextoClaro);
        panelTop.add(lblRutTitle);

        txtRut = new JTextField(12);
        estilarCampo(txtRut);
        panelTop.add(txtRut);

        btnBuscar = new JButton("BUSCAR");
        estilarBoton(btnBuscar);
        panelTop.add(btnBuscar);

        add(panelTop, BorderLayout.NORTH);

        // --- PANEL CENTRAL (ESTILO TERMINAL RETRO) ---
        panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(cFondo);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Nombre del Cliente
        panelNombre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNombre.setBackground(cFondo);
        lblNomTitle = new JLabel("NOMBRE : ");
        lblNomTitle.setFont(FUENTE_MONO);
        lblNomTitle.setForeground(cAcento);

        lblNombre = new JLabel(TEXTO_INICIAL_NOMBRE);
        lblNombre.setFont(FUENTE_MONO);
        lblNombre.setForeground(cTextoClaro);
        panelNombre.add(lblNomTitle);
        panelNombre.add(lblNombre);
        panelCentral.add(panelNombre);
        panelCentral.add(Box.createVerticalStrut(10));

        // Saldo Actual
        panelSaldo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSaldo.setBackground(cFondo);
        lblSalTitle = new JLabel("Saldo Actual             : $");
        lblSalTitle.setFont(FUENTE_MONO);
        lblSalTitle.setForeground(cAcento);

        lblSaldo = new JLabel("0,00");
        lblSaldo.setFont(FUENTE_MONO);
        lblSaldo.setForeground(cTextoDato);
        panelSaldo.add(lblSalTitle);
        panelSaldo.add(lblSaldo);
        panelCentral.add(panelSaldo);
        panelCentral.add(Box.createVerticalStrut(14));

        // SECCIÓN COMPRAS
        panelCompras = new JPanel(new GridLayout(5, 2, 5, 5));
        panelCompras.setBackground(cFondo);
        panelCompras.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(cAcento), " COMPRAS ", 0, 0,
                FUENTE_MONO, cAcento));

        lblVecesCompro = crearLabelDato(panelCompras, "Veces que ha comprado    : ");
        lblTotalCompro = crearLabelDatoMoneda(panelCompras, "Total de compras        : $");
        lblPromCompro = crearLabelDatoMoneda(panelCompras, "Promedio de compra      : $");
        lblUltimaCompra = crearLabelDato(panelCompras, "Última compra           : ");
        lblMaxCompro = crearLabelDatoMoneda(panelCompras, "Máximo comprado         : $");

        panelCentral.add(panelCompras);
        panelCentral.add(Box.createVerticalStrut(10));

        // SECCIÓN PAGOS
        panelPagos = new JPanel(new GridLayout(5, 2, 5, 5));
        panelPagos.setBackground(cFondo);
        panelPagos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(cAcento), " PAGOS ", 0, 0,
                FUENTE_MONO, cAcento));

        lblVecesPago = crearLabelDato(panelPagos, "Veces que ha pagado     : ");
        lblTotalPago = crearLabelDatoMoneda(panelPagos, "Total de pagos          : $");
        lblPromPago = crearLabelDatoMoneda(panelPagos, "Promedio de pago        : $");
        lblUltimoPago = crearLabelDato(panelPagos, "Último pago             : ");
        lblMaxPago = crearLabelDatoMoneda(panelPagos, "Máximo pagado           : $");

        panelCentral.add(panelPagos);
        add(panelCentral, BorderLayout.CENTER);

        // --- BARRA INFERIOR ---
        panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setBackground(cBarraSur);
        lblInfo = new JLabel();
        lblInfo.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblInfo.setForeground(cTextoClaro);
        actualizarTextoAyuda();
        panelInferior.add(lblInfo);
        add(panelInferior, BorderLayout.SOUTH);

        // --- ACCIONES DE BUSCAR Y TECLAS ---
        btnBuscar.addActionListener(e -> realizarBusqueda());
        txtRut.addActionListener(e -> realizarBusqueda());

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> ciclarSiguienteTema(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el icono interno", e);
        }
    }

    // Constructor que recibe el RUT desde la tabla principal
    public FrmEstadisticasClientes(String rut) {
        this();

        if (rut != null && !rut.trim().isEmpty()) {
            cargarDatosCliente(rut);
        }

        setSize(800, 560);
        setLocation(50, 50);
        setVisible(true);
        toFront();
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cEncabezado = new Color(45, 10, 55);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cTextoDato = new Color(255, 180, 240);
                cBarraSur = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cEncabezado = new Color(10, 40, 50);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cTextoDato = new Color(180, 255, 255);
                cBarraSur = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cEncabezado = new Color(10, 45, 30);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cTextoDato = new Color(180, 255, 200);
                cBarraSur = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cEncabezado = new Color(40, 18, 12);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cTextoDato = new Color(255, 200, 150);
                cBarraSur = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 170);
                cEncabezado = new Color(0, 128, 128);
                cAcento = Color.YELLOW;
                cTextoClaro = Color.WHITE;
                cTextoDato = Color.GREEN;
                cBarraSur = new Color(180, 0, 0);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelTop != null) {
            panelTop.setBackground(cEncabezado);
        }
        if (lblTitulo != null) {
            lblTitulo.setForeground(cAcento);
        }
        if (lblRutTitle != null) {
            lblRutTitle.setForeground(cTextoClaro);
        }

        if (txtRut != null) {
            estilarCampo(txtRut);
        }
        if (btnBuscar != null) {
            estilarBoton(btnBuscar);
        }

        if (panelCentral != null) {
            panelCentral.setBackground(cFondo);
        }
        if (panelNombre != null) {
            panelNombre.setBackground(cFondo);
        }
        if (lblNomTitle != null) {
            lblNomTitle.setForeground(cAcento);
        }
        if (lblNombre != null) {
            if (TEXTO_INICIAL_NOMBRE.equals(lblNombre.getText())) {
                lblNombre.setForeground(cTextoClaro);
            } else {
                lblNombre.setForeground(cTextoDato);
            }
        }

        if (panelSaldo != null) {
            panelSaldo.setBackground(cFondo);
        }
        if (lblSalTitle != null) {
            lblSalTitle.setForeground(cAcento);
        }
        if (lblSaldo != null) {
            lblSaldo.setForeground(cTextoDato);
        }

        actualizarColoresSeccion(panelCompras, " COMPRAS ");
        actualizarColoresSeccion(panelPagos, " PAGOS ");

        if (panelInferior != null) {
            panelInferior.setBackground(cBarraSur);
        }
        if (lblInfo != null) {
            lblInfo.setForeground(cTextoClaro);
        }
        actualizarTextoAyuda();

        revalidate();
        repaint();
    }

    private void actualizarColoresSeccion(JPanel panel, String titulo) {
        if (panel == null) {
            return;
        }
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(cAcento), titulo, 0, 0,
                FUENTE_MONO, cAcento));

        Component[] comps = panel.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JLabel l) {
                boolean esEtiqueta = (i % 2 == 0);
                l.setForeground(esEtiqueta ? cTextoClaro : cTextoDato);
            }
        }
    }

    private void actualizarTextoAyuda() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblInfo != null) {
            lblInfo.setText(" [ESC] Salir    [F6] Tema: " + temaActual + " ");
        }
    }

    private JLabel crearLabelDato(JPanel panel, String textoEtiqueta) {
        JLabel lblT = new JLabel(textoEtiqueta);
        lblT.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblT.setForeground(cTextoClaro);
        panel.add(lblT);

        JLabel lblV = new JLabel("0");
        lblV.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblV.setForeground(cTextoDato);
        panel.add(lblV);
        return lblV;
    }

    private JLabel crearLabelDatoMoneda(JPanel panel, String textoEtiqueta) {
        return crearLabelDato(panel, textoEtiqueta);
    }

    private void estilarCampo(JTextField txt) {
        txt.setFont(new Font("Monospaced", Font.BOLD, 16));
        txt.setBackground(cFondo);
        txt.setForeground(cTextoDato);
        txt.setCaretColor(cTextoDato);
        txt.setBorder(new LineBorder(cAcento, 1));
    }

    private void estilarBoton(JButton btn) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setBackground(cFondo);
        btn.setForeground(cAcento);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(cAcento, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void realizarBusqueda() {
        String rutBuscado = txtRut != null ? txtRut.getText().trim() : "";
        if (rutBuscado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un RUT.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            EstadisticasClienteDTO stats = clienteDAO.obtenerEstadisticasPorRut(rutBuscado);
            if (stats != null) {
                mostrarEstadisticas(stats);
            } else {
                limpiarEstadisticas("CLIENTE NO ENCONTRADO");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar estadísticas del cliente", e);
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosCliente(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return;
        }
        String rutLimpio = rut.trim();
        if (txtRut != null) {
            txtRut.setText(rutLimpio);
        }

        try {
            EstadisticasClienteDTO stats = clienteDAO.obtenerEstadisticasPorRut(rutLimpio);
            if (stats != null) {
                mostrarEstadisticas(stats);
            } else {
                limpiarEstadisticas("CLIENTE NO ENCONTRADO");
                JOptionPane.showMessageDialog(this, "No se encontraron estadísticas para el RUT indicado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (HeadlessException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos del cliente", e);
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEstadisticas(EstadisticasClienteDTO stats) {
        String nombre = (stats.getNombre() != null && !stats.getNombre().trim().isEmpty())
                ? stats.getNombre().trim()
                : "SIN NOMBRE";
        lblNombre.setText(nombre);
        lblNombre.setForeground(cTextoDato);

        lblSaldo.setText(String.format("%,.2f", stats.getSaldoActual()));

        lblVecesCompro.setText(String.valueOf(stats.getVecesCompro()));
        lblTotalCompro.setText(String.format("%,.2f", stats.getTotalCompro()));
        lblPromCompro.setText(String.format("%,.2f", stats.getPromCompro()));
        lblUltimaCompra.setText(stats.getUltimaCompra() != null ? stats.getUltimaCompra() : "N/A");
        lblMaxCompro.setText(String.format("%,.2f", stats.getMaxCompro()));

        lblVecesPago.setText(String.valueOf(stats.getVecesPago()));
        lblTotalPago.setText(String.format("%,.2f", stats.getTotalPago()));
        lblPromPago.setText(String.format("%,.2f", stats.getPromPago()));
        lblUltimoPago.setText(stats.getUltimoPago() != null ? stats.getUltimoPago() : "N/A");
        lblMaxPago.setText(String.format("%,.2f", stats.getMaxPago()));
    }

    private void limpiarEstadisticas(String mensajeEstado) {
        lblNombre.setText(mensajeEstado);
        lblNombre.setForeground(cTextoClaro);
        lblSaldo.setText("0,00");

        lblVecesCompro.setText("0");
        lblTotalCompro.setText("0,00");
        lblPromCompro.setText("0,00");
        lblUltimaCompra.setText("N/A");
        lblMaxCompro.setText("0,00");

        lblVecesPago.setText("0");
        lblTotalPago.setText("0,00");
        lblPromPago.setText("0,00");
        lblUltimoPago.setText("N/A");
        lblMaxPago.setText("0,00");
    }
}
