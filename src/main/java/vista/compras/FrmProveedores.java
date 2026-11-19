package vista.compras;

import dao.ProveedorDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FrmProveedores extends JInternalFrame {

    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelNorte, panelBusqueda, panelSurTotal, panelAcciones, panelEstado;
    private JScrollPane scrollPane;
    private JLabel lblTitulo, lblBuscar, lblAcciones, lblEmpresa, lblFecha;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTablas, cZebra1, cZebra2, cAcento, cTextoClaro, cBarraAcciones, cBarraEstado, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Clásico por defecto

    public FrmProveedores() {
        super("1.- Maestro de Proveedores", false, false, false, false);
        setSize(1150, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsCustom();
        cargarDatosDesdeBD();

        // Poner el foco directo en el buscador al abrir la ventana interna
        SwingUtilities.invokeLater(() -> txtBuscar.requestFocusInWindow());
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
                cPanelTablas = new Color(15, 10, 20);
                cZebra1 = new Color(15, 10, 20);
                cZebra2 = new Color(35, 15, 45);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cBarraAcciones = new Color(130, 0, 70);
                cBarraEstado = new Color(30, 10, 40);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTablas = new Color(5, 20, 25);
                cZebra1 = new Color(5, 20, 25);
                cZebra2 = new Color(10, 35, 45);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraAcciones = new Color(0, 60, 70);
                cBarraEstado = new Color(10, 35, 45);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelTablas = new Color(5, 25, 18);
                cZebra1 = new Color(5, 25, 18);
                cZebra2 = new Color(12, 45, 30);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cBarraAcciones = new Color(0, 60, 35);
                cBarraEstado = new Color(10, 40, 25);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelTablas = new Color(25, 12, 10);
                cZebra1 = new Color(25, 12, 10);
                cZebra2 = new Color(50, 22, 15);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cBarraAcciones = new Color(60, 20, 12);
                cBarraEstado = new Color(35, 15, 10);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Clipper Clásico":
            default:
                cFondo = new Color(0, 0, 128);
                cPanelTablas = Color.BLACK;
                cZebra1 = Color.BLACK;
                cZebra2 = new Color(12, 12, 45);
                cAcento = new Color(255, 255, 0);
                cTextoClaro = Color.WHITE;
                cBarraAcciones = new Color(178, 34, 34);
                cBarraEstado = new Color(20, 20, 80);
                cSeleccion = new Color(0, 128, 192);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
            panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));
        }

        if (panelNorte != null) {
            panelNorte.setBackground(cFondo);
        }
        if (panelBusqueda != null) {
            panelBusqueda.setBackground(cFondo);
        }
        if (lblTitulo != null) {
            lblTitulo.setForeground(cAcento);
        }
        if (lblBuscar != null) {
            lblBuscar.setForeground(cAcento);
        }

        if (txtBuscar != null) {
            txtBuscar.setBackground(cPanelTablas);
            txtBuscar.setForeground(cAcento);
            txtBuscar.setCaretColor(cAcento);
            txtBuscar.setBorder(BorderFactory.createLineBorder(cAcento));
        }

        if (tablaProveedores != null) {
            tablaProveedores.setBackground(cPanelTablas);
            tablaProveedores.setForeground(cTextoClaro);
            tablaProveedores.setSelectionBackground(cSeleccion);
            tablaProveedores.setSelectionForeground(Color.WHITE);
            tablaProveedores.setGridColor(cFondo);
            tablaProveedores.getTableHeader().setBackground(cFondo);
            tablaProveedores.getTableHeader().setForeground(cAcento);
            tablaProveedores.getTableHeader().setBorder(BorderFactory.createLineBorder(cAcento));
            tablaProveedores.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cPanelTablas);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 15, 0, 15),
                    BorderFactory.createLineBorder(cAcento)
            ));
        }

        if (panelSurTotal != null) {
            panelSurTotal.setBackground(cFondo);
        }
        if (panelAcciones != null) {
            panelAcciones.setBackground(cBarraAcciones);
        }
        actualizarTextoAtajos();

        if (panelEstado != null) {
            panelEstado.setBackground(cBarraEstado);
        }
        if (lblEmpresa != null) {
            lblEmpresa.setForeground(cTextoClaro);
        }
        if (lblFecha != null) {
            lblFecha.setForeground(cAcento);
        }

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAcciones != null) {
            lblAcciones.setText("  [Ins]ertar   [Supr]imir   [M]odificar   [B]uscar   [F6] Tema: " + temaActual + "   [Esc] Salir  ");
        }
    }

    private void initComponentsCustom() {
        panelPrincipal = new JPanel(new BorderLayout(0, 0));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createLineBorder(cAcento, 2));

        // 1. ENCABEZADO Y TÍTULO DE LA SECCIÓN
        panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(cFondo);
        panelNorte.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        lblTitulo = new JLabel("=== MAESTRO DE PROVEEDORES ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 18));
        lblTitulo.setForeground(cAcento);
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(cFondo);

        lblBuscar = new JLabel("BUSCAR (R.U.T. o Nombre): ");
        lblBuscar.setFont(FUENTE_CONSOLA);
        lblBuscar.setForeground(cAcento);

        txtBuscar = new JTextField(30);
        txtBuscar.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtBuscar.setBackground(cPanelTablas);
        txtBuscar.setForeground(cAcento);
        txtBuscar.setCaretColor(cAcento);
        txtBuscar.setBorder(BorderFactory.createLineBorder(cAcento));

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarProveedores();
            }
        });

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
        panelNorte.add(panelBusqueda, BorderLayout.SOUTH);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // 2. TABLA CENTRAL CON FRANJAS CEBRA DINÁMICAS
        String[] columnas = {"R.U.T.", "NOMBRE DEL PROVEEDOR", "CIUDAD"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProveedores = new JTable(modeloTabla);
        tablaProveedores.setFont(new Font("Consolas", Font.PLAIN, 13));
        tablaProveedores.setBackground(cPanelTablas);
        tablaProveedores.setForeground(cTextoClaro);
        tablaProveedores.setSelectionBackground(cSeleccion);
        tablaProveedores.setSelectionForeground(Color.WHITE);
        tablaProveedores.setRowHeight(25);
        tablaProveedores.setShowGrid(true);
        tablaProveedores.setGridColor(cFondo);

        tablaProveedores.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaProveedores.getTableHeader().setBackground(cFondo);
        tablaProveedores.getTableHeader().setForeground(cAcento);
        tablaProveedores.getTableHeader().setBorder(BorderFactory.createLineBorder(cAcento));

        DefaultTableCellRenderer renderizadorCebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(cSeleccion);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? cZebra1 : cZebra2);
                    c.setForeground(cTextoClaro);
                }

                setHorizontalAlignment(column == 0 ? JLabel.CENTER : JLabel.LEFT);
                return c;
            }
        };

        for (int i = 0; i < tablaProveedores.getColumnCount(); i++) {
            tablaProveedores.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaProveedores.getColumnModel().getColumn(0).setPreferredWidth(180);
        tablaProveedores.getColumnModel().getColumn(1).setPreferredWidth(600);
        tablaProveedores.getColumnModel().getColumn(2).setPreferredWidth(250);

        sorter = new TableRowSorter<>(modeloTabla);
        tablaProveedores.setRowSorter(sorter);

        scrollPane = new JScrollPane(tablaProveedores);
        scrollPane.getViewport().setBackground(cPanelTablas);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 15),
                BorderFactory.createLineBorder(cAcento)
        ));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // 3. ZONA INFERIOR: ATAJOS Y BARRA DE ESTADO INSTITUCIONAL
        panelSurTotal = new JPanel(new BorderLayout());
        panelSurTotal.setBackground(cFondo);

        lblAcciones = new JLabel("", JLabel.CENTER);
        lblAcciones.setFont(FUENTE_CONSOLA);
        lblAcciones.setForeground(cAcento);
        actualizarTextoAtajos();

        panelAcciones = new JPanel(new BorderLayout());
        panelAcciones.setBackground(cBarraAcciones);
        panelAcciones.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panelAcciones.add(lblAcciones, BorderLayout.CENTER);
        panelSurTotal.add(panelAcciones, BorderLayout.NORTH);

        panelEstado = new JPanel(new BorderLayout());
        panelEstado.setBackground(cBarraEstado);
        panelEstado.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        lblEmpresa = new JLabel("CASA VICTORIA");
        lblEmpresa.setFont(new Font("Consolas", Font.BOLD, 12));
        lblEmpresa.setForeground(cTextoClaro);

        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        lblFecha = new JLabel(fechaActual + "  ");
        lblFecha.setFont(new Font("Consolas", Font.BOLD, 12));
        lblFecha.setForeground(cAcento);

        panelEstado.add(lblEmpresa, BorderLayout.CENTER);
        panelEstado.add(lblFecha, BorderLayout.EAST);

        panelSurTotal.add(panelEstado, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurTotal, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);

        setupKeyBindings();
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        root.getActionMap().put("salir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F6] Ciclar Tema
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclar_tema");
        root.getActionMap().put("ciclar_tema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insertar");
        root.getActionMap().put("insertar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Window ventanaPadre = SwingUtilities.getWindowAncestor(FrmProveedores.this);
                DlgInsertarProveedor dlg = new DlgInsertarProveedor(ventanaPadre);
                dlg.setVisible(true);
                cargarDatosDesdeBD();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int fila = tablaProveedores.getSelectedRow();
                if (fila != -1) {
                    int filaModelo = tablaProveedores.convertRowIndexToModel(fila);

                    String rutCompletoStr = (String) modeloTabla.getValueAt(filaModelo, 0);
                    String nombreProveedor = (String) modeloTabla.getValueAt(filaModelo, 1);

                    JDialog dialogoConfirm = new JDialog(SwingUtilities.getWindowAncestor(FrmProveedores.this), "CONFIRMAR ELIMINACIÓN", Dialog.ModalityType.APPLICATION_MODAL);
                    dialogoConfirm.setSize(480, 220);
                    dialogoConfirm.setLocationRelativeTo(FrmProveedores.this);
                    dialogoConfirm.getContentPane().setBackground(cFondo);
                    dialogoConfirm.setLayout(new BorderLayout());

                    JPanel panelCentro = new JPanel(new GridBagLayout());
                    panelCentro.setBackground(cFondo);
                    panelCentro.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(cAcento, 2),
                            " ADVERTENCIA DE ELIMINACIÓN ",
                            0, 0, new Font("Consolas", Font.BOLD, 13), cAcento
                    ));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(10, 15, 5, 15);
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.anchor = GridBagConstraints.CENTER;

                    JLabel lblPregunta = new JLabel("¿Desea eliminar al proveedor seleccionado?");
                    lblPregunta.setFont(new Font("Consolas", Font.BOLD, 13));
                    lblPregunta.setForeground(cAcento);
                    panelCentro.add(lblPregunta, gbc);

                    gbc.gridy = 1;
                    gbc.insets = new Insets(5, 15, 10, 15);
                    JLabel lblDetalle = new JLabel("<html><center>" + nombreProveedor + "<br><font color='" + colorToHex(cAcento) + "'>[RUT: " + rutCompletoStr + "]</font></center></html>");
                    lblDetalle.setFont(new Font("Consolas", Font.PLAIN, 13));
                    lblDetalle.setForeground(cTextoClaro);
                    panelCentro.add(lblDetalle, gbc);

                    dialogoConfirm.add(panelCentro, BorderLayout.CENTER);

                    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
                    panelBotones.setBackground(cBarraAcciones);
                    panelBotones.setBorder(BorderFactory.createLineBorder(cAcento, 1));

                    JButton btnSi = new JButton("Sí [Enter]");
                    btnSi.setFont(new Font("Consolas", Font.BOLD, 13));
                    JButton btnNo = new JButton("No [Esc]");
                    btnNo.setFont(new Font("Consolas", Font.BOLD, 13));

                    panelBotones.add(btnSi);
                    panelBotones.add(btnNo);
                    dialogoConfirm.add(panelBotones, BorderLayout.SOUTH);

                    Runnable accionEliminar = () -> {
                        try {
                            int rutBuscado = Integer.parseInt(rutCompletoStr.split("-")[0]);
                            ProveedorDAO dao = new ProveedorDAO();
                            if (dao.eliminarProveedor(rutBuscado)) {
                                dialogoConfirm.dispose();
                                cargarDatosDesdeBD();
                            } else {
                                JOptionPane.showMessageDialog(dialogoConfirm, "No se pudo eliminar el proveedor de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialogoConfirm, "Error de sistema al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    };

                    btnSi.addActionListener(e1 -> accionEliminar.run());
                    btnNo.addActionListener(e1 -> dialogoConfirm.dispose());

                    dialogoConfirm.getRootPane().registerKeyboardAction(e1 -> dialogoConfirm.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
                    dialogoConfirm.getRootPane().registerKeyboardAction(e1 -> accionEliminar.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

                    dialogoConfirm.setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(FrmProveedores.this, "Por favor seleccione un proveedor para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (txtBuscar.isFocusOwner()) {
                    return;
                }

                int fila = tablaProveedores.getSelectedRow();
                if (fila != -1) {
                    int filaModelo = tablaProveedores.convertRowIndexToModel(fila);
                    String rutCompletoStr = (String) modeloTabla.getValueAt(filaModelo, 0);

                    try {
                        int rutBuscado = Integer.parseInt(rutCompletoStr.split("-")[0]);

                        Window ventanaPadre = SwingUtilities.getWindowAncestor(FrmProveedores.this);
                        DlgModificarProveedor dlg = new DlgModificarProveedor(ventanaPadre, rutBuscado);
                        dlg.setVisible(true);

                        cargarDatosDesdeBD();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(FrmProveedores.this, "Error al procesar el RUT del proveedor seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(FrmProveedores.this, "Por favor seleccione un proveedor para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "focalizarBuscar");
        root.getActionMap().put("focalizarBuscar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtBuscar.requestFocus();
                txtBuscar.selectAll();
            }
        });

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (tablaProveedores.getRowCount() > 0) {
                        tablaProveedores.requestFocus();
                        tablaProveedores.setRowSelectionInterval(0, 0);
                    }
                }
            }
        });
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void filtrarProveedores() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 0, 1));
        }
    }

    private void cargarDatosDesdeBD() {
        modeloTabla.setRowCount(0);
        try {
            ProveedorDAO dao = new ProveedorDAO();
            java.util.List<Object[]> lista = dao.obtenerProveedoresParaTabla();
            for (Object[] fila : lista) {
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar proveedores en la vista: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
