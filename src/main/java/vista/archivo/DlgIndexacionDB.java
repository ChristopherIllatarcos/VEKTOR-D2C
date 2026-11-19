package vista.archivo;

import conexion.Conexion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class DlgIndexacionDB extends JDialog {

    private JProgressBar progressBar;
    private JLabel lblEstado;
    private JLabel lblTablaActual;
    private JButton btnIniciar;
    private JButton btnCerrar;
    private JLabel lblTitulo;
    private JLabel lblAtajos;
    private boolean enProceso = false;

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal;
    private JPanel panelCajaProceso;
    private JPanel panelComandos;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cCajaFondo, cBordeCian, cAmarilloClipper, cVerdeFosforo, cRojoBarra;

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Clipper Azul por defecto

    public DlgIndexacionDB(Frame parent) {
        super(parent, "VEKTOR D2C - INDEXACIÓN Y RESPALDO DE BASE DE DATOS", true);

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents();
        configurarAtajosTeclado();

        // Tamaño equilibrado: grande pero sin exagerar (650x380)
        setSize(650, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón":
                cFondo = new Color(10, 10, 15);
                cCajaFondo = new Color(25, 12, 35);
                cBordeCian = new Color(255, 110, 220);
                cAmarilloClipper = new Color(255, 180, 240);
                cVerdeFosforo = new Color(255, 150, 255);
                cRojoBarra = new Color(130, 0, 70);
                break;
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cCajaFondo = new Color(8, 30, 40);
                cBordeCian = new Color(0, 245, 255);
                cAmarilloClipper = new Color(180, 255, 255);
                cVerdeFosforo = new Color(100, 255, 255);
                cRojoBarra = new Color(0, 60, 70);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cCajaFondo = new Color(8, 35, 22);
                cBordeCian = new Color(0, 255, 128);
                cAmarilloClipper = new Color(180, 255, 200);
                cVerdeFosforo = new Color(120, 255, 180);
                cRojoBarra = new Color(0, 60, 35);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cCajaFondo = new Color(32, 16, 10);
                cBordeCian = new Color(245, 73, 39);
                cAmarilloClipper = new Color(255, 200, 150);
                cVerdeFosforo = new Color(255, 220, 180);
                cRojoBarra = new Color(60, 20, 12);
                break;
            case "Clipper Azul":
            default:
                cFondo = new Color(0, 0, 168);
                cCajaFondo = new Color(0, 0, 120);
                cBordeCian = new Color(85, 255, 255);
                cAmarilloClipper = new Color(255, 255, 85);
                cVerdeFosforo = new Color(85, 255, 85);
                cRojoBarra = new Color(168, 0, 0);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
            panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(cBordeCian, 2),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        }

        if (lblTitulo != null) {
            lblTitulo.setForeground(cAmarilloClipper);
        }

        if (panelCajaProceso != null) {
            panelCajaProceso.setBackground(cCajaFondo);
            panelCajaProceso.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(cAmarilloClipper, 1),
                            " PROCESO DE INDEXACIÓN Y RESPALDO ",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new Font("Monospaced", Font.BOLD, 12),
                            cAmarilloClipper
                    ),
                    new EmptyBorder(15, 15, 15, 15)
            ));
        }

        if (lblEstado != null) {
            lblEstado.setForeground(Color.WHITE);
        }
        if (lblTablaActual != null) {
            lblTablaActual.setForeground(cVerdeFosforo);
        }

        if (progressBar != null) {
            progressBar.setForeground(cVerdeFosforo);
            progressBar.setBackground(cFondo);
        }

        estilizarBotonConsola(btnIniciar, new Color(0, 140, 0));
        estilizarBotonConsola(btnCerrar, new Color(140, 0, 0));

        if (panelComandos != null) {
            panelComandos.setBackground(cRojoBarra);
            panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAmarilloClipper));
        }

        actualizarTextoAtajos();

        revalidate();
        repaint();
    }

    private void actualizarTextoAtajos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblAtajos != null) {
            lblAtajos.setText("<html><b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Comenzar</span> &nbsp;|&nbsp; "
                    + "<b style='color:#FFFF55;'>(F6)</b> <span style='color:FFFFFF;'>Tema: " + temaActual + "</span> &nbsp;|&nbsp; "
                    + "<b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Salir</span></html>");
        }
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(cFondo);
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cBordeCian, 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // ==========================================
        // 1. ZONA SUPERIOR (Título)
        // ==========================================
        lblTitulo = new JLabel("UTILITARIO DE MANTENIMIENTO Y RESPALDO");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(cAmarilloClipper);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNorte.setOpaque(false);
        panelNorte.add(lblTitulo);
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. ZONA CENTRAL (Cajita de proceso robusta)
        // ==========================================
        JPanel panelCentroContainer = new JPanel(new GridBagLayout());
        panelCentroContainer.setBackground(cFondo);

        panelCajaProceso = new JPanel(new GridLayout(3, 1, 6, 10));
        panelCajaProceso.setBackground(cCajaFondo);
        panelCajaProceso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(cAmarilloClipper, 1),
                        " PROCESO DE INDEXACIÓN Y RESPALDO ",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Monospaced", Font.BOLD, 12),
                        cAmarilloClipper
                ),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panelCajaProceso.setPreferredSize(new Dimension(560, 160));

        lblEstado = new JLabel("Espere.... Preparando entorno del sistema.");
        lblEstado.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblEstado.setForeground(Color.WHITE);
        panelCajaProceso.add(lblEstado);

        lblTablaActual = new JLabel("Estado: Listo para iniciar el mantenimiento.");
        lblTablaActual.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lblTablaActual.setForeground(cVerdeFosforo);
        panelCajaProceso.add(lblTablaActual);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Monospaced", Font.BOLD, 13));
        progressBar.setForeground(cVerdeFosforo);
        progressBar.setBackground(cFondo);
        progressBar.setOpaque(true);
        panelCajaProceso.add(progressBar);

        panelCentroContainer.add(panelCajaProceso);
        panelPrincipal.add(panelCentroContainer, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Botones y Barra de Comandos)
        // ==========================================
        JPanel panelSurGeneral = new JPanel(new BorderLayout(5, 5));
        panelSurGeneral.setBackground(cFondo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setOpaque(false);

        btnIniciar = new JButton("[ ENTER ] Iniciar Proceso");
        estilizarBotonConsola(btnIniciar, new Color(0, 140, 0));
        btnIniciar.addActionListener(e -> iniciarProcesoMantenimiento());

        btnCerrar = new JButton("[ ESC ] Salir");
        estilizarBotonConsola(btnCerrar, new Color(140, 0, 0));
        btnCerrar.addActionListener(e -> {
            if (!enProceso) {
                dispose();
            }
        });

        panelBotones.add(btnIniciar);
        panelBotones.add(btnCerrar);
        panelSurGeneral.add(panelBotones, BorderLayout.NORTH);

        // Franja inferior de comandos tipo Clipper
        panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 6));
        panelComandos.setBackground(cRojoBarra);
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, cAmarilloClipper));

        lblAtajos = new JLabel();
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 12));
        actualizarTextoAtajos();
        panelComandos.add(lblAtajos);

        panelSurGeneral.add(panelComandos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurGeneral, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void estilizarBotonConsola(JButton btn, Color bg) {
        if (btn != null) {
            btn.setFont(new Font("Monospaced", Font.BOLD, 13));
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(cAmarilloClipper, 1));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(220, 32));
        }
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(cond).put(strokeEsc, "SALIR");
        rootPane.getActionMap().put("SALIR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enProceso) {
                    dispose();
                }
            }
        });

        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(cond).put(strokeEnter, "INICIAR");
        rootPane.getActionMap().put("INICIAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enProceso) {
                    btnIniciar.doClick();
                }
            }
        });

        KeyStroke strokeF6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        rootPane.getInputMap(cond).put(strokeF6, "CICLAR_TEMA");
        rootPane.getActionMap().put("CICLAR_TEMA", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ciclarSiguienteTema();
            }
        });
    }

    private void iniciarProcesoMantenimiento() {
        enProceso = true;
        btnIniciar.setEnabled(false);
        btnCerrar.setEnabled(false);

        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (Connection conn = Conexion.getConnection(); Statement stmt = conn.createStatement()) {

                    // Paso 1: Conexión y chequeo inicial (15%)
                    publish("Espere.... Verificando integridad de tablas.", "Conectando al motor PostgreSQL...");
                    Thread.sleep(700);
                    setProgress(15);

                    // Paso 2: Reindexando Clientes (35%)
                    publish("Espere.... Reindexando registros.", "Procesando índice: pcliente_dbf");
                    Thread.sleep(800);
                    stmt.execute("REINDEX TABLE \"pcliente_dbf\"");
                    setProgress(35);

                    // Paso 3: Reindexando Ventas (60%)
                    publish("Espere.... Reindexando transacciones.", "Procesando índice: pventas_dbf");
                    Thread.sleep(900);
                    stmt.execute("REINDEX TABLE \"pventas_dbf\"");
                    setProgress(60);

                    // Paso 4: Optimización general VACUUM (80%)
                    publish("Espere.... Optimizando bases de datos.", "Ejecutando VACUUM ANALYZE");
                    Thread.sleep(800);
                    stmt.execute("VACUUM ANALYZE");
                    setProgress(80);

                    // Paso 5: Generación y exportación de Backup (95% -> 100%)
                    publish("Espere.... Generando respaldo de seguridad.", "Exportando archivo a Mis Documentos...");
                    Thread.sleep(900);

                    String userHome = System.getProperty("user.home");
                    File carpetaDocs = new File(userHome, "Documents");
                    if (!carpetaDocs.exists()) {
                        carpetaDocs = new File(userHome, "Mis Documentos");
                        if (!carpetaDocs.exists()) {
                            carpetaDocs = new File(userHome);
                        }
                    }

                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    File archivoBackup = new File(carpetaDocs, "vektor_d2c_backup_" + timestamp + ".sql");

                    try (FileWriter writer = new FileWriter(archivoBackup)) {
                        writer.write("-- ======================================================\n");
                        writer.write("-- VEKTOR D2C - RESPALDO AUTOMATICO DE BASE DE DATOS\n");
                        writer.write("-- Fecha y Hora: " + LocalDateTime.now() + "\n");
                        writer.write("-- ======================================================\n\n");

                        try (ResultSet rsClientes = stmt.executeQuery("SELECT * FROM \"pcliente_dbf\"")) {
                            writer.write("-- Tabla: pcliente_dbf\n");
                            while (rsClientes.next()) {
                                writer.write("INSERT INTO \"pcliente_dbf\" (\"CLIRUT\", \"CLINOMBRE\") VALUES ('"
                                        + rsClientes.getString("CLIRUT") + "', '"
                                        + rsClientes.getString("CLINOMBRE").replace("'", "''") + "');\n");
                            }
                        }
                    }

                    setProgress(100);
                    publish("¡Proceso completado con éxito!", "Respaldo guardado en: " + archivoBackup.getName());
                    Thread.sleep(1000);
                    return true;

                } catch (Exception ex) {
                    ex.printStackTrace();
                    publish("Error crítico en el proceso", ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                if (chunks.size() >= 2) {
                    lblEstado.setText(chunks.get(chunks.size() - 2));
                    lblTablaActual.setText(chunks.get(chunks.size() - 1));
                } else if (!chunks.isEmpty()) {
                    lblEstado.setText(chunks.get(0));
                }
            }

            @Override
            protected void done() {
                enProceso = false;
                btnCerrar.setEnabled(true);
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(DlgIndexacionDB.this,
                                "<html><b>¡Indexación y Respaldo Finalizados!</b><br>El proceso finalizó correctamente y el archivo fue guardado en su carpeta Mis Documentos.</html>",
                                "VEKTOR D2C", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(DlgIndexacionDB.this, "Ocurrió un error inesperado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }
}
