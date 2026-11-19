package utilidades;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

public class IconoUtils {

    public static void aplicarIcono(Object ventana) {
        List<Image> iconosApp = new ArrayList<>();
        try {
            Class<?> clazz = IconoUtils.class;
            iconosApp.add(new ImageIcon(clazz.getResource("/vektor/cubo-azul-16.png")).getImage());
            iconosApp.add(new ImageIcon(clazz.getResource("/vektor/cubo-azul-32.png")).getImage());
            iconosApp.add(new ImageIcon(clazz.getResource("/vektor/cubo-azul-64.png")).getImage());
            iconosApp.add(new ImageIcon(clazz.getResource("/vektor/cubo-azul-128.png")).getImage());

            // Si es un JFrame o JDialog (Ventanas de primer nivel)
            if (ventana instanceof JFrame jFrame) {
                jFrame.setIconImages(iconosApp);
            } else if (ventana instanceof JDialog jDialog) {
                jDialog.setIconImages(iconosApp);
            } // Si es un JInternalFrame (como FrmArchivoClientes o FrmVenta)
            else if (ventana instanceof JInternalFrame) {
                // Los JInternalFrame heredan el icono de la barra de título del JFrame principal automáticamente,
                // pero si quieres asegurar un Frame interno, puedes omitir acción o gestionarlo en el Frame contenedor.
            }
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el icono: " + e.getMessage());
        }
    }
}
