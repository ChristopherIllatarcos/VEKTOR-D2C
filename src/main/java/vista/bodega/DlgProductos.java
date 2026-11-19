package vista.bodega;

import dao.ProductoVentaDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class DlgProductos extends JInternalFrame {

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelNorte, panelBusqueda, panelSur;
    private JScrollPane scrollPane;
    private JLabel lblTitulo, lblBuscar, lblAcciones;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTablas, cAcento, cTextoClaro, cBarraAcciones, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Clásico por defecto

    public DlgProductos() {
        super("1.- Fichero Productos (Principal)", false, false, false, false);
        putClientProperty("JInternalFrame.isPalette", Boolean.FALSE);
        setSize(1150, 650);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsCustom();
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
                cPanelTablas = new Color(15, 10, 20);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cBarraAcciones = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTablas = new Color(5, 20, 25);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraAcciones = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelTablas = new Color(5, 25, 18);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cBarraAcciones = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelTablas = new Color(25, 12, 10);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cBarraAcciones = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Clipper Clásico":
            default:
                cFondo = new Color(0, 0, 128);
                cPanelTablas = Color.BLACK;
                cAcento = new Color(255, 255, 0);
                cTextoClaro = Color.WHITE;
                cBarraAcciones = new Color(178, 34, 34);
                cSeleccion = new Color(0, 128, 192);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
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

        if (tablaProductos != null) {
            tablaProductos.setBackground(cPanelTablas);
            tablaProductos.setForeground(cTextoClaro);
            tablaProductos.setSelectionBackground(cSeleccion);
            tablaProductos.setSelectionForeground(Color.WHITE);
            tablaProductos.getTableHeader().setBackground(cFondo);
            tablaProductos.getTableHeader().setForeground(cAcento);
            tablaProductos.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cPanelTablas);
            scrollPane.setBorder(BorderFactory.createLineBorder(cAcento));
        }

        if (panelSur != null) {
            panelSur.setBackground(cBarraAcciones);
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAcciones != null) {
            lblAcciones.setText(" (INS) Insertar | (M) Modificar | (SUPR) Eliminar | (F6) Tema: " + temaActual + " | (ESC) Salir ");
        }
    }

    private void initComponentsCustom() {
        panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. TÍTULO SUPERIOR Y BUSCADOR
        lblTitulo = new JLabel("=== FICHERO PRODUCTOS (PRINCIPAL) ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 18));
        lblTitulo.setForeground(cAcento);

        panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.setBackground(cFondo);
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelBusqueda.setBackground(cFondo);

        lblBuscar = new JLabel("BUSCAR (Descripción o Barra): ");
        lblBuscar.setFont(new Font("Consolas", Font.BOLD, 16));
        lblBuscar.setForeground(cAcento);

        txtBuscar = new JTextField(35);
        txtBuscar.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtBuscar.setBackground(cPanelTablas);
        txtBuscar.setForeground(cAcento);
        txtBuscar.setCaretColor(cAcento);
        txtBuscar.setBorder(BorderFactory.createLineBorder(cAcento));

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarProductos();
            }
        });

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
        panelNorte.add(panelBusqueda, BorderLayout.SOUTH);

        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // 2. TABLA DE PRODUCTOS
        String[] columnas = {"DESCRIPCIÓN", "#BARRA", "PRECIO"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(new Font("Consolas", Font.PLAIN, 14));
        tablaProductos.setBackground(cPanelTablas);
        tablaProductos.setForeground(cTextoClaro);
        tablaProductos.setSelectionBackground(cSeleccion);
        tablaProductos.setSelectionForeground(Color.WHITE);
        tablaProductos.setRowHeight(24);
        tablaProductos.setShowGrid(false);

        tablaProductos.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 16));
        tablaProductos.getTableHeader().setBackground(cFondo);
        tablaProductos.getTableHeader().setForeground(cAcento);

        DefaultTableCellRenderer tcrIzq = new DefaultTableCellRenderer();
        tcrIzq.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer tcrCentro = new DefaultTableCellRenderer();
        tcrCentro.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer tcrDer = new DefaultTableCellRenderer();
        tcrDer.setHorizontalAlignment(JLabel.RIGHT);

        tablaProductos.getColumnModel().getColumn(0).setCellRenderer(tcrIzq);
        tablaProductos.getColumnModel().getColumn(1).setCellRenderer(tcrCentro);
        tablaProductos.getColumnModel().getColumn(2).setCellRenderer(tcrDer);

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(500);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(150);

        sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);

        scrollPane = new JScrollPane(tablaProductos);
        scrollPane.getViewport().setBackground(cPanelTablas);
        scrollPane.setBorder(BorderFactory.createLineBorder(cAcento));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // 3. BARRA DE ACCIONES INFERIOR
        lblAcciones = new JLabel("", JLabel.CENTER);
        lblAcciones.setFont(new Font("Consolas", Font.BOLD, 16));
        lblAcciones.setForeground(cAcento);
        actualizarTextoAtajos();

        panelSur = new JPanel(new BorderLayout());
        panelSur.setBackground(cBarraAcciones);
        panelSur.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
        panelSur.add(lblAcciones, BorderLayout.CENTER);
        panelPrincipal.add(panelSur, BorderLayout.SOUTH);

        add(panelPrincipal);

        setupKeyBindings();
        cargarDatosIniciales();
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
                accionInsertar();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (txtBuscar != null && txtBuscar.isFocusOwner()) {
                    return;
                }
                accionModificar();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionEliminar();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "focalizarBuscar");
        root.getActionMap().put("focalizarBuscar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (txtBuscar != null) {
                    txtBuscar.requestFocus();
                    txtBuscar.selectAll();
                }
            }
        });
    }

    private void filtrarProductos() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 0, 1));
        }
    }

    public void cargarProductosEnTabla(List<Object[]> listaProductos) {
        modeloTabla.setRowCount(0);
        if (listaProductos != null) {
            for (Object[] fila : listaProductos) {
                modeloTabla.addRow(fila);
            }
        }
    }

    private void cargarDatosIniciales() {
        try {
            ProductoVentaDAO dao = new ProductoVentaDAO();
            List<Object[]> lista = dao.obtenerProductosParaTabla();
            cargarProductosEnTabla(lista);
        } catch (Exception ex) {
            System.err.println("Error al cargar productos iniciales: " + ex.getMessage());
        }
    }

    private void accionInsertar() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        DlgInsertarProducto dlgInsertar = new DlgInsertarProducto(parentWindow);
        dlgInsertar.setVisible(true);

        try {
            ProductoVentaDAO dao = new ProductoVentaDAO();
            List<Object[]> lista = dao.obtenerProductosParaTabla();
            cargarProductosEnTabla(lista);
        } catch (Exception ex) {
            System.err.println("Error al refrescar tabla: " + ex.getMessage());
        }
    }

    private void accionModificar() {
        int fila = tablaProductos.getSelectedRow();
        if (fila != -1) {
            int filaModelo = tablaProductos.convertRowIndexToModel(fila);
            String barra = (String) modeloTabla.getValueAt(filaModelo, 1);

            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            DlgModificarProducto dlg = new DlgModificarProducto(parentWindow, barra);
            dlg.setVisible(true);

            try {
                ProductoVentaDAO daoProd = new ProductoVentaDAO();
                cargarProductosEnTabla(daoProd.obtenerProductosParaTabla());
            } catch (Exception ex) {
                System.err.println("Error al recargar tabla tras modificar: " + ex.getMessage());
            }

        } else {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un producto para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void accionEliminar() {
        int fila = tablaProductos.getSelectedRow();
        if (fila != -1) {
            int filaModelo = tablaProductos.convertRowIndexToModel(fila);

            String desc = (String) modeloTabla.getValueAt(filaModelo, 0);
            String barra = (String) modeloTabla.getValueAt(filaModelo, 1);

            JDialog dialogoConfirm = new JDialog(SwingUtilities.getWindowAncestor(this), "CONFIRMAR", Dialog.ModalityType.APPLICATION_MODAL);
            dialogoConfirm.setUndecorated(true);
            dialogoConfirm.setSize(500, 200);
            dialogoConfirm.setLocationRelativeTo(this);

            JPanel panelBox = new JPanel(new BorderLayout());
            panelBox.setBackground(cFondo);
            panelBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(cAcento, 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JLabel lblTituloBox = new JLabel(" ADVERTENCIA DE ELIMINACIÓN ", JLabel.CENTER);
            lblTituloBox.setFont(new Font("Consolas", Font.BOLD, 14));
            lblTituloBox.setForeground(cAcento);
            panelBox.add(lblTituloBox, BorderLayout.NORTH);

            JPanel panelCentro = new JPanel(new GridLayout(2, 1, 5, 5));
            panelCentro.setBackground(cFondo);

            JLabel lblPregunta = new JLabel("¿Desea eliminar el producto seleccionado?", JLabel.CENTER);
            lblPregunta.setFont(new Font("Consolas", Font.BOLD, 13));
            lblPregunta.setForeground(cAcento);
            panelCentro.add(lblPregunta);

            JLabel lblDetalle = new JLabel("<html><center>" + desc + "<br><font color='" + colorToHex(cAcento) + "'>[Barra: " + barra + "]</font></center></html>", JLabel.CENTER);
            lblDetalle.setFont(new Font("Consolas", Font.PLAIN, 13));
            lblDetalle.setForeground(cTextoClaro);
            panelCentro.add(lblDetalle);

            panelBox.add(panelCentro, BorderLayout.CENTER);

            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
            panelBotones.setBackground(cFondo);

            JButton btnSi = new JButton("[ SÍ ]");
            btnSi.setFont(new Font("Consolas", Font.BOLD, 13));
            btnSi.setBackground(cPanelTablas);
            btnSi.setForeground(cAcento);
            btnSi.setBorder(BorderFactory.createLineBorder(cAcento, 1));
            btnSi.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton btnNo = new JButton("[ NO ]");
            btnNo.setFont(new Font("Consolas", Font.BOLD, 13));
            btnNo.setBackground(cPanelTablas);
            btnNo.setForeground(cAcento);
            btnNo.setBorder(BorderFactory.createLineBorder(cAcento, 1));
            btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));

            java.awt.event.FocusAdapter focusClipper = new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    JButton b = (JButton) e.getSource();
                    b.setBackground(cAcento);
                    b.setForeground(Color.BLACK);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    JButton b = (JButton) e.getSource();
                    b.setBackground(cPanelTablas);
                    b.setForeground(cAcento);
                }
            };

            btnSi.addFocusListener(focusClipper);
            btnNo.addFocusListener(focusClipper);

            panelBotones.add(btnSi);
            panelBotones.add(btnNo);
            panelBox.add(panelBotones, BorderLayout.SOUTH);

            dialogoConfirm.setContentPane(panelBox);

            Runnable accionEliminarBD = () -> {
                try {
                    ProductoVentaDAO dao = new ProductoVentaDAO();
                    boolean exito = dao.eliminarProducto(barra);

                    if (exito) {
                        dialogoConfirm.dispose();
                        List<Object[]> lista = dao.obtenerProductosParaTabla();
                        cargarProductosEnTabla(lista);
                    } else {
                        mostrarMensajeClipper("ERROR", "No se pudo eliminar el producto de la base de datos.");
                    }
                } catch (Exception ex) {
                    mostrarMensajeClipper("ERROR", "Error de sistema al eliminar: " + ex.getMessage());
                    ex.printStackTrace();
                }
            };

            btnSi.addActionListener(e -> accionEliminarBD.run());
            btnNo.addActionListener(e -> dialogoConfirm.dispose());

            dialogoConfirm.getRootPane().registerKeyboardAction(e -> dialogoConfirm.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            dialogoConfirm.getRootPane().registerKeyboardAction(e -> accionEliminarBD.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            SwingUtilities.invokeLater(() -> btnNo.requestFocusInWindow());
            dialogoConfirm.setVisible(true);

        } else {
            mostrarMensajeClipper("ATENCIÓN", "Por favor seleccione un producto para eliminar.");
        }
    }

    private void mostrarMensajeClipper(String titulo, String mensaje) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(480, 160);
        dlg.setLocationRelativeTo(this);

        JPanel panelBox = new JPanel(new BorderLayout());
        panelBox.setBackground(cFondo);
        panelBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTituloBox = new JLabel(" " + titulo + " ", JLabel.CENTER);
        lblTituloBox.setFont(new Font("Consolas", Font.BOLD, 14));
        lblTituloBox.setForeground(cAcento);
        panelBox.add(lblTituloBox, BorderLayout.NORTH);

        JLabel lblMensaje = new JLabel("<html><center>" + mensaje + "</center></html>", JLabel.CENTER);
        lblMensaje.setFont(new Font("Consolas", Font.BOLD, 13));
        lblMensaje.setForeground(cTextoClaro);
        panelBox.add(lblMensaje, BorderLayout.CENTER);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setBackground(cFondo);

        JButton btnAceptar = new JButton("[ ACEPTAR ]");
        btnAceptar.setFont(new Font("Consolas", Font.BOLD, 13));
        btnAceptar.setBackground(cPanelTablas);
        btnAceptar.setForeground(cAcento);
        btnAceptar.setFocusPainted(true);
        btnAceptar.setBorder(BorderFactory.createLineBorder(cAcento, 1));
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAceptar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                btnAceptar.setBackground(cAcento);
                btnAceptar.setForeground(Color.BLACK);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                btnAceptar.setBackground(cPanelTablas);
                btnAceptar.setForeground(cAcento);
            }
        });

        btnAceptar.addActionListener(e -> dlg.dispose());
        panelBoton.add(btnAceptar);
        panelBox.add(panelBoton, BorderLayout.SOUTH);

        dlg.setContentPane(panelBox);

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> btnAceptar.requestFocusInWindow());
        dlg.setVisible(true);
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
