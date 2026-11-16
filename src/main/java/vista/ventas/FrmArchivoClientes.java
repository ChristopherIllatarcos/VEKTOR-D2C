package vista.ventas;

import vista.ctacte.FrmEstadisticasClientes;
import dao.ClienteDAO;
import javax.swing.*;
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
    private ClienteDAO clienteDAO;

    public FrmArchivoClientes() {
        clienteDAO = new ClienteDAO();
        configurarVentana();
        inicializarComponentes();
        cargarClientes();
    }

    private void configurarVentana() {
        setTitle("ARCHIVO DE CLIENTES");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0, 0, 128)); // Azul clásico terminal
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        // --- PANEL SUPERIOR (Título y contador de registros) ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(0, 0, 128));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel lblTitulo = new JLabel("ARCHIVO DE CLIENTES");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setForeground(Color.YELLOW);

        lblRegistros = new JLabel("Registros: 0");
        lblRegistros.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblRegistros.setForeground(Color.GREEN);

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(lblRegistros, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);

        // --- TABLA CENTRAL (R.U.T. y NOMBRE DEL CLIENTE) ---
        String[] columnas = {"R.U.T.", "NOMBRE DEL CLIENTE"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura tipo listado
            }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setFont(new Font("Monospaced", Font.BOLD, 16));
        tablaClientes.setRowHeight(25);
        tablaClientes.setBackground(new Color(0, 0, 170)); // Azul brillante estilo consola
        tablaClientes.setForeground(Color.WHITE);
        tablaClientes.setSelectionBackground(Color.CYAN);
        tablaClientes.setSelectionForeground(Color.BLACK);

        // Cabecera de la tabla
        tablaClientes.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));
        tablaClientes.getTableHeader().setBackground(new Color(0, 0, 100));
        tablaClientes.getTableHeader().setForeground(Color.YELLOW);

        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.getViewport().setBackground(new Color(0, 0, 170));
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));

        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(new Color(0, 0, 128));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        panelCentro.add(scrollPane, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        // --- PANEL INFERIOR (Barra de Acciones / Teclas rápidas) ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelInferior.setBackground(new Color(180, 0, 0)); // Barra roja clásica inferior
        panelInferior.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel lblAcciones = new JLabel("(Esc) Salir   |   (Ins) Insertar   |   (Supr) Eliminar   |   (M) Modificar   |   (B) Buscar   |   (V) Ver Ficha");
        lblAcciones.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblAcciones.setForeground(Color.WHITE);
        panelInferior.add(lblAcciones);
        add(panelInferior, BorderLayout.SOUTH);

        // --- EVENTOS DE TECLADO (Atajos idénticos al sistema original) ---
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

        // Ordena alfabéticamente al vuelo tomando en cuenta el apellido que pusimos al principio
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
        // 🎯 Usamos GridBagLayout en lugar de GridLayout para controlar el alto natural de los campos
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 0, 128));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Espaciado entre elementos
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Solo se estiran a lo ancho, NUNCA a lo alto

        Font fuenteEtiqueta = new Font("Monospaced", Font.BOLD, 14);
        Font fuenteCampo = new Font("Monospaced", Font.PLAIN, 14);
        Color colorTexto = Color.YELLOW;

        JTextField txtRutIns = new JTextField(15); // Ancho estimado
        txtRutIns.setFont(fuenteCampo);

        JTextField txtNombresIns = new JTextField(15);
        txtNombresIns.setFont(fuenteCampo);

        JTextField txtSaldoIns = new JTextField("0", 15);
        txtSaldoIns.setFont(fuenteCampo);

        JTextField txtCupoIns = new JTextField("1000000", 15);
        txtCupoIns.setFont(fuenteCampo);

        JLabel lblR = new JLabel(" R.U.T. :");
        lblR.setForeground(colorTexto);
        lblR.setFont(fuenteEtiqueta);

        JLabel lblNom = new JLabel(" NOMBRE COMPLETO :");
        lblNom.setForeground(colorTexto);
        lblNom.setFont(fuenteEtiqueta);

        JLabel lblS = new JLabel(" SALDO :");
        lblS.setForeground(colorTexto);
        lblS.setFont(fuenteEtiqueta);

        JLabel lblC = new JLabel(" CUPO :");
        lblC.setForeground(colorTexto);
        lblC.setFont(fuenteEtiqueta);

        // --- FILA 0: RUT ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(lblR, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // El campo de texto toma el ancho disponible
        panel.add(txtRutIns, gbc);

        // --- FILA 1: NOMBRE ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(lblNom, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtNombresIns, gbc);

        // --- FILA 2: SALDO ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(lblS, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtSaldoIns, gbc);

        // --- FILA 3: CUPO ---
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(lblC, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtCupoIns, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(550, 240));
        scrollPane.setBorder(null);
        int resultado = JOptionPane.showConfirmDialog(
                this, scrollPane, "INGRESAR NUEVO CLIENTE",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {
            String rutCompleto = txtRutIns.getText().trim();
            String nombresCompleto = txtNombresIns.getText().trim().toUpperCase();

            if (rutCompleto.isEmpty() || nombresCompleto.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El RUT y el Nombre Completo son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Separamos el RUT y el dígito verificador limpiamente
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

                // Creamos el DTO pasando el RUT limpio y el nombre completo unificado
                ClienteDTO nuevoCliente = new ClienteDTO();
                nuevoCliente.setRut(rutParteNumero);
                nuevoCliente.setDigitoVerificador(digitoVerificador);
                nuevoCliente.setNombres(nombresCompleto); // 👈 Todo el nombre y apellidos van aquí juntos
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
            // Convertimos el índice de la vista al índice real del modelo (por si la tabla está ordenada o filtrada)
            int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);

            // Obtenemos el RUT de la columna 0 usando el índice del modelo
            String rut = (String) modeloTabla.getValueAt(filaModelo, 0);

            // Creamos el diálogo modal con estilo Clipper
            JDialog dialog = new JDialog((Frame) null, "Eliminar Cliente", true);
            dialog.setSize(440, 160);
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);

            JPanel panel = new JPanel(null);
            panel.setBackground(new Color(0, 0, 128)); // Azul clásico Clipper

            JLabel lblTit = new JLabel("¿Desea eliminar al cliente RUT: " + rut + "?", JLabel.CENTER);
            lblTit.setBounds(20, 20, 390, 20);
            lblTit.setForeground(Color.YELLOW);
            lblTit.setFont(new Font("Monospaced", Font.BOLD, 12));
            panel.add(lblTit);

            JButton btnSi = new JButton("[S] Sí, Eliminar");
            btnSi.setBounds(40, 70, 170, 30);
            btnSi.setBackground(new Color(128, 0, 0)); // Rojo oscuro consola
            btnSi.setForeground(Color.WHITE);
            btnSi.setFont(new Font("Monospaced", Font.BOLD, 11));
            panel.add(btnSi);

            JButton btnNo = new JButton("[Esc] Cancelar");
            btnNo.setBounds(225, 70, 170, 30);
            btnNo.setBackground(new Color(0, 0, 160)); // Azul oscuro
            btnNo.setForeground(Color.WHITE);
            btnNo.setFont(new Font("Monospaced", Font.BOLD, 11));
            panel.add(btnNo);

            // Acción de confirmar eliminación
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

            // Acción de cancelar
            btnNo.addActionListener(e -> dialog.dispose());

            // Soporte para cerrar con la tecla ESC
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
        panel.setBackground(new Color(0, 0, 128));

        JLabel lblTit = new JLabel("--- MODIFICAR CLIENTE ---", JLabel.CENTER);
        lblTit.setBounds(60, 15, 400, 20);
        lblTit.setForeground(Color.YELLOW);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 14));
        panel.add(lblTit);

        Font fuenteTexto = new Font("Monospaced", Font.BOLD, 13);

        JLabel lblR = new JLabel("R.U.T. (Fijo):");
        lblR.setBounds(30, 55, 140, 25);
        lblR.setForeground(Color.WHITE);
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
        lblNom.setForeground(Color.WHITE);
        lblNom.setFont(fuenteTexto);
        panel.add(lblNom);

        JTextField txtNombresMod = new JTextField(clienteActual.getNombres());
        txtNombresMod.setBounds(180, 95, 290, 26);
        txtNombresMod.setBackground(new Color(0, 0, 160));
        txtNombresMod.setForeground(Color.WHITE);
        txtNombresMod.setCaretColor(Color.WHITE);
        txtNombresMod.setFont(fuenteTexto);
        panel.add(txtNombresMod);

        JLabel lblS = new JLabel("SALDO:");
        lblS.setBounds(30, 135, 140, 25);
        lblS.setForeground(Color.WHITE);
        lblS.setFont(fuenteTexto);
        panel.add(lblS);

        JTextField txtSaldoMod = new JTextField(clienteActual.getSaldoActual() != null ? clienteActual.getSaldoActual().toString() : "0");
        txtSaldoMod.setBounds(180, 135, 150, 26);
        txtSaldoMod.setBackground(new Color(0, 0, 160));
        txtSaldoMod.setForeground(Color.WHITE);
        txtSaldoMod.setCaretColor(Color.WHITE);
        txtSaldoMod.setFont(fuenteTexto);
        panel.add(txtSaldoMod);

        JLabel lblC = new JLabel("CUPO:");
        lblC.setBounds(30, 175, 140, 25);
        lblC.setForeground(Color.WHITE);
        lblC.setFont(fuenteTexto);
        panel.add(lblC);

        JTextField txtCupoMod = new JTextField(clienteActual.getCupo() != null ? clienteActual.getCupo().toString() : "1000000");
        txtCupoMod.setBounds(180, 175, 150, 26);
        txtCupoMod.setBackground(new Color(0, 0, 160));
        txtCupoMod.setForeground(Color.WHITE);
        txtCupoMod.setCaretColor(Color.WHITE);
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
        btnCancelar.setBackground(new Color(0, 0, 160));
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

        // Solución definitiva global para la tecla ESC usando el RootPane de la ventana
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void buscarCliente() {
        // Creamos un JDialog personalizado estilo Clipper para la entrada de texto
        JDialog dialog = new JDialog((Frame) null, "Buscar Cliente", true);
        dialog.setSize(440, 170);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(0, 0, 128)); // Azul clásico Clipper

        JLabel lblTit = new JLabel("--- BUSCAR CLIENTE (RUT / NOMBRE) ---", JLabel.CENTER);
        lblTit.setBounds(20, 15, 390, 20);
        lblTit.setForeground(Color.YELLOW);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lblTit);

        JTextField txtBusqueda = new JTextField();
        txtBusqueda.setBounds(30, 45, 370, 26);
        txtBusqueda.setBackground(new Color(0, 0, 160));
        txtBusqueda.setForeground(Color.WHITE);
        txtBusqueda.setCaretColor(Color.WHITE);
        txtBusqueda.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtBusqueda);

        JButton btnBuscar = new JButton("Buscar [Enter]");
        btnBuscar.setBounds(50, 85, 150, 30);
        btnBuscar.setBackground(new Color(0, 100, 0)); // Verde oscuro clásico
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnBuscar);

        JButton btnCancelar = new JButton("Cancelar [Esc]");
        btnCancelar.setBounds(220, 85, 150, 30);
        btnCancelar.setBackground(new Color(0, 0, 160));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnCancelar);

        // Lógica de búsqueda al presionar Enter o hacer clic en Buscar
        java.awt.event.ActionListener realizarBusqueda = e -> {
            String rutBuscado = txtBusqueda.getText().trim();
            if (!rutBuscado.isEmpty()) {
                dialog.dispose();
                String busquedaLimpia = rutBuscado.toLowerCase();
                boolean encontrado = false;

                // 1. Buscamos dentro de los registros cargados en la JTable
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

                // 2. Si no se encontró, mostramos el aviso con estilo Clipper
                if (!encontrado) {
                    mostrarMensajeClipper("No se encontró ningún cliente con: " + rutBuscado, "AVISO");
                }
            }
        };

        btnBuscar.addActionListener(realizarBusqueda);
        txtBusqueda.addActionListener(realizarBusqueda);
        btnCancelar.addActionListener(e -> dialog.dispose());

        // Soporte para cerrar o cancelar con la tecla ESC
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

    // Método auxiliar para mostrar alertas y avisos con estilo Clipper en Ficha Clientes
    private void mostrarMensajeClipper(String mensaje, String tipo) {
        JDialog dialog = new JDialog((Frame) null, tipo, true);
        dialog.setSize(380, 130);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 128)); // Azul Clipper

        JLabel lbl = new JLabel(mensaje, JLabel.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.CENTER);

        JButton btnOk = new JButton("Aceptar [Enter]");
        btnOk.setBackground(new Color(0, 0, 160));
        btnOk.setForeground(Color.YELLOW);
        btnOk.setFont(new Font("Monospaced", Font.BOLD, 11));

        // Acción para cerrar con clic o Enter
        java.awt.event.ActionListener cerrarAccion = e -> dialog.dispose();
        btnOk.addActionListener(cerrarAccion);

        // Soporte para cerrar con ESC
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
        System.out.println("DEBUG: Fila seleccionada -> " + fila);

        if (fila >= 0) {
            // Usamos el índice del modelo por si la tabla llega a estar filtrada u ordenada
            int filaModelo = tablaClientes.convertRowIndexToModel(fila);
            Object valorCelda = modeloTabla.getValueAt(filaModelo, 0);

            if (valorCelda == null) {
                JOptionPane.showMessageDialog(this, "La celda del RUT está vacía.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rut = valorCelda.toString().trim();
            System.out.println("DEBUG: RUT capturado para la ficha -> [" + rut + "]");

            // 1. Instanciamos el frame interno de estadísticas pasándole el RUT
            FrmEstadisticasClientes frmEstadisticas = new FrmEstadisticasClientes(rut);

            // 2. OBTENEMOS EL ESCRITORIO REAL DE LA APLICACIÓN (el de FrmPrincipal)
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
                // Respaldo por seguridad
                frmEstadisticas.setVisible(true);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para ver su ficha/estadísticas.");
        }
    }
}
