package vista.bodega;

import conexion.Conexion;
import dao.IngresoMercaderiaDAO;
import dao.ParametrosDAO;
import modelo.ParametrosDTO;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrmIngresoMercaderia extends JInternalFrame {

    private JTextField txtFechaInicial;
    private JTextField txtFechaFinal;
    private JTextField txtBuscarProducto;

    private JTable tablaIngresos;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalUnidades;
    private JLabel lblTotalMonto;

    // Paleta Púrpura Oscuro / Ciruela y Magenta
    private final Color COLOR_FONDO = new Color(43, 11, 58);
    private final Color COLOR_PANEL_TABLA = new Color(20, 5, 28);
    private final Color COLOR_ZEBRA_1 = new Color(20, 5, 28);
    private final Color COLOR_ZEBRA_2 = new Color(28, 8, 38);
    private final Color COLOR_BORDE_MAGENTA = new Color(255, 119, 255);
    private final Color COLOR_TEXTO_CLARO = new Color(240, 220, 255);
    private final Color COLOR_BARRA_INF = new Color(75, 20, 100);
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    public FrmIngresoMercaderia() {
        super("3.- INGRESO DE MERCADERÍA", false, false, false, false);
        initComponentsManual();
        cargarFechaDesdeParametros();
        ejecutarBusquedaInicial();
    }

    private void initComponentsManual() {
        setSize(1100, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(0, 6));

        // ==========================================
        // 1. PANEL SUPERIOR (Filtros)
        // ==========================================
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(COLOR_FONDO);
        panelSuperior.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_MAGENTA, 2),
                " INGRESO DE MERCADERÍA - FILTROS DE CONSULTA ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_MAGENTA
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        txtFechaInicial = crearCampoTexto(10);
        txtFechaFinal = crearCampoTexto(10);
        txtBuscarProducto = crearCampoTexto(24);

        panelCampos.add(crearEtiqueta("Fecha Inicial:"));
        panelCampos.add(txtFechaInicial);
        panelCampos.add(crearEtiqueta("Fecha Final:"));
        panelCampos.add(txtFechaFinal);
        panelCampos.add(crearEtiqueta("Barra/Producto:"));
        panelCampos.add(txtBuscarProducto);

        txtFechaInicial.addActionListener(e -> {
            ejecutarBusqueda();
            txtFechaFinal.requestFocusInWindow();
            txtFechaFinal.selectAll();
        });
        txtFechaFinal.addActionListener(e -> {
            ejecutarBusqueda();
            txtBuscarProducto.requestFocusInWindow();
            txtBuscarProducto.selectAll();
        });
        txtBuscarProducto.addActionListener(e -> ejecutarBusqueda());

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla con Filas Cebra)
        // ==========================================
        String[] columnas = {"Fecha", "# Barra", "Descripción del Producto", "Cantidad", "Costo Unit. ($)", "Total Ingreso ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaIngresos = new JTable(modeloTabla);
        tablaIngresos.setFont(FUENTE_CONSOLA);
        tablaIngresos.setBackground(COLOR_PANEL_TABLA);
        tablaIngresos.setForeground(Color.WHITE);
        tablaIngresos.setGridColor(new Color(60, 20, 80));
        tablaIngresos.setRowHeight(28);
        tablaIngresos.setSelectionBackground(COLOR_BARRA_INF);
        tablaIngresos.setSelectionForeground(Color.YELLOW);

        tablaIngresos.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaIngresos.getTableHeader().setBackground(COLOR_FONDO);
        tablaIngresos.getTableHeader().setForeground(COLOR_BORDE_MAGENTA);

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

                if (column >= 3) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tablaIngresos.getColumnCount(); i++) {
            tablaIngresos.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaIngresos.getColumnModel().getColumn(0).setPreferredWidth(110);
        tablaIngresos.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaIngresos.getColumnModel().getColumn(2).setPreferredWidth(360);
        tablaIngresos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaIngresos.getColumnModel().getColumn(4).setPreferredWidth(130);
        tablaIngresos.getColumnModel().getColumn(5).setPreferredWidth(140);

        JScrollPane scrollTabla = new JScrollPane(tablaIngresos);
        scrollTabla.getViewport().setBackground(COLOR_PANEL_TABLA);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE_MAGENTA, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        JPanel panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(COLOR_FONDO);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(COLOR_FONDO);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDE_MAGENTA));

        lblTotalUnidades = crearEtiqueta("TOTAL UNIDADES: 0");
        lblTotalMonto = new JLabel("TOTAL INGRESOS: $0");
        lblTotalMonto.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalMonto.setForeground(COLOR_BORDE_MAGENTA);

        panelTotales.add(lblTotalUnidades);
        panelTotales.add(lblTotalMonto);

        JPanel panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajos.setBackground(COLOR_BARRA_INF);
        panelAtajos.setBorder(new LineBorder(COLOR_BORDE_MAGENTA, 1));

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
        txt.setForeground(COLOR_BORDE_MAGENTA);
        txt.setCaretColor(COLOR_BORDE_MAGENTA);
        txt.setBorder(new LineBorder(COLOR_BORDE_MAGENTA, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(45, 12, 60));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(COLOR_PANEL_TABLA);
                txt.setBorder(new LineBorder(COLOR_BORDE_MAGENTA, 1));
            }
        });

        return txt;
    }

    private void cargarFechaDesdeParametros() {
        String fechaDefecto = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try (Connection con = Conexion.getConnection()) {
            ParametrosDAO pDao = new ParametrosDAO();
            ParametrosDTO param = pDao.obtenerParametros(con);
            if (param.getFecha() != null && !param.getFecha().trim().isEmpty()) {
                fechaDefecto = param.getFecha().trim();
            }
        } catch (Exception ignored) {
        }
        txtFechaInicial.setText(fechaDefecto);
        txtFechaFinal.setText(fechaDefecto);
    }

    private void ejecutarBusquedaInicial() {
        cargarTabla("", "", "");
    }

    private void ejecutarBusqueda() {
        cargarTabla(txtFechaInicial.getText(), txtFechaFinal.getText(), txtBuscarProducto.getText());
    }

    private void cargarTabla(String fIni, String fFin, String prod) {
        modeloTabla.setRowCount(0);
        IngresoMercaderiaDAO dao = new IngresoMercaderiaDAO();
        List<Object[]> resultados = dao.buscarIngresos(fIni, fFin, prod);

        double sumaUnidades = 0;
        double sumaMonto = 0;

        for (Object[] fila : resultados) {
            double cant = (double) fila[3];
            double costo = (double) fila[4];
            double total = (double) fila[5];

            sumaUnidades += cant;
            sumaMonto += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                String.format("%,.0f", cant),
                String.format("$%,.0f", costo),
                String.format("$%,.0f", total)
            });
        }

        lblTotalUnidades.setText(String.format("TOTAL UNIDADES: %,.0f", sumaUnidades));
        lblTotalMonto.setText(String.format("TOTAL INGRESOS: $%,.0f", sumaMonto));
    }

    private void abrirFormularioMantenimiento(boolean esEdicion) {
        String fechaActual = txtFechaInicial.getText().isEmpty() ? LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : txtFechaInicial.getText();
        String fechaSel = "";
        String barraSel = "";
        String nombreSel = "";
        String cantSel = "1";
        String costoSel = "0";

        if (esEdicion) {
            int fila = tablaIngresos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            fechaSel = (String) modeloTabla.getValueAt(fila, 0);
            barraSel = (String) modeloTabla.getValueAt(fila, 1);
            nombreSel = (String) modeloTabla.getValueAt(fila, 2);
            cantSel = modeloTabla.getValueAt(fila, 3).toString().replace(".", "").replace(",", "").trim();
            costoSel = modeloTabla.getValueAt(fila, 4).toString().replace("$", "").replace(".", "").replace(",", "").trim();
        }

        final String barraOriginal = barraSel;

        // JDialog con la misma estética Púrpura / Magenta unificada
        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), esEdicion ? "EDITAR INGRESO DE MERCADERÍA" : "NUEVO INGRESO DE MERCADERÍA", true);
        dialogo.setSize(480, 360);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(COLOR_FONDO);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_MAGENTA, 2),
                esEdicion ? " MODIFICAR DATOS DEL REGISTRO " : " REGISTRAR NUEVA ENTRADA ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_MAGENTA
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtF = crearCampoTexto(14);
        txtF.setText(esEdicion ? fechaSel : fechaActual);
        JTextField txtB = crearCampoTexto(14);
        txtB.setText(barraSel);
        JTextField txtN = crearCampoTexto(22);
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
        panelCentro.add(crearEtiqueta("Código de Barra:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentro.add(crearEtiqueta("Nombre Producto:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtN, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(crearEtiqueta("Cantidad (Stock):"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtC, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCentro.add(crearEtiqueta("Costo Unitario ($):"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCost, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        // Panel de Botones inferior en el diálogo
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(COLOR_BARRA_INF);
        panelBotones.setBorder(new LineBorder(COLOR_BORDE_MAGENTA, 1));

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
                String b = txtB.getText().trim();
                String n = txtN.getText().trim();
                double cant = Double.parseDouble(txtC.getText().trim());
                double cost = Double.parseDouble(txtCost.getText().trim());

                IngresoMercaderiaDAO dao = new IngresoMercaderiaDAO();
                boolean exito;
                if (esEdicion) {
                    exito = dao.actualizarIngreso(barraOriginal, f, b, n, cant, cost);
                } else {
                    exito = dao.insertarIngreso(f, b, n, cant, cost);
                }

                if (exito) {
                    dialogo.dispose();
                    ejecutarBusqueda();
                } else {
                    JOptionPane.showMessageDialog(dialogo, "Error al guardar el registro en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Verifique que los valores numéricos sean válidos.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnGuardar.addActionListener(e -> guardarAccion.run());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> guardarAccion.run(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void eliminarRegistroSeleccionado() {
        int fila = tablaIngresos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String barra = (String) modeloTabla.getValueAt(fila, 1);
        String nombre = (String) modeloTabla.getValueAt(fila, 2);

        // JDialog de confirmación personalizado con la estética Púrpura / Magenta
        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), "CONFIRMAR ELIMINACIÓN", true);
        dialogo.setSize(480, 220);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(COLOR_FONDO);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_MAGENTA, 2),
                " ADVERTENCIA DE ELIMINACIÓN ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_MAGENTA
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblPregunta = new JLabel("¿Desea eliminar el registro seleccionado?");
        lblPregunta.setFont(FUENTE_CONSOLA);
        lblPregunta.setForeground(Color.YELLOW);
        panelCentro.add(lblPregunta, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 15, 10, 15);
        JLabel lblDetalle = new JLabel("<html><center>" + nombre + "<br><font color='#FF77FF'>[Barra: " + barra + "]</font></center></html>");
        lblDetalle.setFont(new Font("Consolas", Font.BOLD, 13));
        lblDetalle.setForeground(COLOR_TEXTO_CLARO);
        panelCentro.add(lblDetalle, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        // Panel inferior de botones con la barra de atajos
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(COLOR_BARRA_INF);
        panelBotones.setBorder(new LineBorder(COLOR_BORDE_MAGENTA, 1));

        JButton btnSi = new JButton("Sí [Enter]");
        btnSi.setFont(FUENTE_CONSOLA);
        JButton btnNo = new JButton("No [Esc]");
        btnNo.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable accionEliminar = () -> {
            IngresoMercaderiaDAO dao = new IngresoMercaderiaDAO();
            if (dao.eliminarIngreso(barra)) {
                dialogo.dispose();
                ejecutarBusqueda();
            } else {
                JOptionPane.showMessageDialog(dialogo, "No se pudo eliminar el registro de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                dialogo.dispose();
            }
        };

        btnSi.addActionListener(e -> accionEliminar.run());
        btnNo.addActionListener(e -> dialogo.dispose());

        // Atajos de teclado dentro del diálogo: [Enter] para confirmar, [Esc] para cancelar
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> accionEliminar.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void configurarAtajos() {
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // [ESC] Cerrar ventana
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F5] Refrescar / Limpiar
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtFechaInicial.setText("");
                txtFechaFinal.setText("");
                txtBuscarProducto.setText("");
                ejecutarBusqueda();
                txtFechaInicial.requestFocusInWindow();
            }
        });

        // [INSERT] Nuevo Registro
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "nuevo");
        root.getActionMap().put("nuevo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(false);
            }
        });

        // [F2] Editar Registro (Blindado tanto a nivel de ventana como de tabla)
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");
        tablaIngresos.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");

        AbstractAction accionEditar = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(true);
            }
        };

        root.getActionMap().put("editar", accionEditar);
        tablaIngresos.getActionMap().put("editar", accionEditar);

        // [SUPR] Eliminar Registro
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarRegistroSeleccionado();
            }
        });
    }
}
