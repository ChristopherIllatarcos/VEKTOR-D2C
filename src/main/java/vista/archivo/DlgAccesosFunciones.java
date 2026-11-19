package vista.archivo;

import dao.AccesoDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class DlgAccesosFunciones extends JDialog {

    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 13);

    private JTable tablaAccesos;
    private DefaultTableModel modeloTabla;
    private JLabel lblAtajos;
    private JLabel lblTitulo;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelSur, panelBarraAtajos, panelBotones;
    private JScrollPane scrollPane;
    private JButton btnNuevo, btnModificar, btnSalir;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cAcento, cTextoBlanco, cSeleccion, cRojoAccion, cTablaFondo;

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public DlgAccesosFunciones(Window parent) {
        super(parent, "3.- Accesos a Funciones", ModalityType.APPLICATION_MODAL);
        setUndecorated(true); // Sin bordes de Windows, estilo DOS puro
        setSize(860, 500);
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

        if (tablaAccesos != null) {
            tablaAccesos.setBackground(cTablaFondo);
            tablaAccesos.setForeground(cTextoBlanco);
            tablaAccesos.setSelectionBackground(cSeleccion);
            tablaAccesos.setGridColor(cEncabezado);
            tablaAccesos.getTableHeader().setBackground(cEncabezado);
            tablaAccesos.getTableHeader().setForeground(cAcento);
            tablaAccesos.getTableHeader().setBorder(new LineBorder(cAcento));
            tablaAccesos.repaint();
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

        estilizarBotonClipper(btnNuevo);
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
            lblAtajos.setText("  [Insert] Agregar Acceso   |   [F2] Modificar Clave   |   [F6] Tema: " + temaActual + "   |   [Esc] Volver  ");
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

        // 1. TÍTULO SUPERIOR
        lblTitulo = new JLabel("=== CONTROL DE PERMISOS Y ACCESOS (KEYS_DBF) ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTitulo.setForeground(cAcento);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // 2. TABLA CENTRAL
        String[] columnas = {"MÓDULO", "RUT", "USUARIO", "CLAVE / ACCESO"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAccesos = new JTable(modeloTabla);
        tablaAccesos.setFont(new Font("Consolas", Font.PLAIN, 13));
        tablaAccesos.setBackground(cTablaFondo);
        tablaAccesos.setForeground(cTextoBlanco);
        tablaAccesos.setSelectionBackground(cSeleccion);
        tablaAccesos.setSelectionForeground(Color.WHITE);
        tablaAccesos.setRowHeight(25);
        tablaAccesos.setShowGrid(true);
        tablaAccesos.setGridColor(cEncabezado);

        tablaAccesos.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 13));
        tablaAccesos.getTableHeader().setBackground(cEncabezado);
        tablaAccesos.getTableHeader().setForeground(cAcento);
        tablaAccesos.getTableHeader().setBorder(new LineBorder(cAcento));

        DefaultTableCellRenderer tcrCentro = new DefaultTableCellRenderer();
        tcrCentro.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer tcrIzq = new DefaultTableCellRenderer();
        tcrIzq.setHorizontalAlignment(JLabel.LEFT);

        tablaAccesos.getColumnModel().getColumn(0).setCellRenderer(tcrCentro);
        tablaAccesos.getColumnModel().getColumn(1).setCellRenderer(tcrCentro);
        tablaAccesos.getColumnModel().getColumn(2).setCellRenderer(tcrIzq);
        tablaAccesos.getColumnModel().getColumn(3).setCellRenderer(tcrCentro);

        tablaAccesos.getColumnModel().getColumn(0).setPreferredWidth(150);
        tablaAccesos.getColumnModel().getColumn(1).setPreferredWidth(140);
        tablaAccesos.getColumnModel().getColumn(2).setPreferredWidth(270);
        tablaAccesos.getColumnModel().getColumn(3).setPreferredWidth(160);

        // Escuchador de teclado directo en la tabla para prioridad de teclas
        tablaAccesos.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    e.consume();
                    abrirDialogoNuevoAcceso();
                } else if (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    editarClaveSeleccionada();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_F6) {
                    ciclarSiguienteTema();
                }
            }
        });

        scrollPane = new JScrollPane(tablaAccesos);
        scrollPane.getViewport().setBackground(cTablaFondo);
        scrollPane.setBorder(new LineBorder(cAcento));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // 3. ZONA INFERIOR (Botones y Atajos)
        panelSur = new JPanel(new BorderLayout(0, 10));
        panelSur.setBackground(cFondo);

        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panelBotones.setBackground(cFondo);

        btnNuevo = new JButton("[ INSERT: Nuevo ]");
        estilizarBotonClipper(btnNuevo);
        btnNuevo.addActionListener(e -> abrirDialogoNuevoAcceso());

        btnModificar = new JButton("[ F2: Modificar Clave ]");
        estilizarBotonClipper(btnModificar);
        btnModificar.addActionListener(e -> editarClaveSeleccionada());

        btnSalir = new JButton("[ ESC: Salir ]");
        estilizarBotonClipper(btnSalir);
        btnSalir.addActionListener(e -> dispose());

        panelBotones.add(btnNuevo);
        panelBotones.add(btnModificar);
        panelBotones.add(btnSalir);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        // Barra de atajos inferior roja institucional
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
            if (tablaAccesos.getRowCount() > 0) {
                tablaAccesos.setRowSelectionInterval(0, 0);
            }
            tablaAccesos.requestFocusInWindow();
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

    private JTextField crearCampoTextoClipper(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(new Font("Consolas", Font.BOLD, 14));
        txt.setBackground(cTablaFondo);
        txt.setForeground(cAcento);
        txt.setCaretColor(cAcento);
        txt.setBorder(new LineBorder(cAcento, 1));
        return txt;
    }

    private void cargarDatosDesdeBD() {
        modeloTabla.setRowCount(0);
        AccesoDAO dao = new AccesoDAO();
        List<Object[]> lista = dao.obtenerAccesosParaTabla();
        for (Object[] fila : lista) {
            modeloTabla.addRow(fila);
        }
    }

    private void abrirDialogoNuevoAcceso() {
        JDialog dlg = new JDialog(this, "Nuevo Acceso a Función", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(520, 320);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitDlg = new JLabel("=== REGISTRAR NUEVO ACCESO ===", JLabel.CENTER);
        lblTitDlg.setFont(new Font("Consolas", Font.BOLD, 15));
        lblTitDlg.setForeground(cAcento);
        panel.add(lblTitDlg, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(cFondo);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtModulo = crearCampoTextoClipper(18);
        JTextField txtRut = crearCampoTextoClipper(18);
        JTextField txtUsuario = crearCampoTextoClipper(18);
        JTextField txtClave = crearCampoTextoClipper(18);

        String[] etiquetas = {"MÓDULO :", "RUT USUARIO :", "NOMBRE USUARIO :", "CLAVE ACCESO :"};
        JTextField[] campos = {txtModulo, txtRut, txtUsuario, txtClave};

        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(FUENTE_CONSOLA);
            lbl.setForeground(cAcento);
            panelForm.add(lbl, gbc);

            gbc.gridx = 1;
            panelForm.add(campos[i], gbc);
        }

        txtModulo.addActionListener(e -> txtRut.requestFocusInWindow());
        txtRut.addActionListener(e -> txtUsuario.requestFocusInWindow());
        txtUsuario.addActionListener(e -> txtClave.requestFocusInWindow());

        panel.add(panelForm, BorderLayout.CENTER);

        JPanel panelBotonesDlg = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotonesDlg.setBackground(cFondo);

        JButton btnGuardar = new JButton("[ ENTER: Guardar ]");
        estilizarBotonClipper(btnGuardar);

        JButton btnCancelar = new JButton("[ ESC: Cancelar ]");
        estilizarBotonClipper(btnCancelar);

        Runnable accionGuardar = () -> {
            String mod = txtModulo.getText().trim();
            String rut = txtRut.getText().trim();
            String usu = txtUsuario.getText().trim();
            String cla = txtClave.getText().trim();

            if (mod.isEmpty() || rut.isEmpty() || usu.isEmpty() || cla.isEmpty()) {
                mostrarMensajeClipper("ATENCIÓN", "Todos los campos son obligatorios.");
                return;
            }

            AccesoDAO dao = new AccesoDAO();
            if (dao.insertarAcceso(mod, rut, usu, cla)) {
                cargarDatosDesdeBD();
                if (tablaAccesos.getRowCount() > 0) {
                    tablaAccesos.setRowSelectionInterval(tablaAccesos.getRowCount() - 1, tablaAccesos.getRowCount() - 1);
                }
                dlg.dispose();
                mostrarMensajeClipper("ÉXITO", "Acceso registrado correctamente en keys_dbf.");
            } else {
                mostrarMensajeClipper("ERROR", "No se pudo guardar el registro en la base de datos.");
            }
        };

        txtClave.addActionListener(e -> accionGuardar.run());
        btnGuardar.addActionListener(e -> accionGuardar.run());
        btnCancelar.addActionListener(e -> dlg.dispose());

        panelBotonesDlg.add(btnGuardar);
        panelBotonesDlg.add(btnCancelar);
        panel.add(panelBotonesDlg, BorderLayout.SOUTH);

        dlg.setContentPane(panel);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> txtModulo.requestFocusInWindow());
        dlg.setVisible(true);
        tablaAccesos.requestFocusInWindow();
    }

    private void editarClaveSeleccionada() {
        int fila = tablaAccesos.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("AVISO", "Debe seleccionar un registro de la tabla.");
            return;
        }

        String modulo = (String) modeloTabla.getValueAt(fila, 0);
        String rut = (String) modeloTabla.getValueAt(fila, 1);
        String claveActual = (String) modeloTabla.getValueAt(fila, 3);

        String nuevaClave = mostrarInputBoxClipper("MODIFICAR CLAVE", "Nueva clave para Módulo [" + modulo + "]:", claveActual);
        if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
            AccesoDAO dao = new AccesoDAO();
            if (dao.actualizarClaveAcceso(modulo, rut, nuevaClave.trim())) {
                cargarDatosDesdeBD();
                tablaAccesos.setRowSelectionInterval(fila, fila);
                mostrarMensajeClipper("ÉXITO", "La clave de acceso ha sido actualizada correctamente.");
            } else {
                mostrarMensajeClipper("ERROR", "No se pudo actualizar la clave en la base de datos.");
            }
        }
    }

    private String mostrarInputBoxClipper(String titulo, String etiqueta, String valorInicial) {
        JDialog dlg = new JDialog(this, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(480, 180);
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

        JTextField txt = crearCampoTextoClipper(15);
        txt.setText(valorInicial);

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

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "nuevo");
        root.getActionMap().put("nuevo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirDialogoNuevoAcceso();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarClaveSeleccionada();
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
