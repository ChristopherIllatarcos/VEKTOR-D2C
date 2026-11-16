package vista.ventas;

import dao.VendedorDAO;
import modelo.VendedorDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class FrmFichaVendedores extends javax.swing.JInternalFrame {

    private JTable tablaVendedores;
    private DefaultTableModel modeloTabla;
    private VendedorDAO vendedorDAO;

    public FrmFichaVendedores() {
        vendedorDAO = new VendedorDAO();
        initComponentsCustom();
        cargarDatos();
    }

    private void initComponentsCustom() {
        setTitle("FICHA DE VENDEDORES");
        setClosable(true);
        setIconifiable(false);
        setMaximizable(false);
        setResizable(false);
        setSize(850, 500);

        // Panel principal con estilo retro (Azul Clipper)
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 128)); // Azul clásico

        // Título superior
        JLabel lblTitulo = new JLabel(" V E N D E D O R E S ", JLabel.CENTER);
        lblTitulo.setForeground(Color.RED);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Configuración de la Tabla (Grilla)
        String[] columnas = {"CODIGO", "NOMBRE DEL VENDEDOR", "COMISION"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVendedores = new JTable(modeloTabla);
        tablaVendedores.setBackground(new Color(0, 0, 160));
        tablaVendedores.setForeground(Color.WHITE);
        tablaVendedores.setFont(new Font("Monospaced", Font.PLAIN, 16));
        tablaVendedores.getTableHeader().setBackground(new Color(0, 0, 100));
        tablaVendedores.getTableHeader().setForeground(Color.YELLOW);
        tablaVendedores.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));

        JScrollPane scrollPane = new JScrollPane(tablaVendedores);
        scrollPane.getViewport().setBackground(new Color(0, 0, 128));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Barra inferior con las opciones estilo Clipper
        JLabel lblAyuda = new JLabel("(Esc) Salir   |   (Ins) Insertar   |   (Supr) Eliminar   |   (M) Modificar", JLabel.CENTER);
        lblAyuda.setForeground(Color.WHITE);
        lblAyuda.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblAyuda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(lblAyuda, BorderLayout.SOUTH);

        add(panelPrincipal);

        // Eventos de teclado globales para las acciones rápidas
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE ->
                        dispose();
                    case KeyEvent.VK_INSERT ->
                        dialogoInsertar();
                    case KeyEvent.VK_DELETE ->
                        dialogoEliminar();
                    case KeyEvent.VK_M ->
                        dialogoModificar();
                    default -> {
                    }
                }
            }
        };

        this.addKeyListener(keyAdapter);
        tablaVendedores.addKeyListener(keyAdapter);
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,##0.00'%'");
        try {
            List<VendedorDTO> lista = vendedorDAO.listarVendedores();
            for (VendedorDTO v : lista) {
                modeloTabla.addRow(new Object[]{
                    v.getCodigo(),
                    v.getNombre(),
                    df.format(v.getComision())
                });
            }
        } catch (SQLException e) {
            mostrarMensajeClipper("Error al cargar vendedores: " + e.getMessage(), "ERROR");
        }
    }

    // ==========================================
    // DIÁLOGOS ESTILO CLIPPER (AZUL / RETRO)
    // ==========================================
    private void dialogoInsertar() {
        JDialog dialog = new JDialog((Frame) null, "Insertar Vendedor", true);
        dialog.setSize(420, 230);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(0, 0, 128));

        JLabel lblTit = new JLabel("--- INGRESO DE VENDEDOR ---");
        lblTit.setBounds(70, 10, 280, 20);
        lblTit.setForeground(Color.YELLOW);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lblTit);

        JLabel lbl1 = new JLabel("Código:");
        lbl1.setBounds(30, 50, 90, 20);
        lbl1.setForeground(Color.WHITE);
        lbl1.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl1);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setBounds(130, 50, 80, 25);
        txtCodigo.setBackground(new Color(0, 0, 160));
        txtCodigo.setForeground(Color.WHITE);
        txtCodigo.setCaretColor(Color.WHITE);
        txtCodigo.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtCodigo);

        JLabel lbl2 = new JLabel("Nombre:");
        lbl2.setBounds(30, 85, 90, 20);
        lbl2.setForeground(Color.WHITE);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl2);

        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(130, 85, 230, 25);
        txtNombre.setBackground(new Color(0, 0, 160));
        txtNombre.setForeground(Color.WHITE);
        txtNombre.setCaretColor(Color.WHITE);
        txtNombre.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtNombre);

        JLabel lbl3 = new JLabel("Comisión (%):");
        lbl3.setBounds(30, 120, 100, 20);
        lbl3.setForeground(Color.WHITE);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl3);

        JTextField txtComision = new JTextField("0.00");
        txtComision.setBounds(130, 120, 80, 25);
        txtComision.setBackground(new Color(0, 0, 160));
        txtComision.setForeground(Color.WHITE);
        txtComision.setCaretColor(Color.WHITE);
        txtComision.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtComision);

        // Botón Agregar / Guardar estilo Clipper
        JButton btnAgregar = new JButton("Agregar [Enter]");
        btnAgregar.setBounds(130, 155, 160, 28);
        btnAgregar.setBackground(new Color(0, 100, 0)); // Verde oscuro clásico
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnAgregar);

        java.awt.event.ActionListener guardarAccion = e -> {
            try {
                int codigo = Integer.parseInt(txtCodigo.getText().trim());
                String nombre = txtNombre.getText().trim().toUpperCase();
                double comision = Double.parseDouble(txtComision.getText().trim().replace(",", "."));

                vendedorDAO.insertarVendedor(codigo, nombre, comision);
                cargarDatos();
                dialog.dispose();
                mostrarMensajeClipper("Vendedor insertado con éxito.", "AVISO");
            } catch (NumberFormatException | SQLException ex) {
                mostrarMensajeClipper("Error al insertar: Verifique los datos.", "ERROR");
            }
        };

        btnAgregar.addActionListener(guardarAccion);
        txtCodigo.addActionListener(guardarAccion);
        txtNombre.addActionListener(guardarAccion);
        txtComision.addActionListener(guardarAccion);

        // Listener global de teclado para cerrar con ESC en cualquier campo del diálogo
        KeyAdapter escAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialog.dispose();
                }
            }
        };

        dialog.addKeyListener(escAdapter);
        txtCodigo.addKeyListener(escAdapter);
        txtNombre.addKeyListener(escAdapter);
        txtComision.addKeyListener(escAdapter);
        btnAgregar.addKeyListener(escAdapter);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void dialogoModificar() {
        int fila = tablaVendedores.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("Seleccione un vendedor de la grilla.", "AVISO");
            return;
        }

        int codigoActual = (int) modeloTabla.getValueAt(fila, 0);
        String nombreActual = (String) modeloTabla.getValueAt(fila, 1);
        String comStr = modeloTabla.getValueAt(fila, 2).toString().replace("%", "").replace(",", ".").trim();
        double comisionActual = Double.parseDouble(comStr);

        JDialog dialog = new JDialog((Frame) null, "Modificar Vendedor", true);
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(0, 0, 128));

        JLabel lblTit = new JLabel("--- MODIFICAR (Cód: " + codigoActual + ") ---");
        lblTit.setBounds(70, 10, 280, 20);
        lblTit.setForeground(Color.YELLOW);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lblTit);

        JLabel lbl2 = new JLabel("Nombre:");
        lbl2.setBounds(30, 50, 90, 20);
        lbl2.setForeground(Color.WHITE);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl2);

        JTextField txtNombre = new JTextField(nombreActual);
        txtNombre.setBounds(130, 50, 230, 25);
        txtNombre.setBackground(new Color(0, 0, 160));
        txtNombre.setForeground(Color.WHITE);
        txtNombre.setCaretColor(Color.WHITE);
        txtNombre.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtNombre);

        JLabel lbl3 = new JLabel("Comisión (%):");
        lbl3.setBounds(30, 85, 100, 20);
        lbl3.setForeground(Color.WHITE);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl3);

        JTextField txtComision = new JTextField(String.valueOf(comisionActual));
        txtComision.setBounds(130, 85, 80, 25);
        txtComision.setBackground(new Color(0, 0, 160));
        txtComision.setForeground(Color.WHITE);
        txtComision.setCaretColor(Color.WHITE);
        txtComision.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtComision);

        // Botón o Enter para guardar cambios
        JButton btnGuardar = new JButton("Guardar [Enter]");
        btnGuardar.setBounds(130, 125, 150, 25);
        btnGuardar.setBackground(new Color(0, 100, 0));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnGuardar);

        ActionListener guardarAccion = e -> {
            try {
                String nombre = txtNombre.getText().trim().toUpperCase();
                double comision = Double.parseDouble(txtComision.getText().trim().replace(",", "."));

                vendedorDAO.actualizarVendedor(codigoActual, nombre, comision);
                cargarDatos();
                dialog.dispose();
            } catch (Exception ex) {
                mostrarMensajeClipper("Error al modificar: " + ex.getMessage(), "ERROR");
            }
        };

        btnGuardar.addActionListener(guardarAccion);
        txtNombre.addActionListener(guardarAccion);
        txtComision.addActionListener(guardarAccion);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void dialogoEliminar() {
        int fila = tablaVendedores.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("Seleccione un vendedor de la grilla.", "AVISO");
            return;
        }

        int codigo = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);

        JDialog dialog = new JDialog((Frame) null, "Eliminar Vendedor", true);
        dialog.setSize(440, 160);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(0, 0, 128));

        JLabel lblTit = new JLabel("¿Eliminar a " + nombre + "?", JLabel.CENTER);
        lblTit.setBounds(20, 20, 390, 20);
        lblTit.setForeground(Color.YELLOW);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lblTit);

        JButton btnSi = new JButton("[S] Sí, Eliminar");
        btnSi.setBounds(40, 70, 170, 30);
        btnSi.setBackground(new Color(128, 0, 0));
        btnSi.setForeground(Color.WHITE);
        btnSi.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnSi);

        JButton btnNo = new JButton("[Esc] Cancelar");
        btnNo.setBounds(225, 70, 170, 30);
        btnNo.setBackground(new Color(0, 0, 160));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(btnNo);

        btnSi.addActionListener(e -> {
            try {
                vendedorDAO.eliminarVendedor(codigo);
                cargarDatos();
                dialog.dispose();
                mostrarMensajeClipper("Vendedor eliminado con éxito.", "AVISO");
            } catch (SQLException ex) {
                mostrarMensajeClipper("No se pudo eliminar: " + ex.getMessage(), "ERROR");
            }
        });

        btnNo.addActionListener(e -> dialog.dispose());

        // Listener global de teclado para cerrar con ESC en el diálogo de eliminar
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
    }

    private void mostrarMensajeClipper(String mensaje, String tipo) {
        JDialog dialog = new JDialog((Frame) null, tipo, true);
        dialog.setSize(350, 130);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 128));

        JLabel lbl = new JLabel(mensaje, JLabel.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.CENTER);

        JButton btnOk = new JButton("Aceptar [Enter]");
        btnOk.setBackground(new Color(0, 0, 160));
        btnOk.setForeground(Color.YELLOW);
        btnOk.addActionListener(e -> dialog.dispose());
        panel.add(btnOk, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
