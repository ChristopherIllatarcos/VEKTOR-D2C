package vista.archivo;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class DlgLicenciaUso extends JDialog {

    private final Font FUENTE_CONSOLA = new Font("Consolas", Font.BOLD, 13);

    private JLabel lblTitulo;
    private JLabel lblInfo;
    private JLabel lblAtajos;
    private JButton btnAceptar;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal;
    private JPanel panelCentro;
    private JPanel panelTextoInterno;
    private JPanel panelSur;
    private JPanel panelBoton;
    private JPanel panelBarraAtajos;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cEncabezado, cAcento, cTextoBlanco, cRojoAccion, cTablaFondo;

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego", "Pink"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public DlgLicenciaUso(Window parent) {
        super(parent, "8.- Licencia de Uso", ModalityType.APPLICATION_MODAL);
        setUndecorated(true); // Sin barra de título de Windows, estilo DOS puro
        setSize(780, 460);    // Ancho y alto ampliados para que no se vea apretado
        setLocationRelativeTo(parent);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponentsCustom();
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
                    " DETALLE DE REGISTRO ",
                    0, 0, FUENTE_CONSOLA, cAcento
            ));
        }

        if (panelTextoInterno != null) {
            panelTextoInterno.setBackground(cFondo);
        }

        if (lblInfo != null) {
            // Actualizar colores del HTML interno para adaptarlos al acento del tema activo
            String hexAcento = String.format("#%02x%02x%02x", cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue());
            String textoLicencia = "<html><div style='color: #FFFFFF; font-family: Consolas; font-size: 13px; line-height: 1.5em;'>"
                    + "<b>SISTEMA &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> Gestión Integral de Bodega y Ventas<br>"
                    + "<b>VERSIÓN &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> 2.5.0 (Modernización Arquitectura Java)<br>"
                    + "<b>CLIENTE / LOCAL:</b> Casa Victoria<br>"
                    + "<b>DESARROLLO &nbsp;&nbsp;&nbsp;&nbsp;:</b> NetCom-ERP / Soporte Técnico & Software<br>"
                    + "<b>ESTADO LICENCIA:</b> Registrado y Licenciado para Uso Exclusivo<br>"
                    + "<hr style='border-color: " + hexAcento + "; margin-top: 10px; margin-bottom: 10px;'>"
                    + "<i style='color: #E0E0E0;'>Este software se encuentra protegido por leyes de derecho de autor. "
                    + "Cualquier duplicación o distribución no autorizada está estrictamente prohibida.</i>"
                    + "</div></html>";
            lblInfo.setText(textoLicencia);
        }

        if (panelSur != null) {
            panelSur.setBackground(cFondo);
        }
        if (panelBoton != null) {
            panelBoton.setBackground(cFondo);
        }

        estilizarBotonClipper(btnAceptar);

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
            lblAtajos.setText("  [Enter] Aceptar    |    [F6] Tema: " + temaActual + "    |    [Esc] Salir  ");
            lblAtajos.setForeground(cAcento);
        }
    }

    private void initComponentsCustom() {
        panelPrincipal = new JPanel(new BorderLayout(0, 10));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cAcento, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // 1. TÍTULO SUPERIOR
        lblTitulo = new JLabel("=== INFORMACIÓN DEL SISTEMA Y LICENCIA DE USO ===", JLabel.CENTER);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 15));
        lblTitulo.setForeground(cAcento);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // 2. CONTENIDO CENTRAL (Detalles de Licencia amplios)
        panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBackground(cFondo);
        panelCentro.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cAcento, 1),
                " DETALLE DE REGISTRO ",
                0, 0, FUENTE_CONSOLA, cAcento
        ));

        // Panel interno con padding para separar el texto del borde del cuadro
        panelTextoInterno = new JPanel(new BorderLayout());
        panelTextoInterno.setBackground(cFondo);
        panelTextoInterno.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String hexAcento = String.format("#%02x%02x%02x", cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue());
        String textoLicencia = "<html><div style='color: #FFFFFF; font-family: Consolas; font-size: 13px; line-height: 1.5em;'>"
                + "<b>SISTEMA &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> Gestión Integral de Bodega y Ventas<br>"
                + "<b>VERSIÓN &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b> 2.5.0 (Modernización Arquitectura Java)<br>"
                + "<b>CLIENTE / LOCAL:</b> Casa Victoria<br>"
                + "<b>DESARROLLO &nbsp;&nbsp;&nbsp;&nbsp;:</b> NetCom-ERP / Soporte Técnico & Software<br>"
                + "<b>ESTADO LICENCIA:</b> Registrado y Licenciado para Uso Exclusivo<br>"
                + "<hr style='border-color: " + hexAcento + "; margin-top: 10px; margin-bottom: 10px;'>"
                + "<i style='color: #E0E0E0;'>Este software se encuentra protegido por leyes de derecho de autor. "
                + "Cualquier duplicación o distribución no autorizada está estrictamente prohibida.</i>"
                + "</div></html>";

        lblInfo = new JLabel(textoLicencia);
        panelTextoInterno.add(lblInfo, BorderLayout.CENTER);
        panelCentro.add(panelTextoInterno, BorderLayout.CENTER);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        // 3. ZONA INFERIOR (Botón y Barra de Atajos)
        panelSur = new JPanel(new BorderLayout(0, 10));
        panelSur.setBackground(cFondo);

        panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setBackground(cFondo);

        btnAceptar = new JButton("[ ACEPTAR ]");
        estilizarBotonClipper(btnAceptar);
        btnAceptar.addActionListener(e -> dispose());
        panelBoton.add(btnAceptar);
        panelSur.add(panelBoton, BorderLayout.NORTH);

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

        // Atajos de teclado globales (ESC, ENTER o F6)
        JRootPane root = getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "aceptar");
        root.getActionMap().put("aceptar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");
        root.getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        SwingUtilities.invokeLater(() -> btnAceptar.requestFocusInWindow());
    }

    private void estilizarBotonClipper(JButton btn) {
        if (btn != null) {
            btn.setFont(FUENTE_CONSOLA);
            btn.setBackground(cTablaFondo);
            btn.setForeground(cAcento);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(cAcento, 1),
                    BorderFactory.createEmptyBorder(5, 20, 5, 20)
            ));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            for (java.awt.event.FocusListener fl : btn.getFocusListeners()) {
                btn.removeFocusListener(fl);
            }

            // Efecto video inverso clásico de consola
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
}
