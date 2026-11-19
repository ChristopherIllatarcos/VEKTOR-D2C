package vista.bodega;

import modelo.InventarioItemDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class DlgResultadoInventario extends JDialog {
    private JButton btnCerrar;

    public DlgResultadoInventario(Frame parent, List<InventarioItemDTO> items, String fechaCorte) {
        super(parent, "Listado de Inventario al " + fechaCorte, true);
        initComponents(items, fechaCorte);
        configurarAtajosTeclado(); // <--- Activamos ESC y Enter global aquí también
        
        setSize(850, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents(List<InventarioItemDTO> items, String fechaCorte) {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(30, 30, 30));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Cabecera informativa
        JLabel lblEncabezado = new JLabel("INVENTARIO DE PRODUCTOS (Al " + fechaCorte + ")");
        lblEncabezado.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEncabezado.setForeground(new Color(220, 220, 220));
        panelPrincipal.add(lblEncabezado, BorderLayout.NORTH);

        // Configuración de la JTable moderna
        String[] columnas = {"Código", "Descripción", "Familia", "Stock Actual", "Costo Unit.", "Valor Total"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Solo lectura
        };

        double costoTotalGeneral = 0;
        for (InventarioItemDTO item : items) {
            model.addRow(new Object[]{
                item.getCodigo(),
                item.getDescripcion(),
                item.getFamiliaNombre(),
                String.format("%.2f", item.getStockActual()),
                String.format("$ %.2f", item.getCostoUnitario()),
                String.format("$ %.2f", item.getValorTotal())
            });
            costoTotalGeneral += item.getValorTotal();
        }

        JTable tabla = new JTable(model);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(new Color(45, 45, 45));
        tabla.getTableHeader().setForeground(new Color(100, 180, 255));
        
        JScrollPane scrollPane = new JScrollPane(tabla);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con totales y botón cerrar
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);

        JLabel lblTotal = new JLabel(String.format("Valorización Total Inventario: $ %.2f", costoTotalGeneral));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(new Color(100, 180, 255));
        panelInferior.add(lblTotal, BorderLayout.WEST);

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setBackground(new Color(60, 60, 60));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());
        panelInferior.add(btnCerrar, BorderLayout.EAST);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
          SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();
        
        // Tecla ESC para cerrar el listado al instante
        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Tecla ENTER para activar el botón "Cerrar" si está seleccionado o enfocado
        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "CERRAR_ENTER");
        rootPane.getActionMap().put("CERRAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnCerrar.doClick();
            }
        });
    }
}