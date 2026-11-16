package dao;

import conexion.Conexion;
import modelo.VentaClienteDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VentaClienteDAO {

    public List<VentaClienteDTO> obtenerVentasPorCliente(String fechaInicialStr, String fechaFinalStr) throws SQLException {
        List<VentaClienteDTO> lista = new ArrayList<>();

        String fechaInicioBD = "";
        String fechaFinBD = "";

        try {
            // Convertimos de formato pantalla (dd/MM/yyyy) a formato BD (yyyy-MM-dd)
            java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (fechaInicialStr != null && !fechaInicialStr.trim().isEmpty()) {
                java.time.LocalDate dIni = java.time.LocalDate.parse(fechaInicialStr.trim(), formatoPantalla);
                fechaInicioBD = dIni.format(formatoBD);
            }

            if (fechaFinalStr != null && !fechaFinalStr.trim().isEmpty()) {
                java.time.LocalDate dFin = java.time.LocalDate.parse(fechaFinalStr.trim(), formatoPantalla);
                fechaFinBD = dFin.format(formatoBD);
            }
        } catch (Exception e) {
            System.err.println("Error al formatear fechas en ventas por cliente: " + e.getMessage());
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.\"CLIRUT\", c.\"CLINOMBRE\", COUNT(v.\"VCODIGO\") as docs, SUM(v.\"VPXTOTAL\") as total ");
        sql.append("FROM \"pventas_dbf\" v ");
        sql.append("JOIN \"pcliente_dbf\" c ON v.\"VCLIENTE\" = c.\"CLIRUT\" ");
        sql.append("WHERE v.\"VFECHA\" BETWEEN ? AND ? ");
        sql.append("GROUP BY c.\"CLIRUT\", c.\"CLINOMBRE\" ");
        sql.append("ORDER BY total DESC");

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, fechaInicioBD);
            pstmt.setString(2, fechaFinBD);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Obtenemos el nombre original y le aplicamos el formato de apellido primero
                    String nombreOriginal = rs.getString("CLINOMBRE");
                    String nombreFormateado = formatearApellidoPrimero(nombreOriginal);

                    VentaClienteDTO dto = new VentaClienteDTO(
                            rs.getString("CLIRUT"),
                            nombreFormateado,
                            rs.getInt("docs"),
                            rs.getDouble("total")
                    );
                    lista.add(dto);
                }
            }
        }
        return lista;
    }

    /**
     * Método auxiliar para transformar "NOMBRE APELLIDO" a "APELLIDO, NOMBRE"
     */
    private String formatearApellidoPrimero(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "";
        }

        nombreCompleto = nombreCompleto.trim();

        // Si ya contiene una coma, lo dejamos tal cual
        if (nombreCompleto.contains(",")) {
            return nombreCompleto;
        }

        String[] partes = nombreCompleto.split("\\s+");

        if (partes.length <= 1) {
            return nombreCompleto;
        } else if (partes.length == 2) {
            // Ejemplo: "LEONOR DIAZ" -> "DIAZ, LEONOR"
            return partes[1] + ", " + partes[0];
        } else {
            // Para nombres de 3 o más palabras, tomamos las últimas 2 como apellido y el resto como nombre
            // (Ej: "ANA VALVERDE M." -> "VALVERDE M., ANA")
            int indiceCorte = partes.length - 2;
            StringBuilder apellido = new StringBuilder();
            StringBuilder nombres = new StringBuilder();

            for (int i = 0; i < partes.length; i++) {
                if (i >= indiceCorte) {
                    apellido.append(partes[i]).append(" ");
                } else {
                    nombres.append(partes[i]).append(" ");
                }
            }
            return apellido.toString().trim() + ", " + nombres.toString().trim();
        }
    }
}
