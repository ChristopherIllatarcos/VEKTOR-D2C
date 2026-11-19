package vista;

import vista.ctacte.FrmEstadisticasClientes;
import vista.ventas.FrmArchivoClientes;
import com.formdev.flatlaf.FlatDarkLaf;
import dao.CuentaCorrienteDAO;
import dao.ProductoVentaDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import vista.archivo.DlgAccesosFunciones;
import vista.archivo.DlgCamaraKey;
import vista.archivo.DlgFoliadorHojas;
import vista.archivo.DlgIndexacionDB;
import vista.archivo.DlgLicenciaUso;
import vista.archivo.DlgUsuariosSistema;
import vista.archivo.FrmParametros;
import vista.bodega.DlgProductos;
import vista.bodega.FrmInventarioCritico;
import vista.bodega.FrmResultadoInventario;
import vista.bodega.FrmRotacionVentas;
import vista.bodega.FrmVentasPorFamilia;
import vista.compras.FrmIngresoCompra;
import vista.compras.FrmLibroCompras;
import vista.compras.FrmProveedores;
import vista.ctacte.FrmCarteraCheques;
import vista.ctacte.FrmCartolaCliente;
import vista.ctacte.FrmCartolaPorFecha;
import vista.ctacte.FrmCuentasCobrarAlfabetico;
import vista.ctacte.FrmEliminarRecibo;
import vista.ventas.FrmComisionesCobranza;
import vista.ventas.FrmComisionesVentas;
import vista.ventas.FrmConsultaDocumento;
import vista.ventas.FrmFichaVendedores;
import vista.ventas.FrmHojaRuta;
import vista.ventas.FrmMantencionVentas;
import vista.ventas.FrmVenta;

/**
 *
 * @author caill
 */
public class FrmPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmPrincipal.class.getName());

    private JDesktopPane escritorio = new JDesktopPane();
    private final CuentaCorrienteDAO dao = new CuentaCorrienteDAO();

    private JLabel lblTitulo;
    private Color cColorTema = new Color(100, 180, 255);

    // Listas para almacenar los componentes del menú y submenús y cambiarles el color en vivo
    private final List<JMenu> listaMenus = new ArrayList<>();
    private final List<JMenuItem> listaSubMenus = new ArrayList<>();

    // Sistema de Temas Ciclos [F6]
    private final String[] listaTemas = {"Clipper Azul", "Morado Neón", "Turquesa Pro", "Esmeralda Terminal", "Naranja Fuego"};
    private int indiceTemaActual = 0;

    public FrmPrincipal() {
        initComponents();

        // 1. Configuración de la ventana principal
        setMinimumSize(new Dimension(1024, 768));
        setSize(1024, 768);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1.5. Cargar la familia de iconos nexum para barra de tareas y ventanas (Adiós Java Icon)
        java.util.List<Image> iconosApp = new java.util.ArrayList<>();
        try {
            iconosApp.add(new ImageIcon(getClass().getResource("/vektor/cubo-azul-16.png")).getImage());
            iconosApp.add(new ImageIcon(getClass().getResource("/vektor/cubo-azul-32.png")).getImage());
            iconosApp.add(new ImageIcon(getClass().getResource("/vektor/cubo-azul-64.png")).getImage());
            iconosApp.add(new ImageIcon(getClass().getResource("/vektor/cubo-azul-128.png")).getImage());
            setIconImages(iconosApp);
        } catch (Exception e) {
            System.out.println("No se pudieron cargar los iconos de la aplicación: " + e.getMessage());
        }

        // 2. Establecer el JDesktopPane como el contenedor principal (ContentPane)
        setContentPane(escritorio);
        escritorio.setLayout(new BorderLayout());

        // 3. Cargar la barra de menú superior
        configurarMenu();

        // 4. Configurar atajo global [F6] para cambio de tema en la ventana principal
        configurarAtajoTemaF6();

        // 5. Aplicar colores iniciales a menús y títulos
        aplicarTemaPrincipal(listaTemas[indiceTemaActual]);

        // 6. Colocar el Panel Dashboard de bienvenida al centro del escritorio
        PanelDashboard dashboard = new PanelDashboard();
        escritorio.add(dashboard, BorderLayout.CENTER);

        // 7. Listeners para restaurar y minimizar
        minimizarPrincipal();
        verificarRespaldoAutomatico();

        setVisible(true);
    }

    private void configurarAtajoTemaF6() {
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "ciclarTemaF6");
        root.getActionMap().put("ciclarTemaF6", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ciclarSiguienteTemaPrincipal();
            }
        });
    }

    private void ciclarSiguienteTemaPrincipal() {
        indiceTemaActual = (indiceTemaActual + 1) % listaTemas.length;
        aplicarTemaPrincipal(listaTemas[indiceTemaActual]);
    }

    private void aplicarTemaPrincipal(String nombreTema) {
        switch (nombreTema) {
            case "Morado Neón" ->
                cColorTema = new Color(255, 110, 220);
            case "Turquesa Pro" ->
                cColorTema = new Color(0, 245, 255);
            case "Esmeralda Terminal" ->
                cColorTema = new Color(0, 255, 128);
            case "Naranja Fuego" ->
                cColorTema = new Color(245, 73, 39);
            case "Clipper Azul" ->
                cColorTema = new Color(100, 180, 255);
            default ->
                cColorTema = new Color(100, 180, 255);
        }

        // Aplicar color al título de la barra superior
        if (lblTitulo != null) {
            lblTitulo.setForeground(cColorTema);
        }

        // Aplicar color a los menús principales (VENTAS, COMPRAS, etc.)
        for (JMenu menu : listaMenus) {
            menu.setForeground(cColorTema);
        }

        // Aplicar color a los submenús
        for (JMenuItem item : listaSubMenus) {
            item.setForeground(cColorTema);
        }

        repaint();
    }

    private void minimizarPrincipal() {
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                if (getJMenuBar() != null) {
                    getJMenuBar().revalidate();
                    getJMenuBar().repaint();
                }
                repaint();
            }
        });

        this.addWindowStateListener(e -> {
            if ((Frame.NORMAL) != 0 || (e.getNewState() & Frame.MAXIMIZED_BOTH) != 0) {
                if (getJMenuBar() != null) {
                    getJMenuBar().revalidate();
                    getJMenuBar().repaint();
                }
                repaint();
            }
        });
    }

    private void configurarMenu() {
        JMenuBar menuBar = new JMenuBar();

        Font fuenteMenuPrincipal = new Font("Segoe UI", Font.BOLD, 22);
        Font fuenteSubMenu = new Font("Segoe UI", Font.PLAIN, 20);
        Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 18);

        // ==========================================
        // 1. VENTAS (Atajo: Alt + V)
        // ==========================================
        JMenu menuVentas = new JMenu("VENTAS");
        menuVentas.setFont(fuenteMenuPrincipal);
        menuVentas.setMnemonic(java.awt.event.KeyEvent.VK_V);
        menuVentas.getPopupMenu().setLightWeightPopupEnabled(false);
        listaMenus.add(menuVentas);

        String[] itemsVentas = {
            "1. Ventas", "2. Consulta de documentos", "3. Hoja de ruta",
            "4. Mantención de ventas", "5. Informe ventas por cliente", "6. Ficha clientes",
            "7. Comisión por ventas", "8. Comisión por cobranza", "9. Ficha de vendedores"
        };

        for (int i = 0; i < itemsVentas.length; i++) {
            JMenuItem item = new JMenuItem(itemsVentas[i]);
            item.setFont(fuenteSubMenu);
            item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            listaSubMenus.add(item);

            switch (i) {
                case 0 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_1);
                case 1 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_2);
                case 2 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_3);
                case 3 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_4);
                case 4 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_5);
                case 5 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_6);
                case 6 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_7);
                case 7 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_8);
                case 8 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_9);
            }

            final int indexAccion = i + 1;
            item.addActionListener(e -> {
                switch (indexAccion) {
                    case 1 -> {
                        FrmVenta ventanaVenta = new FrmVenta();
                        escritorio.add(ventanaVenta);
                        ventanaVenta.setVisible(true);
                        try {
                            ventanaVenta.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 2 -> {
                        FrmConsultaDocumento frame = new FrmConsultaDocumento();
                        escritorio.add(frame);
                        frame.setSize(980, 620);
                        frame.setVisible(true);
                        try {
                            frame.setSelected(true);
                            frame.toFront();
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 3 -> {
                        FrmHojaRuta frame = new FrmHojaRuta();
                        escritorio.add(frame);
                        frame.setVisible(true);
                        try {
                            frame.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 4 -> {
                        FrmMantencionVentas frmVentas = new FrmMantencionVentas();
                        escritorio.add(frmVentas);
                        frmVentas.setVisible(true);
                        try {
                            frmVentas.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 5 -> {
                        vista.ventas.FrmResultadoVentaCliente frm = new vista.ventas.FrmResultadoVentaCliente();
                        escritorio.add(frm);
                        int x = (escritorio.getWidth() - frm.getWidth()) / 2;
                        int y = (escritorio.getHeight() - frm.getHeight()) / 2;
                        frm.setLocation(Math.max(0, x), Math.max(0, y));
                        frm.toFront();
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 6 -> {
                        FrmArchivoClientes frmClientes = new FrmArchivoClientes();
                        escritorio.add(frmClientes);
                        frmClientes.setVisible(true);
                        try {
                            frmClientes.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 7 -> {
                        FrmComisionesVentas frame = new FrmComisionesVentas();
                        escritorio.add(frame);
                        frame.setSize(980, 620);
                        frame.setVisible(true);
                        try {
                            frame.setSelected(true);
                            frame.toFront();
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 8 -> {
                        FrmComisionesCobranza frame = new FrmComisionesCobranza();
                        escritorio.add(frame);
                        frame.setSize(980, 620);
                        frame.setVisible(true);
                        try {
                            frame.setSelected(true);
                            frame.toFront();
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 9 -> {
                        FrmFichaVendedores frame = new FrmFichaVendedores();
                        escritorio.add(frame);
                        frame.setSize(980, 620);
                        frame.setVisible(true);
                        try {
                            frame.setSelected(true);
                            frame.toFront();
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            menuVentas.add(item);
        }

        // ==========================================
        // 2. COMPRAS (Atajo: Alt + C)
        // ==========================================
        JMenu menuCompras = new JMenu("COMPRAS");
        menuCompras.setFont(fuenteMenuPrincipal);
        menuCompras.setMnemonic(java.awt.event.KeyEvent.VK_C);
        menuCompras.getPopupMenu().setLightWeightPopupEnabled(false);
        listaMenus.add(menuCompras);

        String[] itemsCompras = {"1. Proveedores", "2. Ingreso de Compras", "3. Libro de Compras"};

        for (int i = 0; i < itemsCompras.length; i++) {
            JMenuItem item = new JMenuItem(itemsCompras[i]);
            item.setFont(fuenteSubMenu);
            item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            listaSubMenus.add(item);

            switch (i) {
                case 0 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_1);
                case 1 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_2);
                case 2 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_3);
            }

            final int indexAccion = i + 1;
            item.addActionListener(e -> {
                switch (indexAccion) {
                    case 1 -> {
                        FrmProveedores frm = new FrmProveedores();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                        }
                    }
                    case 2 -> {
                        FrmIngresoCompra frm = new FrmIngresoCompra();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                        }
                    }
                    case 3 -> {
                        FrmLibroCompras frm = new FrmLibroCompras();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                        }
                    }
                }
            });
            menuCompras.add(item);
        }

        // ==========================================
        // 3. BODEGAS (Atajo: Alt + B)
        // ==========================================
        JMenu menuBodegas = new JMenu("BODEGAS");
        menuBodegas.setFont(fuenteMenuPrincipal);
        menuBodegas.setMnemonic(java.awt.event.KeyEvent.VK_B);
        menuBodegas.getPopupMenu().setLightWeightPopupEnabled(false);
        listaMenus.add(menuBodegas);

        String[] itemsBodegas = {
            "1. Registros de Productos", "2. Registros de Familias", "3. Ingreso de Mercadería",
            "4. Kardex de Mercadería", "5. Inventario de Mercadería", "6. Inventario Crítico",
            "7. Rotación de Ventas", "8. Ventas por Familias"
        };

        for (int i = 0; i < itemsBodegas.length; i++) {
            JMenuItem item = new JMenuItem(itemsBodegas[i]);
            item.setFont(fuenteSubMenu);
            item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            listaSubMenus.add(item);

            switch (i) {
                case 0 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_1);
                case 1 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_2);
                case 2 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_3);
                case 3 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_4);
                case 4 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_5);
                case 5 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_6);
                case 6 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_7);
                case 7 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_8);
            }

            final int indexAccion = i;
            item.addActionListener(e -> {
                switch (indexAccion) {
                    case 0 -> {
                        DlgProductos dlg = new DlgProductos();
                        escritorio.add(dlg);
                        dlg.setVisible(true);
                        try {
                            dlg.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 1 -> {
                        vista.bodega.FrmRegistroFamilias frm = new vista.bodega.FrmRegistroFamilias();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 2 -> {
                        vista.bodega.FrmIngresoMercaderia frm = new vista.bodega.FrmIngresoMercaderia();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 3 -> {
                        vista.bodega.FrmKardexMercaderia frm = new vista.bodega.FrmKardexMercaderia();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 4 -> {
                        String fechaActual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        FrmResultadoInventario frm = new FrmResultadoInventario(new ProductoVentaDAO().listarInventario(), fechaActual);
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 5 -> {
                        FrmInventarioCritico frm = new FrmInventarioCritico();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                        }
                    }
                    case 6 -> {
                        FrmRotacionVentas frm = new FrmRotacionVentas();
                        escritorio.add(frm);
                        frm.setVisible(true);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                        }
                    }
                    case 7 -> {
                        FrmVentasPorFamilia frm = new FrmVentasPorFamilia();
                        escritorio.add(frm);
                        frm.setLocation(50, 50);
                        try {
                            frm.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            menuBodegas.add(item);
        }

        // ==========================================
        // 4. CTACTE (Atajo: Alt + T)
        // ==========================================
        JMenu menuCtacte = new JMenu("CTACTE");
        menuCtacte.setFont(fuenteMenuPrincipal);
        menuCtacte.setMnemonic(java.awt.event.KeyEvent.VK_T);
        menuCtacte.getPopupMenu().setLightWeightPopupEnabled(false);
        listaMenus.add(menuCtacte);

        String[] itemsCtaCte = {
            "1. Eliminar Pago", "2. Listado de Documentos", "3. Estadístico de Clientes",
            "4. Cartola por Ventas", "5. Cartola por Fecha", "6. Cobros de la A a la Z"
        };

        for (int i = 0; i < itemsCtaCte.length; i++) {
            JMenuItem item = new JMenuItem(itemsCtaCte[i]);
            item.setFont(fuenteSubMenu);
            item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            listaSubMenus.add(item);

            switch (i) {
                case 0 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_1);
                case 1 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_2);
                case 2 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_3);
                case 3 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_4);
                case 4 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_5);
                case 5 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_6);
            }

            final int indexAccion = i + 1;
            item.addActionListener(e -> {
                switch (indexAccion) {
                    case 1 -> {
                        FrmEliminarRecibo frmEliminar = new FrmEliminarRecibo();
                        this.escritorio.add(frmEliminar);
                        frmEliminar.setVisible(true);
                        try {
                            frmEliminar.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 2 -> {
                        FrmCarteraCheques frmCartera = new FrmCarteraCheques();
                        escritorio.add(frmCartera);
                        frmCartera.setVisible(true);
                        try {
                            frmCartera.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 3 -> {
                        FrmEstadisticasClientes frmCte = new FrmEstadisticasClientes();
                        escritorio.add(frmCte);
                        frmCte.setVisible(true);
                        try {
                            frmCte.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 4 -> {
                        FrmCartolaCliente frmCartola = new FrmCartolaCliente();
                        escritorio.add(frmCartola);
                        frmCartola.setVisible(true);
                        try {
                            frmCartola.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 5 -> {
                        FrmCartolaPorFecha frmFecha = new FrmCartolaPorFecha();
                        escritorio.add(frmFecha);
                        frmFecha.setVisible(true);
                        try {
                            frmFecha.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                    case 6 -> {
                        FrmCuentasCobrarAlfabetico frmCuentas = new FrmCuentasCobrarAlfabetico();
                        this.escritorio.add(frmCuentas);
                        frmCuentas.setVisible(true);
                        try {
                            frmCuentas.setSelected(true);
                        } catch (java.beans.PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            menuCtacte.add(item);
        }

        // ==========================================
        // 5. ARCHIVOS (Atajo: Alt + A)
        // ==========================================
        JMenu menuArchivos = new JMenu("ARCHIVOS");
        menuArchivos.setFont(fuenteMenuPrincipal);
        menuArchivos.setMnemonic(java.awt.event.KeyEvent.VK_A);
        menuArchivos.getPopupMenu().setLightWeightPopupEnabled(false);
        listaMenus.add(menuArchivos);

        String[] itemsArchivos = {
            "1. Finalizar la sesión", "2. Usuario del sistema", "3. Accesos a funciones",
            "4. Cámara key", "5. Foliador de hojas", "6. Parámetros y correlativos",
            "7. Indexación de la base de datos", "8. Licencia de uso"
        };

        for (int i = 0; i < itemsArchivos.length; i++) {
            JMenuItem item = new JMenuItem(itemsArchivos[i]);
            item.setFont(fuenteSubMenu);
            item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            listaSubMenus.add(item);

            switch (i) {
                case 0 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_1);
                case 1 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_2);
                case 2 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_3);
                case 3 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_4);
                case 4 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_5);
                case 5 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_6);
                case 6 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_7);
                case 7 ->
                    item.setMnemonic(java.awt.event.KeyEvent.VK_8);
            }

            final int indexAccion = i + 1;
            item.addActionListener(e -> {
                switch (indexAccion) {
                    case 1 ->
                        this.dispose();
                    case 2 -> {
                        DlgUsuariosSistema dlg = new DlgUsuariosSistema(this);
                        dlg.setVisible(true);
                    }
                    case 3 -> {
                        DlgAccesosFunciones dlg = new DlgAccesosFunciones(this);
                        dlg.setVisible(true);
                    }
                    case 4 -> {
                        DlgCamaraKey dlg = new DlgCamaraKey(this);
                        dlg.setVisible(true);
                    }
                    case 5 -> {
                        DlgFoliadorHojas dlg = new DlgFoliadorHojas(this);
                        dlg.setVisible(true);
                    }
                    case 6 -> {
                        Window parentWindow = SwingUtilities.getWindowAncestor(escritorio);
                        FrmParametros dialog = new FrmParametros(parentWindow);
                        dialog.setVisible(true);
                    }
                    case 7 -> {
                        Window parentWindow = SwingUtilities.getWindowAncestor(escritorio);
                        DlgIndexacionDB dialog = new DlgIndexacionDB((Frame) parentWindow);
                        dialog.setVisible(true);
                    }
                    case 8 -> {
                        DlgLicenciaUso dlg = new DlgLicenciaUso(this);
                        dlg.setVisible(true);
                    }
                }
            });
            menuArchivos.add(item);
        }

        // ==========================================
        // DISTRIBUCIÓN AUTOMÁTICA EN LA BARRA
        // ==========================================
        menuBar.add(Box.createHorizontalStrut(15));
        menuBar.add(menuVentas);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuCompras);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuBodegas);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuCtacte);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuArchivos);

        menuBar.add(Box.createHorizontalStrut(30));

        lblTitulo = new JLabel("");
        lblTitulo.setFont(fuenteTitulo);
        lblTitulo.setForeground(cColorTema);

//        try {
//            ImageIcon iconoNexum = new ImageIcon(getClass().getResource("/iconos/nexum-16.png"));
//            lblTitulo.setIcon(iconoNexum);
//            lblTitulo.setIconTextGap(8);
//            lblTitulo.setHorizontalTextPosition(JLabel.RIGHT);
//        } catch (Exception e) {
//            System.out.println("No se pudo cargar el icono de la barra: " + e.getMessage());
//        }

        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
        menuBar.add(lblTitulo);

        setJMenuBar(menuBar);
        revalidate();
        repaint();
    }

    private void verificarRespaldoAutomatico() {
        new Thread(() -> {
            if (utilidades.ValidadorRespaldoUtils.esNecesarioRespaldoAutomatico()) {
                SwingUtilities.invokeLater(() -> {
                    DlgIndexacionDB dlgAuto = new DlgIndexacionDB((Frame) SwingUtilities.getWindowAncestor(this));
                });
            }
        }).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        menuVentas = new javax.swing.JMenu();
        menuCompras = new javax.swing.JMenu();
        menuBodegas = new javax.swing.JMenu();
        menuCtacte = new javax.swing.JMenu();
        menuArchivos = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        menuVentas.setText("VENTAS");
        menuVentas.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jMenuBar1.add(menuVentas);

        menuCompras.setText("COMPRAS");
        menuCompras.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jMenuBar1.add(menuCompras);

        menuBodegas.setText("BODEGAS");
        menuBodegas.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jMenuBar1.add(menuBodegas);

        menuCtacte.setText("CTACTE CLIENTES");
        menuCtacte.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jMenuBar1.add(menuCtacte);

        menuArchivos.setText("ARCHIVOS");
        menuArchivos.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jMenuBar1.add(menuArchivos);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

   /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Aplicar FlatLaf Dark para el diseño moderno y oscuro
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        // Lanzar el Splash de Nexum y al finalizar abrir FrmPrincipal
        SwingUtilities.invokeLater(() -> {
            SplashNexum splash = new SplashNexum();
            splash.iniciarCarga(() -> {
                // Ventana principal que se abre al terminar los 10 segundos
                new FrmPrincipal().setVisible(true);
            });
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu menuArchivos;
    private javax.swing.JMenu menuBodegas;
    private javax.swing.JMenu menuCompras;
    private javax.swing.JMenu menuCtacte;
    private javax.swing.JMenu menuVentas;
    // End of variables declaration//GEN-END:variables
}
