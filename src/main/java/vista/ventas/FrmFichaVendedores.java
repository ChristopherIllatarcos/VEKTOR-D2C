package vista.ventas;

import dao.VendedorDAO;
import modelo.VendedorDTO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private JLabel lblAyuda;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelCentro, panelInferior;
    private JScrollPane scrollPane;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelSuperior, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur;
    private final Font FUENTE_CONSOLA = new Font("Monospaced", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Naranja Fuego por defecto

    public FrmFichaVendedores() {
        vendedorDAO = new VendedorDAO();

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsCustom();
        // Asignar el icono Nexum a la ventana interna (JInternalFrame)
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
        cargarDatos();
        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });

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

        // Refrescar componentes visuales en tiempo de ejecución
        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        if (panelCentro != null) {
            panelCentro.setBackground(cFondoNegro);
        }

        if (tablaVendedores != null) {
            tablaVendedores.setBackground(cFondoNegro);
            tablaVendedores.setForeground(cTextoClaro);
            tablaVendedores.setSelectionBackground(cSeleccion);
            tablaVendedores.getTableHeader().setBackground(cEncabezadoTabla);
            tablaVendedores.getTableHeader().setForeground(cAcento);
            tablaVendedores.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
            tablaVendedores.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondoNegro);
            scrollPane.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        }

        if (panelInferior != null) {
            panelInferior.setBackground(cBarraSur);
            panelInferior.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));
        }

        actualizarTextoAtajos();

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAyuda != null) {
            lblAyuda.setText("<html>(Esc) Salir | (Ins) Insertar | (Supr) Eliminar | (M) Modificar &nbsp;|&nbsp; <b style='color:#FFFF55;'>(F5) Tema: " + temaActual + "</b></html>");
        }
    }

    private void initComponentsCustom() {
        setTitle("9.- FICHA DE VENDEDORES");
        setClosable(false);
        setIconifiable(false);
        setMaximizable(false);
        setResizable(false);
        setSize(850, 500);

        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondoNegro);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        JLabel lblTitulo = new JLabel(" V E N D E D O R E S ", JLabel.CENTER);
        lblTitulo.setForeground(cAcento);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        lblTitulo.setBackground(cPanelSuperior);
        lblTitulo.setOpaque(true);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        String[] columnas = {"CODIGO", "NOMBRE DEL VENDEDOR", "COMISION"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVendedores = new JTable(modeloTabla);
        tablaVendedores.setBackground(cFondoNegro);
        tablaVendedores.setForeground(cTextoClaro);
        tablaVendedores.setFont(new Font("Monospaced", Font.PLAIN, 16));
        tablaVendedores.setRowHeight(25);
        tablaVendedores.setShowGrid(false);
        tablaVendedores.setIntercellSpacing(new Dimension(0, 0));
        tablaVendedores.setSelectionBackground(cSeleccion);
        tablaVendedores.setSelectionForeground(Color.WHITE);

        tablaVendedores.getTableHeader().setBackground(cEncabezadoTabla);
        tablaVendedores.getTableHeader().setForeground(cAcento);
        tablaVendedores.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));
        tablaVendedores.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        // Renderizador para franjas horizontales alternadas transparentes
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

                if (column == 0 || column == 2) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tablaVendedores.getColumnCount(); i++) {
            tablaVendedores.getColumnModel().getColumn(i).setCellRenderer(rendererRayas);
        }

        scrollPane = new JScrollPane(tablaVendedores);
        scrollPane.getViewport().setBackground(cFondoNegro);
        scrollPane.setBorder(BorderFactory.createLineBorder(cAcento, 1));

        panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(cFondoNegro);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelCentro.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panelInferior.setBackground(cBarraSur);
        panelInferior.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAcento));

        lblAyuda = new JLabel();
        lblAyuda.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblAyuda.setForeground(Color.WHITE);
        actualizarTextoAtajos();
        panelInferior.add(lblAyuda);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);

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
                    case KeyEvent.VK_F5 ->
                        ciclarSiguienteTema(); // 👈 Cambio de tema dinámico con F5
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
    // DIÁLOGOS ESTILO CLIPPER
    // ==========================================
    private void dialogoInsertar() {
        JDialog dialog = new JDialog((Frame) null, "Insertar Vendedor", true);
        dialog.setSize(420, 230);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(cFondoNegro);

        JLabel lblTit = new JLabel("--- INGRESO DE VENDEDOR ---");
        lblTit.setBounds(70, 10, 280, 20);
        lblTit.setForeground(cAcento);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lblTit);

        JLabel lbl1 = new JLabel("Código:");
        lbl1.setBounds(30, 50, 90, 20);
        lbl1.setForeground(cTextoClaro);
        lbl1.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl1);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setBounds(130, 50, 80, 25);
        txtCodigo.setBackground(cEncabezadoTabla);
        txtCodigo.setForeground(cTextoClaro);
        txtCodigo.setCaretColor(cAcento);
        txtCodigo.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtCodigo);

        JLabel lbl2 = new JLabel("Nombre:");
        lbl2.setBounds(30, 85, 90, 20);
        lbl2.setForeground(cTextoClaro);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl2);

        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(130, 85, 230, 25);
        txtNombre.setBackground(cEncabezadoTabla);
        txtNombre.setForeground(cTextoClaro);
        txtNombre.setCaretColor(cAcento);
        txtNombre.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtNombre);

        JLabel lbl3 = new JLabel("Comisión (%):");
        lbl3.setBounds(30, 120, 100, 20);
        lbl3.setForeground(cTextoClaro);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl3);

        JTextField txtComision = new JTextField("0.00");
        txtComision.setBounds(130, 120, 80, 25);
        txtComision.setBackground(cEncabezadoTabla);
        txtComision.setForeground(cTextoClaro);
        txtComision.setCaretColor(cAcento);
        txtComision.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtComision);

        JButton btnAgregar = new JButton("Agregar [Enter]");
        btnAgregar.setBounds(130, 155, 160, 28);
        btnAgregar.setBackground(new Color(0, 100, 0));
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
        panel.setBackground(cFondoNegro);

        JLabel lblTit = new JLabel("--- MODIFICAR (Cód: " + codigoActual + ") ---");
        lblTit.setBounds(70, 10, 280, 20);
        lblTit.setForeground(cAcento);
        lblTit.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lblTit);

        JLabel lbl2 = new JLabel("Nombre:");
        lbl2.setBounds(30, 50, 90, 20);
        lbl2.setForeground(cTextoClaro);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl2);

        JTextField txtNombre = new JTextField(nombreActual);
        txtNombre.setBounds(130, 50, 230, 25);
        txtNombre.setBackground(cEncabezadoTabla);
        txtNombre.setForeground(cTextoClaro);
        txtNombre.setCaretColor(cAcento);
        txtNombre.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtNombre);

        JLabel lbl3 = new JLabel("Comisión (%):");
        lbl3.setBounds(30, 85, 100, 20);
        lbl3.setForeground(cTextoClaro);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl3);

        JTextField txtComision = new JTextField(String.valueOf(comisionActual));
        txtComision.setBounds(130, 85, 80, 25);
        txtComision.setBackground(cEncabezadoTabla);
        txtComision.setForeground(cTextoClaro);
        txtComision.setCaretColor(cAcento);
        txtComision.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(txtComision);

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
        panel.setBackground(cFondoNegro);

        JLabel lblTit = new JLabel("¿Eliminar a " + nombre + "?", JLabel.CENTER);
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
        panel.setBackground(cFondoNegro);

        JLabel lbl = new JLabel(mensaje, JLabel.CENTER);
        lbl.setForeground(cTextoClaro);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.CENTER);

        JButton btnOk = new JButton("Aceptar [Enter]");
        btnOk.setBackground(cEncabezadoTabla);
        btnOk.setForeground(cAcento);
        btnOk.addActionListener(e -> dialog.dispose());
        panel.add(btnOk, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
