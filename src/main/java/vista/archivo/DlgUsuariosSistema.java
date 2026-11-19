package vista.archivo;

import dao.UsuarioDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class DlgUsuariosSistema extends JDialog {

    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 13);

    private JTable tablaUsuarios;
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

    public DlgUsuariosSistema(Window parent) {
        super(parent, "2.- Usuarios del Sistema", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(920, 520);
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

        if (tablaUsuarios != null) {
            tablaUsuarios.setBackground(cTablaFondo);
            tablaUsuarios.setForeground(cTextoBlanco);
            tablaUsuarios.setSelectionBackground(cSeleccion);
            tablaUsuarios.setGridColor(cEncabezado);
            tablaUsuarios.getTableHeader().setBackground(cEncabezado);
            tablaUsuarios.getTableHeader().setForeground(cAcento);
            tablaUsuarios.getTableHeader().setBorder(new LineBorder(cAcento));
            tablaUsuarios.repaint();
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
            lblAtajos.setText("  [Insert] Nuevo   |   [F2] Modificar   |   [F6] Tema: " + temaActual + "   |   [Esc] Volver  ");
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

        // Título Superior
        lblTitulo = new JLabel("=== MAESTRO DE USUARIOS DEL SISTEMA (USUARIO_DBF) ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTitulo.setForeground(cAcento);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Tabla Central
        String[] columnas = {"RUT", "NOMBRE / USUARIO", "LOCAL", "LABOR / CARGO", "NIVEL", "CLAVE"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setFont(new Font("Consolas", Font.PLAIN, 13));
        tablaUsuarios.setBackground(cTablaFondo);
        tablaUsuarios.setForeground(cTextoBlanco);
        tablaUsuarios.setSelectionBackground(cSeleccion);
        tablaUsuarios.setSelectionForeground(Color.WHITE);
        tablaUsuarios.setRowHeight(25);
        tablaUsuarios.setShowGrid(true);
        tablaUsuarios.setGridColor(cEncabezado);

        tablaUsuarios.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 13));
        tablaUsuarios.getTableHeader().setBackground(cEncabezado);
        tablaUsuarios.getTableHeader().setForeground(cAcento);
        tablaUsuarios.getTableHeader().setBorder(new LineBorder(cAcento));

        DefaultTableCellRenderer tcrCentro = new DefaultTableCellRenderer();
        tcrCentro.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer tcrIzq = new DefaultTableCellRenderer();
        tcrIzq.setHorizontalAlignment(JLabel.LEFT);

        tablaUsuarios.getColumnModel().getColumn(0).setCellRenderer(tcrCentro);
        tablaUsuarios.getColumnModel().getColumn(1).setCellRenderer(tcrIzq);
        tablaUsuarios.getColumnModel().getColumn(2).setCellRenderer(tcrCentro);
        tablaUsuarios.getColumnModel().getColumn(3).setCellRenderer(tcrIzq);
        tablaUsuarios.getColumnModel().getColumn(4).setCellRenderer(tcrCentro);
        tablaUsuarios.getColumnModel().getColumn(5).setCellRenderer(tcrCentro);

        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(230);
        tablaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(170);
        tablaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(100);

        tablaUsuarios.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    e.consume();
                    abrirFormularioUsuario(null);
                } else if (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    editarUsuarioSeleccionado();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_F6) {
                    ciclarSiguienteTema();
                }
            }
        });

        scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.getViewport().setBackground(cTablaFondo);
        scrollPane.setBorder(new LineBorder(cAcento));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Zona Inferior (Botones y Atajos)
        panelSur = new JPanel(new BorderLayout(0, 10));
        panelSur.setBackground(cFondo);

        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panelBotones.setBackground(cFondo);

        btnNuevo = new JButton("[ INSERT: Nuevo ]");
        estilizarBotonClipper(btnNuevo);
        btnNuevo.addActionListener(e -> abrirFormularioUsuario(null));

        btnModificar = new JButton("[ F2: Modificar ]");
        estilizarBotonClipper(btnModificar);
        btnModificar.addActionListener(e -> editarUsuarioSeleccionado());

        btnSalir = new JButton("[ ESC: Salir ]");
        estilizarBotonClipper(btnSalir);
        btnSalir.addActionListener(e -> dispose());

        panelBotones.add(btnNuevo);
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
            if (tablaUsuarios.getRowCount() > 0) {
                tablaUsuarios.setRowSelectionInterval(0, 0);
            }
            tablaUsuarios.requestFocusInWindow();
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

            // Remover listeners previos si los hubiera para evitar duplicados en ciclado de temas
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
        UsuarioDAO dao = new UsuarioDAO();
        List<Object[]> lista = dao.obtenerUsuariosParaTabla();
        for (Object[] fila : lista) {
            modeloTabla.addRow(fila);
        }
    }

    private void abrirFormularioUsuario(Object[] usuarioDatos) {
        boolean esEdicion = (usuarioDatos != null);
        String tituloVentana = esEdicion ? "MODIFICAR USUARIO DEL SISTEMA" : "REGISTRAR NUEVO USUARIO";

        JDialog dlg = new JDialog(this, tituloVentana, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(540, 380);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitDlg = new JLabel("=== " + tituloVentana + " ===", JLabel.CENTER);
        lblTitDlg.setFont(new Font("Consolas", Font.BOLD, 15));
        lblTitDlg.setForeground(cAcento);
        panel.add(lblTitDlg, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(cFondo);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtRut = crearCampoTextoClipper(18);
        JTextField txtNombre = crearCampoTextoClipper(22);
        JTextField txtLocal = crearCampoTextoClipper(12);
        JTextField txtLabor = crearCampoTextoClipper(18);
        JTextField txtNivel = crearCampoTextoClipper(15);
        JTextField txtPass = crearCampoTextoClipper(15);

        if (esEdicion) {
            txtRut.setText((String) usuarioDatos[0]);
            txtRut.setEditable(false);
            txtRut.setBackground(new Color(30, 30, 30));
            txtNombre.setText((String) usuarioDatos[1]);
            txtLocal.setText((String) usuarioDatos[2]);
            txtLabor.setText((String) usuarioDatos[3]);
            txtNivel.setText((String) usuarioDatos[4]);
            txtPass.setText((String) usuarioDatos[5]);
        }

        String[] etiquetas = {"RUT :", "NOMBRE :", "LOCAL :", "LABOR / CARGO :", "NIVEL :", "CLAVE (PASS) :"};
        JTextField[] campos = {txtRut, txtNombre, txtLocal, txtLabor, txtNivel, txtPass};

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

        txtRut.addActionListener(e -> txtNombre.requestFocusInWindow());
        txtNombre.addActionListener(e -> txtLocal.requestFocusInWindow());
        txtLocal.addActionListener(e -> txtLabor.requestFocusInWindow());
        txtLabor.addActionListener(e -> txtNivel.requestFocusInWindow());
        txtNivel.addActionListener(e -> txtPass.requestFocusInWindow());

        panel.add(panelForm, BorderLayout.CENTER);

        JPanel panelBotonesDlg = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotonesDlg.setBackground(cFondo);

        JButton btnGuardar = new JButton("[ ENTER: Guardar ]");
        estilizarBotonClipper(btnGuardar);

        JButton btnCancelar = new JButton("[ ESC: Cancelar ]");
        estilizarBotonClipper(btnCancelar);

        Runnable accionGuardar = () -> {
            String rut = txtRut.getText().trim();
            String nombre = txtNombre.getText().trim();
            String local = txtLocal.getText().trim();
            String labor = txtLabor.getText().trim();
            String nivel = txtNivel.getText().trim();
            String pass = txtPass.getText().trim();

            if (rut.isEmpty() || nombre.isEmpty()) {
                mostrarMensajeClipper("ATENCIÓN", "El RUT y el Nombre son obligatorios.");
                return;
            }

            UsuarioDAO dao = new UsuarioDAO();
            boolean exito;
            if (esEdicion) {
                exito = dao.actualizarUsuario(rut, nombre, pass, local, labor, nivel);
            } else {
                exito = dao.insertarUsuario(rut, nombre, pass, local, labor, nivel);
            }

            if (exito) {
                cargarDatosDesdeBD();
                dlg.dispose();
                mostrarMensajeClipper("ÉXITO", "Usuario guardado correctamente.");
            } else {
                mostrarMensajeClipper("ERROR", "No se pudo guardar el usuario en la base de datos.");
            }
        };

        txtPass.addActionListener(e -> accionGuardar.run());
        btnGuardar.addActionListener(e -> accionGuardar.run());
        btnCancelar.addActionListener(e -> dlg.dispose());

        panelBotonesDlg.add(btnGuardar);
        panelBotonesDlg.add(btnCancelar);
        panel.add(panelBotonesDlg, BorderLayout.SOUTH);

        dlg.setContentPane(panel);
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> txtRut.requestFocusInWindow());
        dlg.setVisible(true);
        tablaUsuarios.requestFocusInWindow();
    }

    private void editarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("AVISO", "Debe seleccionar un usuario de la tabla.");
            return;
        }

        Object[] datosFila = new Object[]{
            modeloTabla.getValueAt(fila, 0),
            modeloTabla.getValueAt(fila, 1),
            modeloTabla.getValueAt(fila, 2),
            modeloTabla.getValueAt(fila, 3),
            modeloTabla.getValueAt(fila, 4),
            modeloTabla.getValueAt(fila, 5)
        };

        abrirFormularioUsuario(datosFila);
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
                abrirFormularioUsuario(null);
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editarUsuarioSeleccionado();
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
