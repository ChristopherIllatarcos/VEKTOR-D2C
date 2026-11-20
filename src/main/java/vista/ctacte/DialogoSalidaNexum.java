package vista; // Ajusta el paquete según corresponda en tu proyecto

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DialogoSalidaNexum extends JDialog {

    private final JProgressBar barra;
    private final JLabel lblEstado;
    private final int duracionMs;

    public DialogoSalidaNexum(Window padre, int duracionMs,
            Color fondo, Color borde, Color textoBlanco, Color textoVerde, Color barraFondo, Font fuente) {
        super(padre, "Finalizando sistema", Dialog.ModalityType.APPLICATION_MODAL);
        this.duracionMs = Math.max(1000, duracionMs);

        Color cFondo = fondo != null ? fondo : new Color(0, 0, 132);
        Color cBorde = borde != null ? borde : Color.CYAN;
        Color cBlanco = textoBlanco != null ? textoBlanco : Color.WHITE;
        Color cVerde = textoVerde != null ? textoVerde : Color.GREEN;
        Color cBarraBg = barraFondo != null ? barraFondo : Color.BLACK;
        Font fConsola = fuente != null ? fuente : new Font("Courier New", Font.BOLD, 13);

        setUndecorated(true);
        setSize(440, 140);
        setLocationRelativeTo(padre);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cBorde, 2),
                new EmptyBorder(16, 18, 16, 18)
        ));

        lblEstado = new JLabel("Cerrando programa... 0%", SwingConstants.CENTER);
        lblEstado.setFont(fConsola);
        lblEstado.setForeground(cBlanco);

        barra = new JProgressBar(0, 100);
        barra.setValue(0);
        barra.setStringPainted(false);
        barra.setFont(fConsola);
        barra.setForeground(cVerde);
        barra.setBackground(cBarraBg);
        barra.setBorder(new LineBorder(cBorde, 1));
        barra.setPreferredSize(new Dimension(380, 12));

        panel.add(lblEstado, BorderLayout.NORTH);
        panel.add(barra, BorderLayout.CENTER);
        setContentPane(panel);
    }

    public void iniciarYFinalizarPrograma() {
        final int intervaloMs = 50;
        final long inicioMs = System.currentTimeMillis();

        Timer timer = new Timer(intervaloMs, e -> {
            long transcurrido = System.currentTimeMillis() - inicioMs;
            int progreso = (int) Math.min(100, (transcurrido * 100L) / duracionMs);

            barra.setValue(progreso);
            lblEstado.setText("Cerrando programa... " + progreso + "%");

            if (progreso >= 100) {
                ((Timer) e.getSource()).stop();
                dispose();
                Window owner = getOwner();
                if (owner != null) {
                    owner.dispose();
                }
                System.exit(0);
            }
        });

        timer.setInitialDelay(0);
        timer.start();
        setVisible(true);
    }
}