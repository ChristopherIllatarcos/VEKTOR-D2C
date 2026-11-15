package utilidades;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class ValidadorRespaldoUtils {

    /**
     * Verifica si han pasado 15 días o más desde el último respaldo automático.
     * @return 
     */
    public static boolean esNecesarioRespaldoAutomatico() {
        try {
            String userHome = System.getProperty("user.home");
            File carpetaDocs = new File(userHome, "Documents");
            if (!carpetaDocs.exists()) {
                carpetaDocs = new File(userHome, "Mis Documents");
                if (!carpetaDocs.exists()) {
                    carpetaDocs = new File(userHome);
                }
            }

            // Buscamos archivos que comiencen con nuestro patrón de respaldo
            File[] backups = carpetaDocs.listFiles((dir, name) -> name.startsWith("vektor_d2c_backup_") && name.endsWith(".sql"));

            if (backups == null || backups.length == 0) {
                return true; // Si nunca se ha hecho un respaldo, toca hacerlo al tiro
            }

            // Ordenamos para encontrar el archivo más reciente por fecha de modificación
            Arrays.sort(backups, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            File ultimoBackup = backups[0];
            LocalDate fechaUltimo = java.time.Instant.ofEpochMilli(ultimoBackup.lastModified())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            long diasDiferencia = ChronoUnit.DAYS.between(fechaUltimo, LocalDate.now());

            // Retorna true si ya pasaron 15 o más días
            return diasDiferencia >= 15;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
