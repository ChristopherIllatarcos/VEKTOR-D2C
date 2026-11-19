package vista.ventas;

import conexion.Conexion;
import dao.VentaDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.List;
import modelo.VentaDTO;
import utilidades.PDFUtils;

public class FrmMantencionVentas extends JInternalFrame {

    private JTextField txtFiltro;
    private JTable tablaVentas;
    private DefaultTableModel modeloTabla;
    private JButton btnBuscar;
    private JLabel lblAtajos;
    private JLabel lblResumenTotales;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelFiltro, panelInferior, panelResumen, panelSurGeneral;
    private JScrollPane scroll;

    // Dispatcher global para [F2] (se limpia en dispose() para evitar fugas)
    private KeyEventDispatcher dispatcherF2;

    // Variables de Colores Actuales del Tema
    private Color cFondoNegro, cPanelNorte, cEncabezadoTabla, cAcento, cTextoClaro, cSeleccion, cBarraSur;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 12);

    // Sistema de Temas Ciclos [F5]
    private final String[] listaTemas = {"Clipper Clásico", "Naranja Fuego", "Morado Neón", "Esmeralda Terminal", "Turquesa Pro"};
    private int indiceTemaActual = 0;

    public FrmMantencionVentas() {
        super(" 4.- MANTENCIÓN DE VENTAS ", false, true, false, true);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        setSize(950, 550);
        setLocation(35, 20);
        getContentPane().setBackground(cFondoNegro);
        setLayout(new BorderLayout());

        // 1. PANEL DE BÚSQUEDA / FILTRO
        panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltro.setBackground(cPanelNorte);
        panelFiltro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cAcento, 2), " BUSCAR EN REGISTRO DE VENTAS ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));

        JLabel lblBuscar = new JLabel("Cliente / Boleta / Factura:");
        lblBuscar.setForeground(cAcento);
        lblBuscar.setFont(FUENTE_CONSOLA);
        panelFiltro.add(lblBuscar);

        txtFiltro = new JTextField(20);
        estilizarCampo(txtFiltro);
        txtFiltro.addActionListener(e -> cargarDatos());
        panelFiltro.add(txtFiltro);

        btnBuscar = new JButton("[ENTER] Buscar");
        estilizarBoton(btnBuscar);
        btnBuscar.addActionListener(e -> cargarDatos());
        panelFiltro.add(btnBuscar);

        add(panelFiltro, BorderLayout.NORTH);

        // 2. TABLA CON FRANJAS HORIZONTALES SUTILES
        String[] columnas = {"Código", "Cliente", "Fecha", "Boleta", "Factura", "Guía", "Total ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setBackground(cFondoNegro);
        tablaVentas.setForeground(cTextoClaro);
        tablaVentas.setFont(FUENTE_CONSOLA);
        tablaVentas.setSelectionBackground(cSeleccion);
        tablaVentas.setSelectionForeground(Color.WHITE);
        tablaVentas.getTableHeader().setBackground(cEncabezadoTabla);
        tablaVentas.getTableHeader().setForeground(cAcento);
        tablaVentas.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaVentas.setRowHeight(22);
        tablaVentas.setShowGrid(false);
        tablaVentas.setIntercellSpacing(new Dimension(0, 0));

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

                switch (column) {
                    case 6, 0 -> setHorizontalAlignment(JLabel.RIGHT);
                    case 2, 3, 4, 5 -> setHorizontalAlignment(JLabel.CENTER);
                    default -> setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tablaVentas.getColumnCount(); i++) {
            tablaVentas.getColumnModel().getColumn(i).setCellRenderer(rendererRayas);
        }

        TableColumnModel colModel = tablaVentas.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(75);
        colModel.getColumn(1).setPreferredWidth(260);
        colModel.getColumn(2).setPreferredWidth(105);
        colModel.getColumn(3).setPreferredWidth(95);
        colModel.getColumn(4).setPreferredWidth(95);
        colModel.getColumn(5).setPreferredWidth(95);
        colModel.getColumn(6).setPreferredWidth(130);

        scroll = new JScrollPane(tablaVentas);
        scroll.getViewport().setBackground(cFondoNegro);
        scroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cAcento, 2), " REGISTROS DE VENTAS ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));
        add(scroll, BorderLayout.CENTER);

        // 3. ZONA INFERIOR: RESUMEN DE TOTALES + BARRA DE ATAJOS
        panelSurGeneral = new JPanel(new BorderLayout());
        panelSurGeneral.setBackground(cFondoNegro);

        panelResumen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        panelResumen.setBackground(cFondoNegro);

        lblResumenTotales = new JLabel("Registros: 0  |  Total Listado: $ 0");
        lblResumenTotales.setFont(FUENTE_CONSOLA);
        lblResumenTotales.setForeground(cAcento);
        panelResumen.add(lblResumenTotales);

        panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelInferior.setBackground(cBarraSur);
        panelInferior.setBorder(new LineBorder(cAcento, 1));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        actualizarTextoAtajos();
        panelInferior.add(lblAtajos);

        panelSurGeneral.add(panelResumen, BorderLayout.NORTH);
        panelSurGeneral.add(panelInferior, BorderLayout.SOUTH);
        add(panelSurGeneral, BorderLayout.SOUTH);

        // Asignar el icono del cubo azul a la ventana interna
        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }

        configurarAtajos();
        cargarDatos();

        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
            txtFiltro.requestFocusInWindow();
        });
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F5]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondoNegro = new Color(10, 10, 15);
                cPanelNorte = new Color(45, 10, 55);
                cEncabezadoTabla = new Color(90, 10, 120);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cSeleccion = new Color(140, 20, 160);
                cBarraSur = new Color(130, 0, 70);
                break;
            case "Esmeralda Terminal":
                cFondoNegro = new Color(5, 20, 15);
                cPanelNorte = new Color(10, 45, 30);
                cEncabezadoTabla = new Color(15, 80, 50);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cSeleccion = new Color(0, 140, 80);
                cBarraSur = new Color(0, 60, 35);
                break;
            case "Clipper Clásico":
                cFondoNegro = Color.BLACK;
                cPanelNorte = new Color(0, 0, 168);
                cEncabezadoTabla = new Color(0, 0, 168);
                cAcento = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cSeleccion = new Color(0, 168, 168);
                cBarraSur = new Color(168, 0, 0);
                break;
            case "Naranja Fuego":
                cFondoNegro = new Color(15, 10, 8);
                cPanelNorte = new Color(40, 18, 12);
                cEncabezadoTabla = new Color(80, 28, 18);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cSeleccion = new Color(130, 42, 22);
                cBarraSur = new Color(60, 20, 12);
                break;
            case "Turquesa Pro":
            default:
                cFondoNegro = new Color(5, 15, 20);
                cPanelNorte = new Color(10, 40, 50);
                cEncabezadoTabla = new Color(0, 80, 95);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cSeleccion = new Color(0, 120, 140);
                cBarraSur = new Color(0, 60, 70);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        String nuevoTema = listaTemas[indiceTemaActual];
        aplicarTema(nuevoTema);

        getContentPane().setBackground(cFondoNegro);

        if (panelFiltro != null) {
            panelFiltro.setBackground(cPanelNorte);
            panelFiltro.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cAcento, 2), " BUSCAR EN REGISTRO DE VENTAS ",
                    0, 0, FUENTE_CONSOLA, cAcento
            ));
            for (Component comp : panelFiltro.getComponents()) {
                if (comp instanceof JLabel) {
                    comp.setForeground(cAcento);
                }
            }
        }

        estilizarCampo(txtFiltro);
        estilizarBoton(btnBuscar);

        if (tablaVentas != null) {
            tablaVentas.setBackground(cFondoNegro);
            tablaVentas.setForeground(cTextoClaro);
            tablaVentas.setSelectionBackground(cSeleccion);
            tablaVentas.getTableHeader().setBackground(cEncabezadoTabla);
            tablaVentas.getTableHeader().setForeground(cAcento);
            tablaVentas.repaint();
        }

        if (scroll != null) {
            scroll.getViewport().setBackground(cFondoNegro);
            scroll.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cAcento, 2), " REGISTROS DE VENTAS ",
                    0, 0, FUENTE_CONSOLA, cAcento
            ));
        }

        if (panelSurGeneral != null) {
            panelSurGeneral.setBackground(cFondoNegro);
        }
        if (panelResumen != null) {
            panelResumen.setBackground(cFondoNegro);
        }
        if (lblResumenTotales != null) {
            lblResumenTotales.setForeground(cAcento);
        }
        if (panelInferior != null) {
            panelInferior.setBackground(cBarraSur);
            panelInferior.setBorder(new LineBorder(cAcento, 1));
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:#FFFFFF;'>Salir</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F2)</b> <span style='color:#FFFFFF;'>Modificar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F4)</b> <span style='color:#FFFFFF;'>PDF</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(Supr)</b> <span style='color:#FFFFFF;'>Eliminar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                    + "<b style='color:#FFFF55;'>(F5): Tema: " + temaActual + "</b></html>");
        }
    }

    private void actualizarResumenTotales() {
        if (lblResumenTotales == null) {
            return;
        }
        int totalFilas = modeloTabla.getRowCount();
        double suma = 0.0;
        for (int i = 0; i < totalFilas; i++) {
            Object val = modeloTabla.getValueAt(i, 6);
            if (val != null) {
                String limpio = val.toString().replaceAll("[^0-9]", "");
                if (!limpio.isEmpty()) {
                    try {
                        suma += Double.parseDouble(limpio);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        lblResumenTotales.setText(String.format("Registros: %d  |  Total Listado: $ %,.0f", totalFilas, suma));
    }

    private void estilizarCampo(JTextField campo) {
        if (campo != null) {
            campo.setBackground(cFondoNegro);
            campo.setForeground(cTextoClaro);
            campo.setCaretColor(cAcento);
            campo.setFont(FUENTE_CONSOLA);
            campo.setBorder(new LineBorder(cAcento, 1));
        }
    }

    private void estilizarBoton(JButton boton) {
        if (boton != null) {
            boton.setBackground(cEncabezadoTabla);
            boton.setForeground(cTextoClaro);
            boton.setFont(FUENTE_CONSOLA);
            boton.setBorder(new LineBorder(cAcento, 1));
            boton.setFocusPainted(false);
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    private void configurarAtajos() {
        JRootPane root = this.getRootPane();

        // [ESC] Cerrar ventana
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [SUPR] Eliminar registro
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarSeleccionado();
            }
        });

        // [F4] Exportar a PDF
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "exportar_pdf");
        root.getActionMap().put("exportar_pdf", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportarPDF();
            }
        });

        // [F5] Ciclar Tema
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "cambiar_tema_f5");
        root.getActionMap().put("cambiar_tema_f5", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        // [F2] Atajo global interceptado mediante KeyboardFocusManager (limpiado en dispose())
        dispatcherF2 = e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F2) {
                if (FrmMantencionVentas.this.isDisplayable() && FrmMantencionVentas.this.isSelected()) {
                    abrirModificacion();
                    e.consume();
                    return true;
                }
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcherF2);
    }

    @Override
    public void dispose() {
        if (dispatcherF2 != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcherF2);
            dispatcherF2 = null;
        }
        super.dispose();
    }

    private void exportarPDF() {
        String filtroTexto = txtFiltro.getText().trim();
        String subtitulo = filtroTexto.isEmpty() ? "Todos los registros" : "Filtro aplicado: " + filtroTexto;
        PDFUtils.exportarTablaPDF(
                tablaVentas,
                "MANTENCIÓN DE VENTAS",
                subtitulo,
                lblResumenTotales != null ? lblResumenTotales.getText() : "Total Registros Listados: " + tablaVentas.getRowCount()
        );
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        String texto = txtFiltro.getText().trim();

        try (Connection con = Conexion.getConnection()) {
            VentaDAO dao = new VentaDAO();
            List<VentaDTO> lista = dao.obtenerVentas(con, texto);

            for (VentaDTO v : lista) {
                modeloTabla.addRow(new Object[]{
                    v.getVcodigo(),
                    v.getVcliente(),
                    v.getVfecha(),
                    v.getVboleta(),
                    v.getVfactura(),
                    v.getVguia(),
                    String.format("$ %,.0f", v.getVpxtotal())
                });
            }
            actualizarResumenTotales();
        } catch (Exception ex) {
            ex.printStackTrace();
            actualizarResumenTotales();
            JOptionPane.showMessageDialog(this, "Error al cargar ventas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSeleccionado() {
        int filaVista = tablaVentas.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de venta para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVista);
        Object valorCodigo = modeloTabla.getValueAt(filaModelo, 0);
        if (!(valorCodigo instanceof Number)) {
            JOptionPane.showMessageDialog(this, "Código de venta inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        long codigo = ((Number) valorCodigo).longValue();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar el registro ID: " + codigo + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = Conexion.getConnection()) {
                VentaDAO dao = new VentaDAO();
                if (dao.eliminarVenta(con, codigo)) {
                    JOptionPane.showMessageDialog(this, "Venta eliminada correctamente.");
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el registro.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar la venta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModificacion() {
        int filaVista = tablaVentas.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de venta en la tabla para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVista);
        Object valorCodigo = modeloTabla.getValueAt(filaModelo, 0);

        VentaDTO v = new VentaDTO();
        v.setVcodigo(valorCodigo instanceof Number ? ((Number) valorCodigo).longValue() : 0L);
        v.setVcliente((String) modeloTabla.getValueAt(filaModelo, 1));
        v.setVfecha((String) modeloTabla.getValueAt(filaModelo, 2));
        v.setVboleta((String) modeloTabla.getValueAt(filaModelo, 3));
        v.setVfactura((String) modeloTabla.getValueAt(filaModelo, 4));
        v.setVguia((String) modeloTabla.getValueAt(filaModelo, 5));

        Object valTotal = modeloTabla.getValueAt(filaModelo, 6);
        String totalStr = valTotal != null ? valTotal.toString().replaceAll("[^0-9]", "") : "";
        v.setVpxtotal(totalStr.isEmpty() ? 0.0 : Double.parseDouble(totalStr));

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        FrmModificarVenta frmMod = new FrmModificarVenta(parentWindow, v);
        frmMod.setVisible(true);

        if (frmMod.isGuardadoExitoso()) {
            cargarDatos();
        }
    }

    public void recargarDatosExternos() {
        cargarDatos();
    }
}