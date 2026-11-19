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

    // Componentes estructurales para actualización dinámica de colores
    private JPanel panelPrincipal, panelNorte, panelSur;
    private JScrollPane scrollPane;
    private JLabel lblEncabezado, lblComandos, lblSalir;

    // Variables de Colores Actuales del Tema
    private Color cFondo, cPanelNorte, cEncabezadoTabla, cAcento, cTextoCrema, cBarraSur, cSeleccion;
    private final Font FUENTE_CONSOLA = new Font("Monospaced", Font.BOLD, 15);

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Clásico", "Turquesa Pro", "Esmeralda Terminal","Morado Neón" , "Naranja Fuego"};
    private int indiceTemaActual = 0; // Empieza en Morado Neón por defecto

    public FrmRegistroFamilias() {
        setTitle("2.- REGISTRO DE FAMILIAS");
        setClosable(false);
        setIconifiable(false);
        setMaximizable(false);
        setResizable(false);
        setSize(780, 500);

        familiaDAO = new FamiliaDAO();

        // Aplicar tema inicial
        aplicarTema(listaTemas[indiceTemaActual]);

        initComponents();
        cargarDatosTabla();
        configurarAtajosTeclado();
         // Asignar el icono de Nexum directamente a este JInternalFrame
        try {
            setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono interno: " + e.getMessage());
        }
    }

    // ==========================================
    // SISTEMA DE CAMBIO DE TEMAS [F6]
    // ==========================================
    private void aplicarTema(String nombreTema) {
        switch (nombreTema) {
            case "Turquesa Pro":
                cFondo = new Color(5, 15, 20);
                cPanelNorte = new Color(10, 40, 50);
                cEncabezadoTabla = new Color(0, 80, 95);
                cAcento = new Color(0, 245, 255);
                cTextoCrema = new Color(220, 255, 255);
                cBarraSur = new Color(0, 60, 70);
                cSeleccion = new Color(0, 120, 140);
                break;
            case "Esmeralda Terminal":
                cFondo = new Color(5, 20, 15);
                cPanelNorte = new Color(10, 45, 30);
                cEncabezadoTabla = new Color(15, 80, 50);
                cAcento = new Color(0, 255, 128);
                cTextoCrema = new Color(220, 255, 240);
                cBarraSur = new Color(0, 60, 35);
                cSeleccion = new Color(0, 140, 80);
                break;
            case "Clipper Clásico":
                cFondo = Color.BLACK;
                cPanelNorte = new Color(0, 0, 120);
                cEncabezadoTabla = new Color(0, 0, 168);
                cAcento = new Color(255, 255, 85);
                cTextoCrema = Color.WHITE;
                cBarraSur = new Color(168, 0, 0);
                cSeleccion = new Color(0, 168, 168);
                break;
            case "Naranja Fuego":
                cFondo = new Color(15, 10, 8);
                cPanelNorte = new Color(40, 18, 12);
                cEncabezadoTabla = new Color(80, 28, 18);
                cAcento = new Color(245, 73, 39);
                cTextoCrema = new Color(255, 235, 225);
                cBarraSur = new Color(60, 20, 12);
                cSeleccion = new Color(130, 42, 22);
                break;
            case "Morado Neón":
            default:
                cFondo = new Color(10, 10, 15);
                cPanelNorte = new Color(45, 10, 55);
                cEncabezadoTabla = new Color(90, 10, 120);
                cAcento = new Color(255, 110, 220);
                cTextoCrema = new Color(255, 230, 255);
                cBarraSur = new Color(130, 0, 70);
                cSeleccion = new Color(140, 20, 160);
                break;
        }
    }

    private void ciclarSiguienteTema() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTema(listaTemas[indiceTemaActual]);

        if (panelPrincipal != null) {
            panelPrincipal.setBackground(cFondo);
        }
        if (panelNorte != null) {
            panelNorte.setBackground(cPanelNorte);
            panelNorte.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                    BorderFactory.createEmptyBorder(12, 10, 12, 10)
            ));
        }
        if (lblEncabezado != null) {
            lblEncabezado.setForeground(cAcento);
        }

        if (tabla != null) {
            tabla.setBackground(cFondo);
            tabla.setForeground(cTextoCrema);
            tabla.setSelectionBackground(cSeleccion);
            tabla.setGridColor(new Color(
                    Math.max(0, cFondo.getRed() + 20),
                    Math.max(0, cFondo.getGreen() + 10),
                    Math.max(0, cFondo.getBlue() + 25)
            ));
            tabla.getTableHeader().setBackground(cEncabezadoTabla);
            tabla.getTableHeader().setForeground(cAcento);
            tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));
            tabla.repaint();
        }

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(cFondo);
            scrollPane.setBorder(BorderFactory.createLineBorder(cEncabezadoTabla, 2));
        }

        if (panelSur != null) {
            panelSur.setBackground(cBarraSur);
            panelSur.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 0, 0, 0, cAcento),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }
        actualizarTextoComandos();

        revalidate();
        repaint();
    }

    private void actualizarTextoComandos() {
        String temaActual = listaTemas[indiceTemaActual];
        if (lblComandos != null) {
            lblComandos.setText("(Ins)erta. (Supr)ime. (M)odifica. (F6) Tema: " + temaActual + ".");
        }
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(cFondo);

        // --- PANEL NORTE (Encabezado) ---
        panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNorte.setBackground(cPanelNorte);
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));

        lblEncabezado = new JLabel("=== REGISTRO DE FAMILIAS ===", JLabel.CENTER);
        lblEncabezado.setForeground(cAcento);
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

        // --- TABLA PERSONALIZADA ---
        tabla = new JTable(modeloTabla) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(60, 20, 70));
                int alturaTotal = Math.max(getHeight(), getParent() != null ? getParent().getHeight() : getHeight());
                int x = 0;
                for (int i = 0; i < getColumnCount() - 1; i++) {
                    x += getColumnModel().getColumn(i).getWidth();
                    g.drawLine(x - 1, 0, x - 1, alturaTotal);
                }
            }
        };

        tabla.setBackground(cFondo);
        tabla.setForeground(cTextoCrema);
        tabla.setFont(FUENTE_CONSOLA);
        tabla.setRowHeight(24);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setFillsViewportHeight(true);

        tabla.setShowGrid(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(40, 15, 50));
        tabla.setSelectionBackground(cSeleccion);
        tabla.setSelectionForeground(Color.WHITE);

        tabla.getTableHeader().setBackground(cEncabezadoTabla);
        tabla.getTableHeader().setForeground(cAcento);
        tabla.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 15));
        tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, cAcento));

        scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(cFondo);
        scrollPane.setBorder(BorderFactory.createLineBorder(cEncabezadoTabla, 2));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // --- PANEL SUR (Barra de comandos estilo Clipper) ---
        panelSur = new JPanel(new BorderLayout());
        panelSur.setBackground(cBarraSur);
        panelSur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, cAcento),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        lblComandos = new JLabel("", JLabel.LEFT);
        lblComandos.setForeground(Color.WHITE);
        lblComandos.setFont(new Font("Monospaced", Font.BOLD, 13));
        actualizarTextoComandos();
        panelSur.add(lblComandos, BorderLayout.WEST);

        lblSalir = new JLabel("(ESC) Salir", JLabel.RIGHT);
        lblSalir.setForeground(cTextoCrema);
        lblSalir.setFont(new Font("Monospaced", Font.BOLD, 13));
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
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        root.getActionMap().put("salir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // [F6] Ciclar Tema
        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclar_tema");
        root.getActionMap().put("ciclar_tema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTema();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insertar");
        root.getActionMap().put("insertar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionInsertar();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        root.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionEliminar();
            }
        });

        root.getInputMap(cond).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "modificar");
        root.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accionModificar();
            }
        });
    }

    // ==========================================
    // ACCIONES Y MODALES (DINÁMICOS SEGÚN TEMA)
    // ==========================================
    private void accionInsertar() {
        JDialog dialogo = crearDialogoClipper(" INSERTA FAMILIA ");
        JPanel panel = (JPanel) dialogo.getContentPane();
        panel.setLayout(null);

        JLabel lbl1 = new JLabel("Código     :");
        lbl1.setBounds(30, 25, 100, 25);
        lbl1.setForeground(cAcento);
        lbl1.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl1);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setBounds(140, 25, 90, 25);
        estilizarCampoTexto(txtCodigo);
        panel.add(txtCodigo);

        JLabel lbl2 = new JLabel("Descripción:");
        lbl2.setBounds(30, 65, 100, 25);
        lbl2.setForeground(cAcento);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl2);

        JTextField txtDesc = new JTextField();
        txtDesc.setBounds(140, 65, 250, 25);
        estilizarCampoTexto(txtDesc);
        panel.add(txtDesc);

        JLabel lbl3 = new JLabel("IVA Adic.  :");
        lbl3.setBounds(30, 105, 100, 25);
        lbl3.setForeground(cAcento);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl3);

        JTextField txtIva = new JTextField("0");
        txtIva.setBounds(140, 105, 90, 25);
        estilizarCampoTexto(txtIva);
        panel.add(txtIva);

        JLabel lblAccion = new JLabel("[ENTER] Guardar   [ESC] Cancelar", JLabel.CENTER);
        lblAccion.setBounds(30, 145, 360, 20);
        lblAccion.setForeground(cTextoCrema);
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
        lbl1.setBounds(30, 25, 320, 25);
        lbl1.setForeground(cAcento);
        lbl1.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl1);

        JLabel lbl2 = new JLabel("Descripción:");
        lbl2.setBounds(30, 65, 100, 25);
        lbl2.setForeground(cAcento);
        lbl2.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl2);

        JTextField txtDesc = new JTextField(descActual);
        txtDesc.setBounds(140, 65, 250, 25);
        estilizarCampoTexto(txtDesc);
        panel.add(txtDesc);

        JLabel lbl3 = new JLabel("IVA Adic.  :");
        lbl3.setBounds(30, 105, 100, 25);
        lbl3.setForeground(cAcento);
        lbl3.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(lbl3);

        JTextField txtIva = new JTextField(ivaActual);
        txtIva.setBounds(140, 105, 90, 25);
        estilizarCampoTexto(txtIva);
        panel.add(txtIva);

        JLabel lblAccion = new JLabel("[ENTER] Guardar   [ESC] Cancelar", JLabel.CENTER);
        lblAccion.setBounds(30, 145, 360, 20);
        lblAccion.setForeground(cTextoCrema);
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
        lbl.setBounds(20, 30, 400, 25);
        lbl.setForeground(cAcento);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl);

        JLabel lblOpciones = new JLabel("[S] Sí (ENTER)    [N] No (ESC)", JLabel.CENTER);
        lblOpciones.setBounds(20, 80, 400, 25);
        lblOpciones.setForeground(cTextoCrema);
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
        dialogo.setSize(440, 195);
        dialogo.setLocationRelativeTo(this);
        dialogo.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createLineBorder(cEncabezadoTabla, 2));
        dialogo.setContentPane(panel);
        return dialogo;
    }

    private void estilizarCampoTexto(JTextField txt) {
        txt.setBackground(new Color(
                Math.max(0, cFondo.getRed() + 15),
                Math.max(0, cFondo.getGreen() + 5),
                Math.max(0, cFondo.getBlue() + 20)
        ));
        txt.setForeground(cTextoCrema);
        txt.setCaretColor(cTextoCrema);
        txt.setFont(new Font("Monospaced", Font.BOLD, 13));
        txt.setBorder(BorderFactory.createLineBorder(cEncabezadoTabla));
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
        panel.setBackground(cFondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cEncabezadoTabla, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel(mensaje, JLabel.CENTER);
        lbl.setForeground(cAcento);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.CENTER);

        JLabel lblEnter = new JLabel("[ ENTER / ESC: Continuar ]", JLabel.CENTER);
        lblEnter.setForeground(cTextoCrema);
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
