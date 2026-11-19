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
    private JLabel lblAtajos;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelSuperior, panelInferiorContenedor, panelTotales, panelAtajosContainer;
    private JScrollPane scrollTabla;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelTabla, cZebra1, cZebra2, cBordeCian, cTextoClaro, cBarraInf, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 14);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Turquesa Pro", "Esmeralda Terminal","Azul Océano" , "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Azul Océano por defecto

    public FrmKardexMercaderia() {
        super("4.- KÁRDEX DE MERCADERÍA", false, false, false, false);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsManual();
        ejecutarBusqueda();
        // Asignar el icono de Nexum directamente a este JInternalFrame
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/viktor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelTabla = new Color(5, 20, 25);
                cZebra1 = new Color(5, 20, 25);
                cZebra2 = new Color(10, 35, 45);
                cBordeCian = new Color(0, 245, 255);
                cTextoClaro = new Color(220, 255, 255);
                cBarraInf = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelTabla = new Color(5, 25, 18);
                cZebra1 = new Color(5, 25, 18);
                cZebra2 = new Color(12, 45, 30);
                cBordeCian = new Color(0, 255, 128);
                cTextoClaro = new Color(220, 255, 240);
                cBarraInf = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Clipper Clásico":
                cFondo = Color.BLACK;
                cPanelTabla = new Color(0, 0, 120);
                cZebra1 = new Color(0, 0, 120);
                cZebra2 = new Color(0, 0, 168);
                cBordeCian = new Color(255, 255, 85);
                cTextoClaro = Color.WHITE;
                cBarraInf = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelTabla = new Color(25, 12, 10);
                cZebra1 = new Color(25, 12, 10);
                cZebra2 = new Color(50, 22, 15);
                cBordeCian = new Color(245, 73, 39);
                cTextoClaro = new Color(255, 235, 225);
                cBarraInf = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Azul Océano":
            default:
                cFondo = new Color(11, 34, 58);
                cPanelTabla = new Color(5, 18, 30);
                cZebra1 = new Color(5, 18, 30);
                cZebra2 = new Color(15, 42, 68);
                cBordeCian = new Color(0, 229, 255);
                cTextoClaro = new Color(220, 240, 255);
                cBarraInf = new Color(20, 70, 110);
                cSeleccion = new Color(30, 95, 145);
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
                    new LineBorder(cBordeCian, 2),
                    " KÁRDEX Y CONSULTA DE STOCK - FILTROS ",
                    0, 0, FUENTE_CONSOLA, cBordeCian
            ));
        }

        if (tablaKardex != null) {
            tablaKardex.setBackground(cPanelTabla);
            tablaKardex.setForeground(cTextoClaro);
            tablaKardex.setGridColor(cBordeCian);
            tablaKardex.setSelectionBackground(cBarraInf);
            tablaKardex.setSelectionForeground(Color.YELLOW);
            tablaKardex.getTableHeader().setBackground(cFondo);
            tablaKardex.getTableHeader().setForeground(cBordeCian);
            tablaKardex.repaint();
        }

        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(cPanelTabla);
            scrollTabla.setBorder(new LineBorder(cBordeCian, 1));
        }

        if (panelInferiorContenedor != null) {
            panelInferiorContenedor.setBackground(cFondo);
        }

        if (panelTotales != null) {
            panelTotales.setBackground(cFondo);
            panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeCian));
        }

        if (lblTotalValorizacion != null) {
            lblTotalValorizacion.setForeground(cBordeCian);
        }

        if (panelAtajosContainer != null) {
            panelAtajosContainer.setBackground(cBarraInf);
            panelAtajosContainer.setBorder(new LineBorder(cBordeCian, 1));
        }

        estilizarCampoDinamico(txtBuscarProducto);
        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void estilizarCampoDinamico(JTextField txt) {
        if (txt != null) {
            txt.setBackground(cPanelTabla);
            txt.setForeground(cBordeCian);
            txt.setCaretColor(cBordeCian);
            txt.setBorder(new LineBorder(cBordeCian, 1));
        }
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("(F5) Refrescar / Buscar    |    (F6) Tema: " + temaActual + "    |    (Esc) Salir");
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
                new LineBorder(cBordeCian, 2),
                " KÁRDEX Y CONSULTA DE STOCK - FILTROS ",
                0, 0, FUENTE_CONSOLA, cBordeCian
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
        tablaKardex.setBackground(cPanelTabla);
        tablaKardex.setForeground(cTextoClaro);
        tablaKardex.setGridColor(cBordeCian);
        tablaKardex.setRowHeight(28);
        tablaKardex.setSelectionBackground(cBarraInf);
        tablaKardex.setSelectionForeground(Color.YELLOW);

        tablaKardex.getTableHeader().setFont(FUENTE_CONSOLA);
        tablaKardex.getTableHeader().setBackground(cFondo);
        tablaKardex.getTableHeader().setForeground(cBordeCian);

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

        scrollTabla = new JScrollPane(tablaKardex);
        scrollTabla.getViewport().setBackground(cPanelTabla);
        scrollTabla.setBorder(new LineBorder(cBordeCian, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL INFERIOR (Totales + Atajos)
        // ==========================================
        panelInferiorContenedor = new JPanel(new BorderLayout());
        panelInferiorContenedor.setBackground(cFondo);

        panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 6));
        panelTotales.setBackground(cFondo);
        panelTotales.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, cBordeCian));

        lblTotalValorizacion = new JLabel("VALORIZACIÓN TOTAL INVENTARIO: $0");
        lblTotalValorizacion.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTotalValorizacion.setForeground(cBordeCian);
        panelTotales.add(lblTotalValorizacion);

        panelAtajosContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        panelAtajosContainer.setBackground(cBarraInf);
        panelAtajosContainer.setBorder(new LineBorder(cBordeCian, 1));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        lblAtajos.setForeground(cTextoClaro);
        actualizarTextoAtajos();
        panelAtajosContainer.add(lblAtajos);

        panelInferiorContenedor.add(panelTotales, BorderLayout.NORTH);
        panelInferiorContenedor.add(panelAtajosContainer, BorderLayout.SOUTH);
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
        txt.setForeground(cBordeCian);
        txt.setCaretColor(cBordeCian);
        txt.setBorder(new LineBorder(cBordeCian, 1));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txt.setBackground(new Color(Math.min(255, cPanelTabla.getRed() + 30), Math.min(255, cPanelTabla.getGreen() + 30), Math.min(255, cPanelTabla.getBlue() + 30)));
                txt.setBorder(new LineBorder(Color.YELLOW, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txt.setBackground(cPanelTabla);
                txt.setBorder(new LineBorder(cBordeCian, 1));
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

        // [F6] Ciclar Tema en tiempo real
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclar_tema");
        root.getActionMap().put("ciclar_tema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }
}
