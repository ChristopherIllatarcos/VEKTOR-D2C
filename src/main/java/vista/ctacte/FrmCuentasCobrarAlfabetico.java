package vista.ctacte;

import conexion.Conexion;
import dao.CuentaCorrienteDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.List;
import java.util.Vector;
import modelo.DocumentoCarteraDTO;

public class FrmCuentasCobrarAlfabetico extends JInternalFrame {

    private JTextField txtBuscarAlfabetico;
    private JTable tablaCuentas;
    private DefaultTableModel modeloTabla;
    private JLabel lblModoVal;
    private JLabel lblAyuda;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal;
    private JPanel panelBusqueda;
    private JPanel panelModoMini;
    private JPanel panelAyudaContainer;
    private JScrollPane scrollTabla;
    private JLabel lblFiltro;
    private JLabel lblTituloMini;

    private boolean modoResumen = true; // Inicia en Resumen por defecto

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelSup, cTablaFondo, cBordeAcento, cTextoVerde, cTextoBlanco, cBarraAyuda;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 13);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public FrmCuentasCobrarAlfabetico() {
        super(" 6.- CUENTAS x COBRAR x ALFABETICO ", false, false, false, false);
        setSize(850, 560);
        setFrameIcon(null);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel superior de búsqueda
        panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setBackground(cFondo);
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2), " BUSQUEDA ALFABETICA CLIENTE ",
                0, 0, FUENTE_CONSOLA, cBordeAcento
        ));

        lblFiltro = new JLabel("Cliente / RUT :");
        lblFiltro.setFont(FUENTE_CONSOLA);
        lblFiltro.setForeground(cTextoBlanco);
        panelBusqueda.add(lblFiltro);

        txtBuscarAlfabetico = new JTextField(30);
        estilarCampo(txtBuscarAlfabetico);
        panelBusqueda.add(txtBuscarAlfabetico);

        // Ventanita mini de estado (Resumen/Detalle)
        panelModoMini = new JPanel(new GridLayout(2, 1));
        panelModoMini.setBackground(cPanelSup);
        panelModoMini.setBorder(new LineBorder(cBordeAcento, 1));

        lblTituloMini = new JLabel(" Ctas x Cobrar ", JLabel.CENTER);
        lblTituloMini.setFont(new Font("Courier New", Font.BOLD, 11));
        lblTituloMini.setForeground(Color.RED);
        panelModoMini.add(lblTituloMini);

        lblModoVal = new JLabel(" (R)esumen  SECTOR: 0 ", JLabel.CENTER);
        lblModoVal.setFont(new Font("Courier New", Font.BOLD, 11));
        lblModoVal.setForeground(cBordeAcento);
        panelModoMini.add(lblModoVal);

        panelBusqueda.add(Box.createHorizontalStrut(30));
        panelBusqueda.add(panelModoMini);
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla central (Se inicializa con columnas de Resumen)
        modeloTabla = new DefaultTableModel(obtenerColumnasActuales(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCuentas = new JTable(modeloTabla);
        estilarTabla(tablaCuentas);
        scrollTabla = new JScrollPane(tablaCuentas);
        scrollTabla.getViewport().setBackground(cTablaFondo);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cBordeAcento, 2), " LISTADO GENERAL ORDENADO ALFABETICAMENTE ",
                0, 0, FUENTE_CONSOLA, cBordeAcento
        ));
        panelPrincipal.add(scrollTabla, BorderLayout.CENTER);

        // Barra inferior de atajos
        panelAyudaContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        panelAyudaContainer.setBackground(cBarraAyuda);
        lblAyuda = new JLabel();
        lblAyuda.setFont(new Font("Courier New", Font.BOLD, 12));
        lblAyuda.setForeground(cBordeAcento);
        actualizarTextoAyuda();
        panelAyudaContainer.add(lblAyuda);
        panelPrincipal.add(panelAyudaContainer, BorderLayout.SOUTH);

        add(panelPrincipal);

        // Carga inicial
        recargarDatos();

        // Eventos
        txtBuscarAlfabetico.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (tablaCuentas.getRowCount() > 0) {
                        tablaCuentas.requestFocus();
                        tablaCuentas.setRowSelectionInterval(0, 0);
                    }
                } else {
                    recargarDatos();
                }
            }
        });

        // Atajos globales
        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        panelPrincipal.getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "MODO_RESUMEN");
        panelPrincipal.getActionMap().put("MODO_RESUMEN", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                modoResumen = true;
                lblModoVal.setText(" (R)esumen  SECTOR: 0 ");
                actualizarEstructuraTabla();
                recargarDatos();
            }
        });

        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "MODO_DETALLE");
        panelPrincipal.getActionMap().put("MODO_DETALLE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                modoResumen = false;
                lblModoVal.setText(" (D)etalle  SECTOR: 0 ");
                actualizarEstructuraTabla();
                recargarDatos();
            }
        });

        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "EXPORTAR_PDF");
        panelPrincipal.getActionMap().put("EXPORTAR_PDF", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportarPDFActual();
            }
        });

        // [F6] Ciclar Temas en tiempo real
        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "CICLAR_TEMA");
        panelPrincipal.getActionMap().put("CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
        
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
                cPanelSup = new Color(45, 10, 55);
                cTablaFondo = new Color(15, 10, 20);
                cBordeAcento = new Color(255, 110, 220);
                cTextoVerde = new Color(255, 180, 240);
                cTextoBlanco = new Color(255, 230, 255);
                cBarraAyuda = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelSup = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cBordeAcento = new Color(0, 245, 255);
                cTextoVerde = new Color(180, 255, 255);
                cTextoBlanco = new Color(220, 255, 255);
                cBarraAyuda = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelSup = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cBordeAcento = new Color(0, 255, 128);
                cTextoVerde = new Color(180, 255, 200);
                cTextoBlanco = new Color(220, 255, 240);
                cBarraAyuda = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelSup = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cBordeAcento = new Color(245, 73, 39);
                cTextoVerde = new Color(255, 200, 150);
                cTextoBlanco = new Color(255, 235, 225);
                cBarraAyuda = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 132);
                cPanelSup = new Color(0, 0, 90);
                cTablaFondo = Color.BLACK;
                cBordeAcento = Color.CYAN;
                cTextoVerde = Color.GREEN;
                cTextoBlanco = Color.WHITE;
                cBarraAyuda = new Color(0, 0, 80);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
        }

        if (panelBusqueda != null) {
            panelBusqueda.setBackground(cFondo);
            panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2), " BUSQUEDA ALFABETICA CLIENTE ",
                    0, 0, FUENTE_CONSOLA, cBordeAcento
            ));
            if (lblFiltro != null) {
                lblFiltro.setForeground(cTextoBlanco);
            }
        }

        estilarCampo(txtBuscarAlfabetico);

        if (panelModoMini != null) {
            panelModoMini.setBackground(cPanelSup);
            panelModoMini.setBorder(new LineBorder(cBordeAcento, 1));
            if (lblModoVal != null) {
                lblModoVal.setForeground(cBordeAcento);
            }
        }

        if (tablaCuentas != null) {
            estilarTabla(tablaCuentas);
            tablaCuentas.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cTablaFondo);
            scrollTabla.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cBordeAcento, 2), " LISTADO GENERAL ORDENADO ALFABETICAMENTE ",
                    0, 0, FUENTE_CONSOLA, cBordeAcento
            ));
        }

        if (panelAyudaContainer != null) {
            panelAyudaContainer.setBackground(cBarraAyuda);
        }
        actualizarTextoAyuda();

        revalidate();
        repaint();
    }

    private void actualizarTextoAyuda() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAyuda != null) {
            lblAyuda.setText(" [ESC] Salir    [R] Resumen    [D] Detalle    [F6] Tema: " + temaActual + "    [P] PDF ");
        }
    }

    private String[] obtenerColumnasActuales() {
        if (modoResumen) {
            return new String[]{"RUT / Cliente", "Cant. Documentos", "Fecha Ref.", "Total Acumulado ($)"};
        } else {
            return new String[]{"RUT / Cliente", "N° Folio", "Fecha Documento", "Monto Abono ($)"};
        }
    }

    private void actualizarEstructuraTabla() {
        modeloTabla.setColumnIdentifiers(obtenerColumnasActuales());
    }

    private void estilarCampo(JTextField txt) {
        if (txt != null) {
            txt.setFont(FUENTE_CONSOLA);
            txt.setBackground(cTablaFondo);
            txt.setForeground(cTextoVerde);
            txt.setCaretColor(cTextoVerde);
            txt.setBorder(new LineBorder(cBordeAcento, 1));
        }
    }

    private void estilarTabla(JTable tabla) {
        if (tabla != null) {
            tabla.setFont(FUENTE_CONSOLA);
            tabla.setBackground(cTablaFondo);
            tabla.setForeground(cTextoVerde);
            tabla.setGridColor(cBordeAcento);
            tabla.setRowHeight(22);
            tabla.getTableHeader().setFont(FUENTE_CONSOLA);
            tabla.getTableHeader().setBackground(cPanelSup);
            tabla.getTableHeader().setForeground(cTextoBlanco);
        }
    }

    private void recargarDatos() {
        modeloTabla.setRowCount(0);
        String filtro = txtBuscarAlfabetico.getText().trim();

        try (Connection con = Conexion.getConnection()) {
            CuentaCorrienteDAO dao = new CuentaCorrienteDAO();
            List<DocumentoCarteraDTO> lista;

            if (modoResumen) {
                lista = dao.obtenerCuentasResumen(con, filtro);
            } else {
                lista = dao.obtenerCuentasDetalle(con, filtro);
            }

            for (DocumentoCarteraDTO doc : lista) {
                Vector<Object> fila = new Vector<>();
                fila.add(doc.getCliente());
                fila.add(doc.getFolio());
                fila.add(doc.getFecha());
                fila.add(String.format("$ %,.0f", doc.getMonto()));
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void exportarPDFActual() {
        String filtro = txtBuscarAlfabetico.getText().trim();
        try (Connection con = Conexion.getConnection()) {
            CuentaCorrienteDAO dao = new CuentaCorrienteDAO();
            List<DocumentoCarteraDTO> lista;

            if (modoResumen) {
                lista = dao.obtenerCuentasResumen(con, filtro);
            } else {
                lista = dao.obtenerCuentasDetalle(con, filtro);
            }

            boolean exito = utilidades.PDFUtils.exportarCuentasCobrarPDF(lista, modoResumen);
            if (exito) {
                JOptionPane.showMessageDialog(this, "¡PDF exportado y generado con éxito!", "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al generar el archivo PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Excepción al conectar para exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
