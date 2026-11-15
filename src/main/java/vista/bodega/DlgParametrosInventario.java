
package vista.bodega;

import controlador.InventarioController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import modelo.InventarioItemDTO;

public class DlgParametrosInventario extends JDialog {
    private JTextField txtFechaCorte;
    private JCheckBox chkVerCeros;
    private JButton btnGenerar;
    private InventarioController controlador;

    public DlgParametrosInventario(Frame parent) {
        super(parent, "VEKTOR D2C - Inventario de Mercadería", true);
        this.controlador = new InventarioController();
        
        initComponents();
        configurarAtajosTeclado(); // <--- Aquí activamos ESC y Enter global
        
        setSize(420, 260);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(new Color(30, 30, 30));
        panelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Título del diálogo
        JLabel lblTitulo = new JLabel("<html><span style='color: #DCDCDC;'>INVENTARIO DE</span> <span style='color: #64B4FF;'>PRODUCTOS</span></html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelPrincipal.add(lblTitulo, gbc);

        // Campo Fecha al (Al 12/07/2026 estilo Clipper)
        JLabel lblFecha = new JLabel("Al (Fecha de Corte):");
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFecha.setForeground(new Color(200, 200, 200));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panelPrincipal.add(lblFecha, gbc);

        txtFechaCorte = new JTextField("12/07/2026", 12);
        txtFechaCorte.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 1;
        panelPrincipal.add(txtFechaCorte, gbc);

        // Checkbox Ver Saldos Ceros (N / S)
        JLabel lblCeros = new JLabel("Ver Saldos Ceros:");
        lblCeros.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCeros.setForeground(new Color(200, 200, 200));
        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(lblCeros, gbc);

        chkVerCeros = new JCheckBox();
        chkVerCeros.setBackground(new Color(30, 30, 30));
        chkVerCeros.setForeground(new Color(100, 180, 255));
        chkVerCeros.setSelected(false);
        gbc.gridx = 1; gbc.gridy = 2;
        panelPrincipal.add(chkVerCeros, gbc);

        // Botonera inferior (Aceptar / Cancelar)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        btnGenerar = new JButton("Generar Reporte");
        estilizarBoton(btnGenerar, new Color(100, 180, 255), Color.BLACK);
        btnGenerar.addActionListener(e -> generarReporte());

        JButton btnCancelar = new JButton("Cancelar");
        estilizarBoton(btnCancelar, new Color(60, 60, 60), Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGenerar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
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
        
        // 1. Tecla ESC para cerrar la ventana al instante
        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "CERRAR_ESC");
        rootPane.getActionMap().put("CERRAR_ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // 2. Tecla ENTER para activar el botón "Generar Reporte" desde cualquier campo
        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "GENERAR_ENTER");
        rootPane.getActionMap().put("GENERAR_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnGenerar.doClick(); // Simula el clic en el botón de generar
            }
        });
    }

    private void generarReporte() {
        String fecha = txtFechaCorte.getText().trim();
        boolean verCeros = chkVerCeros.isSelected();

        List<InventarioItemDTO> resultados = controlador.cargarReporteInventario(fecha, verCeros);

        if (resultados != null) {
            DlgResultadoInventario dlgResultado = new DlgResultadoInventario((Frame) getParent(), resultados, fecha);
            dispose();
            dlgResultado.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Error al generar el inventario.", "VEKTOR D2C", JOptionPane.ERROR_MESSAGE);
        }
    }
}