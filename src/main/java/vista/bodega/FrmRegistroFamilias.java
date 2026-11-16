package vista.bodega;

import modelo.FamiliaDTO;
import dao.FamiliaDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class FrmRegistroFamilias extends JInternalFrame {

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private FamiliaDAO familiaDAO;

    public FrmRegistroFamilias() {
        setTitle("REGISTRO DE FAMILIAS");
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setSize(750, 480);

        familiaDAO = new FamiliaDAO();
        initComponents();
        cargarDatosTabla();
        configurarAtajosTeclado();
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(Color.BLACK); // Fondo Negro Estilo Consola

        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNorte.setBackground(Color.DARK_GRAY);
        panelNorte.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel lblEncabezado = new JLabel("=== FAMILIAS ===", JLabel.CENTER);
        lblEncabezado.setForeground(Color.YELLOW);
        lblEncabezado.setFont(new Font("Monospaced", Font.BOLD, 18));
        panelNorte.add(lblEncabezado);
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        String[] columnas = {"CODIGO", "DESCRIPCION", "IVA ADIC."};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // --- TABLA PERSONALIZADA PARA LÍNEAS VERTICALES DE EXTREMO A EXTREMO ---
        tabla = new JTable(modeloTabla) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Color gris oscuro (dark_grey) para las líneas verticales
                g.setColor(new Color(60, 60, 60));

                // Tomamos la altura total visible del área de la tabla
                int alturaTotal = Math.max(getHeight(), getParent() != null ? getParent().getHeight() : getHeight());

                // Dibujamos las líneas verticales en los límites de cada columna abarcando todo el alto
                int x = 0;
                for (int i = 0; i < getColumnCount() - 1; i++) {
                    x += getColumnModel().getColumn(i).getWidth();
                    g.drawLine(x - 1, 0, x - 1, alturaTotal);
                }
            }
        };

        tabla.setBackground(new Color(15, 15, 15));
        tabla.setForeground(Color.WHITE);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 16));
        tabla.setRowHeight(22);

        // Hacer que las columnas ocupen todo el ancho de la tabla y rellenen el alto si sobra espacio
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setFillsViewportHeight(true);

        // Apagamos las líneas nativas ya que las pintamos de extremo a extremo a medida
        tabla.setShowGrid(false);
        tabla.setShowHorizontalLines(false);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(1, 1));

        // Encabezado de la tabla en Azul Clipper
        tabla.getTableHeader().setBackground(new Color(0, 0, 140));
        tabla.getTableHeader().setForeground(Color.YELLOW);
        tabla.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 16));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 100)));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setBackground(new Color(160, 0, 0)); // Rojo Clipper Abajo
        panelSur.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel lblComandos = new JLabel("(Ins)erta.  (Supr)ime.  (M)odifica.", JLabel.LEFT);
        lblComandos.setForeground(Color.WHITE);
        lblComandos.setFont(new Font("Monospaced", Font.BOLD, 14));
        panelSur.add(lblComandos, BorderLayout.WEST);

        JLabel lblSalir = new JLabel("(ESC) Salir", JLabel.RIGHT);
        lblSalir.setForeground(Color.WHITE);
        lblSalir.setFont(new Font("Monospaced", Font.BOLD, 14));
        panelSur.add(lblSalir, BorderLayout.EAST);

        panelPrincipal.add(panelSur, BorderLayout.SOUTH);
        add(panelPrincipal);
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        List<FamiliaDTO> familias = familiaDAO.listarFamilias();
        for (FamiliaDTO f : familias) {
            modeloTabla.addRow(new Object[]{
                f.getCodigo(),
                f.getDescripcion(),
                String.format("%.0f", f.getIvaAdic())
            });
        }
    }

    private void configurarAtajosTeclado() {
        JComponent root = getRootPane();

        // ESC para salir
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        root.getActionMap().put("salir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // INSERT para insertar
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insertar");
        root.getActionMap().put("insertar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionInsertar();
            }
        });

        // SUPR / DELETE para eliminar
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionEliminar();
            }
        });

        // M para modificar
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionModificar();
            }
        });
    }

    // ==========================================
    // ACCIONES Y MODALES ESTILO CLIPPER
    // ==========================================
    private void accionInsertar() {
        JDialog dialogo = crearDialogoClipper(" INSERTA FAMILIA ");
        JPanel panel = (JPanel) dialogo.getContentPane();
        panel.setLayout(null);

        JLabel lbl1 = new JLabel("Código     :");
        lbl1.setBounds(30, 25, 100, 25);
        lbl1.setForeground(Color.YELLOW);
        lbl1.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl1);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setBounds(140, 25, 80, 25);
        estilizarCampoTexto(txtCodigo);
        panel.add(txtCodigo);

        JLabel lbl2 = new JLabel("Descripción:");
        lbl2.setBounds(30, 65, 100, 25);
        lbl2.setForeground(Color.YELLOW);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl2);

        JTextField txtDesc = new JTextField();
        txtDesc.setBounds(140, 65, 240, 25);
        estilizarCampoTexto(txtDesc);
        panel.add(txtDesc);

        JLabel lbl3 = new JLabel("IVA Adic.  :");
        lbl3.setBounds(30, 105, 100, 25);
        lbl3.setForeground(Color.YELLOW);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl3);

        JTextField txtIva = new JTextField("0");
        txtIva.setBounds(140, 105, 80, 25);
        estilizarCampoTexto(txtIva);
        panel.add(txtIva);

        // Indicador de (ENTER) en el cuadro modal inferior
        JLabel lblAccion = new JLabel("[ENTER] Guardar   [ESC] Cancelar", JLabel.CENTER);
        lblAccion.setBounds(30, 145, 350, 20);
        lblAccion.setForeground(Color.WHITE);
        lblAccion.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(lblAccion);

        final boolean[] guardado = {false};
        JButton btnGuardar = new JButton();
        btnGuardar.addActionListener(e -> {
            try {
                FamiliaDTO nueva = new FamiliaDTO(
                        txtCodigo.getText().trim(),
                        txtDesc.getText().trim().toUpperCase(),
                        Double.parseDouble(txtIva.getText().trim())
                );
                if (familiaDAO.insertar(nueva)) {
                    guardado[0] = true;
                    dialogo.dispose();
                } else {
                    mostrarMensajeClipper("Error al guardar en la base de datos.");
                }
            } catch (Exception ex) {
                mostrarMensajeClipper("Verifique que los datos sean correctos.");
            }
        });
        agregarAccionTecladoDialogo(dialogo, txtIva, btnGuardar);

        dialogo.setVisible(true);
        if (guardado[0]) {
            cargarDatosTabla();
            mostrarMensajeClipper("¡Familia registrada con éxito!");
        }
    }

    private void accionModificar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("¡Seleccione una familia de la tabla!");
            return;
        }

        String codigoActual = modeloTabla.getValueAt(fila, 0).toString();
        String descActual = modeloTabla.getValueAt(fila, 1).toString();
        String ivaActual = modeloTabla.getValueAt(fila, 2).toString();

        JDialog dialogo = crearDialogoClipper(" MODIFICA FAMILIA ");
        JPanel panel = (JPanel) dialogo.getContentPane();
        panel.setLayout(null);

        JLabel lbl1 = new JLabel("Código     : " + codigoActual);
        lbl1.setBounds(30, 25, 300, 25);
        lbl1.setForeground(Color.YELLOW);
        lbl1.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl1);

        JLabel lbl2 = new JLabel("Descripción:");
        lbl2.setBounds(30, 65, 100, 25);
        lbl2.setForeground(Color.YELLOW);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl2);

        JTextField txtDesc = new JTextField(descActual);
        txtDesc.setBounds(140, 65, 240, 25);
        estilizarCampoTexto(txtDesc);
        panel.add(txtDesc);

        JLabel lbl3 = new JLabel("IVA Adic.  :");
        lbl3.setBounds(30, 105, 100, 25);
        lbl3.setForeground(Color.YELLOW);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl3);

        JTextField txtIva = new JTextField(ivaActual);
        txtIva.setBounds(140, 105, 80, 25);
        estilizarCampoTexto(txtIva);
        panel.add(txtIva);

        // Indicador de (ENTER) en el cuadro modal inferior
        JLabel lblAccion = new JLabel("[ENTER] Guardar   [ESC] Cancelar", JLabel.CENTER);
        lblAccion.setBounds(30, 145, 350, 20);
        lblAccion.setForeground(Color.WHITE);
        lblAccion.setFont(new Font("Monospaced", Font.BOLD, 11));
        panel.add(lblAccion);

        final boolean[] modificado = {false};
        JButton btnGuardar = new JButton();
        btnGuardar.addActionListener(e -> {
            try {
                FamiliaDTO mod = new FamiliaDTO(
                        codigoActual,
                        txtDesc.getText().trim().toUpperCase(),
                        Double.parseDouble(txtIva.getText().trim())
                );
                if (familiaDAO.modificar(mod)) {
                    modificado[0] = true;
                    dialogo.dispose();
                } else {
                    mostrarMensajeClipper("Error al actualizar la base de datos.");
                }
            } catch (Exception ex) {
                mostrarMensajeClipper("Verifique los datos numéricos.");
            }
        });
        agregarAccionTecladoDialogo(dialogo, txtIva, btnGuardar);

        dialogo.setVisible(true);
        if (modificado[0]) {
            cargarDatosTabla();
            mostrarMensajeClipper("¡Familia modificada con éxito!");
        }
    }

    private void accionEliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            mostrarMensajeClipper("¡Seleccione una familia de la tabla!");
            return;
        }

        String codigo = modeloTabla.getValueAt(fila, 0).toString();
        String desc = modeloTabla.getValueAt(fila, 1).toString();

        JDialog dialogo = crearDialogoClipper(" SUPRIME FAMILIA ");
        JPanel panel = (JPanel) dialogo.getContentPane();
        panel.setLayout(null);

        JLabel lbl = new JLabel("¿Confirma eliminar: " + desc + "?");
        lbl.setBounds(20, 30, 390, 25);
        lbl.setForeground(Color.YELLOW);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl);

        JLabel lblOpciones = new JLabel("[S] Sí (ENTER)    [N] No (ESC)", JLabel.CENTER);
        lblOpciones.setBounds(20, 80, 390, 25);
        lblOpciones.setForeground(Color.WHITE);
        lblOpciones.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lblOpciones);

        final boolean[] eliminado = {false};

        KeyAdapter kAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (familiaDAO.eliminar(codigo)) {
                        eliminado[0] = true;
                    }
                    dialogo.dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_N || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialogo.dispose();
                }
            }
        };

        dialogo.addKeyListener(kAdapter);
        panel.addKeyListener(kAdapter);
        dialogo.setFocusable(true);

        dialogo.setVisible(true);
        if (eliminado[0]) {
            cargarDatosTabla();
            mostrarMensajeClipper("¡Familia eliminada correctamente!");
        }
    }

    private JDialog crearDialogoClipper(String titulo) {
        JDialog dialogo = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(440, 195); // Altura justa para que entre la etiqueta de [ENTER]
        dialogo.setLocationRelativeTo(this);
        dialogo.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 180), 2));
        dialogo.setContentPane(panel);
        return dialogo;
    }

    private void estilizarCampoTexto(JTextField txt) {
        txt.setBackground(new Color(25, 25, 25));
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setFont(new Font("Monospaced", Font.BOLD, 13));
        txt.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 120)));
    }

    private void agregarAccionTecladoDialogo(JDialog dialogo, JTextField ultimoCampo, JButton btnGuardar) {
        KeyAdapter kAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnGuardar.doClick();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialogo.dispose();
                }
            }
        };
        ultimoCampo.addKeyListener(kAdapter);
        dialogo.addKeyListener(kAdapter);
    }

    private void mostrarMensajeClipper(String mensaje) {
        JDialog dialogo = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), " AVISO ", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(380, 130);
        dialogo.setLocationRelativeTo(this);
        dialogo.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 180), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel(mensaje, JLabel.CENTER);
        lbl.setForeground(Color.YELLOW); // Mensajes de alerta en amarillo brillante
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.CENTER);

        JLabel lblEnter = new JLabel("[ ENTER / ESC: Continuar ]", JLabel.CENTER);
        lblEnter.setForeground(Color.WHITE);
        lblEnter.setFont(new Font("Monospaced", Font.PLAIN, 11));
        panel.add(lblEnter, BorderLayout.SOUTH);

        dialogo.setContentPane(panel);

        KeyAdapter kAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialogo.dispose();
                }
            }
        };
        dialogo.addKeyListener(kAdapter);
        panel.addKeyListener(kAdapter);
        dialogo.setFocusable(true);

        dialogo.setVisible(true);
    }
}
