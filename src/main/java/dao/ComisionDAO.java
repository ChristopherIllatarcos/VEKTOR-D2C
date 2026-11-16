package dao;

import modelo.ComisionVendedorDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComisionDAO {

    public List<ComisionVendedorDTO> obtenerComisionesPorFecha(String fechaInicioStr, String fechaFinStr) throws SQLException {
        List<ComisionVendedorDTO> lista = new ArrayList<>();

        try {
            // Convertimos de formato pantalla (dd/MM/yyyy) a formato BD (yyyy-MM-dd)
            java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

            java.time.LocalDate dIni = java.time.LocalDate.parse(fechaInicioStr, formatoPantalla);
            java.time.LocalDate dFin = java.time.LocalDate.parse(fechaFinStr, formatoPantalla);

            String fechaInicioBD = dIni.format(formatoBD);
            String fechaFinBD = dFin.format(formatoBD);

            // Consulta corregida: Partimos del catálogo completo pvende_dbf para que salgan todos los registros (con 0 si no vendieron)
            String sql = "SELECT COUNT(DISTINCT v.\"VCODIGO\") as n_ventas, e.\"VENDECOD\" as VVENDE, "
                    + "e.\"VENDENOM\" as nombre_vendedor, "
                    + "COALESCE(SUM(v.\"VPXTOTAL\"), 0) as total_venta "
                    + "FROM \"pvende_dbf\" e "
                    + "LEFT JOIN \"pventas_dbf\" v ON e.\"VENDECOD\" = v.\"VVENDE\" AND (CASE "
                    + "   WHEN TRIM(CAST(v.\"VFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}' THEN TO_DATE(SUBSTRING(TRIM(CAST(v.\"VFECHA\" AS TEXT)), 1, 10), 'YYYY-MM-DD') "
                    + "   WHEN TRIM(CAST(v.\"VFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}' THEN TO_DATE(SUBSTRING(TRIM(CAST(v.\"VFECHA\" AS TEXT)), 1, 10), 'DD-MM-YYYY') "
                    + "   WHEN TRIM(CAST(v.\"VFECHA\" AS TEXT)) ~ '^\\d{2}/\\d{2}/\\d{4}' THEN TO_DATE(SUBSTRING(TRIM(CAST(v.\"VFECHA\" AS TEXT)), 1, 10), 'DD/MM/YYYY') "
                    + "   ELSE NULL "
                    + "END) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                    + "GROUP BY e.\"VENDECOD\", e.\"VENDENOM\" "
                    + "ORDER BY total_venta DESC";

            try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, fechaInicioBD);
                pstmt.setString(2, fechaFinBD);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int codVende = rs.getInt("VVENDE");
                        int nVentas = rs.getInt("n_ventas");
                        String nombreVendedor = rs.getString("nombre_vendedor");
                        double totalVenta = rs.getDouble("total_venta");

                        double comision = 0.0;
                        double participacion = 0.0;

                        lista.add(new ComisionVendedorDTO(codVende, nVentas, nombreVendedor, totalVenta, comision, participacion));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al formatear fechas en comisiones: " + e.getMessage());
            throw new SQLException(e);
        }

        return lista;
    }

    public List<modelo.DetalleVendedorDTO> obtenerDetalleVentasPorVendedor(int codigoVendedor, String fechaInicioStr, String fechaFinStr) throws SQLException {
        List<modelo.DetalleVendedorDTO> lista = new ArrayList<>();

        try {
            // Convertimos de formato pantalla (dd/MM/yyyy) a formato BD (yyyy-MM-dd)
            java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

            java.time.LocalDate dIni = java.time.LocalDate.parse(fechaInicioStr, formatoPantalla);
            java.time.LocalDate dFin = java.time.LocalDate.parse(fechaFinStr, formatoPantalla);

            String fechaInicioBD = dIni.format(formatoBD);
            String fechaFinBD = dFin.format(formatoBD);

            // Consulta con CASE en el WHERE para el detalle por vendedor
            String sql = "SELECT \"VFECHA\", \"VCLIENTE\", \"VPXTOTAL\", \"VFACTURA\", \"VBOLETA\", \"VGUIA\", \"VVALE\", \"VCOTIZA\" "
                    + "FROM \"pventas_dbf\" "
                    + "WHERE \"VVENDE\" = ? AND (CASE "
                    + "   WHEN TRIM(CAST(\"VFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}' THEN TO_DATE(SUBSTRING(TRIM(CAST(\"VFECHA\" AS TEXT)), 1, 10), 'YYYY-MM-DD') "
                    + "   WHEN TRIM(CAST(\"VFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}' THEN TO_DATE(SUBSTRING(TRIM(CAST(\"VFECHA\" AS TEXT)), 1, 10), 'DD-MM-YYYY') "
                    + "   WHEN TRIM(CAST(\"VFECHA\" AS TEXT)) ~ '^\\d{2}/\\d{2}/\\d{4}' THEN TO_DATE(SUBSTRING(TRIM(CAST(\"VFECHA\" AS TEXT)), 1, 10), 'DD/MM/YYYY') "
                    + "   ELSE NULL "
                    + "END) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                    + "ORDER BY \"VCODIGO\" DESC";

            try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, codigoVendedor);
                pstmt.setString(2, fechaInicioBD);
                pstmt.setString(3, fechaFinBD);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String fecha = rs.getString("VFECHA");
                        String cliente = rs.getString("VCLIENTE");
                        double total = rs.getDouble("VPXTOTAL");

                        String tipoDoc = "FAC";
                        int folio = 0;

                        if (rs.getString("VFACTURA") != null && !rs.getString("VFACTURA").isEmpty()) {
                            tipoDoc = "FAC";
                            folio = rs.getInt("VFACTURA");
                        } else if (rs.getString("VBOLETA") != null && !rs.getString("VBOLETA").isEmpty()) {
                            tipoDoc = "BOL";
                            folio = rs.getInt("VBOLETA");
                        } else if (rs.getString("VGUIA") != null && !rs.getString("VGUIA").isEmpty()) {
                            tipoDoc = "GUI";
                            folio = rs.getInt("VGUIA");
                        } else if (rs.getDouble("VVALE") > 0) {
                            tipoDoc = "VAL";
                            folio = (int) rs.getDouble("VVALE");
                        } else if (rs.getString("VCOTIZA") != null && !rs.getString("VCOTIZA").isEmpty()) {
                            tipoDoc = "COT";
                            folio = Integer.parseInt(rs.getString("VCOTIZA"));
                        }

                        lista.add(new modelo.DetalleVendedorDTO(tipoDoc, folio, fecha, cliente, total));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al formatear fechas en detalle vendedor: " + e.getMessage());
            throw new SQLException(e);
        }

        return lista;
    }

    public List<modelo.DetalleVendedorDTO> obtenerDetallePorFolio(int codigoVendedor, String filtroFolio) throws SQLException {
        List<modelo.DetalleVendedorDTO> lista = new ArrayList<>();

        boolean filtrarFolio = filtroFolio != null && !filtroFolio.trim().isEmpty();

        String sql = "SELECT \"VFECHA\", \"VCLIENTE\", \"VPXTOTAL\", \"VFACTURA\", \"VBOLETA\", \"VGUIA\", \"VVALE\", \"VCOTIZA\" "
                + "FROM \"pventas_dbf\" "
                + "WHERE \"VVENDE\" = ?";

        if (filtrarFolio) {
            sql += " AND (CAST(\"VFACTURA\" AS VARCHAR) LIKE ? OR "
                    + "CAST(\"VBOLETA\" AS VARCHAR) LIKE ? OR "
                    + "CAST(\"VGUIA\" AS VARCHAR) LIKE ? OR "
                    + "CAST(\"VVALE\" AS VARCHAR) LIKE ? OR "
                    + "CAST(\"VCOTIZA\" AS VARCHAR) LIKE ?)";
        }

        sql += " ORDER BY \"VCODIGO\" DESC";

        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, codigoVendedor);

            if (filtrarFolio) {
                String comodin = "%" + filtroFolio.trim() + "%";
                pstmt.setString(2, comodin);
                pstmt.setString(3, comodin);
                pstmt.setString(4, comodin);
                pstmt.setString(5, comodin);
                pstmt.setString(6, comodin);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                // Formatters para convertir de BD (yyyy-MM-dd) a Pantalla (dd/MM/yyyy)
                java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

                while (rs.next()) {
                    String fechaBD = rs.getString("VFECHA");
                    String fechaFormateada = fechaBD; // Por seguridad si viene vacía

                    try {
                        if (fechaBD != null && !fechaBD.trim().isEmpty()) {
                            // Si la fecha de la BD trae hora o viene con otro formato parcial, 
                            // nos quedamos con los primeros 10 caracteres (yyyy-MM-dd)
                            if (fechaBD.length() > 10) {
                                fechaBD = fechaBD.substring(0, 10);
                            }
                            java.time.LocalDate parsedDate = java.time.LocalDate.parse(fechaBD, formatoBD);
                            fechaFormateada = parsedDate.format(formatoPantalla);
                        }
                    } catch (Exception ex) {
                        // Si falla la conversión, mantiene el texto original de la BD
                    }

                    String cliente = rs.getString("VCLIENTE");
                    double total = rs.getDouble("VPXTOTAL");

                    String tipoDoc = "FAC";
                    int folio = 0;

                    if (rs.getString("VFACTURA") != null && !rs.getString("VFACTURA").isEmpty()) {
                        tipoDoc = "FAC";
                        folio = rs.getInt("VFACTURA");
                    } else if (rs.getString("VBOLETA") != null && !rs.getString("VBOLETA").isEmpty()) {
                        tipoDoc = "BOL";
                        folio = rs.getInt("VBOLETA");
                    } else if (rs.getString("VGUIA") != null && !rs.getString("VGUIA").isEmpty()) {
                        tipoDoc = "GUI";
                        folio = rs.getInt("VGUIA");
                    } else if (rs.getDouble("VVALE") > 0) {
                        tipoDoc = "VAL";
                        folio = (int) rs.getDouble("VVALE");
                    } else if (rs.getString("VCOTIZA") != null && !rs.getString("VCOTIZA").isEmpty()) {
                        tipoDoc = "COT";
                        folio = Integer.parseInt(rs.getString("VCOTIZA"));
                    }

                    // Pasamos la fecha ya formateada (dd/MM/yyyy) al DTO
                    lista.add(new modelo.DetalleVendedorDTO(tipoDoc, folio, fechaFormateada, cliente, total));
                }
            }
        }
        return lista;
    }

    public List<ComisionVendedorDTO> obtenerCobranzasPorFecha(String fechaInicioStr, String fechaFinStr) throws SQLException {
        List<ComisionVendedorDTO> lista = new ArrayList<>();

        try {
            java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

            java.time.LocalDate dIni = java.time.LocalDate.parse(fechaInicioStr, formatoPantalla);
            java.time.LocalDate dFin = java.time.LocalDate.parse(fechaFinStr, formatoPantalla);

            String fechaInicioBD = dIni.format(formatoBD);
            String fechaFinBD = dFin.format(formatoBD);

            // Consulta directa sobre pcobra_dbf agrupando por el nombre extraído de FLINEA
            String sql = "SELECT COUNT(DISTINCT c.\"FDOCUMEN\") as n_ventas, "
                    + "TRIM(SPLIT_PART(c.\"FLINEA\", '  ', 1)) as nombre_vendedor, "
                    + "COALESCE(SUM(CAST(NULLIF(REPLACE(REPLACE(CAST(c.\"FCREDITO\" AS TEXT), '.', ''), '$', ''), '' ) AS NUMERIC)), 0) as total_venta "
                    + "FROM \"pcobra_dbf\" c "
                    + "WHERE (CASE "
                    + "   WHEN TRIM(CAST(c.\"FFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}' THEN TO_DATE(SUBSTRING(TRIM(CAST(c.\"FFECHA\" AS TEXT)), 1, 10), 'YYYY-MM-DD') "
                    + "   WHEN TRIM(CAST(c.\"FFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}' THEN TO_DATE(SUBSTRING(TRIM(CAST(c.\"FFECHA\" AS TEXT)), 1, 10), 'DD-MM-YYYY') "
                    + "   WHEN TRIM(CAST(c.\"FFECHA\" AS TEXT)) ~ '^\\d{2}/\\d{2}/\\d{4}' THEN TO_DATE(SUBSTRING(TRIM(CAST(c.\"FFECHA\" AS TEXT)), 1, 10), 'DD/MM/YYYY') "
                    + "   ELSE NULL "
                    + "END) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                    + "GROUP BY TRIM(SPLIT_PART(c.\"FLINEA\", '  ', 1)) "
                    + "HAVING TRIM(SPLIT_PART(c.\"FLINEA\", '  ', 1)) <> '' "
                    + "ORDER BY total_venta DESC";

            try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, fechaInicioBD);
                pstmt.setString(2, fechaFinBD);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int nVentas = rs.getInt("n_ventas");
                        String nombreVendedor = rs.getString("nombre_vendedor");
                        double totalVenta = rs.getDouble("total_venta");

                        // Usamos código 0 temporal ya que no viene de pvende_dbf
                        lista.add(new ComisionVendedorDTO(0, nVentas, nombreVendedor, totalVenta, 0.0, 0.0));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al consultar cobranzas: " + e.getMessage());
            throw new SQLException(e);
        }

        return lista;
    }
}
