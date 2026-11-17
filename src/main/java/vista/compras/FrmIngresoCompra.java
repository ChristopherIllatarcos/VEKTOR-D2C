package vista.compras;

import dao.IngresoCompraDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrmIngresoCompra extends JInternalFrame {

    private JTextField txtFechaInicial;
    private JTextField txtFechaFinal;
    private JTextField txtBuscar;

    private JTable tablaCompras;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalUnidades;
    private JLabel lblTotalMonto;

    // Paleta Esmeralda Oscuro / Verde Bosque y Neón
    private final Color COLOR_FONDO = new Color(11, 58, 26);          // Verde bosque oscuro (#0B3A1A)
    private final Color COLOR_PANEL_TABLA = new Color(5, 28, 12);     // Fondo tabla profundo
    private final Color COLOR_ZEBRA_1 = new Color(5, 28, 12);         // Fila cebra par
    private final Color COLOR_ZEBRA_2 = new Color(12, 45, 20);        // Fila cebra impar
    private final Color COLOR_BORDE_NEON = new Color(0, 255, 136);    // Verde esmeralda neón (#00FF88)
    private final Color COLOR_TEXTO_CLARO = new Color(220, 255, 235);   // Verde claro / Blanco
    private final Color COLOR_BARRA_INF = new Color(20, 90, 45);      // Verde bosque medio
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    public FrmIngresoCompra() {
        super("2.- INGRESO DE COMPRAS", false, false, false, false);
        initComponentsManual();
        cargarFechasPorDefecto();
        ejecutarBusqueda();
    }

    private void initComponentsManual() {
        setSize(1150, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(0, 6));

        // ==========================================
        // 1. PANEL SUPERIOR (Filtros)
        // ==========================================
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(COLOR_FONDO);
        panelSuperior.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_NEON, 2),
                " INGRESO DE COMPRAS - FILTROS DE CONSULTA ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_NEON
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        txtFechaInicial = crearCampoTexto(10);
        txtFechaFinal = crearCampoTexto(10);
        txtBuscar = crearCampoTexto(22);

        panelCampos.add(crearEtiqueta("F. Inicial:"));
        panelCampos.add(txtFechaInicial);
        panelCampos.add(crearEtiqueta("F. Final:"));
        panelCampos.add(txtFechaFinal);
        panelCampos.add(crearEtiqueta("Factura/Proveedor/Producto:"));
        panelCampos.add(txtBuscar);

        txtFechaInicial.addActionListener(e -> {
            ejecutarBusqueda();
            txtFechaFinal.requestFocusInWindow();
            txtFechaFinal.selectAll();
        });
        txtFechaFinal.addActionListener(e -> {
            ejecutarBusqueda();
            txtBuscar.requestFocusInWindow();
            txtBuscar.selectAll();
        });
        txtBuscar.addActionListener(e -> ejecutarBusqueda());

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla con Filas Cebra)
        // ==========================================
        String[] columnas = {"Fecha", "N° Factura", "Proveedor", "# Barra", "Descripción Producto", "Cantidad", "Costo Unit. ($)", "Total Compra ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCompras = new JTable(modeloTabla);
        tablaCompras.setFont(FUENTE_CONSOLA);
        tablaCompras.setBackground(COLOR_PANEL_TABLA);
        tablaCompras.setForeground(Color.WHITE);
        tablaCompras.setGridColor(new Color(25, 90, 45));
        tablaCompras.setRowHeight(28);
        tablaCompras.setSelectionBackground(COLOR_BARRA_INF);
        tablaCompras.setSelectionForeground(Color.YELLOW);

        tablaCompras.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaCompras.getTableHeader().setBackground(COLOR_FONDO);
        tablaCompras.getTableHeader().setForeground(COLOR_BORDE_NEON);

        DefaultTableCellRenderer renderizadorCebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(COLOR_BARRA_INF);
                    c.setForeground(Color.YELLOW);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(COLOR_ZEBRA_1);
                    } else {
                        c.setBackground(COLOR_ZEBRA_2);
                    }
                    c.setForeground(Color.WHITE);
                }

                if (column >= 5) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tablaCompras.getColumnCount(); i++) {
            tablaCompras.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaCompras.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaCompras.getColumnModel().getColumn(1).setPreferredWidth(110);
        tablaCompras.getColumnModel().getColumn(2).setPreferredWidth(180);
        tablaCompras.getColumnModel().getColumn(3).setPreferredWidth(110);
        tablaCompras.getColumnModel().getColumn(4).setPreferredWidth(280);
        tablaCompras.getColumnModel().getColumn(5).setPreferredWidth(90);
        tablaCompras.getColumnModel().getColumn(6).setPreferredWidth(120);
        tablaCompras.getColumnModel().getColumn(7).setPreferredWidth(130);

        JScrollPane scrollTabla = new JScrollPane(tablaCompras);
        scrollTabla.getViewport().setBackground(COLOR_PANEL_TABLA);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE_NEON, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        JPanel panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(COLOR_FONDO);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(COLOR_FONDO);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDE_NEON));

        lblTotalUnidades = crearEtiqueta("TOTAL UNIDADES: 0");
        lblTotalMonto = new JLabel("TOTAL COMPRAS: $0");
        lblTotalMonto.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalMonto.setForeground(COLOR_BORDE_NEON);

        panelTotales.add(lblTotalUnidades);
        panelTotales.add(lblTotalMonto);

        JPanel panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajos.setBackground(COLOR_BARRA_INF);
        panelAtajos.setBorder(new LineBorder(COLOR_BORDE_NEON, 1));

        JLabel lblAtajos = new JLabel("(Ins) Nuevo    |    (F2) Editar    |    (Supr) Eliminar    |    (F5) Refrescar    |    (Esc) Salir");
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(COLOR_TEXTO_CLARO);
        panelAtajos.add(lblAtajos);

        panelInferiorContenedor.add(panelTotales, BorderLayout.NORTH);
        panelInferiorContenedor.add(panelAtajos, BorderLayout.SOUTH);
        add(panelInferiorContenedor, BorderLayout.SOUTH);

        configurarAtajos();
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(COLOR_TEXTO_CLARO);
        return lbl;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(COLOR_PANEL_TABLA);
        txt.setForeground(COLOR_BORDE_NEON);
        txt.setCaretColor(COLOR_BORDE_NEON);
        txt.setBorder(new LineBorder(COLOR_BORDE_NEON, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(15, 60, 30));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(COLOR_PANEL_TABLA);
                txt.setBorder(new LineBorder(COLOR_BORDE_NEON, 1));
            }
        });

        return txt;
    }

    private void cargarFechasPorDefecto() {
        String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        txtFechaInicial.setText(hoy);
        txtFechaFinal.setText(hoy);
    }

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        IngresoCompraDAO dao = new IngresoCompraDAO();
        List<Object[]> resultados = dao.buscarCompras(txtFechaInicial.getText(), txtFechaFinal.getText(), txtBuscar.getText());

        double sumaUnidades = 0;
        double sumaMonto = 0;

        for (Object[] fila : resultados) {
            double cant = (double) fila[5];
            double costo = (double) fila[6];
            double total = (double) fila[7];

            sumaUnidades += cant;
            sumaMonto += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                fila[3],
                fila[4],
                String.format("%,.0f", cant),
                String.format("$%,.0f", costo),
                String.format("$%,.0f", total)
            });
        }

        lblTotalUnidades.setText(String.format("TOTAL UNIDADES: %,.0f", sumaUnidades));
        lblTotalMonto.setText(String.format("TOTAL COMPRAS: $%,.0f", sumaMonto));
    }

    private void abrirFormularioMantenimiento(boolean esEdicion) {
        String fechaActual = txtFechaInicial.getText().isEmpty() ? LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : txtFechaInicial.getText();
        String fechaSel = "";
        String facturaSel = "";
        String proveeSel = "";
        String barraSel = "";
        String nombreSel = "";
        String cantSel = "1";
        String costoSel = "0";

        if (esEdicion) {
            int fila = tablaCompras.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            fechaSel = (String) modeloTabla.getValueAt(fila, 0);
            facturaSel = (String) modeloTabla.getValueAt(fila, 1);
            proveeSel = (String) modeloTabla.getValueAt(fila, 2);
            barraSel = (String) modeloTabla.getValueAt(fila, 3);
            nombreSel = (String) modeloTabla.getValueAt(fila, 4);
            cantSel = modeloTabla.getValueAt(fila, 5).toString().replace(".", "").replace(",", "").trim();
            costoSel = modeloTabla.getValueAt(fila, 6).toString().replace("$", "").replace(".", "").replace(",", "").trim();
        }

        final String facturaOriginal = facturaSel;
        final String barraOriginal = barraSel;

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), esEdicion ? "EDITAR INGRESO DE COMPRA" : "NUEVO INGRESO DE COMPRA", true);
        dialogo.setSize(500, 430);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(COLOR_FONDO);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_NEON, 2),
                esEdicion ? " MODIFICAR DATOS DE COMPRA " : " REGISTRAR NUEVA ADQUISICIÓN ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_NEON
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtF = crearCampoTexto(14);
        txtF.setText(esEdicion ? fechaSel : fechaActual);
        JTextField txtFact = crearCampoTexto(14);
        txtFact.setText(facturaSel);
        JTextField txtProv = crearCampoTexto(22);
        txtProv.setText(proveeSel);
        JTextField txtB = crearCampoTexto(14);
        txtB.setText(barraSel);
        JTextField txtN = crearCampoTexto(24);
        txtN.setText(nombreSel);
        JTextField txtC = crearCampoTexto(14);
        txtC.setText(cantSel);
        JTextField txtCost = crearCampoTexto(14);
        txtCost.setText(costoSel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentro.add(crearEtiqueta("Fecha (dd/MM/yyyy):"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentro.add(crearEtiqueta("N° Factura:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtFact, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentro.add(crearEtiqueta("Proveedor:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtProv, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(crearEtiqueta("Código de Barra:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCentro.add(crearEtiqueta("Nombre Producto:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtN, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCentro.add(crearEtiqueta("Cantidad:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtC, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panelCentro.add(crearEtiqueta("Costo Unitario ($):"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCost, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(COLOR_BARRA_INF);
        panelBotones.setBorder(new LineBorder(COLOR_BORDE_NEON, 1));

        JButton btnGuardar = new JButton("Guardar [F3]");
        btnGuardar.setFont(FUENTE_CONSOLA);
        JButton btnCancelar = new JButton("Cancelar [Esc]");
        btnCancelar.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);
        Runnable guardarAccion = () -> {
            try {
                String f = txtF.getText().trim();
                String fact = txtFact.getText().trim();
                String prov = txtProv.getText().trim();
                String cond = "0";
                String b = txtB.getText().trim();
                String n = txtN.getText().trim();
                String cant = txtC.getText().trim().replace(",", ".");
                String cost = txtCost.getText().trim().replace(",", ".");

                IngresoCompraDAO dao = new IngresoCompraDAO();
                boolean exito;
                if (esEdicion) {
                    exito = dao.actualizarCompra(facturaOriginal, barraOriginal, f, fact, prov, cond, b, n, cant, cost);
                } else {
                    exito = dao.insertarCompra(f, fact, prov, cond, b, n, cant, cost);
                }

                if (exito) {
                    dialogo.dispose();
                    ejecutarBusqueda();
                } else {
                    JOptionPane.showMessageDialog(dialogo, "Error al guardar el registro en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(dialogo, "Verifique que los valores sean válidos.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnGuardar.addActionListener(e -> guardarAccion.run());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> guardarAccion.run(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void eliminarRegistroSeleccionado() {
        int fila = tablaCompras.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String factura = (String) modeloTabla.getValueAt(fila, 1);
        String barra = (String) modeloTabla.getValueAt(fila, 3);
        String nombre = (String) modeloTabla.getValueAt(fila, 4);

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), "CONFIRMAR ELIMINACIÓN DE COMPRA", true);
        dialogo.setSize(480, 220);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(COLOR_FONDO);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_NEON, 2),
                " ADVERTENCIA DE ELIMINACIÓN ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_NEON
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblPregunta = new JLabel("¿Desea eliminar la compra seleccionada?");
        lblPregunta.setFont(FUENTE_CONSOLA);
        lblPregunta.setForeground(Color.YELLOW);
        panelCentro.add(lblPregunta, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 15, 10, 15);
        JLabel lblDetalle = new JLabel("<html><center>" + nombre + "<br><font color='#00FF88'>[Factura: " + factura + "]</font></center></html>");
        lblDetalle.setFont(new Font("Consolas", Font.BOLD, 13));
        lblDetalle.setForeground(COLOR_TEXTO_CLARO);
        panelCentro.add(lblDetalle, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(COLOR_BARRA_INF);
        panelBotones.setBorder(new LineBorder(COLOR_BORDE_NEON, 1));

        JButton btnSi = new JButton("Sí [Enter]");
        btnSi.setFont(FUENTE_CONSOLA);
        JButton btnNo = new JButton("No [Esc]");
        btnNo.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable accionEliminar = () -> {
            IngresoCompraDAO dao = new IngresoCompraDAO();
            if (dao.eliminarCompra(factura, barra)) {
                dialogo.dispose();
                ejecutarBusqueda();
            } else {
                JOptionPane.showMessageDialog(dialogo, "No se pudo eliminar el registro de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                dialogo.dispose();
            }
        };

        btnSi.addActionListener(e -> accionEliminar.run());
        btnNo.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> accionEliminar.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void configurarAtajos() {
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtFechaInicial.setText("");
                txtFechaFinal.setText("");
                txtBuscar.setText("");
                ejecutarBusqueda();
                txtFechaInicial.requestFocusInWindow();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "nuevo");
        root.getActionMap().put("nuevo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(false);
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");
        tablaCompras.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");

        AbstractAction accionEditar = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(true);
            }
        };
        root.getActionMap().put("editar", accionEditar);
        tablaCompras.getActionMap().put("editar", accionEditar);

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarRegistroSeleccionado();
            }
        });
    }
}
