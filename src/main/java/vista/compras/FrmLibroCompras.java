package vista.compras;

import conexion.Conexion;
import dao.LibroComprasDAO;
import dao.ParametrosDAO;
import modelo.ParametrosDTO;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrmLibroCompras extends JInternalFrame {

    private JTextField txtFechaInicial;
    private JTextField txtFechaFinal;
    private JTextField txtNombreProveedor;

    private JTable tablaCompras;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalNeto;
    private JLabel lblTotalIva;
    private JLabel lblTotalGeneral;

    // Nueva paleta Verde Esmeralda / Bosque Oscuro estilo Clipper
    private final Color COLOR_FONDO = new Color(0, 59, 43);          // Verde bosque oscuro (#003B2B)
    private final Color COLOR_PANEL_TABLA = new Color(12, 28, 24);   // Fondo oscuro tabla
    private final Color COLOR_BORDE_MENTA = new Color(85, 255, 153); // Verde menta brillante (#55FF99)
    private final Color COLOR_TEXTO_DORADO = new Color(255, 230, 102); // Ámbar/Dorado suave
    private final Color COLOR_BARRA_INF = new Color(0, 95, 70);      // Verde esmeralda medio
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    public FrmLibroCompras() {
        super("3.- LIBRO DE COMPRAS", false, false, false, false);
        initComponentsManual();
        cargarFechaInicialDesdeParametros();
        ejecutarBusqueda();
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
                new LineBorder(COLOR_BORDE_MENTA, 2),
                " LISTADO DE COMPRAS - FILTROS DE BÚSQUEDA ",
                0, 0, FUENTE_CONSOLA, COLOR_TEXTO_DORADO
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        txtFechaInicial = crearCampoTexto(10);
        txtFechaFinal = crearCampoTexto(10);
        txtNombreProveedor = crearCampoTexto(25);

        panelCampos.add(crearEtiqueta("Fecha Inicial:"));
        panelCampos.add(txtFechaInicial);
        panelCampos.add(crearEtiqueta("Fecha Final:"));
        panelCampos.add(txtFechaFinal);
        panelCampos.add(crearEtiqueta("Proveedor:"));
        panelCampos.add(txtNombreProveedor);

        // Acción Enter en cualquiera de los 3 campos ejecuta la búsqueda al tiro
        txtFechaInicial.addActionListener(e -> {
            ejecutarBusqueda();
            txtFechaFinal.requestFocusInWindow();
            txtFechaFinal.selectAll();
        });
        txtFechaFinal.addActionListener(e -> {
            ejecutarBusqueda();
            txtNombreProveedor.requestFocusInWindow();
            txtNombreProveedor.selectAll();
        });
        txtNombreProveedor.addActionListener(e -> ejecutarBusqueda());

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla de Resultados)
        // ==========================================
        String[] columnas = {"Fecha", "N° Factura", "RUT Prov.", "Nombre Proveedor", "Condición", "Neto", "I.V.A.", "Total"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCompras = new JTable(modeloTabla);
        tablaCompras.setFont(FUENTE_CONSOLA);
        tablaCompras.setBackground(COLOR_PANEL_TABLA);
        tablaCompras.setForeground(Color.WHITE);
        tablaCompras.setGridColor(new Color(30, 75, 60));
        tablaCompras.setRowHeight(25);
        tablaCompras.setSelectionBackground(COLOR_BARRA_INF);
        tablaCompras.setSelectionForeground(COLOR_TEXTO_DORADO);

        tablaCompras.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaCompras.getTableHeader().setBackground(COLOR_FONDO);
        tablaCompras.getTableHeader().setForeground(COLOR_TEXTO_DORADO);

        // Alineación a la derecha para montos numéricos
        DefaultTableCellRenderer derechaRenderer = new DefaultTableCellRenderer();
        derechaRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaCompras.getColumnModel().getColumn(1).setCellRenderer(derechaRenderer);
        tablaCompras.getColumnModel().getColumn(5).setCellRenderer(derechaRenderer);
        tablaCompras.getColumnModel().getColumn(6).setCellRenderer(derechaRenderer);
        tablaCompras.getColumnModel().getColumn(7).setCellRenderer(derechaRenderer);

        // Anchos sugeridos
        tablaCompras.getColumnModel().getColumn(0).setPreferredWidth(105);
        tablaCompras.getColumnModel().getColumn(1).setPreferredWidth(95);
        tablaCompras.getColumnModel().getColumn(2).setPreferredWidth(115);
        tablaCompras.getColumnModel().getColumn(3).setPreferredWidth(290);
        tablaCompras.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollTabla = new JScrollPane(tablaCompras);
        scrollTabla.getViewport().setBackground(COLOR_PANEL_TABLA);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE_MENTA, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Barra Atajos)
        // ==========================================
        JPanel panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(COLOR_FONDO);

        // Barra de Totales
        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(COLOR_FONDO);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDE_MENTA));

        lblTotalNeto = crearEtiqueta("TOTAL NETO: $0");
        lblTotalIva = crearEtiqueta("TOTAL IVA: $0");
        lblTotalGeneral = new JLabel("TOTAL COMPRAS: $0");
        lblTotalGeneral.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalGeneral.setForeground(COLOR_BORDE_MENTA);

        panelTotales.add(lblTotalNeto);
        panelTotales.add(lblTotalIva);
        panelTotales.add(lblTotalGeneral);

        // Barra de Atajos
        JPanel panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        panelAtajos.setBackground(COLOR_BARRA_INF);
        panelAtajos.setBorder(new LineBorder(COLOR_TEXTO_DORADO, 1));

        JLabel lblAtajos = new JLabel("(Enter) Buscar / Filtrar    |    (F5) Limpiar Filtros    |    (Esc) Salir");
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(COLOR_TEXTO_DORADO);
        panelAtajos.add(lblAtajos);

        panelInferiorContenedor.add(panelTotales, BorderLayout.NORTH);
        panelInferiorContenedor.add(panelAtajos, BorderLayout.SOUTH);
        add(panelInferiorContenedor, BorderLayout.SOUTH);

        configurarAtajos();
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(COLOR_TEXTO_DORADO);
        return lbl;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(COLOR_PANEL_TABLA);
        txt.setForeground(COLOR_BORDE_MENTA);
        txt.setCaretColor(COLOR_BORDE_MENTA);
        txt.setBorder(new LineBorder(COLOR_BORDE_MENTA, 1));
        return txt;
    }

    private void cargarFechaInicialDesdeParametros() {
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

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        LibroComprasDAO dao = new LibroComprasDAO();
        List<Object[]> resultados = dao.buscarLibroCompras(
                txtFechaInicial.getText(),
                txtFechaFinal.getText(),
                txtNombreProveedor.getText()
        );

        double sumaNeto = 0;
        double sumaIva = 0;
        double sumaTotal = 0;

        for (Object[] fila : resultados) {
            double neto = (double) fila[5];
            double iva = (double) fila[6];
            double total = (double) fila[7];

            sumaNeto += neto;
            sumaIva += iva;
            sumaTotal += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                fila[3],
                fila[4],
                String.format("$%,.0f", neto),
                String.format("$%,.0f", iva),
                String.format("$%,.0f", total)
            });
        }

        lblTotalNeto.setText(String.format("TOTAL NETO: $%,.0f", sumaNeto));
        lblTotalIva.setText(String.format("TOTAL IVA: $%,.0f", sumaIva));
        lblTotalGeneral.setText(String.format("TOTAL COMPRAS: $%,.0f", sumaTotal));
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
                txtNombreProveedor.setText("");
                ejecutarBusqueda();
                txtFechaInicial.requestFocusInWindow();
            }
        });
    }
}
