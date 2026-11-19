package vista.ventas;

import vista.ctacte.FrmEstadisticasClientes;
import dao.ClienteDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import modelo.ClienteDTO;

public class FrmArchivoClientes extends javax.swing.JInternalFrame {

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JLabel lblRegistros;
    private JLabel lblAcciones;
    private ClienteDAO clienteDAO;

    // Componentes estructurales para actualización dinámica de temas
    private JPanel panelSuperior, panelCentro, panelInferior;
    private JScrollPane scrollPane;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelSuperior, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur;
    private final Font FUENTE_CONSOLA = new Font("Monospaced", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Naranja Fuego por defecto

    public FrmArchivoClientes() {
        clienteDAO = new ClienteDAO();

        // Aplicar tema inicial por defecto
        aplicarTema(listaTemas[indiceTemaActual]);

        configurarVentana();
        inicializarComponentes();
        // Asignar el icono Nexum a la ventana interna (JInternalFrame)
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
        cargarClientes();
        // Forma segura para evitar advertencias de constructor en Swing
        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });
    }

    private void configurarVentana() {
        setTitle("6.- ARCHIVO DE CLIENTES");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(cFondoNegro);
        setLayout(new BorderLayout());
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F5]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondoNegro = new Color(10, 10, 15);
                cPanelSuperior = new Color(45, 10, 55);
                cEncabezadoTabla = new Color(90, 10, 120);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cSeleccion = new Color(140, 20, 160);
                cBarraSur = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondoNegro = new Color(5, 15, 20);
                cPanelSuperior = new Color(10, 40, 50);
                cEncabezadoTabla = new Color(0, 80, 95);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cSeleccion = new Color(0, 120, 140);
                cBarraSur = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondoNegro = new Color(5, 20, 15);
                cPanelSuperior = new Color(10, 45, 30);
                cEncabezadoTabla = new Color(15, 80, 50);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cSeleccion = new Color(0, 140, 80);
                cBarraSur = new Color(0, 60, 35);
                break;
            case "Clipper Clásico":
                cFondoNegro = Color.BLACK;
                cPanelSuperior = new Color(0, 0, 168);
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

        // Refrescar colores en tiempo de ejecución
        getContentPane().setBackground(cFondoNegro);

        if (panelSuperior != null) {
            panelSuperior.setBackground(cPanelSuperior);
            panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                    BorderFactory.createEmptyBorder(15, 20, 10, 20)
            ));
        }

        if (panelCentro != null) {
            panelCentro.setBackground(cFondoNegro);
        }

        if (tablaClientes != null) {
            tablaClientes.setBackground(cFondoNegro);
            tablaClientes.setForeground(cTextoClaro);
            tablaClientes.setSelectionBackground(cSeleccion);
            tablaClientes.getTableHeader().setBackground(cEncabezadoTabla);
            tablaClientes.getTableHeader().setForeground(cAcento);
            tablaClientes.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
            tablaClientes.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondoNegro);
            scrollPane.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        }

        if (panelInferior != null) {
            panelInferior.setBackground(cBarraSur);
            panelInferior.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }

        if (lblRegistros != null) {
            lblRegistros.setForeground(cAcento);
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAcciones != null) {
            lblAcciones.setText("<html>(Esc) Salir | (Ins) Insertar | (Supr) Eliminar | (M) Modificar | (B) Buscar | (V) Ficha &nbsp;|&nbsp; <b style='color:#FFFF55;'>(F5) Tema: " + temaActual + "</b></html>");
        }
    }

    private void inicializarComponentes() {
        // --- PANEL SUPERIOR (Título y contador de registros) ---
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(cPanelSuperior);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                BorderFactory.createEmptyBorder(15, 20, 10, 20)
        ));

        JLabel lblTitulo = new JLabel("ARCHIVO DE CLIENTES");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setForeground(cAcento);

        lblRegistros = new JLabel("Registros: 0");
        lblRegistros.setFont(FUENTE_CONSOLA);
        lblRegistros.setForeground(cAcento);

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(lblRegistros, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);

        // --- TABLA CENTRAL CON FRANJAS HORIZONTALES SUTILES (RAYAS TRANSPARENTES) ---
        String[] columnas = {"R.U.T.", "NOMBRE DEL CLIENTE"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setFont(new Font("Monospaced", Font.BOLD, 16));
        tablaClientes.setRowHeight(25);
        tablaClientes.setBackground(cFondoNegro);
        tablaClientes.setForeground(cTextoClaro);
        tablaClientes.setSelectionBackground(cSeleccion);
        tablaClientes.setSelectionForeground(Color.WHITE);
        tablaClientes.setShowGrid(false);
        tablaClientes.setIntercellSpacing(new Dimension(0, 0));

        // Cabecera de la tabla
        tablaClientes.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));
        tablaClientes.getTableHeader().setBackground(cEncabezadoTabla);
        tablaClientes.getTableHeader().setForeground(cAcento);
        tablaClientes.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        // Renderizador para franjas horizontales alternadas sutiles
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

                if (column == 0) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tablaClientes.getColumnCount(); i++) {
            tablaClientes.getColumnModel().getColumn(i).setCellRenderer(rendererRayas);
        }

        scrollPane = new JScrollPane(tablaClientes);
        scrollPane.getViewport().setBackground(cFondoNegro);
        scrollPane.setBorder(BorderFactory.createLineBorder(cAcento, 1));

        panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(cFondoNegro);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelCentro.add(scrollPane, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        // --- PANEL INFERIOR (Barra de Acciones con soporte para F5) ---
        panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelInferior.setBackground(cBarraSur);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        lblAcciones = new JLabel();
        lblAcciones.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblAcciones.setForeground(Color.WHITE);
        actualizarTextoAtajos();
        panelInferior.add(lblAcciones);
        add(panelInferior, BorderLayout.SOUTH);

        // --- EVENTOS DE TECLADO GLOBALES ---
        KeyAdapter listenerTeclado = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE ->
                        dispose();
                    case KeyEvent.VK_INSERT ->
                        insertarCliente();
                    case KeyEvent.VK_DELETE ->
                        eliminarCliente();
                    case KeyEvent.VK_B ->
                        buscarCliente();
                    case KeyEvent.VK_M ->
                        modificarCliente();
                    case KeyEvent.VK_V, KeyEvent.VK_ENTER ->
                        verEstadisticasCliente();
                    case KeyEvent.VK_F5 ->
                        ciclarSiguienteTema(); // 👈 Cambio de tema dinámico con F5
                    default -> {
                    }
                }
            }
        };

        this.addKeyListener(listenerTeclado);
        tablaClientes.addKeyListener(listenerTeclado);
        setFocusable(true);
    }

    private void cargarClientes() {
        modeloTabla.setRowCount(0);
        List<ClienteDTO> lista = clienteDAO.obtenerTodosLosClientes();

        lista.sort((c1, c2) -> c1.getNombreCompletoFormato().compareToIgnoreCase(c2.getNombreCompletoFormato()));

        for (ClienteDTO c : lista) {
            modeloTabla.addRow(new Object[]{
                c.getRut().trim(),
                c.getNombreCompletoFormato()
            });
        }
        lblRegistros.setText("Registros: " + lista.size());
    }

    private void insertarCliente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(cFondoNegro);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fuenteEtiqueta = new Font("Monospaced", Font.BOLD, 14);
        Font fuenteCampo = new Font("Monospaced", Font.PLAIN, 14);

        JTextField txtRutIns = new JTextField(15);
        txtRutIns.setFont(fuenteCampo);
        txtRutIns.setBackground(new Color(Math.max(0, cFondoNegro.getRed() + 10), Math.max(0, cFondoNegro.getGreen() + 10), Math.max(0, cFondoNegro.getBlue() + 10)));
        txtRutIns.setForeground(cTextoClaro);
        txtRutIns.setCaretColor(cAcento);

        JTextField txtNombresIns = new JTextField(15);
        txtNombresIns.setFont(fuenteCampo);
        txtNombresIns.setBackground(txtRutIns.getBackground());
        txtNombresIns.setForeground(cTextoClaro);
        txtNombresIns.setCaretColor(cAcento);

        JTextField txtSaldoIns = new JTextField("0", 15);
        txtSaldoIns.setFont(fuenteCampo);
        txtSaldoIns.setBackground(txtRutIns.getBackground());
        txtSaldoIns.setForeground(cTextoClaro);
        txtSaldoIns.setCaretColor(cAcento);

        JTextField txtCupoIns = new JTextField("1000000", 15);
        txtCupoIns.setFont(fuenteCampo);
        txtCupoIns.setBackground(txtRutIns.getBackground());
        txtCupoIns.setForeground(cTextoClaro);
        txtCupoIns.setCaretColor(cAcento);

        JLabel lblR = new JLabel(" R.U.T. :");
        lblR.setForeground(cAcento);
        lblR.setFont(fuenteEtiqueta);

        JLabel lblNom = new JLabel(" NOMBRE COMPLETO :");
        lblNom.setForeground(cAcento);
        lblNom.setFont(fuenteEtiqueta);

        JLabel lblS = new JLabel(" SALDO :");
        lblS.setForeground(cAcento);
        lblS.setFont(fuenteEtiqueta);

        JLabel lblC = new JLabel(" CUPO :");
        lblC.setForeground(cAcento);
        lblC.setFont(fuenteEtiqueta);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(lblR, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtRutIns, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(lblNom, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtNombresIns, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(lblS, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtSaldoIns, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(lblC, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtCupoIns, gbc);

        JScrollPane scrollPaneDlg = new JScrollPane(panel);
        scrollPaneDlg.setPreferredSize(new Dimension(550, 240));
        scrollPaneDlg.setBorder(null);
        int resultado = JOptionPane.showConfirmDialog(
                this, scrollPaneDlg, "INGRESAR NUEVO CLIENTE",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {
            String rutCompleto = txtRutIns.getText().trim();
            String nombresCompleto = txtNombresIns.getText().trim().toUpperCase();

            if (rutCompleto.isEmpty() || nombresCompleto.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El RUT y el Nombre Completo son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String rutParteNumero = rutCompleto;
            String digitoVerificador = "";

            if (rutCompleto.contains("-")) {
                String[] partes = rutCompleto.split("-");
                rutParteNumero = partes[0].trim();
                if (partes.length > 1) {
                    digitoVerificador = partes[1].trim();
                }
            }

            try {
                java.math.BigDecimal saldo = new java.math.BigDecimal(txtSaldoIns.getText().trim());
                java.math.BigDecimal cupo = new java.math.BigDecimal(txtCupoIns.getText().trim());

                ClienteDTO nuevoCliente = new ClienteDTO();
                nuevoCliente.setRut(rutParteNumero);
                nuevoCliente.setDigitoVerificador(digitoVerificador);
                nuevoCliente.setNombres(nombresCompleto);
                nuevoCliente.setSaldoActual(saldo);
                nuevoCliente.setCupo(cupo);

                boolean exito = clienteDAO.insertarCliente(nuevoCliente);

                if (exito) {
                    JOptionPane.showMessageDialog(this, "¡Cliente registrado correctamente!");
                    cargarClientes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo guardar el cliente en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Los campos de Saldo y Cupo deben ser numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarCliente() {
        int filaSeleccionada = tablaClientes.getSelectedRow();

        if (filaSeleccionada >= 0) {
            int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);
            String rut = (String) modeloTabla.getValueAt(filaModelo, 0);

            JDialog dialog = new JDialog((Frame) null, "Eliminar Cliente", true);
            dialog.setSize(440, 160);
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);

            JPanel panel = new JPanel(null);
            panel.setBackground(cFondoNegro);

            JLabel lblTit = new JLabel("¿Desea eliminar al cliente RUT: " + rut + "?", JLabel.CENTER);
            lblTit.setBounds(20, 20, 390, 20);
            lblTit.setForeground(cAcento);
            lblTit.setFont(new Font("Monospaced", Font.BOLD, 12));
            panel.add(lblTit);

            JButton btnSi = new JButton("[S] Sí, Eliminar");
            btnSi.setBounds(40, 70, 170, 30);
            btnSi.setBackground(cBarraSur);
            btnSi.setForeground(Color.WHITE);
            btnSi.setFont(new Font("Monospaced", Font.BOLD, 11));
            panel.add(btnSi);

            JButton btnNo = new JButton("[Esc] Cancelar");
            btnNo.setBounds(225, 70, 170, 30);
            btnNo.setBackground(cEncabezadoTabla);
            btnNo.setForeground(Color.WHITE);
            btnNo.setFont(new Font("Monospaced", Font.BOLD, 11));
            panel.add(btnNo);

            btnSi.addActionListener(e -> {
                dialog.dispose();
                boolean exito = clienteDAO.eliminarCliente(rut);

                if (exito) {
                    cargarClientes();
                    mostrarMensajeClipper("Cliente eliminado correctamente.", "AVISO");
                } else {
                    mostrarMensajeClipper("No se pudo eliminar el cliente de la base de datos.", "ERROR");
                }
            });

            btnNo.addActionListener(e -> dialog.dispose());

            KeyAdapter escAdapter = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        dialog.dispose();
                    }
                }
            };

            dialog.addKeyListener(escAdapter);
            btnSi.addKeyListener(escAdapter);
            btnNo.addKeyListener(escAdapter);

            dialog.add(panel);
            dialog.setVisible(true);

        } else {
            mostrarMensajeClipper("Por favor, seleccione un cliente de la lista.", "AVISO");
        }
    }

    private void modificarCliente() {
        int filaSeleccionada = tablaClientes.getSelectedRow();

        if (filaSeleccionada == -1) {
            mostrarMensajeClipper("Selecciona un cliente de la lista para modificar.", "AVISO");
            return;
        }

        int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);
        String rutSeleccionado = (String) modeloTabla.getValueAt(filaModelo, 0);

        ClienteDTO clienteActual = clienteDAO.obtenerClientePorRut(rutSeleccionado);

        if (clienteActual == null) {
            mostrarMensajeClipper("No se encontró la información del cliente en la BD.", "ERROR");
            return;
        }

        JDialog dialog = new JDialog((Frame) null, "Modificar Cliente", true);
        dialog.setSize(520, 310);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(cFondoNegro);

        JLabel lblTit = new JLabel("--- MODIFICAR CLIENTE ---", JLabel.CENTER);
        lblTit.setBounds(60, 15, 400, 20);
        lblTit.setForeground(cAcento);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 14));
        panel.add(lblTit);

        Font fuenteTexto = new Font("Monospaced", Font.BOLD, 13);

        JLabel lblR = new JLabel("R.U.T. (Fijo):");
        lblR.setBounds(30, 55, 140, 25);
        lblR.setForeground(cTextoClaro);
        lblR.setFont(fuenteTexto);
        panel.add(lblR);

        JTextField txtRutMod = new JTextField(clienteActual.getRut());
        txtRutMod.setBounds(180, 55, 290, 26);
        txtRutMod.setFont(fuenteTexto);
        txtRutMod.setEditable(false);
        txtRutMod.setBackground(new Color(100, 100, 100));
        txtRutMod.setForeground(Color.LIGHT_GRAY);
        panel.add(txtRutMod);

        JLabel lblNom = new JLabel("NOMBRE COMPLETO:");
        lblNom.setBounds(30, 95, 140, 25);
        lblNom.setForeground(cTextoClaro);
        lblNom.setFont(fuenteTexto);
        panel.add(lblNom);

        JTextField txtNombresMod = new JTextField(clienteActual.getNombres());
        txtNombresMod.setBounds(180, 95, 290, 26);
        txtNombresMod.setBackground(cEncabezadoTabla);
        txtNombresMod.setForeground(cTextoClaro);
        txtNombresMod.setCaretColor(cAcento);
        txtNombresMod.setFont(fuenteTexto);
        panel.add(txtNombresMod);

        JLabel lblS = new JLabel("SALDO:");
        lblS.setBounds(30, 135, 140, 25);
        lblS.setForeground(cTextoClaro);
        lblS.setFont(fuenteTexto);
        panel.add(lblS);

        JTextField txtSaldoMod = new JTextField(clienteActual.getSaldoActual() != null ? clienteActual.getSaldoActual().toString() : "0");
        txtSaldoMod.setBounds(180, 135, 150, 26);
        txtSaldoMod.setBackground(cEncabezadoTabla);
        txtSaldoMod.setForeground(cTextoClaro);
        txtSaldoMod.setCaretColor(cAcento);
        txtSaldoMod.setFont(fuenteTexto);
        panel.add(txtSaldoMod);

        JLabel lblC = new JLabel("CUPO:");
        lblC.setBounds(30, 175, 140, 25);
        lblC.setForeground(cTextoClaro);
        lblC.setFont(fuenteTexto);
        panel.add(lblC);

        JTextField txtCupoMod = new JTextField(clienteActual.getCupo() != null ? clienteActual.getCupo().toString() : "1000000");
        txtCupoMod.setBounds(180, 175, 150, 26);
        txtCupoMod.setBackground(cEncabezadoTabla);
        txtCupoMod.setForeground(cTextoClaro);
        txtCupoMod.setCaretColor(cAcento);
        txtCupoMod.setFont(fuenteTexto);
        panel.add(txtCupoMod);

        JButton btnGuardar = new JButton("Guardar [Enter]");
        btnGuardar.setBounds(90, 220, 165, 30);
        btnGuardar.setBackground(new Color(0, 100, 0));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar [Esc]");
        btnCancelar.setBounds(265, 220, 165, 30);
        btnCancelar.setBackground(cBarraSur);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(btnCancelar);

        java.awt.event.ActionListener guardarAccion = e -> {
            String nombresCompleto = txtNombresMod.getText().trim().toUpperCase();

            if (nombresCompleto.isEmpty()) {
                mostrarMensajeClipper("El Nombre Completo no puede quedar vacío.", "ERROR");
                return;
            }

            try {
                java.math.BigDecimal saldo = new java.math.BigDecimal(txtSaldoMod.getText().trim().replace(",", "."));
                java.math.BigDecimal cupo = new java.math.BigDecimal(txtCupoMod.getText().trim().replace(",", "."));

                ClienteDTO clienteModificado = new ClienteDTO();
                clienteModificado.setRut(rutSeleccionado);
                clienteModificado.setNombres(nombresCompleto);
                clienteModificado.setSaldoActual(saldo);
                clienteModificado.setCupo(cupo);

                boolean exito = clienteDAO.actualizarCliente(clienteModificado);
                dialog.dispose();

                if (exito) {
                    cargarClientes();
                    mostrarMensajeClipper("¡Cliente modificado correctamente!", "AVISO");
                } else {
                    mostrarMensajeClipper("No se pudo actualizar el registro en la BD.", "ERROR");
                }

            } catch (NumberFormatException ex) {
                mostrarMensajeClipper("Los campos de Saldo y Cupo deben ser numéricos.", "ERROR");
            }
        };

        btnGuardar.addActionListener(guardarAccion);
        txtNombresMod.addActionListener(guardarAccion);
        txtSaldoMod.addActionListener(guardarAccion);
        txtCupoMod.addActionListener(guardarAccion);
        btnCancelar.addActionListener(e -> dialog.dispose());

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void buscarCliente() {
        JDialog dialog = new JDialog((Frame) null, "Buscar Cliente", true);
        dialog.setSize(440, 170);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(cFondoNegro);

        JLabel lblTit = new JLabel("--- BUSCAR CLIENTE (RUT / NOMBRE) ---", JLabel.CENTER);
        lblTit.setBounds(20, 15, 390, 20);
        lblTit.setForeground(cAcento);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lblTit);

        JTextField txtBusqueda = new JTextField();
        txtBusqueda.setBounds(30, 45, 370, 26);
        txtBusqueda.setBackground(cEncabezadoTabla);
        txtBusqueda.setForeground(cTextoClaro);
        txtBusqueda.setCaretColor(cAcento);
        txtBusqueda.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtBusqueda);

        JButton btnBuscar = new JButton("Buscar [Enter]");
        btnBuscar.setBounds(50, 85, 150, 30);
        btnBuscar.setBackground(new Color(0, 100, 0));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnBuscar);

        JButton btnCancelar = new JButton("Cancelar [Esc]");
        btnCancelar.setBounds(220, 85, 150, 30);
        btnCancelar.setBackground(cBarraSur);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnCancelar);

        java.awt.event.ActionListener realizarBusqueda = e -> {
            String rutBuscado = txtBusqueda.getText().trim();
            if (!rutBuscado.isEmpty()) {
                dialog.dispose();
                String busquedaLimpia = rutBuscado.toLowerCase();
                boolean encontrado = false;

                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    String rutTabla = (String) modeloTabla.getValueAt(i, 0);
                    String nombreTabla = (String) modeloTabla.getValueAt(i, 1);

                    if ((rutTabla != null && rutTabla.toLowerCase().contains(busquedaLimpia))
                            || (nombreTabla != null && nombreTabla.toLowerCase().contains(busquedaLimpia))) {

                        tablaClientes.setRowSelectionInterval(i, i);
                        tablaClientes.scrollRectToVisible(tablaClientes.getCellRect(i, 0, true));
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado) {
                    mostrarMensajeClipper("No se encontró ningún cliente con: " + rutBuscado, "AVISO");
                }
            }
        };

        btnBuscar.addActionListener(realizarBusqueda);
        txtBusqueda.addActionListener(realizarBusqueda);
        btnCancelar.addActionListener(e -> dialog.dispose());

        java.awt.event.KeyAdapter escAdapter = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    dialog.dispose();
                }
            }
        };

        dialog.addKeyListener(escAdapter);
        txtBusqueda.addKeyListener(escAdapter);
        btnBuscar.addKeyListener(escAdapter);
        btnCancelar.addKeyListener(escAdapter);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void mostrarMensajeClipper(String mensaje, String tipo) {
        JDialog dialog = new JDialog((Frame) null, tipo, true);
        dialog.setSize(380, 130);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(cFondoNegro);

        JLabel lbl = new JLabel(mensaje, JLabel.CENTER);
        lbl.setForeground(cTextoClaro);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.CENTER);

        JButton btnOk = new JButton("Aceptar [Enter]");
        btnOk.setBackground(cEncabezadoTabla);
        btnOk.setForeground(cAcento);
        btnOk.setFont(new Font("Monospaced", Font.BOLD, 11));

        java.awt.event.ActionListener cerrarAccion = e -> dialog.dispose();
        btnOk.addActionListener(cerrarAccion);

        dialog.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    dialog.dispose();
                }
            }
        });

        panel.add(btnOk, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void verEstadisticasCliente() {
        int fila = tablaClientes.getSelectedRow();

        if (fila >= 0) {
            int filaModelo = tablaClientes.convertRowIndexToModel(fila);
            Object valorCelda = modeloTabla.getValueAt(filaModelo, 0);

            if (valorCelda == null) {
                JOptionPane.showMessageDialog(this, "La celda del RUT está vacía.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rut = valorCelda.toString().trim();
            FrmEstadisticasClientes frmEstadisticas = new FrmEstadisticasClientes(rut);
            JDesktopPane escritorioReal = this.getDesktopPane();

            if (escritorioReal != null) {
                escritorioReal.add(frmEstadisticas);
                frmEstadisticas.setVisible(true);
                try {
                    frmEstadisticas.setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            } else {
                frmEstadisticas.setVisible(true);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para ver su ficha/estadísticas.");
        }
    }
}
