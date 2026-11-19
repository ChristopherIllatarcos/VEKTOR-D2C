package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public class SplashNexum extends JWindow {

    private int progreso = 0;
    private String mensajeEstado = "Iniciando núcleo del sistema...";
    private ImageIcon imagenFondo;

    public SplashNexum() {
        // Tamaño proporcional a la imagen de 900x520
        setSize(750, 433);
        setLocationRelativeTo(null); // Centrado en pantalla

        // Carga de la imagen de fondo limpia (sin barra estática)
        try {
            java.net.URL urlFondo = getClass().getResource("/vektor/vektor-splash-900x520-sin-barra.png");
            if (urlFondo != null) {
                imagenFondo = new ImageIcon(urlFondo);
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen de fondo del splash: " + e.getMessage());
        }

        JPanel panelDibujo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Activar antialiasing para bordes y textos ultra nítidos
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                // 1. Dibujar la imagen de fondo escalada exactamente a la ventana
                if (imagenFondo != null) {
                    g2.drawImage(imagenFondo.getImage(), 0, 0, w, h, this);
                } else {
                    // Fondo de respaldo por si no encuentra la imagen
                    g2.setColor(new Color(15, 23, 42));
                    g2.fillRect(0, 0, w, h);
                }

                // 2. Borde sutil alrededor de la ventana
                g2.setColor(new Color(50, 120, 200, 100));
                g2.drawRect(0, 0, w - 1, h - 1);

                // 3. Coordenadas de la barra de progreso (posicionada abajo, donde indica la imagen)
                int margenX = 50;
                int barraY = h - 65;
                int anchoBarra = w - (margenX * 2);
                int altoBarra = 6;

                // Fondo de la barra (píldora semi-transparente oscura)
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fill(new RoundRectangle2D.Double(margenX, barraY, anchoBarra, altoBarra, 4, 4));

                // Barra de progreso activa (relleno blanco brillante o azulado)
                int anchoActual = (int) ((progreso / 100.0) * anchoBarra);
                if (anchoActual > 0) {
                    g2.setColor(new Color(0, 220, 255)); // Color cian brillante corporativo
                    g2.fill(new RoundRectangle2D.Double(margenX, barraY, anchoActual, altoBarra, 4, 4));
                }

                // 4. Mensaje de estado dinámico a la izquierda de la barra
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(new Color(200, 215, 235));
                g2.drawString(mensajeEstado, margenX, barraY - 10);

                // 5. Porcentaje a la derecha de la barra
                String textoProgreso = progreso + "%";
                int anchoTextoProgreso = g2.getFontMetrics().stringWidth(textoProgreso);
                g2.drawString(textoProgreso, margenX + anchoBarra - anchoTextoProgreso, barraY - 10);
            }
        };

        setContentPane(panelDibujo);
    }

    /**
     * Ejecuta el Splash durante 10 segundos exactos y luego abre el sistema.
     *
     * @param alFinalizar Código a ejecutar al terminar la carga.
     */
    public void iniciarCarga(Runnable alFinalizar) {
        setVisible(true);

        // Hilo secundario para no congelar la interfaz mientras avanza la barra
        new Thread(() -> {
            try {
                // 100 pasos x 100 milisegundos = 10,000 ms (10 segundos exactos)
                for (int i = 0; i <= 100; i++) {
                    progreso = i;

                    // Mensajes realistas de carga según el avance
                    switch (i) {
                        case 5 -> mensajeEstado = "Cargando configuración y parámetros...";
                        case 25 -> mensajeEstado = "Estableciendo conexión segura con base de datos...";
                        case 50 -> mensajeEstado = "Inicializando módulos de Ventas e Inventario...";
                        case 75 -> mensajeEstado = "Cargando temas visuales y controladores de teclado...";
                        case 92 -> mensajeEstado = "Preparando entorno de trabajo...";
                        default -> {
                        }
                    }

                    // Repintar barra de progreso en el hilo gráfico
                    SwingUtilities.invokeLater(this::repaint);

                    Thread.sleep(300); // Pausa de 0.1 segundos por cada 1%
                }

                // Cerrar splash y abrir la ventana principal
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    if (alFinalizar != null) {
                        alFinalizar.run();
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
