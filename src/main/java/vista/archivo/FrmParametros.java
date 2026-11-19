package vista.archivo;

import conexion.Conexion;
import dao.ParametrosDAO;
import modelo.ParametrosDTO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FrmParametros extends JDialog {

    private JTextField txtBoleta;
    private JTextField txtFactura;
    private JTextField txtGuiaDespacho;
    private JTextField txtIva;
    private JTextField txtFecha;
    private JLabel lblAtajos;
    private JPanel panelContenedor;
    private JPanel panelInferior;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cCianBorde, cAmarilloTexto, cRojoBarra;
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 12);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public FrmParametros(Window parent) {
        super(parent, " 6.- PARAMETROS Y CORRELATIVOS ", Dialog.ModalityType.APPLICATION_MODAL);
        setSize(460, 315);
        setResizable(false);
        setLocationRelativeTo(parent);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        getContentPane().setBackground(cFondo);
        setLayout(null);

        // Panel contenedor con borde dinámico
        panelContenedor = new JPanel(null);
        panelContenedor.setBounds(15, 15, 415, 195);
        panelContenedor.setBackground(cFondo);
        panelContenedor.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(cCianBorde, 2), " PARAMETROS DEL SISTEMA ",
                0, 0, FUENTE_CONSOLA, cAmarilloTexto
        ));
        ((javax.swing.border.TitledBorder) panelContenedor.getBorder()).setTitleColor(cAmarilloTexto);
        add(panelContenedor);

        // Posicionamiento milimétrico de etiquetas y campos estilo Clipper
        int xLbl = 25, xTxt = 185, y = 30, alto = 22, separacion = 28;

        agregarFila(panelContenedor, "# Boleta       :", txtBoleta = new JTextField(), xLbl, xTxt, y, 120, alto);
        y += separacion;
        agregarFila(panelContenedor, "# Factura     :", txtFactura = new JTextField(), xLbl, xTxt, y, 120, alto);
        y += separacion;
        agregarFila(panelContenedor, "# G. Despach  :", txtGuiaDespacho = new JTextField(), xLbl, xTxt, y, 120, alto);
        y += separacion;
        agregarFila(panelContenedor, "I.V.A. (%)    :", txtIva = new JTextField(), xLbl, xTxt, y, 120, alto);
        y += separacion;
        agregarFila(panelContenedor, "FECHA (Sistema):", txtFecha = new JTextField(), xLbl, xTxt, y, 120, alto);

        // Barra inferior dinámica con atajos
        panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelInferior.setBounds(0, 225, 445, 38);
        panelInferior.setBackground(cRojoBarra);
        panelInferior.setBorder(new LineBorder(cAmarilloTexto, 1));

        lblAtajos = new JLabel();
        lblAtajos.setFont(FUENTE_CONSOLA);
        actualizarTextoAtajos();
        panelInferior.add(lblAtajos);
        add(panelInferior);

        configurarAtajos();
        cargarParametros();
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cCianBorde = new Color(255, 110, 220);
                cAmarilloTexto = new Color(255, 180, 240);
                cRojoBarra = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cCianBorde = new Color(0, 245, 255);
                cAmarilloTexto = new Color(180, 255, 255);
                cRojoBarra = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cCianBorde = new Color(0, 255, 128);
                cAmarilloTexto = new Color(180, 255, 200);
                cRojoBarra = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cCianBorde = new Color(245, 73, 39);
                cAmarilloTexto = new Color(255, 200, 150);
                cRojoBarra = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 139);
                cCianBorde = new Color(0, 255, 255);
                cAmarilloTexto = new Color(255, 255, 0);
                cRojoBarra = new Color(178, 0, 0);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        getContentPane().setBackground(cFondo);

        if (panelContenedor != null) {
            panelContenedor.setBackground(cFondo);
            panelContenedor.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(cCianBorde, 2), " PARAMETROS DEL SISTEMA ",
                    0, 0, FUENTE_CONSOLA, cAmarilloTexto
            ));
            ((javax.swing.border.TitledBorder) panelContenedor.getBorder()).setTitleColor(cAmarilloTexto);

            for (Component comp : panelContenedor.getComponents()) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setForeground(cAmarilloTexto);
                } else if (comp instanceof JTextField) {
                    estilizarCampo((JTextField) comp);
                }
            }
        }

        if (panelInferior != null) {
            panelInferior.setBackground(cRojoBarra);
            panelInferior.setBorder(new LineBorder(cAmarilloTexto, 1));
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Salir</span> &nbsp;|&nbsp; "
                    + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Guardar</span> &nbsp;|&nbsp; "
                    + "<b style='color:#FFFF55;'>(F6)</b> <span style='color:FFFFFF;'>Tema: " + temaActual + "</span></html>");
        }
    }

    private void agregarFila(JPanel panel, String textoLbl, JTextField campo, int xL, int xT, int y, int anchoTxt, int alto) {
        JLabel lbl = new JLabel(textoLbl);
        lbl.setBounds(xL, y, 150, alto);
        lbl.setForeground(cAmarilloTexto);
        lbl.setFont(FUENTE_CONSOLA);
        panel.add(lbl);

        estilizarCampo(campo);
        campo.setBounds(xT, y, anchoTxt, alto);
        panel.add(campo);
    }

    private void estilizarCampo(JTextField campo) {
        if (campo != null) {
            campo.setBackground(cFondo);
            campo.setForeground(cAmarilloTexto);
            campo.setCaretColor(cAmarilloTexto);
            campo.setFont(FUENTE_CONSOLA);
            campo.setBorder(new LineBorder(cCianBorde, 1));
        }
    }

    private void configurarAtajos() {
        JRootPane root = this.getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // [ESC] Cerrar
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F3] Guardar
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "guardar");
        root.getActionMap().put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                guardarParametros();
            }
        });

        // [F6] Ciclar Tema
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");
        root.getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }

    private void cargarParametros() {
        try (Connection con = Conexion.getConnection()) {
            ParametrosDAO dao = new ParametrosDAO();
            ParametrosDTO p = dao.obtenerParametros(con);

            txtBoleta.setText(String.valueOf(p.getBoleta()));
            txtFactura.setText(String.valueOf(p.getFactura()));
            txtGuiaDespacho.setText(String.valueOf(p.getGuia()));
            txtIva.setText(String.format("%.2f", p.getIva()).replace(",", "."));

            if (p.getFecha() == null || p.getFecha().trim().isEmpty()) {
                txtFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                txtFecha.setText(p.getFecha().trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            txtBoleta.setText("0");
            txtFactura.setText("0");
            txtGuiaDespacho.setText("0");
            txtIva.setText("19.00");
            txtFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void guardarParametros() {
        try {
            long boleta = Long.parseLong(txtBoleta.getText().trim());
            long factura = Long.parseLong(txtFactura.getText().trim());
            long guia = Long.parseLong(txtGuiaDespacho.getText().trim());
            double iva = Double.parseDouble(txtIva.getText().trim().replace(",", "."));
            String fecha = txtFecha.getText().trim();

            ParametrosDTO p = new ParametrosDTO(boleta, factura, guia, iva, fecha);

            try (Connection con = Conexion.getConnection()) {
                ParametrosDAO dao = new ParametrosDAO();
                if (dao.guardarParametros(con, p)) {
                    JOptionPane.showMessageDialog(this, "Parámetros guardados correctamente en la BD.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudieron guardar los parámetros.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique que los valores numéricos sean válidos.", "Error de Formato", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
