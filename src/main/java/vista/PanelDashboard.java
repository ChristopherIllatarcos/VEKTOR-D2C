package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author caill
 */
public class PanelDashboard extends javax.swing.JPanel {

    private JLabel lblTitulo;
    private JLabel lblSub;
    private JLabel lblAtajosPie;
    private JPanel contenedor;
    private JPanel panelComandos;

    private Image imagenFondo;

    // Variables de Colores Actuales del Tema
    private Color cColorD2C, cTextoSub, cTextoAccion, cTextoTecla, cBordeAcento;

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Fondo Oscuro", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public PanelDashboard() {
        // Layout principal para centrar todo en la pantalla
        setLayout(new GridBagLayout());
        setFocusable(true);

        // Cargar imagen de fondo desde la ruta especificada
        try {
            ImageIcon iconoFondo = new ImageIcon(getClass().getResource("/img/fondo-negro.jpg"));
            imagenFondo = iconoFondo.getImage();
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen de fondo: " + e.getMessage());
        }

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setOpaque(false);

        lblTitulo = new JLabel();
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

//        // ==========================================
//        // CARGAR EL ICONO AL LADO IZQUIERDO DEL TÍTULO CENTRAL
//        // ==========================================
//        try {
//            ImageIcon iconoCentro = new ImageIcon(getClass().getResource("/iconos/nexum-32.png"));
//            lblTitulo.setIcon(iconoCentro);
//            lblTitulo.setIconTextGap(12);
//            lblTitulo.setHorizontalTextPosition(JLabel.RIGHT);
//        } catch (Exception e) {
//            System.out.println("No se pudo cargar el icono central: " + e.getMessage());
//        }

        actualizarTituloTexto();
        contenedor.add(lblTitulo);
        contenedor.add(Box.createVerticalStrut(10));

        lblSub = new JLabel("");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSub.setForeground(cTextoSub);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedor.add(lblSub);

        contenedor.add(Box.createVerticalStrut(35));

        // Panel con la lista de comandos (dos columnas: Acción y Tecla)
        panelComandos = new JPanel(new GridLayout(0, 2, 40, 12));
        panelComandos.setOpaque(false);
        panelComandos.setAlignmentX(Component.CENTER_ALIGNMENT);

        actualizarListadoComandos();
        contenedor.add(panelComandos);

        contenedor.add(Box.createVerticalStrut(25));

        // Etiqueta de atajo inferior para el cambio de tema [F6]
        lblAtajosPie = new JLabel("", JLabel.CENTER);
        lblAtajosPie.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblAtajosPie.setForeground(cBordeAcento);
        lblAtajosPie.setAlignmentX(Component.CENTER_ALIGNMENT);
        actualizarTextoPie();
        contenedor.add(lblAtajosPie);

        add(contenedor);

        // Escuchador de eventos de teclado para el atajo [F6]
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F6) {
                    ciclarSiguienteTema();
                }
            }
        });

        // Asegurar que el panel reciba el foco al hacer clic
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                requestFocusInWindow();
            }
        });
    }

    // ==========================================
    // SOBREESCRIBIR PAINTCOMPONENT PARA EL FONDO
    // ==========================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagenFondo != null) {
            g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(30, 30, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cColorD2C = new Color(255, 110, 220);
                cTextoSub = new Color(220, 160, 240);
                cTextoAccion = new Color(240, 200, 240);
                cTextoTecla = new Color(255, 110, 220);
                cBordeAcento = new Color(255, 110, 220);
                break;
            case "Turquesa Pro":
                cColorD2C = new Color(0, 245, 255);
                cTextoSub = new Color(150, 230, 240);
                cTextoAccion = new Color(200, 245, 255);
                cTextoTecla = new Color(0, 245, 255);
                cBordeAcento = new Color(0, 245, 255);
                break;
            case "Esmeralda Terminal":
                cColorD2C = new Color(0, 255, 128);
                cTextoSub = new Color(150, 240, 200);
                cTextoAccion = new Color(200, 255, 220);
                cTextoTecla = new Color(0, 255, 128);
                cBordeAcento = new Color(0, 255, 128);
                break;
            case "Naranja Fuego":
                cColorD2C = new Color(245, 73, 39);
                cTextoSub = new Color(240, 190, 150);
                cTextoAccion = new Color(255, 220, 200);
                cTextoTecla = new Color(245, 73, 39);
                cBordeAcento = new Color(245, 73, 39);
                break;
            case "Fondo Oscuro":
            default:
                cColorD2C = new Color(100, 180, 255);
                cTextoSub = new Color(140, 140, 140);
                cTextoAccion = new Color(180, 180, 180);
                cTextoTecla = new Color(100, 180, 255);
                cBordeAcento = new Color(100, 180, 255);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        actualizarTituloTexto();
        if (lblSub != null) {
            lblSub.setForeground(cTextoSub);
        }
        if (lblAtajosPie != null) {
            lblAtajosPie.setForeground(cBordeAcento);
            actualizarTextoPie();
        }

        actualizarListadoComandos();

        revalidate();
        repaint();
    }

    private void actualizarTituloTexto() {
        if (lblTitulo != null && cColorD2C != null) {
            String hexColor = String.format("#%02x%02x%02x", cColorD2C.getRed(), cColorD2C.getGreen(), cColorD2C.getBlue());
            lblTitulo.setText("<html><span style='color: #DCDCDC;'>VEKTOR</span> <span style='color: " + hexColor + ";'>D2C</span></html>");
        }
    }

    private void actualizarTextoPie() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajosPie != null) {
            lblAtajosPie.setText("");
        }
    }

    private void actualizarListadoComandos() {
        if (panelComandos == null) {
            return;
        }
        panelComandos.removeAll();

        String[][] listadoComandos = {
            {"Menú Archivos", "Alt + A"},
            {"Menú Ventas", "Alt + V"},
            {"Menú Compras", "Alt + C"},
            {"Menú Bodegas", "Alt + B"},
            {"Menú Cuenta Corriente", "Alt + T"},
            {"Navegación en Formularios", "Enter / Flechas"},
            {"Cerrar Ventana Activa / Salir", "ESC"}
        };

        for (String[] cmd : listadoComandos) {
            JLabel lblAccion = new JLabel(cmd[0]);
            lblAccion.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblAccion.setForeground(cTextoAccion != null ? cTextoAccion : new Color(180, 180, 180));

            JLabel lblTecla = new JLabel(cmd[1], JLabel.RIGHT);
            lblTecla.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTecla.setForeground(cTextoTecla != null ? cTextoTecla : new Color(100, 180, 255));

            panelComandos.add(lblAccion);
            panelComandos.add(lblTecla);
        }
        panelComandos.revalidate();
        panelComandos.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 812, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
