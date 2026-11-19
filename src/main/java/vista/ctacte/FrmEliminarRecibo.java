package vista.ctacte;

import conexion.Conexion;
import dao.CuentaCorrienteDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.List;
import java.util.Vector;
import modelo.DocumentoCarteraDTO;

public class FrmEliminarRecibo extends JInternalFrame {

    private JTextField txtFiltroCliente;
    private JTable tablaRecibos;
    private DefaultTableModel modeloTabla;

    private JLabel lblValorFolio;
    private JLabel lblValorFecha;
    private JLabel lblValorRut;
    private JLabel lblValorMonto;
    private JLabel lblAyuda;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelBusqueda, panelDetalle, panelAyuda;
    private JSplitPane splitPane;
    private JScrollPane scrollTabla;

    private boolean eliminadoConExito = false;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelSup, cTablaFondo, cBordeAcento, cTextoVerde, cTextoBlanco, cBarraAyuda;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public FrmEliminarRecibo() {
        super("1.- ADMINISTRADOR Y ELIMINACION DE RECIBOS ", false, false, false, false);
        setSize(850, 580);
        setFrameIcon(null);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Panel superior: Buscador
        panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setBackground(cFondo);
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2), " 1. BUSCAR CLIENTE (RUT / FOLIO) ",
                0, 0, FUENTE_CONSOLA, cBordeAcento
        ));

        JLabel lblFiltro = new JLabel("Filtro:");
        lblFiltro.setFont(FUENTE_CONSOLA);
        lblFiltro.setForeground(cTextoBlanco);
        panelBusqueda.add(lblFiltro);

        txtFiltroCliente = new JTextField(35);
        estilarCampo(txtFiltroCliente);
        panelBusqueda.add(txtFiltroCliente);

        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // 2. Panel Central: Tabla y Detalles
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(cFondo);
        splitPane.setDividerLocation(240);
        splitPane.setResizeWeight(0.5);

        String[] columnas = {"N° Folio", "Fecha", "RUT Cliente", "Abono ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRecibos = new JTable(modeloTabla);
        estilarTabla(tablaRecibos);
        scrollTabla = new JScrollPane(tablaRecibos);
        scrollTabla.getViewport().setBackground(cTablaFondo);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2), " 2. SELECCIONE EL RECIBO DE LA LISTA ",
                0, 0, FUENTE_CONSOLA, cBordeAcento
        ));
        splitPane.setTopComponent(scrollTabla);

        // Panel de detalles inferior
        panelDetalle = new JPanel(new GridBagLayout());
        panelDetalle.setBackground(cFondo);
        panelDetalle.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2), " 3. DETALLE DEL FOLIO Y ACCION DE ELIMINACION ",
                0, 0, FUENTE_CONSOLA, cBordeAcento
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 25, 12, 25);
        gbc.anchor = GridBagConstraints.WEST;

        Font fuenteDetalleGrande = new Font("Courier New", Font.BOLD, 15);

        // Fila 0: Folio
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblF = new JLabel("Folio Seleccionado :");
        lblF.setFont(fuenteDetalleGrande);
        lblF.setForeground(cBordeAcento);
        panelDetalle.add(lblF, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        lblValorFolio = new JLabel("---");
        lblValorFolio.setFont(fuenteDetalleGrande);
        lblValorFolio.setForeground(cTextoVerde);
        panelDetalle.add(lblValorFolio, gbc);

        // Fila 1: Fecha
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblFe = new JLabel("Fecha del Pago     :");
        lblFe.setFont(fuenteDetalleGrande);
        lblFe.setForeground(cBordeAcento);
        panelDetalle.add(lblFe, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        lblValorFecha = new JLabel("---");
        lblValorFecha.setFont(fuenteDetalleGrande);
        lblValorFecha.setForeground(cTextoVerde);
        panelDetalle.add(lblValorFecha, gbc);

        // Fila 2: RUT
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblR = new JLabel("RUT del Cliente    :");
        lblR.setFont(fuenteDetalleGrande);
        lblR.setForeground(cBordeAcento);
        panelDetalle.add(lblR, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        lblValorRut = new JLabel("---");
        lblValorRut.setFont(fuenteDetalleGrande);
        lblValorRut.setForeground(cTextoVerde);
        panelDetalle.add(lblValorRut, gbc);

        // Fila 3: Monto
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblM = new JLabel("Monto Abono        :");
        lblM.setFont(fuenteDetalleGrande);
        lblM.setForeground(cBordeAcento);
        panelDetalle.add(lblM, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        lblValorMonto = new JLabel("---");
        lblValorMonto.setFont(fuenteDetalleGrande);
        lblValorMonto.setForeground(cTextoVerde);
        panelDetalle.add(lblValorMonto, gbc);

        splitPane.setBottomComponent(panelDetalle);
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        // 3. Barra de atajos inferior
        panelAyuda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelAyuda.setBackground(cBarraAyuda);
        lblAyuda = new JLabel();
        lblAyuda.setFont(new Font("Courier New", Font.BOLD, 12));
        lblAyuda.setForeground(cBordeAcento);
        actualizarTextoAyuda();
        panelAyuda.add(lblAyuda);
        panelPrincipal.add(panelAyuda, BorderLayout.SOUTH);

        add(panelPrincipal);

        cargarDatosTabla("");

        // Eventos
        txtFiltroCliente.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (tablaRecibos.getRowCount() > 0) {
                        tablaRecibos.setRowSelectionInterval(0, 0);
                        tablaRecibos.requestFocus();
                    }
                } else {
                    cargarDatosTabla(txtFiltroCliente.getText().trim());
                }
            }
        });

        tablaRecibos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarDetallesSeleccionados();
            }
        });

        tablaRecibos.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_F3:
                        confirmarYEliminarSeleccionado();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        dispose();
                        break;
                    case KeyEvent.VK_F6:
                        ciclarSiguienteTema();
                        break;
                    default:
                        break;
                }
            }
        });

        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        panelPrincipal.getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F6] Ciclar Tema global en la ventana
        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "CICLAR_TEMA");
        panelPrincipal.getActionMap().put("CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
        
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
                cPanelSup = new Color(45, 10, 55);
                cTablaFondo = new Color(15, 10, 20);
                cBordeAcento = new Color(255, 110, 220);
                cTextoVerde = new Color(255, 180, 240);
                cTextoBlanco = new Color(255, 230, 255);
                cBarraAyuda = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelSup = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cBordeAcento = new Color(0, 245, 255);
                cTextoVerde = new Color(180, 255, 255);
                cTextoBlanco = new Color(220, 255, 255);
                cBarraAyuda = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelSup = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cBordeAcento = new Color(0, 255, 128);
                cTextoVerde = new Color(180, 255, 200);
                cTextoBlanco = new Color(220, 255, 240);
                cBarraAyuda = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelSup = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cBordeAcento = new Color(245, 73, 39);
                cTextoVerde = new Color(255, 200, 150);
                cTextoBlanco = new Color(255, 235, 225);
                cBarraAyuda = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 132);
                cPanelSup = new Color(0, 0, 80);
                cTablaFondo = Color.BLACK;
                cBordeAcento = Color.CYAN;
                cTextoVerde = Color.GREEN;
                cTextoBlanco = Color.WHITE;
                cBarraAyuda = new Color(0, 0, 80);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
        }

        if (panelBusqueda != null) {
            panelBusqueda.setBackground(cFondo);
            panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2), " 1. BUSCAR CLIENTE (RUT / FOLIO) ",
                    0, 0, FUENTE_CONSOLA, cBordeAcento
            ));
        }

        if (txtFiltroCliente != null) {
            txtFiltroCliente.setBackground(cTablaFondo);
            txtFiltroCliente.setForeground(cTextoVerde);
            txtFiltroCliente.setCaretColor(cTextoVerde);
            txtFiltroCliente.setBorder(new LineBorder(cBordeAcento, 1));
        }

        if (splitPane != null) {
            splitPane.setBackground(cFondo);
        }

        if (tablaRecibos != null) {
            tablaRecibos.setBackground(cTablaFondo);
            tablaRecibos.setForeground(cTextoVerde);
            tablaRecibos.setGridColor(cBordeAcento);
            tablaRecibos.getTableHeader().setBackground(cPanelSup);
            tablaRecibos.getTableHeader().setForeground(cTextoBlanco);
            tablaRecibos.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cTablaFondo);
            scrollTabla.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2), " 2. SELECCIONE EL RECIBO DE LA LISTA ",
                    0, 0, FUENTE_CONSOLA, cBordeAcento
            ));
        }

        if (panelDetalle != null) {
            panelDetalle.setBackground(cFondo);
            panelDetalle.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2), " 3. DETALLE DEL FOLIO Y ACCION DE ELIMINACION ",
                    0, 0, FUENTE_CONSOLA, cBordeAcento
            ));
            // Repintar etiquetas internas del panel de detalle
            for (Component comp : panelDetalle.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel lbl = (JLabel) comp;
                    if (lbl.getForeground().equals(Color.CYAN) || lbl.getForeground().equals(cBordeAcento)) {
                        lbl.setForeground(cBordeAcento);
                    } else if (!lbl.getText().equals("---") && !lbl.getText().isEmpty()) {
                        lbl.setForeground(cTextoVerde);
                    }
                }
            }
        }

        if (panelAyuda != null) {
            panelAyuda.setBackground(cBarraAyuda);
        }
        actualizarTextoAyuda();

        revalidate();
        repaint();
    }

    private void actualizarTextoAyuda() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAyuda != null) {
            lblAyuda.setText(" [ESC] Salir    [ENTER] Seleccionar    [DEL] Eliminar    [F6] Tema: " + temaActual + " ");
        }
    }

    private void estilarCampo(JTextField txt) {
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(cTablaFondo);
        txt.setForeground(cTextoVerde);
        txt.setCaretColor(cTextoVerde);
        txt.setBorder(new LineBorder(cBordeAcento, 1));
    }

    private void estilarTabla(JTable tabla) {
        tabla.setFont(FUENTE_CONSOLA);
        tabla.setBackground(cTablaFondo);
        tabla.setForeground(cTextoVerde);
        tabla.setGridColor(cBordeAcento);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setFont(FUENTE_CONSOLA);
        tabla.getTableHeader().setBackground(cPanelSup);
        tabla.getTableHeader().setForeground(cTextoBlanco);
    }

    private void cargarDatosTabla(String filtro) {
        modeloTabla.setRowCount(0);
        try (Connection con = Conexion.getConnection()) {
            CuentaCorrienteDAO dao = new CuentaCorrienteDAO();
            List<DocumentoCarteraDTO> lista = dao.buscarRecibosPorFiltro(con, filtro);

            for (DocumentoCarteraDTO doc : lista) {
                Vector<Object> fila = new Vector<>();
                fila.add(doc.getFolio());
                fila.add(doc.getFecha());
                fila.add(doc.getCliente());
                fila.add(String.format("$ %,.0f", doc.getMonto()));
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void actualizarDetallesSeleccionados() {
        int fila = tablaRecibos.getSelectedRow();
        if (fila != -1) {
            lblValorFolio.setText(modeloTabla.getValueAt(fila, 0).toString());
            lblValorFecha.setText(modeloTabla.getValueAt(fila, 1).toString());
            lblValorRut.setText(modeloTabla.getValueAt(fila, 2).toString());
            lblValorMonto.setText(modeloTabla.getValueAt(fila, 3).toString());
        } else {
            lblValorFolio.setText("---");
            lblValorFecha.setText("---");
            lblValorRut.setText("---");
            lblValorMonto.setText("---");
        }
    }

    private void confirmarYEliminarSeleccionado() {
        int fila = tablaRecibos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un recibo de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String folio = modeloTabla.getValueAt(fila, 0).toString();
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está ABSOLUTAMENTE seguro de eliminar el recibo N° " + folio + "?",
                "Confirmación de Seguridad",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try (Connection con = Conexion.getConnection()) {
                CuentaCorrienteDAO dao = new CuentaCorrienteDAO();
                if (dao.eliminarPagoPorFolio(con, folio)) {
                    JOptionPane.showMessageDialog(this, "¡Recibo eliminado con éxito!", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    eliminadoConExito = true;
                    cargarDatosTabla(txtFiltroCliente.getText().trim());
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo completar la eliminación.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isEliminadoConExito() {
        return eliminadoConExito;
    }
}
