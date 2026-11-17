package vista.bodega;

import conexion.Conexion;
import dao.ParametrosDAO;
import dao.RotacionVentasDAO;
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

public class FrmRotacionVentas extends JInternalFrame {

    private JTextField txtFechaInicial;
    private JTextField txtFechaFinal;
    private JTextField txtBuscarProducto;

    private JTable tablaRotacion;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalUnidades;
    private JLabel lblTotalMonto;

    // Paleta Terracota / Cobre Oscuro para Bodegas
    private final Color COLOR_FONDO = new Color(61, 28, 2);             // Cobre / Terracota oscuro (#3D1C02)
    private final Color COLOR_PANEL_TABLA = new Color(26, 14, 5);       // Fondo oscuro cálido tabla
    private final Color COLOR_ZEBRA_1 = new Color(26, 14, 5);           // Fondo cebra par
    private final Color COLOR_ZEBRA_2 = new Color(36, 20, 8);           // Fondo cebra impar (más legible)
    private final Color COLOR_BORDE_AMBAR = new Color(255, 170, 51);    // Ámbar / Naranja dorado (#FFAA33)
    private final Color COLOR_TEXTO_CREMA = new Color(255, 240, 204);   // Crema dorado claro (#FFF0CC)
    private final Color COLOR_BARRA_INF = new Color(115, 50, 5);        // Terracota medio
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    public FrmRotacionVentas() {
        super("7.- ROTACIÓN DE MERCADERÍA POR VENTAS", false, false, false, false);
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
        // 1. PANEL SUPERIOR (Filtros estilo Clipper)
        // ==========================================
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(COLOR_FONDO);
        panelSuperior.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_BORDE_AMBAR, 2),
                " ROTACIÓN DE MERCADERÍA - FILTROS DE CONSULTA ",
                0, 0, FUENTE_CONSOLA, COLOR_BORDE_AMBAR
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

        // Enter en cualquiera de los campos filtra al instante
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
        // 2. PANEL CENTRAL (Tabla de Resultados Cebra)
        // ==========================================
        String[] columnas = {"# Barra", "Fam.", "Fecha", "Descripción del Producto", "Costo", "Rotac. Cant.", "Rotac. Total ($)", "Media ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRotacion = new JTable(modeloTabla);
        tablaRotacion.setFont(FUENTE_CONSOLA);
        tablaRotacion.setBackground(COLOR_PANEL_TABLA);
        tablaRotacion.setForeground(Color.WHITE);
        tablaRotacion.setGridColor(new Color(90, 45, 15));
        tablaRotacion.setRowHeight(28); // Altura más cómoda para respirar visualmente
        tablaRotacion.setSelectionBackground(COLOR_BARRA_INF);
        tablaRotacion.setSelectionForeground(Color.YELLOW);

        tablaRotacion.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaRotacion.getTableHeader().setBackground(COLOR_FONDO);
        tablaRotacion.getTableHeader().setForeground(COLOR_BORDE_AMBAR);

        // Renderizador personalizado para Filas Cebra y alineación numérica
        DefaultTableCellRenderer renderizadorCebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(COLOR_BARRA_INF);
                    c.setForeground(Color.YELLOW);
                } else {
                    // Alternar colores cebra para facilitar la lectura
                    if (row % 2 == 0) {
                        c.setBackground(COLOR_ZEBRA_1);
                    } else {
                        c.setBackground(COLOR_ZEBRA_2);
                    }
                    c.setForeground(Color.WHITE);
                }

                // Alinear a la derecha las columnas numéricas (Fam, Costo, Cant, Total, Media)
                if (column == 1 || column >= 4) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        // Aplicar el renderizador a todas las columnas de la tabla
        for (int i = 0; i < tablaRotacion.getColumnCount(); i++) {
            tablaRotacion.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        // Anchos sugeridos de columnas
        tablaRotacion.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaRotacion.getColumnModel().getColumn(1).setPreferredWidth(55);
        tablaRotacion.getColumnModel().getColumn(2).setPreferredWidth(105);
        tablaRotacion.getColumnModel().getColumn(3).setPreferredWidth(320);
        tablaRotacion.getColumnModel().getColumn(4).setPreferredWidth(95);
        tablaRotacion.getColumnModel().getColumn(5).setPreferredWidth(110);
        tablaRotacion.getColumnModel().getColumn(6).setPreferredWidth(135);
        tablaRotacion.getColumnModel().getColumn(7).setPreferredWidth(110);

        JScrollPane scrollTabla = new JScrollPane(tablaRotacion);
        scrollTabla.getViewport().setBackground(COLOR_PANEL_TABLA);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE_AMBAR, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Barra Atajos)
        // ==========================================
        JPanel panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(COLOR_FONDO);

        // Barra de Totales
        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(COLOR_FONDO);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDE_AMBAR));

        lblTotalUnidades = crearEtiqueta("UNIDADES ROTADAS: 0");
        lblTotalMonto = new JLabel("TOTAL ROTACIÓN: $0");
        lblTotalMonto.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalMonto.setForeground(COLOR_BORDE_AMBAR);

        panelTotales.add(lblTotalUnidades);
        panelTotales.add(lblTotalMonto);

        // Barra de Atajos
        JPanel panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        panelAtajos.setBackground(COLOR_BARRA_INF);
        panelAtajos.setBorder(new LineBorder(COLOR_BORDE_AMBAR, 1));

        JLabel lblAtajos = new JLabel("(Enter) Buscar / Filtrar    |    (F5) Ver Todo / Limpiar    |    (Esc) Salir");
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(COLOR_TEXTO_CREMA);
        panelAtajos.add(lblAtajos);

        panelInferiorContenedor.add(panelTotales, BorderLayout.NORTH);
        panelInferiorContenedor.add(panelAtajos, BorderLayout.SOUTH);
        add(panelInferiorContenedor, BorderLayout.SOUTH);

        configurarAtajos();
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(COLOR_TEXTO_CREMA);
        return lbl;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(COLOR_PANEL_TABLA);
        txt.setForeground(COLOR_BORDE_AMBAR);
        txt.setCaretColor(COLOR_BORDE_AMBAR);
        txt.setBorder(new LineBorder(COLOR_BORDE_AMBAR, 1));

        // Efecto visual de foco activo (el campo resalta al seleccionarse)
        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(45, 22, 6));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(COLOR_PANEL_TABLA);
                txt.setBorder(new LineBorder(COLOR_BORDE_AMBAR, 1));
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
        RotacionVentasDAO dao = new RotacionVentasDAO();
        List<Object[]> resultados = dao.buscarRotacionVentas(fIni, fFin, prod);

        double sumaUnidades = 0;
        double sumaMonto = 0;

        for (Object[] fila : resultados) {
            double costo = (double) fila[4];
            double cant = (double) fila[5];
            double total = (double) fila[6];
            double media = (double) fila[7];

            sumaUnidades += cant;
            sumaMonto += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                fila[3],
                String.format("$%,.0f", costo),
                String.format("%,.0f", cant),
                String.format("$%,.0f", total),
                String.format("$%,.0f", media)
            });
        }

        lblTotalUnidades.setText(String.format("UNIDADES ROTADAS: %,.0f", sumaUnidades));
        lblTotalMonto.setText(String.format("TOTAL ROTACIÓN: $%,.0f", sumaMonto));
    }

    private void configurarAtajos() {
        JRootPane root = getRootPane();

        // [ESC] Cerrar
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F5] Limpiar filtros y mostrar todo
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
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
    }
}
