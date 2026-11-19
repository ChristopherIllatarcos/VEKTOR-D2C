package vista.ctacte;

import conexion.Conexion;
import dao.CuentaCorrienteDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import modelo.DocumentoCarteraDTO;
import utilidades.PDFUtils;

public class FrmCartolaPorFecha extends JInternalFrame {

    private JTextField txtRut;
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JTable tablaCartola;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;
    private JLabel lblAtajos;
    private JButton btnConsultar;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelFiltros;
    private JPanel panelInferior;
    private JScrollPane scrollTabla;
    private JLabel lblRutLabel, lblDesdeLabel, lblHastaLabel;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cTexto, cBordeCian, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 12);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Terminal Verde", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Terminal Verde por defecto

    public FrmCartolaPorFecha() {
        super("5.- CARTOLA DE MOVIMIENTOS POR FECHA ", false, true, false, false);
        setSize(850, 520);
        setLocation(50, 30);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        getContentPane().setBackground(cFondo);
        setLayout(new BorderLayout());

        // 1. PANEL SUPERIOR DE FILTROS
        panelFiltros = new JPanel(new GridBagLayout());
        panelFiltros.setBackground(cFondo);
        panelFiltros.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeCian, 2), " PARAMETROS DE CONSULTA ",
                0, 0, FUENTE_CONSOLA, cBordeCian
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // RUT Cliente
        gbc.gridx = 0;
        gbc.gridy = 0;
        lblRutLabel = new JLabel("RUT / Cliente:");
        lblRutLabel.setForeground(cTexto);
        lblRutLabel.setFont(FUENTE_CONSOLA);
        panelFiltros.add(lblRutLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        txtRut = new JTextField(12);
        estilizarCampo(txtRut);
        panelFiltros.add(txtRut, gbc);

        // Fecha Desde
        gbc.gridx = 2;
        gbc.gridy = 0;
        lblDesdeLabel = new JLabel("Desde (DD-MM-YYYY):");
        lblDesdeLabel.setForeground(cTexto);
        lblDesdeLabel.setFont(FUENTE_CONSOLA);
        panelFiltros.add(lblDesdeLabel, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        txtFechaDesde = new JTextField(10);
        estilizarCampo(txtFechaDesde);
        txtFechaDesde.setText("01-01-2026");
        panelFiltros.add(txtFechaDesde, gbc);

        // Fecha Hasta
        gbc.gridx = 0;
        gbc.gridy = 1;
        lblHastaLabel = new JLabel("Hasta (DD-MM-YYYY):");
        lblHastaLabel.setForeground(cTexto);
        lblHastaLabel.setFont(FUENTE_CONSOLA);
        panelFiltros.add(lblHastaLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        txtFechaHasta = new JTextField(10);
        estilizarCampo(txtFechaHasta);
        txtFechaHasta.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        panelFiltros.add(txtFechaHasta, gbc);

        // Botón Consultar
        gbc.gridx = 3;
        gbc.gridy = 1;
        btnConsultar = new JButton("[ENTER] Consultar");
        estilizarBoton(btnConsultar);
        btnConsultar.addActionListener(e -> realizarConsulta());
        panelFiltros.add(btnConsultar, gbc);

        add(panelFiltros, BorderLayout.NORTH);

        // 2. TABLA / GRILLA CENTRAL
        String[] columnas = {"Cliente / RUT", "Folio Doc.", "Fecha", "Monto Abono ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCartola = new JTable(modeloTabla);
        estilarTabla(tablaCartola);

        scrollTabla = new JScrollPane(tablaCartola);
        scrollTabla.getViewport().setBackground(cFondo);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeCian, 2), " LISTADO DE MOVIMIENTOS EN CARTOLA ",
                0, 0, FUENTE_CONSOLA, cBordeCian
        ));
        add(scrollTabla, BorderLayout.CENTER);

        // 3. PANEL INFERIOR (TOTALES Y ATAJOS)
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(cFondo);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblTotal = new JLabel("TOTAL ABONOS PERIODO: $ 0");
        lblTotal.setForeground(cBordeCian);
        lblTotal.setFont(new Font("Courier New", Font.BOLD, 13));
        panelInferior.add(lblTotal, BorderLayout.WEST);

        lblAtajos = new JLabel();
        lblAtajos.setForeground(cTexto);
        lblAtajos.setFont(FUENTE_CONSOLA);
        actualizarTextoAtajos();
        panelInferior.add(lblAtajos, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        // 4. CONFIGURACIÓN DE ATAJOS DE TECLADO GLOBALES EN LA VENTANA
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
                cEncabezado = new Color(45, 10, 55);
                cBordeCian = new Color(255, 110, 220);
                cTexto = new Color(255, 180, 240);
                cSeleccion = new Color(140, 20, 160);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cEncabezado = new Color(10, 40, 50);
                cBordeCian = new Color(0, 245, 255);
                cTexto = new Color(180, 255, 255);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cEncabezado = new Color(10, 45, 30);
                cBordeCian = new Color(0, 255, 128);
                cTexto = new Color(180, 255, 200);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cEncabezado = new Color(40, 18, 12);
                cBordeCian = new Color(245, 73, 39);
                cTexto = new Color(255, 200, 150);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Terminal Verde":
            default:
                cFondo = Color.BLACK;
                cEncabezado = Color.BLACK;
                cBordeCian = Color.CYAN;
                cTexto = Color.GREEN;
                cSeleccion = new Color(0, 0, 128);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        getContentPane().setBackground(cFondo);

        if (panelFiltros != null) {
            panelFiltros.setBackground(cFondo);
            panelFiltros.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeCian, 2), " PARAMETROS DE CONSULTA ",
                    0, 0, FUENTE_CONSOLA, cBordeCian
            ));
            if (lblRutLabel != null) {
                lblRutLabel.setForeground(cTexto);
            }
            if (lblDesdeLabel != null) {
                lblDesdeLabel.setForeground(cTexto);
            }
            if (lblHastaLabel != null) {
                lblHastaLabel.setForeground(cTexto);
            }
        }

        estilizarCampo(txtRut);
        estilizarCampo(txtFechaDesde);
        estilizarCampo(txtFechaHasta);
        estilizarBoton(btnConsultar);

        if (tablaCartola != null) {
            estilarTabla(tablaCartola);
            tablaCartola.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cFondo);
            scrollTabla.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeCian, 2), " LISTADO DE MOVIMIENTOS EN CARTOLA ",
                    0, 0, FUENTE_CONSOLA, cBordeCian
            ));
        }

        if (panelInferior != null) {
            panelInferior.setBackground(cFondo);
        }
        if (lblTotal != null) {
            lblTotal.setForeground(cBordeCian);
        }
        if (lblAtajos != null) {
            lblAtajos.setForeground(cTexto);
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("[P] PDF   |   [F6] Tema: " + temaActual + "   |   [ESC] Salir");
        }
    }

    private void estilizarCampo(JTextField campo) {
        if (campo != null) {
            campo.setBackground(cFondo);
            campo.setForeground(cTexto);
            campo.setCaretColor(cTexto);
            campo.setFont(FUENTE_CONSOLA);
            campo.setBorder(new LineBorder(cBordeCian, 1));
            // Asegurar acción de enter
            for (java.awt.event.ActionListener al : campo.getActionListeners()) {
                campo.removeActionListener(al);
            }
            campo.addActionListener(e -> realizarConsulta());
        }
    }

    private void estilizarBoton(JButton boton) {
        if (boton != null) {
            boton.setBackground(cFondo);
            boton.setForeground(cBordeCian);
            boton.setFont(FUENTE_CONSOLA);
            boton.setBorder(new LineBorder(cBordeCian, 1));
            boton.setFocusPainted(false);
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    private void estilarTabla(JTable tabla) {
        if (tabla != null) {
            tabla.setBackground(cFondo);
            tabla.setForeground(cTexto);
            tabla.setFont(FUENTE_CONSOLA);
            tabla.setGridColor(cBordeCian);
            tabla.setSelectionBackground(cSeleccion);
            tabla.setSelectionForeground(Color.WHITE);
            tabla.getTableHeader().setBackground(cEncabezado);
            tabla.getTableHeader().setForeground(cBordeCian);
            tabla.getTableHeader().setFont(FUENTE_CONSOLA);
            tabla.setRowHeight(22);
        }
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // Atajo [ESC] para cerrar la ventana
        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrarVentana");
        rootPane.getActionMap().put("cerrarVentana", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // Atajo [F6] para ciclar temas en tiempo real
        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");
        rootPane.getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        // Atajo [P] o [p] para imprimir / exportar a PDF
        rootPane.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "imprimirPdf");
        rootPane.getActionMap().put("imprimirPdf", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportarCartolaPDF();
            }
        });
    }

    private void realizarConsulta() {
        String rut = txtRut.getText().trim();
        String fechaDesde = txtFechaDesde.getText().trim();
        String fechaHasta = txtFechaHasta.getText().trim();

        if (rut.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un RUT de cliente válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            txtRut.requestFocus();
            return;
        }

        modeloTabla.setRowCount(0);
        double sumaTotal = 0.0;

        try (Connection con = Conexion.getConnection()) {
            CuentaCorrienteDAO dao = new CuentaCorrienteDAO();
            List<DocumentoCarteraDTO> lista = dao.obtenerCartolaPorFecha(con, rut, fechaDesde, fechaHasta);

            for (DocumentoCarteraDTO doc : lista) {
                modeloTabla.addRow(new Object[]{
                    doc.getCliente(),
                    doc.getFolio(),
                    doc.getFecha(),
                    String.format("$ %,.0f", doc.getMonto())
                });
                sumaTotal += doc.getMonto();
            }

            lblTotal.setText(String.format("TOTAL ABONOS PERIODO: $ %,.0f", sumaTotal));

            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron movimientos para el RUT y rango de fechas indicado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al consultar la base de datos: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarCartolaPDF() {
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos en la cartola para exportar a PDF.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String rutCliente = txtRut.getText().trim();
            String fechaDesde = txtFechaDesde.getText().trim();
            String fechaHasta = txtFechaHasta.getText().trim();
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            PDFUtils.generarCartolaPorFechaPDF(
                    modeloTabla,
                    rutCliente,
                    fechaDesde,
                    fechaHasta,
                    fechaActual,
                    lblTotal.getText(),
                    this
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar el archivo PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
