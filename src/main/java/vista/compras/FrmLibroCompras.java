package vista.compras;

import com.toedter.calendar.JDateChooser;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class FrmLibroCompras extends JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JDateChooser dcFechaInicial;
    private JDateChooser dcFechaFinal;
    private JTextField txtNombreProveedor;

    private JTable tablaCompras;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalNeto;
    private JLabel lblTotalIva;
    private JLabel lblTotalGeneral;
    private JLabel lblAtajos;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelSuperior, panelInferiorContenedor, panelTotales, panelAtajos;
    private JScrollPane scrollTabla;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTabla, cZebra1, cZebra2, cBordeAcento, cTextoClaro, cBarraInf, cSeleccion, cTextoDorado;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Verde Esmeralda", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmLibroCompras() {
        super("3.- LIBRO DE COMPRAS", false, false, false, false);

        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        cargarFechaInicialDesdeParametros();
        ejecutarBusqueda();

        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
            estilizarDateChooser(dcFechaInicial);
            estilizarDateChooser(dcFechaFinal);
        });
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cPanelTabla = new Color(15, 10, 20);
                cZebra1 = new Color(15, 10, 20);
                cZebra2 = new Color(35, 15, 45);
                cBordeAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cTextoDorado = new Color(255, 220, 130);
                cBarraInf = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTabla = new Color(5, 20, 25);
                cZebra1 = new Color(5, 20, 25);
                cZebra2 = new Color(10, 35, 45);
                cBordeAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cTextoDorado = new Color(150, 245, 255);
                cBarraInf = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Clipper Clásico":
                cFondo = Color.BLACK;
                cPanelTabla = new Color(0, 0, 120);
                cZebra1 = new Color(0, 0, 120);
                cZebra2 = new Color(0, 0, 168);
                cBordeAcento = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cTextoDorado = Color.YELLOW;
                cBarraInf = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelTabla = new Color(25, 12, 10);
                cZebra1 = new Color(25, 12, 10);
                cZebra2 = new Color(50, 22, 15);
                cBordeAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cTextoDorado = new Color(255, 180, 100);
                cBarraInf = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Verde Esmeralda":
            default:
                cFondo = new Color(0, 59, 43);
                cPanelTabla = new Color(12, 28, 24);
                cZebra1 = new Color(12, 28, 24);
                cZebra2 = new Color(20, 45, 38);
                cBordeAcento = new Color(85, 255, 153);
                cTextoClaro = Color.WHITE;
                cTextoDorado = new Color(255, 230, 102);
                cBarraInf = new Color(0, 95, 70);
                cSeleccion = new Color(30, 110, 85);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        String nuevoTema = listaTemas[indiceTemaActual];
        aplicarTema(nuevoTema);

        getContentPane().setBackground(cFondo);

        if (panelSuperior != null) {
            panelSuperior.setBackground(cFondo);
            panelSuperior.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2),
                    " LISTADO DE COMPRAS - FILTROS DE BÚSQUEDA ",
                    0, 0, FUENTE_CONSOLA, cTextoDorado
            ));
        }

        if (tablaCompras != null) {
            tablaCompras.setBackground(cPanelTabla);
            tablaCompras.setForeground(cTextoClaro);
            tablaCompras.setGridColor(cBordeAcento);
            tablaCompras.setSelectionBackground(cBarraInf);
            tablaCompras.setSelectionForeground(cTextoDorado);
            tablaCompras.getTableHeader().setBackground(cFondo);
            tablaCompras.getTableHeader().setForeground(cTextoDorado);
            tablaCompras.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cPanelTabla);
            scrollTabla.setBorder(new LineBorder(cBordeAcento, 1));
        }

        if (panelInferiorContenedor != null) {
            panelInferiorContenedor.setBackground(cFondo);
        }

        if (panelTotales != null) {
            panelTotales.setBackground(cFondo);
            panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeAcento));
        }

        if (lblTotalGeneral != null) {
            lblTotalGeneral.setForeground(cBordeAcento);
        }

        if (panelAtajos != null) {
            panelAtajos.setBackground(cBarraInf);
            panelAtajos.setBorder(new LineBorder(cTextoDorado, 1));
        }

        estilizarDateChooser(dcFechaInicial);
        estilizarDateChooser(dcFechaFinal);
        estilizarCampoDinamico(txtNombreProveedor);

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void estilizarCampoDinamico(JTextField txt) {
        if (txt != null) {
            txt.setBackground(cPanelTabla);
            txt.setForeground(cBordeAcento);
            txt.setCaretColor(cBordeAcento);
            txt.setBorder(new LineBorder(cBordeAcento, 1));
        }
    }

    private JDateChooser crearDateChooserConsola(LocalDate fechaInicial, int anchoPx) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("dd/MM/yyyy");
        chooser.setFont(FUENTE_CONSOLA);
        chooser.setPreferredSize(new Dimension(anchoPx, 26));
        if (fechaInicial != null) {
            chooser.setDate(Date.from(fechaInicial.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        estilizarDateChooser(chooser);
        return chooser;
    }

    private void estilizarDateChooser(JDateChooser chooser) {
        if (chooser == null) {
            return;
        }
        chooser.setBackground(cFondo);
        chooser.setForeground(Color.WHITE);
        chooser.setFont(FUENTE_CONSOLA);

        Component editor = chooser.getDateEditor().getUiComponent();
        if (editor instanceof JComponent jcomp) {
            jcomp.setBackground(cPanelTabla);
            jcomp.setForeground(Color.WHITE);
            jcomp.setFont(FUENTE_CONSOLA);
            jcomp.setBorder(new LineBorder(cBordeAcento, 1));
        }
        if (editor instanceof JTextField tf) {
            tf.setBackground(cPanelTabla);
            tf.setForeground(Color.WHITE);
            tf.setDisabledTextColor(Color.WHITE);
            tf.setCaretColor(cBordeAcento);
            tf.repaint();
        }
    }

    private String obtenerTextoFecha(JDateChooser chooser) {
        if (chooser == null || chooser.getDate() == null) {
            return "";
        }
        LocalDate ld = Instant.ofEpochMilli(chooser.getDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return ld.format(FORMATO_FECHA);
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("(Enter) Buscar / Filtrar    |    (F5) Limpiar    |    (F6) Tema: " + temaActual + "    |    (Esc) Salir");
        }
    }

    private void initComponentsManual() {
        setSize(1100, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(cFondo);
        setLayout(new BorderLayout(0, 6));

        // ==========================================
        // 1. PANEL SUPERIOR (Filtros estilo Clipper)
        // ==========================================
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(cFondo);
        panelSuperior.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2),
                " LISTADO DE COMPRAS - FILTROS DE BÚSQUEDA ",
                0, 0, FUENTE_CONSOLA, cTextoDorado
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        LocalDate hoy = LocalDate.now();
        dcFechaInicial = crearDateChooserConsola(hoy, 145);
        dcFechaFinal = crearDateChooserConsola(hoy, 145);
        txtNombreProveedor = crearCampoTexto(25);

        dcFechaInicial.addPropertyChangeListener("date", evt -> {
            estilizarDateChooser(dcFechaInicial);
            ejecutarBusqueda();
        });
        dcFechaFinal.addPropertyChangeListener("date", evt -> {
            estilizarDateChooser(dcFechaFinal);
            ejecutarBusqueda();
        });
        txtNombreProveedor.addActionListener(e -> ejecutarBusqueda());

        panelCampos.add(crearEtiqueta("Fecha Inicial:"));
        panelCampos.add(dcFechaInicial);
        panelCampos.add(crearEtiqueta("Fecha Final:"));
        panelCampos.add(dcFechaFinal);
        panelCampos.add(crearEtiqueta("Proveedor:"));
        panelCampos.add(txtNombreProveedor);

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla de Resultados con Cebra)
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
        tablaCompras.setBackground(cPanelTabla);
        tablaCompras.setForeground(cTextoClaro);
        tablaCompras.setGridColor(cBordeAcento);
        tablaCompras.setRowHeight(25);
        tablaCompras.setSelectionBackground(cBarraInf);
        tablaCompras.setSelectionForeground(cTextoDorado);

        tablaCompras.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaCompras.getTableHeader().setBackground(cFondo);
        tablaCompras.getTableHeader().setForeground(cTextoDorado);

        DefaultTableCellRenderer renderizadorCebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(cBarraInf);
                    c.setForeground(cTextoDorado);
                } else {
                    c.setBackground(row % 2 == 0 ? cZebra1 : cZebra2);
                    c.setForeground(cTextoClaro);
                }

                if (column == 1 || column >= 5) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tablaCompras.getColumnCount(); i++) {
            tablaCompras.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaCompras.getColumnModel().getColumn(0).setPreferredWidth(105);
        tablaCompras.getColumnModel().getColumn(1).setPreferredWidth(95);
        tablaCompras.getColumnModel().getColumn(2).setPreferredWidth(115);
        tablaCompras.getColumnModel().getColumn(3).setPreferredWidth(290);
        tablaCompras.getColumnModel().getColumn(4).setPreferredWidth(100);

        scrollTabla = new JScrollPane(tablaCompras);
        scrollTabla.getViewport().setBackground(cPanelTabla);
        scrollTabla.setBorder(new LineBorder(cBordeAcento, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Barra Atajos)
        // ==========================================
        panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(cFondo);

        panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(cFondo);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeAcento));

        lblTotalNeto = crearEtiqueta("TOTAL NETO: $0");
        lblTotalIva = crearEtiqueta("TOTAL IVA: $0");
        lblTotalGeneral = new JLabel("TOTAL COMPRAS: $0");
        lblTotalGeneral.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalGeneral.setForeground(cBordeAcento);

        panelTotales.add(lblTotalNeto);
        panelTotales.add(lblTotalIva);
        panelTotales.add(lblTotalGeneral);

        panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        panelAtajos.setBackground(cBarraInf);
        panelAtajos.setBorder(new LineBorder(cTextoDorado, 1));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(cTextoDorado);
        actualizarTextoAtajos();
        panelAtajos.add(lblAtajos);

        panelInferiorContenedor.add(panelTotales, BorderLayout.NORTH);
        panelInferiorContenedor.add(panelAtajos, BorderLayout.SOUTH);
        add(panelInferiorContenedor, BorderLayout.SOUTH);

        configurarAtajos();
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(cTextoDorado);
        return lbl;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(cPanelTabla);
        txt.setForeground(cBordeAcento);
        txt.setCaretColor(cBordeAcento);
        txt.setBorder(new LineBorder(cBordeAcento, 1));
        return txt;
    }

    private void cargarFechaInicialDesdeParametros() {
        String fechaDefectoStr = LocalDate.now().format(FORMATO_FECHA);
        try (Connection con = Conexion.getConnection()) {
            ParametrosDAO pDao = new ParametrosDAO();
            ParametrosDTO param = pDao.obtenerParametros(con);
            if (param.getFecha() != null && !param.getFecha().trim().isEmpty()) {
                fechaDefectoStr = param.getFecha().trim();
            }
        } catch (Exception ignored) {
        }

        Date fechaDefecto = new Date();
        try {
            LocalDate ld = LocalDate.parse(fechaDefectoStr, FORMATO_FECHA);
            fechaDefecto = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        dcFechaInicial.setDate(fechaDefecto);
        dcFechaFinal.setDate(fechaDefecto);
        estilizarDateChooser(dcFechaInicial);
        estilizarDateChooser(dcFechaFinal);
    }

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        LibroComprasDAO dao = new LibroComprasDAO();
        List<Object[]> resultados = dao.buscarLibroCompras(
                obtenerTextoFecha(dcFechaInicial),
                obtenerTextoFecha(dcFechaFinal),
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
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dcFechaInicial.setDate(null);
                dcFechaFinal.setDate(null);
                estilizarDateChooser(dcFechaInicial);
                estilizarDateChooser(dcFechaFinal);
                txtNombreProveedor.setText("");
                ejecutarBusqueda();
                txtNombreProveedor.requestFocusInWindow();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclar_tema");
        root.getActionMap().put("ciclar_tema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }
}
