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

    // Estética Clipper Clásica
    private final Color COLOR_AZUL_FONDO = new Color(0, 0, 139);      // Azul clásico Clipper
    private final Color COLOR_AMARILLO = new Color(255, 255, 0);       // Amarillo brillante
    private final Color COLOR_CIAN = new Color(0, 255, 255);           // Cian para bordes
    private final Color COLOR_ROJO_BARRA = new Color(178, 0, 0);       // Rojo barra inferior
    private final Font FUENTE_CONSOLA = new Font("Courier New", Font.BOLD, 12);

    public FrmParametros(Window parent) {
        super(parent, " 6.- PARAMETROS Y CORRELATIVOS ", Dialog.ModalityType.APPLICATION_MODAL);
        setSize(460, 305);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(COLOR_AZUL_FONDO);
        setLayout(null);

        // Panel contenedor con borde cian
        JPanel panelContenedor = new JPanel(null);
        panelContenedor.setBounds(15, 15, 415, 195);
        panelContenedor.setBackground(COLOR_AZUL_FONDO);
        panelContenedor.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_CIAN, 2), " PARAMETROS DEL SISTEMA ",
                0, 0, FUENTE_CONSOLA, COLOR_AMARILLO
        ));
        ((javax.swing.border.TitledBorder) panelContenedor.getBorder()).setTitleColor(COLOR_AMARILLO);
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

        // Barra inferior roja auténtica con atajos
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelInferior.setBounds(0, 225, 445, 32);
        panelInferior.setBackground(COLOR_ROJO_BARRA);
        panelInferior.setBorder(new LineBorder(COLOR_AMARILLO, 1));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cancelar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(F3)</b> <span style='color:FFFFFF;'>Guardar Parámetros</span></html>");
        lblAtajos.setFont(FUENTE_CONSOLA);
        panelInferior.add(lblAtajos);
        add(panelInferior);

        configurarAtajos();
        cargarParametros();
    }

    private void agregarFila(JPanel panel, String textoLbl, JTextField campo, int xL, int xT, int y, int anchoTxt, int alto) {
        JLabel lbl = new JLabel(textoLbl);
        lbl.setBounds(xL, y, 150, alto);
        lbl.setForeground(COLOR_AMARILLO);
        lbl.setFont(FUENTE_CONSOLA);
        panel.add(lbl);

        estilizarCampo(campo);
        campo.setBounds(xT, y, anchoTxt, alto);
        panel.add(campo);
    }

    private void estilizarCampo(JTextField campo) {
        campo.setBackground(COLOR_AZUL_FONDO);
        campo.setForeground(COLOR_AMARILLO);
        campo.setCaretColor(COLOR_AMARILLO);
        campo.setFont(FUENTE_CONSOLA);
        campo.setBorder(new LineBorder(COLOR_CIAN, 1));
    }

    private void configurarAtajos() {
        JRootPane root = this.getRootPane();

        // [ESC] Cerrar
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar");
        root.getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F3] Guardar
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "guardar");
        root.getActionMap().put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                guardarParametros();
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
