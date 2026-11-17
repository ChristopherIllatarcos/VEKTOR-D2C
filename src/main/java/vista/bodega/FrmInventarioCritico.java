package vista.bodega;

import conexion.Conexion;
import dao.InventarioCriticoDAO;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class FrmInventarioCritico extends JInternalFrame {

    private JTextField txtBuscarProducto;
    private JTable tablaCriticos;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalProductos;

    // Paleta Borgoña Oscuro / Rojo Alerta
    private final Color COLOR_FONDO = new Color(58, 11, 11);          // Borgoña oscuro (#3A0B0B)
    private final Color COLOR_PANEL_TABLA = new Color(28, 5, 5);      // Fondo tabla oscuro
    private final Color COLOR_ZEBRA_1 = new Color(28, 5, 5);          // Fila cebra par
    private final Color COLOR_ZEBRA_2 = new Color(38, 8, 8);          // Fila cebra impar
    private final Color COLOR_BORDE_ALERTA = new Color(255, 102, 102);  // Rojo coral brillante (#FF6666)
    private final Color COLOR_TEXTO_CLARO = new Color(255, 230, 230);   // Rosado / Crema claro
    private final Color COLOR_BARRA_INF = new Color(100, 20, 20);     // Rojo borgoña medio
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    public FrmInventarioCritico() {
        super("6.- INVENTARIO CRÍTICO", false, false, false, false);
        initComponentsManual();
        ejecutarBusqueda();
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
                new LineBorder(COLOR_BORDE_ALERTA, 2),
                " INVENTARIO CRÍTICO - FILTRO DE BÚSQUEDA ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_ALERTA
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        txtBuscarProducto = crearCampoTexto(30);
        panelCampos.add(crearEtiqueta("Código Barra / Producto:"));
        panelCampos.add(txtBuscarProducto);

        txtBuscarProducto.addActionListener(e -> ejecutarBusqueda());

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla con Filas Cebra)
        // ==========================================
        String[] columnas = {"# Barra", "Fam.", "Descripción del Producto", "Costo ($)", "Stock Actual", "Mínimo Crítico"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCriticos = new JTable(modeloTabla);
        tablaCriticos.setFont(FUENTE_CONSOLA);
        tablaCriticos.setBackground(COLOR_PANEL_TABLA);
        tablaCriticos.setForeground(Color.WHITE);
        tablaCriticos.setGridColor(new Color(80, 25, 25));
        tablaCriticos.setRowHeight(28);
        tablaCriticos.setSelectionBackground(COLOR_BARRA_INF);
        tablaCriticos.setSelectionForeground(Color.YELLOW);

        tablaCriticos.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaCriticos.getTableHeader().setBackground(COLOR_FONDO);
        tablaCriticos.getTableHeader().setForeground(COLOR_BORDE_ALERTA);

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
                    // Resaltar en rojo claro si el stock es crítico
                    if (column == 4) {
                        c.setForeground(new Color(255, 150, 150));
                    } else {
                        c.setForeground(Color.WHITE);
                    }
                }

                if (column >= 3) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tablaCriticos.getColumnCount(); i++) {
            tablaCriticos.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaCriticos.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaCriticos.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaCriticos.getColumnModel().getColumn(2).setPreferredWidth(380);
        tablaCriticos.getColumnModel().getColumn(3).setPreferredWidth(110);
        tablaCriticos.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablaCriticos.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollTabla = new JScrollPane(tablaCriticos);
        scrollTabla.getViewport().setBackground(COLOR_PANEL_TABLA);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE_ALERTA, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        JPanel panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(COLOR_FONDO);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(COLOR_FONDO);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDE_ALERTA));

        lblTotalProductos = crearEtiqueta("PRODUCTOS EN ESTADO CRÍTICO: 0");
        lblTotalProductos.setForeground(COLOR_BORDE_ALERTA);
        panelTotales.add(lblTotalProductos);

        JPanel panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajos.setBackground(COLOR_BARRA_INF);
        panelAtajos.setBorder(new LineBorder(COLOR_BORDE_ALERTA, 1));

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
        txt.setForeground(COLOR_BORDE_ALERTA);
        txt.setCaretColor(COLOR_BORDE_ALERTA);
        txt.setBorder(new LineBorder(COLOR_BORDE_ALERTA, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(65, 15, 15));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(COLOR_PANEL_TABLA);
                txt.setBorder(new LineBorder(COLOR_BORDE_ALERTA, 1));
            }
        });

        return txt;
    }

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        InventarioCriticoDAO dao = new InventarioCriticoDAO();
        List<Object[]> resultados = dao.buscarInventarioCritico(txtBuscarProducto.getText());

        for (Object[] fila : resultados) {
            double costo = (double) fila[3];
            double stock = (double) fila[4];
            double critico = (double) fila[5];

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                String.format("$%,.0f", costo),
                String.format("%,.0f", stock),
                String.format("%,.0f", critico)
            });
        }

        lblTotalProductos.setText(String.format("PRODUCTOS EN ESTADO CRÍTICO: %d", resultados.size()));
    }

    private void abrirFormularioMantenimiento(boolean esEdicion) {
        String barraSel = "";
        String familiaSel = "";
        String nombreSel = "";
        String costoSel = "0";
        String stockSel = "0";
        String criticoSel = "5";

        if (esEdicion) {
            int fila = tablaCriticos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            barraSel = (String) modeloTabla.getValueAt(fila, 0);
            familiaSel = (String) modeloTabla.getValueAt(fila, 1);
            nombreSel = (String) modeloTabla.getValueAt(fila, 2);
            costoSel = modeloTabla.getValueAt(fila, 3).toString().replace("$", "").replace(".", "").replace(",", "").trim();
            stockSel = modeloTabla.getValueAt(fila, 4).toString().replace(".", "").replace(",", "").trim();
            criticoSel = modeloTabla.getValueAt(fila, 5).toString().replace(".", "").replace(",", "").trim();
        }

        final String barraOriginal = barraSel;

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), esEdicion ? "EDITAR ITEM CRÍTICO" : "NUEVO ITEM CRÍTICO", true);
        dialogo.setSize(500, 400);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(COLOR_FONDO);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_ALERTA, 2),
                esEdicion ? " MODIFICAR PARAMETROS CRÍTICOS " : " REGISTRAR STOCK CRÍTICO ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_ALERTA
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtB = crearCampoTexto(14);
        txtB.setText(barraSel);
        JTextField txtFam = crearCampoTexto(10);
        txtFam.setText(familiaSel);
        JTextField txtN = crearCampoTexto(24);
        txtN.setText(nombreSel);
        JTextField txtCost = crearCampoTexto(14);
        txtCost.setText(costoSel);
        JTextField txtS = crearCampoTexto(14);
        txtS.setText(stockSel);
        JTextField txtCrit = crearCampoTexto(14);
        txtCrit.setText(criticoSel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentro.add(crearEtiqueta("Código de Barra:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentro.add(crearEtiqueta("Familia:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtFam, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentro.add(crearEtiqueta("Nombre Producto:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtN, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(crearEtiqueta("Costo Unitario ($):"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCost, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCentro.add(crearEtiqueta("Stock Actual:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtS, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCentro.add(crearEtiqueta("Mínimo Crítico:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCrit, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(COLOR_BARRA_INF);
        panelBotones.setBorder(new LineBorder(COLOR_BORDE_ALERTA, 1));

        JButton btnGuardar = new JButton("Guardar [F3]");
        btnGuardar.setFont(FUENTE_CONSOLA);
        JButton btnCancelar = new JButton("Cancelar [Esc]");
        btnCancelar.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable guardarAccion = () -> {
            try {
                String b = txtB.getText().trim();
                String fam = txtFam.getText().trim();
                String n = txtN.getText().trim();

                
                String cost = txtCost.getText().trim();
                String stock = txtS.getText().trim();
                String crit = txtCrit.getText().trim();

                InventarioCriticoDAO dao = new InventarioCriticoDAO();
                boolean exito;
                if (esEdicion) {
                    exito = dao.actualizarItemCritico(barraOriginal, b, fam, n, cost, stock, crit);
                } else {
                    exito = dao.insertarItemCritico(b, fam, n, cost, stock, crit);
                }

                if (exito) {
                    dialogo.dispose();
                    ejecutarBusqueda();
                } else {
                    JOptionPane.showMessageDialog(dialogo, "Error al guardar el registro en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Verifique que los valores ingresados sean válidos.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnGuardar.addActionListener(e -> guardarAccion.run());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> guardarAccion.run(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void eliminarRegistroSeleccionado() {
        int fila = tablaCriticos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String barra = (String) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 2);

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), "CONFIRMAR ELIMINACIÓN", true);
        dialogo.setSize(480, 220);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(COLOR_FONDO);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_ALERTA, 2),
                " ADVERTENCIA DE ELIMINACIÓN ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_ALERTA
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblPregunta = new JLabel("¿Desea eliminar el producto crítico?");
        lblPregunta.setFont(FUENTE_CONSOLA);
        lblPregunta.setForeground(Color.YELLOW);
        panelCentro.add(lblPregunta, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 15, 10, 15);
        JLabel lblDetalle = new JLabel("<html><center>" + nombre + "<br><font color='#FF6666'>[Barra: " + barra + "]</font></center></html>");
        lblDetalle.setFont(new Font("Consolas", Font.BOLD, 13));
        lblDetalle.setForeground(COLOR_TEXTO_CLARO);
        panelCentro.add(lblDetalle, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(COLOR_BARRA_INF);
        panelBotones.setBorder(new LineBorder(COLOR_BORDE_ALERTA, 1));

        JButton btnSi = new JButton("Sí [Enter]");
        btnSi.setFont(FUENTE_CONSOLA);
        JButton btnNo = new JButton("No [Esc]");
        btnNo.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable accionEliminar = () -> {
            InventarioCriticoDAO dao = new InventarioCriticoDAO();
            if (dao.eliminarItemCritico(barra)) {
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
                txtBuscarProducto.setText("");
                ejecutarBusqueda();
                txtBuscarProducto.requestFocusInWindow();
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
        tablaCriticos.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");

        AbstractAction accionEditar = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(true);
            }
        };
        root.getActionMap().put("editar", accionEditar);
        tablaCriticos.getActionMap().put("editar", accionEditar);

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarRegistroSeleccionado();
            }
        });
    }
}
