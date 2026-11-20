package vista.compras;

import com.toedter.calendar.JDateChooser;
import dao.IngresoCompraDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class FrmIngresoCompra extends JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JDateChooser dcFechaInicial;
    private JDateChooser dcFechaFinal;
    private JTextField txtBuscar;

    private JTable tablaCompras;
    private DefaultTableModel modeloTabla;

    private JLabel lblTotalUnidades;
    private JLabel lblTotalMonto;
    private JLabel lblAtajos;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelSuperior, panelInferiorContenedor, panelTotales, panelAtajos;
    private JScrollPane scrollTabla;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTabla, cZebra1, cZebra2, cBordeNeon, cTextoClaro, cBarraInf, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmIngresoCompra() {
        super("2.- INGRESO DE COMPRAS", false, false, false, false);

        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        cargarFechasPorDefecto();
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
                cBordeNeon = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cBarraInf = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTabla = new Color(5, 20, 25);
                cZebra1 = new Color(5, 20, 25);
                cZebra2 = new Color(10, 35, 45);
                cBordeNeon = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraInf = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Clipper Clásico":
                cFondo = Color.BLACK;
                cPanelTabla = new Color(0, 0, 120);
                cZebra1 = new Color(0, 0, 120);
                cZebra2 = new Color(0, 0, 168);
                cBordeNeon = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cBarraInf = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelTabla = new Color(25, 12, 10);
                cZebra1 = new Color(25, 12, 10);
                cZebra2 = new Color(50, 22, 15);
                cBordeNeon = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cBarraInf = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Esmeralda Terminal":
            default:
                cFondo = new Color(11, 58, 26);
                cPanelTabla = new Color(5, 28, 12);
                cZebra1 = new Color(5, 28, 12);
                cZebra2 = new Color(12, 45, 20);
                cBordeNeon = new Color(0, 255, 136);
                cTextoClaro = new Color(220, 255, 235);
                cBarraInf = new Color(20, 90, 45);
                cSeleccion = new Color(30, 120, 65);
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
                    new LineBorder(cBordeNeon, 2),
                    " INGRESO DE COMPRAS - FILTROS DE CONSULTA ",
                    0, 0, FUENTE_CONSOLA, cBordeNeon
            ));
        }

        if (tablaCompras != null) {
            tablaCompras.setBackground(cPanelTabla);
            tablaCompras.setForeground(cTextoClaro);
            tablaCompras.setGridColor(cBordeNeon);
            tablaCompras.setSelectionBackground(cBarraInf);
            tablaCompras.setSelectionForeground(Color.YELLOW);
            tablaCompras.getTableHeader().setBackground(cFondo);
            tablaCompras.getTableHeader().setForeground(cBordeNeon);
            tablaCompras.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cPanelTabla);
            scrollTabla.setBorder(new LineBorder(cBordeNeon, 1));
        }

        if (panelInferiorContenedor != null) {
            panelInferiorContenedor.setBackground(cFondo);
        }

        if (panelTotales != null) {
            panelTotales.setBackground(cFondo);
            panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeNeon));
        }

        if (lblTotalMonto != null) {
            lblTotalMonto.setForeground(cBordeNeon);
        }

        if (panelAtajos != null) {
            panelAtajos.setBackground(cBarraInf);
            panelAtajos.setBorder(new LineBorder(cBordeNeon, 1));
        }

        estilizarDateChooser(dcFechaInicial);
        estilizarDateChooser(dcFechaFinal);
        estilizarCampoDinamico(txtBuscar);

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void estilizarCampoDinamico(JTextField txt) {
        if (txt != null) {
            txt.setBackground(cPanelTabla);
            txt.setForeground(cBordeNeon);
            txt.setCaretColor(cBordeNeon);
            txt.setBorder(new LineBorder(cBordeNeon, 1));
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
            jcomp.setBorder(new LineBorder(cBordeNeon, 1));
        }
        if (editor instanceof JTextField tf) {
            tf.setBackground(cPanelTabla);
            tf.setForeground(Color.WHITE);
            tf.setDisabledTextColor(Color.WHITE);
            tf.setCaretColor(cBordeNeon);
            tf.repaint();
        }
    }

    private void asignarFechaChooser(JDateChooser chooser, String fechaDdMmYyyy) {
        if (chooser == null) {
            return;
        }
        if (fechaDdMmYyyy != null && !fechaDdMmYyyy.trim().isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(fechaDdMmYyyy.trim(), FORMATO_FECHA);
                chooser.setDate(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                estilizarDateChooser(chooser);
                return;
            } catch (DateTimeParseException ignored) {
            }
        }
        chooser.setDate(new Date());
        estilizarDateChooser(chooser);
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
            lblAtajos.setText("<html>(Ins) Nuevo | (F2) Editar | (Supr) Eliminar | (F5) Limpiar | (F6) Tema: " + temaActual + " | (Esc) Salir</html>");
        }
    }

    private void initComponentsManual() {
        setSize(1150, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(cFondo);
        setLayout(new BorderLayout(0, 6));

        // ==========================================
        // 1. PANEL SUPERIOR (Filtros)
        // ==========================================
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(cFondo);
        panelSuperior.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeNeon, 2),
                " INGRESO DE COMPRAS - FILTROS DE CONSULTA ",
                0, 0, FUENTE_CONSOLA, cBordeNeon
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        LocalDate hoy = LocalDate.now();
        dcFechaInicial = crearDateChooserConsola(hoy, 145);
        dcFechaFinal = crearDateChooserConsola(hoy, 145);
        txtBuscar = crearCampoTexto(22);

        dcFechaInicial.addPropertyChangeListener("date", evt -> {
            estilizarDateChooser(dcFechaInicial);
            ejecutarBusqueda();
        });
        dcFechaFinal.addPropertyChangeListener("date", evt -> {
            estilizarDateChooser(dcFechaFinal);
            ejecutarBusqueda();
        });
        txtBuscar.addActionListener(e -> ejecutarBusqueda());

        panelCampos.add(crearEtiqueta("F. Inicial:"));
        panelCampos.add(dcFechaInicial);
        panelCampos.add(crearEtiqueta("F. Final:"));
        panelCampos.add(dcFechaFinal);
        panelCampos.add(crearEtiqueta("Factura/Proveedor/Producto:"));
        panelCampos.add(txtBuscar);

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (Tabla con Filas Cebra)
        // ==========================================
        String[] columnas = {"Fecha", "N° Factura", "Proveedor", "# Barra", "Descripción Producto", "Cantidad", "Costo Unit. ($)", "Total Compra ($)"};
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
        tablaCompras.setGridColor(cBordeNeon);
        tablaCompras.setRowHeight(28);
        tablaCompras.setSelectionBackground(cBarraInf);
        tablaCompras.setSelectionForeground(Color.YELLOW);

        tablaCompras.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaCompras.getTableHeader().setBackground(cFondo);
        tablaCompras.getTableHeader().setForeground(cBordeNeon);

        DefaultTableCellRenderer renderizadorCebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(cBarraInf);
                    c.setForeground(Color.YELLOW);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(cZebra1);
                    } else {
                        c.setBackground(cZebra2);
                    }
                    c.setForeground(cTextoClaro);
                }

                if (column >= 5) {
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

        tablaCompras.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaCompras.getColumnModel().getColumn(1).setPreferredWidth(110);
        tablaCompras.getColumnModel().getColumn(2).setPreferredWidth(180);
        tablaCompras.getColumnModel().getColumn(3).setPreferredWidth(110);
        tablaCompras.getColumnModel().getColumn(4).setPreferredWidth(280);
        tablaCompras.getColumnModel().getColumn(5).setPreferredWidth(90);
        tablaCompras.getColumnModel().getColumn(6).setPreferredWidth(120);
        tablaCompras.getColumnModel().getColumn(7).setPreferredWidth(130);

        scrollTabla = new JScrollPane(tablaCompras);
        scrollTabla.getViewport().setBackground(cPanelTabla);
        scrollTabla.setBorder(new LineBorder(cBordeNeon, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(cFondo);

        panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(cFondo);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeNeon));

        lblTotalUnidades = crearEtiqueta("TOTAL UNIDADES: 0");
        lblTotalMonto = new JLabel("TOTAL COMPRAS: $0");
        lblTotalMonto.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalMonto.setForeground(cBordeNeon);

        panelTotales.add(lblTotalUnidades);
        panelTotales.add(lblTotalMonto);

        panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajos.setBackground(cBarraInf);
        panelAtajos.setBorder(new LineBorder(cBordeNeon, 1));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(cTextoClaro);
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
        lbl.setForeground(cTextoClaro);
        return lbl;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(FUENTE_CONSOLA);
        txt.setBackground(cPanelTabla);
        txt.setForeground(cBordeNeon);
        txt.setCaretColor(cBordeNeon);
        txt.setBorder(new LineBorder(cBordeNeon, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(Math.min(255, cPanelTabla.getRed() + 30), Math.min(255, cPanelTabla.getGreen() + 30), Math.min(255, cPanelTabla.getBlue() + 30)));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(cPanelTabla);
                txt.setBorder(new LineBorder(cBordeNeon, 1));
            }
        });

        return txt;
    }

    private void cargarFechasPorDefecto() {
        Date hoy = new Date();
        dcFechaInicial.setDate(hoy);
        dcFechaFinal.setDate(hoy);
        estilizarDateChooser(dcFechaInicial);
        estilizarDateChooser(dcFechaFinal);
    }

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        IngresoCompraDAO dao = new IngresoCompraDAO();
        List<Object[]> resultados = dao.buscarCompras(
                obtenerTextoFecha(dcFechaInicial),
                obtenerTextoFecha(dcFechaFinal),
                txtBuscar.getText()
        );

        double sumaUnidades = 0;
        double sumaMonto = 0;

        for (Object[] fila : resultados) {
            double cant = (double) fila[5];
            double costo = (double) fila[6];
            double total = (double) fila[7];

            sumaUnidades += cant;
            sumaMonto += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                fila[3],
                fila[4],
                String.format("%,.0f", cant),
                String.format("$%,.0f", costo),
                String.format("$%,.0f", total)
            });
        }

        lblTotalUnidades.setText(String.format("TOTAL UNIDADES: %,.0f", sumaUnidades));
        lblTotalMonto.setText(String.format("TOTAL COMPRAS: $%,.0f", sumaMonto));
    }

    private void abrirFormularioMantenimiento(boolean esEdicion) {
        String fechaActual = obtenerTextoFecha(dcFechaInicial);
        if (fechaActual.isEmpty()) {
            fechaActual = LocalDate.now().format(FORMATO_FECHA);
        }
        String fechaSel = "";
        String facturaSel = "";
        String proveeSel = "";
        String barraSel = "";
        String nombreSel = "";
        String cantSel = "1";
        String costoSel = "0";

        if (esEdicion) {
            int filaVista = tablaCompras.getSelectedRow();
            if (filaVista == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int fila = tablaCompras.convertRowIndexToModel(filaVista);
            fechaSel = (String) modeloTabla.getValueAt(fila, 0);
            facturaSel = (String) modeloTabla.getValueAt(fila, 1);
            proveeSel = (String) modeloTabla.getValueAt(fila, 2);
            barraSel = (String) modeloTabla.getValueAt(fila, 3);
            nombreSel = (String) modeloTabla.getValueAt(fila, 4);
            cantSel = modeloTabla.getValueAt(fila, 5).toString().replace(".", "").replace(",", "").trim();
            costoSel = modeloTabla.getValueAt(fila, 6).toString().replace("$", "").replace(".", "").replace(",", "").trim();
        }

        final String facturaOriginal = facturaSel;
        final String barraOriginal = barraSel;

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), esEdicion ? "EDITAR INGRESO DE COMPRA" : "NUEVO INGRESO DE COMPRA", true);
        dialogo.setSize(500, 430);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(cFondo);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(cFondo);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeNeon, 2),
                esEdicion ? " MODIFICAR DATOS DE COMPRA " : " REGISTRAR NUEVA ADQUISICIÓN ",
                0, 0, FUENTE_CONSOLA, cBordeNeon
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JDateChooser dcFecha = crearDateChooserConsola(null, 150);
        asignarFechaChooser(dcFecha, esEdicion ? fechaSel : fechaActual);

        JTextField txtFact = crearCampoTexto(14);
        txtFact.setText(facturaSel);
        JTextField txtProv = crearCampoTexto(22);
        txtProv.setText(proveeSel);
        JTextField txtB = crearCampoTexto(14);
        txtB.setText(barraSel);
        JTextField txtN = crearCampoTexto(24);
        txtN.setText(nombreSel);
        JTextField txtC = crearCampoTexto(14);
        txtC.setText(cantSel);
        JTextField txtCost = crearCampoTexto(14);
        txtCost.setText(costoSel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentro.add(crearEtiqueta("Fecha:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(dcFecha, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentro.add(crearEtiqueta("N° Factura:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtFact, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentro.add(crearEtiqueta("Proveedor:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtProv, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(crearEtiqueta("Código de Barra:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCentro.add(crearEtiqueta("Nombre Producto:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtN, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCentro.add(crearEtiqueta("Cantidad:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtC, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panelCentro.add(crearEtiqueta("Costo Unitario ($):"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCost, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(cBarraInf);
        panelBotones.setBorder(new LineBorder(cBordeNeon, 1));

        JButton btnGuardar = new JButton("Guardar [F3]");
        btnGuardar.setFont(FUENTE_CONSOLA);
        JButton btnCancelar = new JButton("Cancelar [Esc]");
        btnCancelar.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable guardarAccion = () -> {
            try {
                String f = obtenerTextoFecha(dcFecha);
                if (f.isEmpty()) {
                    JOptionPane.showMessageDialog(dialogo, "Seleccione una fecha válida.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String fact = txtFact.getText().trim();
                String prov = txtProv.getText().trim();
                String cond = "0";
                String b = txtB.getText().trim();
                String n = txtN.getText().trim();
                String cant = txtC.getText().trim().replace(",", ".");
                String cost = txtCost.getText().trim().replace(",", ".");

                IngresoCompraDAO dao = new IngresoCompraDAO();
                boolean exito;
                if (esEdicion) {
                    exito = dao.actualizarCompra(facturaOriginal, barraOriginal, f, fact, prov, cond, b, n, cant, cost);
                } else {
                    exito = dao.insertarCompra(f, fact, prov, cond, b, n, cant, cost);
                }

                if (exito) {
                    dialogo.dispose();
                    ejecutarBusqueda();
                } else {
                    JOptionPane.showMessageDialog(dialogo, "Error al guardar el registro en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(dialogo, "Verifique que los valores sean válidos.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnGuardar.addActionListener(e -> guardarAccion.run());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> guardarAccion.run(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void eliminarRegistroSeleccionado() {
        int filaVista = tablaCompras.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un registro de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaCompras.convertRowIndexToModel(filaVista);
        String factura = (String) modeloTabla.getValueAt(fila, 1);
        String barra = (String) modeloTabla.getValueAt(fila, 3);
        String nombre = (String) modeloTabla.getValueAt(fila, 4);

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), "CONFIRMAR ELIMINACIÓN DE COMPRA", true);
        dialogo.setSize(480, 220);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(cFondo);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(cFondo);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeNeon, 2),
                " ADVERTENCIA DE ELIMINACIÓN ",
                0, 0, FUENTE_CONSOLA, cBordeNeon
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblPregunta = new JLabel("¿Desea eliminar la compra seleccionada?");
        lblPregunta.setFont(FUENTE_CONSOLA);
        lblPregunta.setForeground(Color.YELLOW);
        panelCentro.add(lblPregunta, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 15, 10, 15);
        JLabel lblDetalle = new JLabel("<html><center>" + nombre + "<br><font color='" + colorToHex(cBordeNeon) + "'>[Factura: " + factura + "]</font></center></html>");
        lblDetalle.setFont(new Font("Consolas", Font.BOLD, 13));
        lblDetalle.setForeground(cTextoClaro);
        panelCentro.add(lblDetalle, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(cBarraInf);
        panelBotones.setBorder(new LineBorder(cBordeNeon, 1));

        JButton btnSi = new JButton("Sí [Enter]");
        btnSi.setFont(FUENTE_CONSOLA);
        JButton btnNo = new JButton("No [Esc]");
        btnNo.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable accionEliminar = () -> {
            IngresoCompraDAO dao = new IngresoCompraDAO();
            if (dao.eliminarCompra(factura, barra)) {
                dialogo.dispose();
                ejecutarBusqueda();
            } else {
                JOptionPane.showMessageDialog(dialogo, "No se pudo eliminar el registro de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                dialogo.dispose();
            }
        };

        btnSi.addActionListener(e -> accionEliminar.run());
        btnNo.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> accionEliminar.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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

        // [F5] Limpiar búsqueda
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dcFechaInicial.setDate(null);
                dcFechaFinal.setDate(null);
                estilizarDateChooser(dcFechaInicial);
                estilizarDateChooser(dcFechaFinal);
                txtBuscar.setText("");
                ejecutarBusqueda();
                txtBuscar.requestFocusInWindow();
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

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "nuevo");
        root.getActionMap().put("nuevo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(false);
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");
        tablaCompras.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");

        AbstractAction accionEditar = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(true);
            }
        };
        root.getActionMap().put("editar", accionEditar);
        tablaCompras.getActionMap().put("editar", accionEditar);

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarRegistroSeleccionado();
            }
        });
    }
}
