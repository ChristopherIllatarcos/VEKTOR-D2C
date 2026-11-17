package vista.bodega;

import dao.KardexMercaderiaDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class FrmKardexMercaderia extends JInternalFrame {

    private JTextField txtBuscarProducto;
    private JTable tablaKardex;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalValorizacion;

    // Paleta Azul Océano Profundo / Cian Brillante
    private final Color COLOR_FONDO = new Color(11, 34, 58);          // Azul marino oscuro (#0B223A)
    private final Color COLOR_PANEL_TABLA = new Color(5, 18, 30);     // Fondo tabla profundo
    private final Color COLOR_ZEBRA_1 = new Color(5, 18, 30);         // Fila cebra par
    private final Color COLOR_ZEBRA_2 = new Color(15, 42, 68);        // Fila cebra impar
    private final Color COLOR_BORDE_CIAN = new Color(0, 229, 255);    // Cian brillante (#00E5FF)
    private final Color COLOR_TEXTO_CLARO = new Color(220, 240, 255);   // Azul claro / Blanco
    private final Color COLOR_BARRA_INF = new Color(20, 70, 110);     // Azul marino medio
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    public FrmKardexMercaderia() {
        super("4.- KÁRDEX DE MERCADERÍA", false, false, false, false);
        initComponentsManual();
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
                new LineBorder(COLOR_BORDE_CIAN, 2),
                " KÁRDEX Y CONSULTA DE STOCK - FILTROS ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_CIAN
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
        String[] columnas = {"# Barra", "Fam.", "Descripción del Producto", "Costo Unit. ($)", "Stock 0", "Stock 1", "Stock 2", "Total Valorizado ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaKardex = new JTable(modeloTabla);
        tablaKardex.setFont(FUENTE_CONSOLA);
        tablaKardex.setBackground(COLOR_PANEL_TABLA);
        tablaKardex.setForeground(Color.WHITE);
        tablaKardex.setGridColor(new Color(25, 70, 110));
        tablaKardex.setRowHeight(28);
        tablaKardex.setSelectionBackground(COLOR_BARRA_INF);
        tablaKardex.setSelectionForeground(Color.YELLOW);

        tablaKardex.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaKardex.getTableHeader().setBackground(COLOR_FONDO);
        tablaKardex.getTableHeader().setForeground(COLOR_BORDE_CIAN);

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

        for (int i = 0; i < tablaKardex.getColumnCount(); i++) {
            tablaKardex.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaKardex.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaKardex.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaKardex.getColumnModel().getColumn(2).setPreferredWidth(340);
        tablaKardex.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaKardex.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablaKardex.getColumnModel().getColumn(5).setPreferredWidth(80);
        tablaKardex.getColumnModel().getColumn(6).setPreferredWidth(80);
        tablaKardex.getColumnModel().getColumn(7).setPreferredWidth(140);

        JScrollPane scrollTabla = new JScrollPane(tablaKardex);
        scrollTabla.getViewport().setBackground(COLOR_PANEL_TABLA);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE_CIAN, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        JPanel panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(COLOR_FONDO);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(COLOR_FONDO);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDE_CIAN));

        lblTotalValorizacion = new JLabel("VALORIZACIÓN TOTAL INVENTARIO: $0");
        lblTotalValorizacion.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalValorizacion.setForeground(COLOR_BORDE_CIAN);
        panelTotales.add(lblTotalValorizacion);

        JPanel panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajos.setBackground(COLOR_BARRA_INF);
        panelAtajos.setBorder(new LineBorder(COLOR_BORDE_CIAN, 1));

        JLabel lblAtajos = new JLabel("(F5) Refrescar / Buscar    |    (Esc) Salir");
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
        txt.setForeground(COLOR_BORDE_CIAN);
        txt.setCaretColor(COLOR_BORDE_CIAN);
        txt.setBorder(new LineBorder(COLOR_BORDE_CIAN, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(10, 35, 60));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(COLOR_PANEL_TABLA);
                txt.setBorder(new LineBorder(COLOR_BORDE_CIAN, 1));
            }
        });

        return txt;
    }

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        KardexMercaderiaDAO dao = new KardexMercaderiaDAO();
        List<Object[]> resultados = dao.buscarKardex(txtBuscarProducto.getText());

        double sumaValorizacionTotal = 0;

        for (Object[] fila : resultados) {
            double costo = (double) fila[3];
            double stock0 = (double) fila[4];
            double stock1 = (double) fila[5];
            double stock2 = (double) fila[6];
            double total = (double) fila[7];

            sumaValorizacionTotal += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                String.format("$%,.0f", costo),
                String.format("%,.0f", stock0),
                String.format("%,.0f", stock1),
                String.format("%,.0f", stock2),
                String.format("$%,.0f", total)
            });
        }

        lblTotalValorizacion.setText(String.format("VALORIZACIÓN TOTAL INVENTARIO: $%,.0f", sumaValorizacionTotal));
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

        // [F5] Refrescar / Limpiar búsqueda
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtBuscarProducto.setText("");
                ejecutarBusqueda();
                txtBuscarProducto.requestFocusInWindow();
            }
        });
    }
}