package vista.archivo;

import dao.ParametroDAO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class DlgCamaraKey extends JDialog {

    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 13);

    private JTextField txtClaveActual;
    private JTextField txtNuevaClave;
    private JLabel lblAtajos;
    private JLabel lblTitulo;
    private JPanel panelCentro;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelSur, panelBarraAtajos, panelBotones;
    private JButton btnGuardar, btnSalir;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cAcento, cTextoBlanco, cRojoAccion, cTablaFondo;

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public DlgCamaraKey(Window parent) {
        super(parent, "4.- Cámara Key", ModalityType.APPLICATION_MODAL);
        setUndecorated(true); // Sin barra de título de Windows, estilo DOS puro
        setSize(650, 380);
        setLocationRelativeTo(parent);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsCustom();
        cargarClaveDesdeBD();
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
                cRojoAccion = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cEncabezado = new Color(10, 40, 50);
                cTablaFondo = new Color(5, 20, 25);
                cAcento = new Color(0, 245, 255);
                cTextoBlanco = new Color(220, 255, 255);
                cRojoAccion = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cEncabezado = new Color(10, 45, 30);
                cTablaFondo = new Color(5, 25, 18);
                cAcento = new Color(0, 255, 128);
                cTextoBlanco = new Color(220, 255, 240);
                cRojoAccion = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cEncabezado = new Color(40, 18, 12);
                cTablaFondo = new Color(25, 12, 10);
                cAcento = new Color(245, 73, 39);
                cTextoBlanco = new Color(255, 235, 225);
                cRojoAccion = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 128);
                cEncabezado = new Color(0, 0, 128);
                cTablaFondo = Color.BLACK;
                cAcento = new Color(255, 255, 0);
                cTextoBlanco = new Color(255, 255, 255);
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
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        }

        if (lblTitulo != null) {
            lblTitulo.setForeground(cAcento);
        }

        if (panelCentro != null) {
            panelCentro.setBackground(cFondo);
            panelCentro.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cAcento, 1),
                    " CONFIGURACIÓN DE LLAVE DE ACCESO ",
                    0, 0, FUENTE_CONSOLA, cAcento
            ));
            for (Component comp : panelCentro.getComponents()) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setForeground(cAcento);
                }
            }
        }

        if (txtClaveActual != null) {
            txtClaveActual.setBackground(cTablaFondo);
            txtClaveActual.setForeground(cTextoBlanco);
            txtClaveActual.setBorder(new LineBorder(cAcento, 1));
        }

        if (txtNuevaClave != null) {
            txtNuevaClave.setBackground(cTablaFondo);
            txtNuevaClave.setForeground(cAcento);
            txtNuevaClave.setCaretColor(cAcento);
            txtNuevaClave.setBorder(new LineBorder(cAcento, 1));
        }

        if (panelSur != null) {
            panelSur.setBackground(cFondo);
        }
        if (panelBotones != null) {
            panelBotones.setBackground(cFondo);
        }

        estilizarBotonClipper(btnGuardar);
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
            lblAtajos.setText("  [Enter] Guardar    |    [F6] Tema: " + temaActual + "    |    [Esc] Volver  ");
            lblAtajos.setForeground(cAcento);
        }
    }

    private void initComponentsCustom() {
        panelPrincipal = new JPanel(new BorderLayout(0, 15));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // 1. TÍTULO SUPERIOR
        lblTitulo = new JLabel("=== GESTIÓN DE CLAVE DE SEGURIDAD (CÁMARA KEY) ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 15));
        lblTitulo.setForeground(cAcento);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // 2. PANEL CENTRAL (Formulario)
        panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBackground(cFondo);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cAcento, 1),
                " CONFIGURACIÓN DE LLAVE DE ACCESO ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Etiqueta Clave Actual
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lbl1 = new JLabel("CLAVE REGISTRADA :");
        lbl1.setFont(FUENTE_CONSOLA);
        lbl1.setForeground(cAcento);
        panelCentro.add(lbl1, gbc);

        txtClaveActual = new JTextField(15);
        txtClaveActual.setFont(new Font("Consolas", Font.BOLD, 14));
        txtClaveActual.setBackground(cTablaFondo);
        txtClaveActual.setForeground(cTextoBlanco);
        txtClaveActual.setBorder(new LineBorder(cAcento, 1));
        txtClaveActual.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panelCentro.add(txtClaveActual, gbc);

        // Etiqueta Nueva Clave
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lbl2 = new JLabel("NUEVA CÁMARA KEY :");
        lbl2.setFont(FUENTE_CONSOLA);
        lbl2.setForeground(cAcento);
        panelCentro.add(lbl2, gbc);

        txtNuevaClave = new JTextField(15);
        txtNuevaClave.setFont(new Font("Consolas", Font.BOLD, 14));
        txtNuevaClave.setBackground(cTablaFondo);
        txtNuevaClave.setForeground(cAcento);
        txtNuevaClave.setCaretColor(cAcento);
        txtNuevaClave.setBorder(new LineBorder(cAcento, 1));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panelCentro.add(txtNuevaClave, gbc);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        // 3. ZONA INFERIOR (Botones y Barra de Atajos)
        panelSur = new JPanel(new BorderLayout(0, 10));
        panelSur.setBackground(cFondo);

        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panelBotones.setBackground(cFondo);

        btnGuardar = new JButton("[ ENTER: Guardar ]");
        estilizarBotonClipper(btnGuardar);
        btnGuardar.addActionListener(e -> guardarNuevaClave());

        btnSalir = new JButton("[ ESC: Cancelar ]");
        estilizarBotonClipper(btnSalir);
        btnSalir.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
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

        // Configuración de atajos de teclado globales
        setupKeyBindings();

        SwingUtilities.invokeLater(() -> txtNuevaClave.requestFocusInWindow());
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

    private void cargarClaveDesdeBD() {
        ParametroDAO dao = new ParametroDAO();
        String claveActual = dao.obtenerCamaraKey();
        txtClaveActual.setText(claveActual);
    }

    private void guardarNuevaClave() {
        String nuevaClave = txtNuevaClave.getText().trim();
        if (nuevaClave.isEmpty()) {
            mostrarMensajeClipper("ATENCIÓN", "Debe ingresar una clave válida.");
            return;
        }

        ParametroDAO dao = new ParametroDAO();
        boolean exito = dao.actualizarCamaraKey(nuevaClave);

        if (exito) {
            mostrarMensajeClipper("ÉXITO", "La Cámara Key ha sido actualizada en la base de datos.");
            dispose();
        } else {
            mostrarMensajeClipper("ERROR", "No se pudo guardar la clave en la base de datos.");
        }
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

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "guardar");
        root.getActionMap().put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                guardarNuevaClave();
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
