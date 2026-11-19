package vista.archivo;

import dao.ParametroDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class DlgFoliadorHojas extends JDialog {

    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 13);

    private JTable tablaFolios;
    private DefaultTableModel modeloTabla;
    private JLabel lblAtajos;
    private JLabel lblTitulo;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelSur, panelBarraAtajos, panelBotones;
    private JScrollPane scrollPane;
    private JButton btnModificar, btnSalir;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cAcento, cTextoBlanco, cSeleccion, cRojoAccion, cTablaFondo;

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public DlgFoliadorHojas(Window parent) {
        super(parent, "5.- Foliador de Hojas", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(800, 500);
        setLocationRelativeTo(parent);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsCustom();
        cargarDatosDesdeBD();
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cEncabezado = new Color(45, 10, 55);
                cTablaFondo = new Color(15, 10, 20);
                cAcento = new Color(255, 110, 220);
                cTextoBlanco = new Color(255, 230, 255);
                cSeleccion = new Color(140, 20, 160);
                cRojoAccion = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cEncabezado = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cAcento = new Color(0, 245, 255);
                cTextoBlanco = new Color(220, 255, 255);
                cSeleccion = new Color(0, 120, 140);
                cRojoAccion = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cEncabezado = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cAcento = new Color(0, 255, 128);
                cTextoBlanco = new Color(220, 255, 240);
                cSeleccion = new Color(0, 140, 80);
                cRojoAccion = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cEncabezado = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cAcento = new Color(245, 73, 39);
                cTextoBlanco = new Color(255, 235, 225);
                cSeleccion = new Color(130, 42, 22);
                cRojoAccion = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 128);
                cEncabezado = new Color(0, 0, 128);
                cTablaFondo = Color.BLACK;
                cAcento = new Color(255, 255, 0);
                cTextoBlanco = new Color(255, 255, 255);
                cSeleccion = new Color(0, 128, 192);
                cRojoAccion = new Color(178, 34, 34);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
            panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(cAcento, 2),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
        }

        if (lblTitulo != null) {
            lblTitulo.setForeground(cAcento);
        }

        if (tablaFolios != null) {
            tablaFolios.setBackground(cTablaFondo);
            tablaFolios.setForeground(cTextoBlanco);
            tablaFolios.setSelectionBackground(cSeleccion);
            tablaFolios.setGridColor(cEncabezado);
            tablaFolios.getTableHeader().setBackground(cEncabezado);
            tablaFolios.getTableHeader().setForeground(cAcento);
            tablaFolios.getTableHeader().setBorder(new LineBorder(cAcento));
            tablaFolios.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cTablaFondo);
            scrollPane.setBorder(new LineBorder(cAcento));
        }

        if (panelSur != null) {
            panelSur.setBackground(cFondo);
        }
        if (panelBotones != null) {
            panelBotones.setBackground(cFondo);
        }

        estilizarBotonClipper(btnModificar);
        estilizarBotonClipper(btnSalir);

        if (panelBarraAtajos != null) {
            panelBarraAtajos.setBackground(cRojoAccion);
        }
        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("  [F2] Modificar Folio   |   [F6] Tema: " + temaActual + "   |   [Esc] Volver  ");
            lblAtajos.setForeground(cAcento);
        }
    }

    private void initComponentsCustom() {
        panelPrincipal = new JPanel(new BorderLayout(0, 10));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        lblTitulo = new JLabel("=== CONTROL Y CORRELATIVO DE FOLIADORES ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTitulo.setForeground(cAcento);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        String[] columnas = {"CÓDIGO", "DESCRIPCIÓN DEL DOCUMENTO", "PRÓXIMO FOLIO", "ESTADO"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFolios = new JTable(modeloTabla);
        tablaFolios.setFont(new Font("Consolas", Font.PLAIN, 13));
        tablaFolios.setBackground(cTablaFondo);
        tablaFolios.setForeground(cTextoBlanco);
        tablaFolios.setSelectionBackground(cSeleccion);
        tablaFolios.setSelectionForeground(Color.WHITE);
        tablaFolios.setRowHeight(25);
        tablaFolios.setShowGrid(true);
        tablaFolios.setGridColor(cEncabezado);

        tablaFolios.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    editarFolioSeleccionado();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_F6) {
                    ciclarSiguienteTema();
                }
            }
        });

        tablaFolios.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 13));
        tablaFolios.getTableHeader().setBackground(cEncabezado);
        tablaFolios.getTableHeader().setForeground(cAcento);
        tablaFolios.getTableHeader().setBorder(new LineBorder(cAcento));

        DefaultTableCellRenderer tcrCentro = new DefaultTableCellRenderer();
        tcrCentro.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer tcrIzq = new DefaultTableCellRenderer();
        tcrIzq.setHorizontalAlignment(JLabel.LEFT);

        tablaFolios.getColumnModel().getColumn(0).setCellRenderer(tcrCentro);
        tablaFolios.getColumnModel().getColumn(1).setCellRenderer(tcrIzq);
        tablaFolios.getColumnModel().getColumn(2).setCellRenderer(tcrCentro);
        tablaFolios.getColumnModel().getColumn(3).setCellRenderer(tcrCentro);

        tablaFolios.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaFolios.getColumnModel().getColumn(1).setPreferredWidth(350);
        tablaFolios.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaFolios.getColumnModel().getColumn(3).setPreferredWidth(120);

        scrollPane = new JScrollPane(tablaFolios);
        scrollPane.getViewport().setBackground(cTablaFondo);
        scrollPane.setBorder(new LineBorder(cAcento));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        panelSur = new JPanel(new BorderLayout(0, 10));
        panelSur.setBackground(cFondo);

        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panelBotones.setBackground(cFondo);

        btnModificar = new JButton("[ F2: Modificar Folio ]");
        estilizarBotonClipper(btnModificar);
        btnModificar.addActionListener(e -> editarFolioSeleccionado());

        btnSalir = new JButton("[ ESC: Salir ]");
        estilizarBotonClipper(btnSalir);
        btnSalir.addActionListener(e -> dispose());

        panelBotones.add(btnModificar);
        panelBotones.add(btnSalir);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        panelBarraAtajos = new JPanel(new BorderLayout());
        panelBarraAtajos.setBackground(cRojoAccion);
        panelBarraAtajos.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        lblAtajos = new JLabel("", JLabel.CENTER);
        lblAtajos.setFont(FUENTE_CONSOLA);
        actualizarTextoAtajos();
        panelBarraAtajos.add(lblAtajos, BorderLayout.CENTER);

        panelSur.add(panelBarraAtajos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSur, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);

        setupKeyBindings();

        SwingUtilities.invokeLater(() -> {
            if (tablaFolios.getRowCount() > 0) {
                tablaFolios.setRowSelectionInterval(0, 0);
                tablaFolios.requestFocusInWindow();
            }
        });
    }

    private void estilizarBotonClipper(JButton btn) {
        if (btn != null) {
            btn.setFont(FUENTE_CONSOLA);
            btn.setBackground(cTablaFondo);
            btn.setForeground(cAcento);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(cAcento, 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            for (java.awt.event.FocusListener fl : btn.getFocusListeners()) {
                btn.removeFocusListener(fl);
            }

            btn.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    btn.setBackground(cAcento);
                    btn.setForeground(cTablaFondo);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    btn.setBackground(cTablaFondo);
                    btn.setForeground(cAcento);
                }
            });
        }
    }

    private void cargarDatosDesdeBD() {
        modeloTabla.setRowCount(0);
        ParametroDAO dao = new ParametroDAO();
        List<Object[]> lista = dao.obtenerFoliosParaTabla();
        for (Object[] fila : lista) {
            modeloTabla.addRow(fila);
        }
    }

    private void editarFolioSeleccionado() {
        int fila = tablaFolios.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("AVISO", "Debe seleccionar un documento de la tabla.");
            return;
        }

        String codigoDoc = (String) modeloTabla.getValueAt(fila, 0);
        String documento = (String) modeloTabla.getValueAt(fila, 1);
        String folioActual = (String) modeloTabla.getValueAt(fila, 2);

        String nuevoFolioStr = mostrarInputBoxClipper("MODIFICAR CORRELATIVO", "Nuevo número para [" + documento + "]:", folioActual);
        if (nuevoFolioStr != null && !nuevoFolioStr.trim().isEmpty()) {
            try {
                long nuevoFolio = Long.parseLong(nuevoFolioStr.trim());
                ParametroDAO dao = new ParametroDAO();
                if (dao.actualizarFolio(codigoDoc, nuevoFolio)) {
                    cargarDatosDesdeBD();
                    mostrarMensajeClipper("ÉXITO", "El correlativo ha sido actualizado correctamente.");
                } else {
                    mostrarMensajeClipper("ERROR", "No se pudo actualizar el folio en la base de datos.");
                }
            } catch (NumberFormatException ex) {
                mostrarMensajeClipper("ERROR", "Debe ingresar un número de folio válido.");
            }
        }
    }

    private String mostrarInputBoxClipper(String titulo, String etiqueta, String valorInicial) {
        JDialog dlg = new JDialog(this, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(460, 180);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel("<html><center>" + etiqueta + "</center></html>");
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(cAcento);
        panel.add(lbl, BorderLayout.NORTH);

        JTextField txt = new JTextField(valorInicial, 15);
        txt.setFont(new Font("Consolas", Font.BOLD, 14));
        txt.setBackground(cTablaFondo);
        txt.setForeground(cAcento);
        txt.setCaretColor(cAcento);
        txt.setBorder(new LineBorder(cAcento, 1));

        JPanel panelTxt = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelTxt.setBackground(cFondo);
        panelTxt.add(txt);
        panel.add(panelTxt, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(cFondo);

        JButton btnAceptar = new JButton("[ ACEPTAR ]");
        estilizarBotonClipper(btnAceptar);

        JButton btnCancelar = new JButton("[ CANCELAR ]");
        estilizarBotonClipper(btnCancelar);

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        dlg.setContentPane(panel);

        final String[] resultado = {null};

        btnAceptar.addActionListener(e -> {
            resultado[0] = txt.getText();
            dlg.dispose();
        });
        btnCancelar.addActionListener(e -> dlg.dispose());

        txt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    resultado[0] = txt.getText();
                    dlg.dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dlg.dispose();
                }
            }
        });

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> {
            txt.requestFocusInWindow();
            txt.selectAll();
        });
        dlg.setVisible(true);

        return resultado[0];
    }

    private void mostrarMensajeClipper(String titulo, String mensaje) {
        JDialog dlg = new JDialog(this, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(440, 160);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel("<html><center>" + mensaje + "</center></html>");
        lbl.setFont(FUENTE_CONSOLA);
        lbl.setForeground(cAcento);
        panel.add(lbl, BorderLayout.CENTER);

        JButton btnAceptar = new JButton("[ ACEPTAR ]");
        estilizarBotonClipper(btnAceptar);
        btnAceptar.addActionListener(e -> dlg.dispose());

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setBackground(cFondo);
        panelBoton.add(btnAceptar);
        panel.add(panelBoton, BorderLayout.SOUTH);

        dlg.setContentPane(panel);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> btnAceptar.requestFocusInWindow());
        dlg.setVisible(true);
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        root.getActionMap().put("salir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarFolioSeleccionado();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");
        root.getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }
}
