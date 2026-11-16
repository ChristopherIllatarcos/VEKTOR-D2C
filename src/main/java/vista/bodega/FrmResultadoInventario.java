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

    public FrmResultadoInventario(List<InventarioItemDTO> items, String fechaCorteInicial) {
        setTitle("VEKTOR D2C - INVENTARIO DE PRODUCTOS");
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setSize(950, 580);

        initComponents(items, fechaCorteInicial);
        configurarAtajosTeclado();
    }

    private void initComponents(List<InventarioItemDTO> items, String fechaCorteInicial) {
        // Panel principal con fondo azul clásico Clipper
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(0, 0, 128));

        // ==========================================
        // 1. ZONA SUPERIOR (Cabecera, Buscador y Filtros)
        // ==========================================
        JPanel panelNorte = new JPanel(new GridLayout(3, 1, 5, 5));
        panelNorte.setBackground(new Color(0, 1, 36));
        panelNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título estilo consola
        JLabel lblEncabezado = new JLabel("=== INVENTARIO DE PRODUCTOS (Al " + fechaCorteInicial + ") ===", JLabel.CENTER);
        lblEncabezado.setForeground(Color.YELLOW);
        lblEncabezado.setFont(new Font("Monospaced", Font.BOLD, 15));
        panelNorte.add(lblEncabezado);

        // Panel de búsqueda y fecha al corte
        JPanel panelFiltrosSup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltrosSup.setBackground(new Color(0, 0, 128));

        JLabel lblBuscar = new JLabel("BUSCAR (Descripción o Barra):");
        lblBuscar.setForeground(Color.YELLOW);
        lblBuscar.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(lblBuscar);

        txtBuscar = new JTextField(18);
        txtBuscar.setBackground(new Color(0, 0, 160));
        txtBuscar.setForeground(Color.WHITE);
        txtBuscar.setCaretColor(Color.WHITE);
        txtBuscar.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(txtBuscar);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String texto = txtBuscar.getText().toLowerCase().trim();
                javax.swing.RowFilter<DefaultTableModel, Object> rf = null;
                try {
                    // Filtra por las columnas 0 (Código) y 1 (Descripción)
                    rf = javax.swing.RowFilter.regexFilter("(?i)" + texto, 0, 1);
                } catch (java.util.regex.PatternSyntaxException ex) {
                    return;
                }
                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(modeloTabla);
                tabla.setRowSorter(sorter);
                sorter.setRowFilter(rf);
            }
        });

        JLabel lblFecha = new JLabel("Fecha al Corte:");
        lblFecha.setForeground(Color.YELLOW);
        lblFecha.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(lblFecha);

        txtFechaCorte = new JTextField(fechaCorteInicial, 12);
        txtFechaCorte.setBackground(new Color(0, 0, 160));
        txtFechaCorte.setForeground(Color.WHITE);
        txtFechaCorte.setCaretColor(Color.WHITE);
        txtFechaCorte.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelFiltrosSup.add(txtFechaCorte);

        panelNorte.add(panelFiltrosSup);

        // Panel para el Checkbox de saldo cero
        JPanel panelFiltrosInf = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltrosInf.setBackground(new Color(0, 0, 128));

        chkSaldoCero = new JCheckBox("Ver Saldo / Stock Cero");
        chkSaldoCero.setBackground(new Color(0, 0, 128));
        chkSaldoCero.setForeground(Color.WHITE);
        chkSaldoCero.setFont(new Font("Monospaced", Font.BOLD, 12));
        chkSaldoCero.setFocusPainted(false);
        panelFiltrosInf.add(chkSaldoCero);

        // Lógica para que el Checkbox filtre el stock en tiempo real
        chkSaldoCero.addItemListener((java.awt.event.ItemEvent e) -> {
            javax.swing.table.TableRowSorter<DefaultTableModel> sorter
                    = (javax.swing.table.TableRowSorter<DefaultTableModel>) tabla.getRowSorter();
            
            if (sorter == null) {
                sorter = new javax.swing.table.TableRowSorter<>(modeloTabla);
                tabla.setRowSorter(sorter);
            }
            
            if (chkSaldoCero.isSelected()) {
                // Si está marcado, mostramos todo (quitamos el filtro de stock)
                sorter.setRowFilter(null);
            } else {
                // Si NO está marcado, filtramos para Ocultar los que tengan stock 0,00
                sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        // La columna 3 es "Stock Actual"
                        String stockStr = entry.getStringValue(3).replace(",", ".");
                        try {
                            double stock = Double.parseDouble(stockStr);
                            return stock > 0.0; // Solo muestra si es mayor a cero
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
        tabla.setBackground(new Color(0, 0, 160));
        tabla.setForeground(Color.WHITE);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 16));
        tabla.setRowHeight(22);

        // Encabezado de la tabla estilo Clipper
        tabla.getTableHeader().setBackground(new Color(0, 0, 100));
        tabla.getTableHeader().setForeground(Color.YELLOW);
        tabla.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(new Color(0, 0, 128));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 90)));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // ==========================================
        // 3. BARRA INFERIOR (Totales y Pie Rojo Clipper)
        // ==========================================
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setBackground(new Color(160, 0, 0)); // Rojo clásico Clipper
        panelSur.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        lblTotalValor = new JLabel(String.format("Valorización Total: $ %.2f", costoTotalGeneral));
        lblTotalValor.setForeground(Color.YELLOW);
        lblTotalValor.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelSur.add(lblTotalValor, BorderLayout.WEST);

        JLabel lblAyuda = new JLabel("(ESC) Salir", JLabel.RIGHT);
        lblAyuda.setForeground(Color.WHITE);
        lblAyuda.setFont(new Font("Monospaced", Font.BOLD, 13));
        panelSur.add(lblAyuda, BorderLayout.EAST);

        panelPrincipal.add(panelSur, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void configurarAtajosTeclado() {
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose(); // Cierra el JInternalFrame al instante con ESC
                }
            }
        };

        this.addKeyListener(keyAdapter);
        tabla.addKeyListener(keyAdapter);
        txtBuscar.addKeyListener(keyAdapter);
        txtFechaCorte.addKeyListener(keyAdapter);
        chkSaldoCero.addKeyListener(keyAdapter);
    }
}
