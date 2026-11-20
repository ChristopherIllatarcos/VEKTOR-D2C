package vista.ventas;

import com.toedter.calendar.JDateChooser;
import dao.HojaRutaDAO;
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
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class FrmHojaRuta extends JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JDateChooser dcFechaInicial;
    private JDateChooser dcFechaFinal;
    private JTextField txtBuscar;

    private JTable tablaRutas;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalRegistros;
    private JLabel lblTotalMonto;
    private JLabel lblAtajos;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelSuperior, panelCentroTabla, panelInferiorContenedor, panelTotales, panelAtajos;
    private JScrollPane scrollTabla;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTablas, cZebra1, cZebra2, cAcento, cTextoClaro, cBarraInf, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmHojaRuta() {
        super("3.- HOJAS DE RUTA (DESPACHOS)", false, false, false, false);

        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        try {
            java.net.URL urlIcono = getClass().getResource("/vektor/cubo-azul-16.png");
            if (urlIcono != null) {
                setFrameIcon(new ImageIcon(urlIcono));
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
        cargarFechasPorDefecto();
        ejecutarBusqueda();
        SwingUtilities.invokeLater(() -> {
            utilidades.IconoUtils.aplicarIcono(this);
        });
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cPanelTablas = new Color(15, 10, 20);
                cZebra1 = new Color(15, 10, 20);
                cZebra2 = new Color(35, 15, 45);
                cAcento = new Color(255, 110, 220);
                cTextoClaro = new Color(255, 230, 255);
                cBarraInf = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTablas = new Color(5, 20, 25);
                cZebra1 = new Color(5, 20, 25);
                cZebra2 = new Color(10, 35, 45);
                cAcento = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraInf = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelTablas = new Color(5, 25, 18);
                cZebra1 = new Color(5, 25, 18);
                cZebra2 = new Color(12, 45, 30);
                cAcento = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cBarraInf = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Clipper Clásico":
                cFondo = Color.BLACK;
                cPanelTablas = new Color(0, 0, 120);
                cZebra1 = new Color(0, 0, 120);
                cZebra2 = new Color(0, 0, 168);
                cAcento = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cBarraInf = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
            case "Naranja Fuego":
            default:
                cFondo = new Color(15, 10, 8);
                cPanelTablas = new Color(25, 12, 10);
                cZebra1 = new Color(25, 12, 10);
                cZebra2 = new Color(50, 22, 15);
                cAcento = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cBarraInf = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
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
                    new LineBorder(cAcento, 2),
                    " CONTROL DE HOJAS DE RUTA Y DESPACHOS - FILTROS ",
                    0, 0, FUENTE_CONSOLA, cAcento
            ));
        }

        if (panelCentroTabla != null) {
            panelCentroTabla.setBackground(cFondo);
        }

        if (tablaRutas != null) {
            tablaRutas.setBackground(cPanelTablas);
            tablaRutas.setForeground(cTextoClaro);
            tablaRutas.setGridColor(cAcento);
            tablaRutas.setSelectionBackground(cBarraInf);
            tablaRutas.setSelectionForeground(Color.YELLOW);
            tablaRutas.getTableHeader().setBackground(cFondo);
            tablaRutas.getTableHeader().setForeground(cAcento);
            tablaRutas.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cPanelTablas);
            scrollTabla.setBorder(new LineBorder(cAcento, 1));
        }

        if (panelInferiorContenedor != null) {
            panelInferiorContenedor.setBackground(cFondo);
        }

        if (panelTotales != null) {
            panelTotales.setBackground(cFondo);
            panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cAcento));
        }

        if (lblTotalMonto != null) {
            lblTotalMonto.setForeground(cAcento);
        }

        if (panelAtajos != null) {
            panelAtajos.setBackground(cBarraInf);
            panelAtajos.setBorder(new LineBorder(cAcento, 1));
        }

        estilizarDateChooser(dcFechaInicial);
        estilizarDateChooser(dcFechaFinal);
        estilizarCampoTextoDinamico(txtBuscar);

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void estilizarCampoTextoDinamico(JTextField txt) {
        if (txt != null) {
            txt.setBackground(cPanelTablas);
            txt.setForeground(cAcento);
            txt.setCaretColor(cAcento);
            txt.setBorder(new LineBorder(cAcento, 1));
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
            jcomp.setBackground(cPanelTablas);
            jcomp.setForeground(Color.WHITE);
            jcomp.setFont(FUENTE_CONSOLA);
            jcomp.setBorder(new LineBorder(cAcento, 1));
        }
        if (editor instanceof JTextField tf) {
            tf.setBackground(cPanelTablas);
            tf.setForeground(Color.WHITE);
            tf.setDisabledTextColor(Color.WHITE);
            tf.setCaretColor(cAcento);
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
                return;
            } catch (DateTimeParseException ignored) {
            }
        }
        chooser.setDate(new Date());
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
            lblAtajos.setText("<html>(Ins) Nuevo | (F2) Editar | (F3) Imprimir | (Supr) Eliminar | (F5) Refrescar | (F6) Tema: " + temaActual + " | (Esc) Salir</html>");
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
                new LineBorder(cAcento, 2),
                " CONTROL DE HOJAS DE RUTA Y DESPACHOS - FILTROS ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelCampos.setOpaque(false);

        LocalDate hoy = LocalDate.now();
        dcFechaInicial = crearDateChooserConsola(hoy, 145);
        dcFechaFinal = crearDateChooserConsola(hoy, 145);
        txtBuscar = crearCampoTexto(22);

        dcFechaInicial.addPropertyChangeListener("date", evt -> ejecutarBusqueda());
        dcFechaFinal.addPropertyChangeListener("date", evt -> ejecutarBusqueda());
        txtBuscar.addActionListener(e -> ejecutarBusqueda());

        panelCampos.add(crearEtiqueta("F. Inicial:"));
        panelCampos.add(dcFechaInicial);
        panelCampos.add(crearEtiqueta("F. Final:"));
        panelCampos.add(dcFechaFinal);
        panelCampos.add(crearEtiqueta("Guía / Cliente / Chofer / Patente:"));
        panelCampos.add(txtBuscar);

        panelSuperior.add(panelCampos, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLA CENTRAL (Filas Cebra Transparentes)
        // ==========================================
        String[] columnas = {"Fecha", "N° Guía", "N° Despacho", "Cliente", "Patente Vehículo", "Chofer Asignado", "Total Venta ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaRutas = new JTable(modeloTabla);
        tablaRutas.setFont(FUENTE_CONSOLA);
        tablaRutas.setBackground(cPanelTablas);
        tablaRutas.setForeground(cTextoClaro);
        tablaRutas.setGridColor(cAcento);
        tablaRutas.setRowHeight(28);
        tablaRutas.setSelectionBackground(cBarraInf);
        tablaRutas.setSelectionForeground(Color.YELLOW);

        tablaRutas.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaRutas.getTableHeader().setBackground(cFondo);
        tablaRutas.getTableHeader().setForeground(cAcento);

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

                if (column == 6) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };

        for (int i = 0; i < tablaRutas.getColumnCount(); i++) {
            tablaRutas.getColumnModel().getColumn(i).setCellRenderer(renderizadorCebra);
        }

        tablaRutas.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaRutas.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaRutas.getColumnModel().getColumn(2).setPreferredWidth(110);
        tablaRutas.getColumnModel().getColumn(3).setPreferredWidth(260);
        tablaRutas.getColumnModel().getColumn(4).setPreferredWidth(130);
        tablaRutas.getColumnModel().getColumn(5).setPreferredWidth(180);
        tablaRutas.getColumnModel().getColumn(6).setPreferredWidth(130);

        scrollTabla = new JScrollPane(tablaRutas);
        scrollTabla.getViewport().setBackground(cPanelTablas);
        scrollTabla.setBorder(new LineBorder(cAcento, 1));

        panelCentroTabla = new JPanel(new BorderLayout());
        panelCentroTabla.setBackground(cFondo);
        panelCentroTabla.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelCentroTabla.add(scrollTabla, BorderLayout.CENTER);
        add(panelCentroTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(cFondo);

        panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(cFondo);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cAcento));

        lblTotalRegistros = crearEtiqueta("TOTAL GUIAS: 0");
        lblTotalMonto = new JLabel("MONTO TOTAL: $0");
        lblTotalMonto.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalMonto.setForeground(cAcento);

        panelTotales.add(lblTotalRegistros);
        panelTotales.add(lblTotalMonto);

        panelAtajos = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajos.setBackground(cBarraInf);
        panelAtajos.setBorder(new LineBorder(cAcento, 1));

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
        txt.setBackground(cPanelTablas);
        txt.setForeground(cAcento);
        txt.setCaretColor(cAcento);
        txt.setBorder(new LineBorder(cAcento, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(Math.min(255, cPanelTablas.getRed() + 30), Math.min(255, cPanelTablas.getGreen() + 30), Math.min(255, cPanelTablas.getBlue() + 30)));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(cPanelTablas);
                txt.setBorder(new LineBorder(cAcento, 1));
            }
        });

        return txt;
    }

    private void cargarFechasPorDefecto() {
        Date hoy = new Date();
        dcFechaInicial.setDate(hoy);
        dcFechaFinal.setDate(hoy);
    }

    private void ejecutarBusqueda() {
        modeloTabla.setRowCount(0);
        HojaRutaDAO dao = new HojaRutaDAO();
        List<Object[]> resultados = dao.buscarHojasRuta(
                obtenerTextoFecha(dcFechaInicial),
                obtenerTextoFecha(dcFechaFinal),
                txtBuscar.getText()
        );

        double sumaMonto = 0;

        for (Object[] fila : resultados) {
            double total = (double) fila[6];
            sumaMonto += total;

            modeloTabla.addRow(new Object[]{
                fila[0],
                fila[1],
                fila[2],
                fila[3],
                fila[4],
                fila[5],
                String.format("$%,.0f", total)
            });
        }

        lblTotalRegistros.setText(String.format("TOTAL GUIAS: %d", resultados.size()));
        lblTotalMonto.setText(String.format("MONTO TOTAL: $%,.0f", sumaMonto));
    }

    private void abrirFormularioMantenimiento(boolean esEdicion) {
        String fechaActual = obtenerTextoFecha(dcFechaInicial);
        if (fechaActual.isEmpty()) {
            fechaActual = LocalDate.now().format(FORMATO_FECHA);
        }
        String fechaSel = "";
        String guiaSel = "";
        String despachoSel = "";
        String clienteSel = "";
        String patenteSel = "";
        String choferSel = "";

        if (esEdicion) {
            int filaVista = tablaRutas.getSelectedRow();
            if (filaVista == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una hoja de ruta de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int fila = tablaRutas.convertRowIndexToModel(filaVista);
            fechaSel = (String) modeloTabla.getValueAt(fila, 0);
            guiaSel = (String) modeloTabla.getValueAt(fila, 1);
            despachoSel = (String) modeloTabla.getValueAt(fila, 2);
            clienteSel = (String) modeloTabla.getValueAt(fila, 3);
            patenteSel = (String) modeloTabla.getValueAt(fila, 4);
            choferSel = (String) modeloTabla.getValueAt(fila, 5);
        }

        final String guiaOriginal = guiaSel;

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), esEdicion ? "EDITAR HOJA DE RUTA" : "NUEVA HOJA DE RUTA", true);
        dialogo.setSize(520, 420);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(cFondo);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(cFondo);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cAcento, 2),
                esEdicion ? " MODIFICAR DATOS DE DESPACHO " : " REGISTRAR NUEVA RUTA ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JDateChooser dcFecha = crearDateChooserConsola(null, 150);
        asignarFechaChooser(dcFecha, esEdicion ? fechaSel : fechaActual);

        JTextField txtG = crearCampoTexto(14);
        txtG.setText(guiaSel);
        JTextField txtD = crearCampoTexto(14);
        txtD.setText(despachoSel);
        JTextField txtC = crearCampoTexto(24);
        txtC.setText(clienteSel);
        JTextField txtP = crearCampoTexto(14);
        txtP.setText(patenteSel);
        JTextField txtCh = crearCampoTexto(22);
        txtCh.setText(choferSel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentro.add(crearEtiqueta("Fecha:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(dcFecha, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentro.add(crearEtiqueta("N° Guía:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtG, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentro.add(crearEtiqueta("N° Despacho:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtD, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(crearEtiqueta("Cliente / Destino:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtC, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCentro.add(crearEtiqueta("Patente Vehículo:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtP, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCentro.add(crearEtiqueta("Chofer Asignado:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCh, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(cBarraInf);
        panelBotones.setBorder(new LineBorder(cAcento, 1));

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
                String g = txtG.getText().trim();
                String d = txtD.getText().trim();
                String c = txtC.getText().trim();
                String p = txtP.getText().trim();
                String ch = txtCh.getText().trim();

                HojaRutaDAO dao = new HojaRutaDAO();
                boolean exito;
                if (esEdicion) {
                    exito = dao.actualizarHojaRuta(guiaOriginal, f, g, d, c, p, ch);
                } else {
                    exito = dao.insertarHojaRuta(f, g, d, c, p, ch);
                }

                if (exito) {
                    dialogo.dispose();
                    ejecutarBusqueda();
                } else {
                    JOptionPane.showMessageDialog(dialogo, "Error al guardar la hoja de ruta en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Verifique que los datos ingresados sean válidos.", "Error de Formato", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnGuardar.addActionListener(e -> guardarAccion.run());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.getRootPane().registerKeyboardAction(e -> guardarAccion.run(), KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.setVisible(true);
    }

    private void eliminarRegistroSeleccionado() {
        int filaVista = tablaRutas.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una hoja de ruta de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaRutas.convertRowIndexToModel(filaVista);
        String guia = (String) modeloTabla.getValueAt(fila, 1);
        String cliente = (String) modeloTabla.getValueAt(fila, 3);

        JDialog dialogo = new JDialog(JOptionPane.getFrameForComponent(this), "CONFIRMAR ELIMINACIÓN DE RUTA", true);
        dialogo.setSize(480, 220);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(cFondo);
        dialogo.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(cFondo);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cAcento, 2),
                " ADVERTENCIA DE ELIMINACIÓN ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblPregunta = new JLabel("¿Desea eliminar la hoja de ruta seleccionada?");
        lblPregunta.setFont(FUENTE_CONSOLA);
        lblPregunta.setForeground(Color.YELLOW);
        panelCentro.add(lblPregunta, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 15, 10, 15);
        JLabel lblDetalle = new JLabel("<html><center>" + cliente + "<br><font color='" + colorToHex(cAcento) + "'>[Guía: " + guia + "]</font></center></html>");
        lblDetalle.setFont(new Font("Consolas", Font.BOLD, 13));
        lblDetalle.setForeground(cTextoClaro);
        panelCentro.add(lblDetalle, gbc);

        dialogo.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(cBarraInf);
        panelBotones.setBorder(new LineBorder(cAcento, 1));

        JButton btnSi = new JButton("Sí [Enter]");
        btnSi.setFont(FUENTE_CONSOLA);
        JButton btnNo = new JButton("No [Esc]");
        btnNo.setFont(FUENTE_CONSOLA);

        panelBotones.add(btnSi);
        panelBotones.add(btnNo);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        Runnable accionEliminar = () -> {
            HojaRutaDAO dao = new HojaRutaDAO();
            if (dao.eliminarHojaRuta(guia)) {
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

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refrescar");
        root.getActionMap().put("refrescar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ejecutarBusqueda();
            }
        });

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
        tablaRutas.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "editar");

        AbstractAction accionEditar = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirFormularioMantenimiento(true);
            }
        };
        root.getActionMap().put("editar", accionEditar);
        tablaRutas.getActionMap().put("editar", accionEditar);

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eliminarRegistroSeleccionado();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "imprimir");
        root.getActionMap().put("imprimir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                imprimirHojaRutaSeleccionada();
            }
        });
    }

    private void imprimirHojaRutaSeleccionada() {
        int fila = tablaRutas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una hoja de ruta de la tabla para imprimir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaRutas.convertRowIndexToModel(fila);
        String fecha = (String) modeloTabla.getValueAt(filaModelo, 0);
        String guia = (String) modeloTabla.getValueAt(filaModelo, 1);
        String despacho = (String) modeloTabla.getValueAt(filaModelo, 2);
        String cliente = (String) modeloTabla.getValueAt(filaModelo, 3);
        String patente = (String) modeloTabla.getValueAt(filaModelo, 4);
        String chofer = (String) modeloTabla.getValueAt(filaModelo, 5);
        String total = (String) modeloTabla.getValueAt(filaModelo, 6);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Hoja de Ruta - Guía N° " + guia);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
            g2d.setColor(Color.BLACK);

            int y = 40;
            int x = 40;

            g2d.setFont(new Font("Consolas", Font.BOLD, 16));
            g2d.drawString("CASA VICTORIA - HOJA DE RUTA", x, y);
            y += 25;

            g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
            g2d.drawString("----------------------------------------------------------------", x, y);
            y += 20;

            g2d.drawString("Fecha de Emisión : " + fecha, x, y);
            y += 20;
            g2d.drawString("N° de Guía       : " + guia, x, y);
            y += 20;
            g2d.drawString("N° de Despacho   : " + despacho, x, y);
            y += 20;
            g2d.drawString("Cliente / Destino: " + cliente, x, y);
            y += 25;

            g2d.drawString("----------------------------------------------------------------", x, y);
            y += 20;

            g2d.setFont(new Font("Consolas", Font.BOLD, 13));
            g2d.drawString("DATOS DE TRANSPORTE:", x, y);
            y += 22;

            g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
            g2d.drawString("Patente Vehículo : " + patente, x, y);
            y += 20;
            g2d.drawString("Chofer Asignado  : " + chofer, x, y);
            y += 25;

            g2d.drawString("----------------------------------------------------------------", x, y);
            y += 20;

            g2d.setFont(new Font("Consolas", Font.BOLD, 13));
            g2d.drawString("MONTO TOTAL VENTA: " + total, x, y);
            y += 40;

            g2d.setFont(new Font("Consolas", Font.ITALIC, 10));
            g2d.drawString("Documento generado por Sistema de Bodega y Ventas - Casa Victoria", x, y);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Error al intentar imprimir: " + ex.getMessage(), "Error de Impresión", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
