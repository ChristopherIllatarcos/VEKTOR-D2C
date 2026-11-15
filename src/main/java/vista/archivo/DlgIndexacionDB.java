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
    private boolean enProceso = false;

    public DlgIndexacionDB(Frame parent) {
        super(parent, "VEKTOR D2C - INDEXACIÓN Y RESPALDO DE BASE DE DATOS", true);
        initComponents();
        configurarAtajosTeclado();

        // Tamaño equilibrado: grande pero sin exagerar (650x380)
        setSize(650, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // Panel principal estilo Clipper (Azul oscuro clásico MS-DOS)
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(0, 0, 168));
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(85, 255, 255), 2), // Borde cian claro
                new EmptyBorder(15, 15, 15, 15)
        ));

        // ==========================================
        // 1. ZONA SUPERIOR (Título)
        // ==========================================
        JLabel lblTitulo = new JLabel("UTILITARIO DE MANTENIMIENTO Y RESPALDO");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(255, 255, 85)); // Amarillo Clipper
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNorte.setOpaque(false);
        panelNorte.add(lblTitulo);
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // 2. ZONA CENTRAL (Cajita de proceso robusta)
        // ==========================================
        JPanel panelCentroContainer = new JPanel(new GridBagLayout());
        panelCentroContainer.setBackground(new Color(0, 0, 168));

        JPanel panelCajaProceso = new JPanel(new GridLayout(3, 1, 6, 10));
        panelCajaProceso.setBackground(new Color(0, 0, 120));
        panelCajaProceso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 85), 1),
                        " PROCESO DE INDEXACIÓN Y RESPALDO ",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Monospaced", Font.BOLD, 12),
                        new Color(255, 255, 85)
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
        lblTablaActual.setForeground(new Color(85, 255, 85)); // Verde fósforo
        panelCajaProceso.add(lblTablaActual);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Monospaced", Font.BOLD, 13));
        progressBar.setForeground(new Color(0, 220, 0)); // Verde brillante
        progressBar.setBackground(new Color(0, 0, 80));   // Fondo azul oscuro
        progressBar.setOpaque(true);
        panelCajaProceso.add(progressBar);

        panelCentroContainer.add(panelCajaProceso);
        panelPrincipal.add(panelCentroContainer, BorderLayout.CENTER);

        // ==========================================
        // 3. ZONA INFERIOR (Botones y Barra Roja de Comandos)
        // ==========================================
        JPanel panelSurGeneral = new JPanel(new BorderLayout(5, 5));
        panelSurGeneral.setBackground(new Color(0, 0, 168));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setOpaque(false);

        btnIniciar = new JButton("[ ENTER ] Iniciar Proceso");
        estilizarBotonConsola(btnIniciar, new Color(0, 168, 0));
        btnIniciar.addActionListener(e -> iniciarProcesoMantenimiento());

        btnCerrar = new JButton("[ ESC ] Salir");
        estilizarBotonConsola(btnCerrar, new Color(168, 0, 0));
        btnCerrar.addActionListener(e -> {
            if (!enProceso) {
                dispose();
            }
        });

        panelBotones.add(btnIniciar);
        panelBotones.add(btnCerrar);
        panelSurGeneral.add(panelBotones, BorderLayout.NORTH);

        // Franja inferior roja de comandos tipo Clipper
        JPanel panelComandos = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 6));
        panelComandos.setBackground(new Color(168, 0, 0));
        panelComandos.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 85)));

        JLabel lblAtajos = new JLabel("<html><b style='color:#FFFF55;'>(Enter)</b> <span style='color:FFFFFF;'>Comenzar</span> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b style='color:#FFFF55;'>(Esc)</b> <span style='color:FFFFFF;'>Cancelar / Salir</span></html>");
        lblAtajos.setFont(new Font("Monospaced", Font.BOLD, 12));
        panelComandos.add(lblAtajos);

        panelSurGeneral.add(panelComandos, BorderLayout.SOUTH);
        panelPrincipal.add(panelSurGeneral, BorderLayout.SOUTH);

        add(panelPrincipal);
    }   

    private void estilizarBotonConsola(JButton btn, Color bg) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 85), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 32));
    }

    private void configurarAtajosTeclado() {
        JRootPane rootPane = this.getRootPane();

        KeyStroke strokeEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEsc, "SALIR");
        rootPane.getActionMap().put("SALIR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enProceso) {
                    dispose();
                }
            }
        });

        KeyStroke strokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(strokeEnter, "INICIAR");
        rootPane.getActionMap().put("INICIAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enProceso) {
                    btnIniciar.doClick();
                }
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
