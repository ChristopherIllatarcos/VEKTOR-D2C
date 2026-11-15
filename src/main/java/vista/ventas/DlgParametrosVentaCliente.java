package vista.ventas;

import controlador.VentaClienteController;
import modelo.VentaClienteDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class DlgParametrosVentaCliente extends JDialog {

    private JTextField txtFechaInicial;
    private JTextField txtFechaFinal;
    private JButton btnGenerar;
    private VentaClienteController controlador;
    private JDesktopPane desktopPane;

    public DlgParametrosVentaCliente(Frame parent, JDesktopPane desktopPane) {
        super(parent, "VEKTOR D2C - INFORME VENTAS POR CLIENTE", true);
        this.desktopPane = desktopPane;
        this.controlador = new VentaClienteController();

        initComponents();
        configurarAtajosTeclado();

        setSize(440, 260);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // Cálculo automático de fechas (Primer día del mes actual y la fecha de hoy)
        LocalDate hoy = LocalDate.now();
        LocalDate primerDiaMes = hoy.with(TemporalAdjusters.firstDayOfMonth());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaHoyStr = hoy.format(dtf);
        String fechaInicioStr = primerDiaMes.format(dtf);

        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(new Color(30, 30, 30));
        panelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitulo = new JLabel("<html><span style='color: #DCDCDC;'>INFORME VENTAS</span> <span style='color: #64B4FF;'>POR CLIENTE</span></html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelPrincipal.add(lblTitulo, gbc);

        JLabel lblFI = new JLabel("Ingrese Fecha Inicial:");
        lblFI.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFI.setForeground(new Color(200, 200, 200));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelPrincipal.add(lblFI, gbc);

        txtFechaInicial = new JTextField(fechaInicioStr, 12);
        txtFechaInicial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panelPrincipal.add(txtFechaInicial, gbc);

        JLabel lblFF = new JLabel("Ingrese Fecha Final :");
        lblFF.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFF.setForeground(new Color(200, 200, 200));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelPrincipal.add(lblFF, gbc);

        txtFechaFinal = new JTextField(fechaHoyStr, 12);
        txtFechaFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelPrincipal.add(txtFechaFinal, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        btnGenerar = new JButton("Generar");
        estilizarBoton(btnGenerar, new Color(100, 180, 255), Color.BLACK);
        btnGenerar.addActionListener(e -> generarInforme());

        JButton btnCancelar = new JButton("Cancelar");
        estilizarBoton(btnCancelar, new Color(60, 60, 60), Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGenerar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panelPrincipal.add(panelBotones, gbc);

        add(panelPrincipal);
    }

    private void estilizarBoton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();
        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "ESC");
        rootPane.getActionMap().put("ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "ENTER");
        rootPane.getActionMap().put("ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnGenerar.doClick();
            }
        });
    }

    private void generarInforme() {
        String fi = txtFechaInicial.getText().trim();
        String ff = txtFechaFinal.getText().trim();

        List<VentaClienteDTO> resultados = controlador.generarInforme(fi, ff);

        if (resultados != null) {
            dispose();
            FrmResultadoVentaCliente frmRes = new FrmResultadoVentaCliente(resultados, fi, ff);

            if (desktopPane != null) {
                desktopPane.add(frmRes);
                try {
                    frmRes.setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                }
                frmRes.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error al consultar los datos.", "VEKTOR D2C", JOptionPane.ERROR_MESSAGE);
        }
    }
}
