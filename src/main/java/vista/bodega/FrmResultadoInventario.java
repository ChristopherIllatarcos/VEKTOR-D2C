package vista.bodega;

import modelo.InventarioItemDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class FrmResultadoInventario extends JInternalFrame {

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JTextField txtFechaCorte;
    private JCheckBox chkSaldoCero;
    private JLabel lblTotalValor;
    private JLabel lblEncabezado;
    private JLabel lblAyuda;
    private JLabel lblBuscar;
    private JLabel lblFecha;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal;
    private JPanel panelNorte;
    private JPanel panelFiltrosSup;
    private JPanel panelFiltrosInf;
    private JPanel panelSur;
    private JScrollPane scrollPane;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelNorte, cPanelTabla, cAcento, cTextoClaro, cBarraSur, cSeleccion;
    private final Font FUENTE_MONOSPACED = new Font("Monospaced", Font.BOLD, 15);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Clásico por defecto

    private List<InventarioItemDTO> cacheItems;
    private String cacheFechaCorte;

    public FrmResultadoInventario(List<InventarioItemDTO> items, String fechaCorteInicial) {
        setTitle("5.- INVENTARIO DE PRODUCTOS");
        setClosable(false);
        setIconifiable(false);
        setMaximizable(false);
        setResizable(false);
        setSize(950, 580);

        this.cacheItems = items;
        this.cacheFechaCorte = fechaCorteInicial;

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents(items, fechaCorteInicial);
        configurarAtajosTeclado();
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
                cPanelNorte = new Color(45, 10, 55);
                cPanelTabla = new Color(15, 10, 20);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cBarraSur = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelNorte = new Color(10, 40, 50);
                cPanelTabla = new Color(5, 20, 25);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraSur = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelNorte = new Color(10, 45, 30);
                cPanelTabla = new Color(5, 25, 18);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cBarraSur = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelNorte = new Color(40, 18, 12);
                cPanelTabla = new Color(25, 12, 10);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cBarraSur = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Clipper Clásico":
            default:
                cFondo = new Color(0, 0, 128);
                cPanelNorte = new Color(0, 1, 36);
                cPanelTabla = new Color(0, 0, 160);
                cAcento = Color.YELLOW;
                cTextoClaro = Color.WHITE;
                cBarraSur = new Color(160, 0, 0);
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
            panelNorte.setBackground(cPanelNorte);
        }
        if (panelFiltrosSup != null) {
            panelFiltrosSup.setBackground(cFondo);
        }
        if (panelFiltrosInf != null) {
            panelFiltrosInf.setBackground(cFondo);
        }

        if (lblEncabezado != null) {
            lblEncabezado.setForeground(cAcento);
        }
        if (lblBuscar != null) {
            lblBuscar.setForeground(cAcento);
        }
        if (lblFecha != null) {
            lblFecha.setForeground(cAcento);
        }

        if (txtBuscar != null) {
            txtBuscar.setBackground(cPanelTabla);
            txtBuscar.setForeground(cTextoClaro);
            txtBuscar.setCaretColor(cTextoClaro);
        }
        if (txtFechaCorte != null) {
            txtFechaCorte.setBackground(cPanelTabla);
            txtFechaCorte.setForeground(cTextoClaro);
            txtFechaCorte.setCaretColor(cTextoClaro);
        }
        if (chkSaldoCero != null) {
            chkSaldoCero.setBackground(cFondo);
            chkSaldoCero.setForeground(cTextoClaro);
        }

        if (tabla != null) {
            tabla.setBackground(cPanelTabla);
            tabla.setForeground(cTextoClaro);
            tabla.setSelectionBackground(cSeleccion);
            tabla.setSelectionForeground(Color.WHITE);
            tabla.getTableHeader().setBackground(cFondo);
            tabla.getTableHeader().setForeground(cAcento);
            tabla.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondo);
            scrollPane.setBorder(BorderFactory.createLineBorder(cFondo));
        }

        if (panelSur != null) {
            panelSur.setBackground(cBarraSur);
        }
        if (lblTotalValor != null) {
            lblTotalValor.setForeground(cAcento);
        }

        actualizarTextoAyuda();

        revalidate();
        repaint();
    }

    private void actualizarTextoAyuda() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAyuda != null) {
            lblAyuda.setText("[F6] Tema: " + temaActual + " | (ESC) Salir");
        }
    }

    private void initComponents(List<InventarioItemDTO> items, String fechaCorteInicial) {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondo);

        // ==========================================
        // 1. ZONA SUPERIOR (Cabecera, Buscador y Filtros)
        // ==========================================
        panelNorte = new JPanel(new GridLayout(3, 1, 5, 5));
        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblEncabezado = new JLabel("=== INVENTARIO DE PRODUCTOS (Al " + fechaCorteInicial + ") ===", JLabel.CENTER);
        lblEncabezado.setForeground(cAcento);
        lblEncabezado.setFont(FUENTE_MONOSPACED);
        panelNorte.add(lblEncabezado);

        panelFiltrosSup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltrosSup.setBackground(cFondo);

        lblBuscar = new JLabel("BUSCAR (Descripción o Barra):");
        lblBuscar.setForeground(cAcento);
        lblBuscar.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(lblBuscar);

        txtBuscar = new JTextField(18);
        txtBuscar.setBackground(cPanelTabla);
        txtBuscar.setForeground(cTextoClaro);
        txtBuscar.setCaretColor(cTextoClaro);
        txtBuscar.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(txtBuscar);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String texto = txtBuscar.getText().toLowerCase().trim();
                javax.swing.RowFilter<DefaultTableModel, Object> rf = null;
                try {
                    rf = javax.swing.RowFilter.regexFilter("(?i)" + texto, 0, 1);
                } catch (java.util.regex.PatternSyntaxException ex) {
                    return;
                }
                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(modeloTabla);
                tabla.setRowSorter(sorter);
                sorter.setRowFilter(rf);
            }
        });

        lblFecha = new JLabel("Fecha al Corte:");
        lblFecha.setForeground(cAcento);
        lblFecha.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(lblFecha);

        txtFechaCorte = new JTextField(fechaCorteInicial, 12);
        txtFechaCorte.setBackground(cPanelTabla);
        txtFechaCorte.setForeground(cTextoClaro);
        txtFechaCorte.setCaretColor(cTextoClaro);
        txtFechaCorte.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(txtFechaCorte);

        panelNorte.add(panelFiltrosSup);

        panelFiltrosInf = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltrosInf.setBackground(cFondo);

        chkSaldoCero = new JCheckBox("Ver Saldo / Stock Cero");
        chkSaldoCero.setBackground(cFondo);
        chkSaldoCero.setForeground(cTextoClaro);
        chkSaldoCero.setFont(new Font("Monospaced", Font.BOLD, 12));
        chkSaldoCero.setFocusPainted(false);
        panelFiltrosInf.add(chkSaldoCero);

        chkSaldoCero.addItemListener((java.awt.event.ItemEvent e) -> {
            javax.swing.table.TableRowSorter<DefaultTableModel> sorter
                    = (javax.swing.table.TableRowSorter<DefaultTableModel>) tabla.getRowSorter();

            if (sorter == null) {
                sorter = new javax.swing.table.TableRowSorter<>(modeloTabla);
                tabla.setRowSorter(sorter);
            }

            if (chkSaldoCero.isSelected()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        String stockStr = entry.getStringValue(3).replace(",", ".");
                        try {
                            double stock = Double.parseDouble(stockStr);
                            return stock > 0.0;
                        } catch (NumberFormatException ex) {
                            return true;
                        }
                    }
                });
            }
        });

        panelNorte.add(panelFiltrosInf);
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. GRILLA / TABLA ESTILO RETRO
        // ==========================================
        String[] columnas = {"Código", "Descripción", "Familia", "Stock Actual", "Costo Unit.", "Valor Total"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        double costoTotalGeneral = 0;
        for (InventarioItemDTO item : items) {
            modeloTabla.addRow(new Object[]{
                item.getCodigo(),
                item.getDescripcion(),
                item.getFamiliaNombre(),
                String.format("%.2f", item.getStockActual()),
                String.format("$ %.2f", item.getCostoUnitario()),
                String.format("$ %.2f", item.getValorTotal())
            });
            costoTotalGeneral += item.getValorTotal();
        }

        tabla = new JTable(modeloTabla);
        tabla.setBackground(cPanelTabla);
        tabla.setForeground(cTextoClaro);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 16));
        tabla.setRowHeight(22);
        tabla.setSelectionBackground(cSeleccion);
        tabla.setSelectionForeground(Color.WHITE);

        tabla.getTableHeader().setBackground(cFondo);
        tabla.getTableHeader().setForeground(cAcento);
        tabla.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));

        scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(cFondo);
        scrollPane.setBorder(BorderFactory.createLineBorder(cFondo));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. BARRA INFERIOR (Totales y Pie)
        // ==========================================
        panelSur = new JPanel(new BorderLayout());
        panelSur.setBackground(cBarraSur);
        panelSur.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        lblTotalValor = new JLabel(String.format("Valorización Total: $ %.2f", costoTotalGeneral));
        lblTotalValor.setForeground(cAcento);
        lblTotalValor.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelSur.add(lblTotalValor, BorderLayout.WEST);

        lblAyuda = new JLabel("", JLabel.RIGHT);
        lblAyuda.setForeground(Color.WHITE);
        lblAyuda.setFont(new Font("Monospaced", Font.BOLD, 13));
        actualizarTextoAyuda();
        panelSur.add(lblAyuda, BorderLayout.EAST);

        panelPrincipal.add(panelSur, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void configurarAtajosTeclado() {
        JComponent root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        root.getActionMap().put("salir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F6] Ciclar Tema en tiempo real
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclar_tema");
        root.getActionMap().put("ciclar_tema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }
}
