package dao;

import conexion.Conexion;
import modelo.VentaFamiliaDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentaFamiliaDAO {

    public List<VentaFamiliaDTO> obtenerVentasPorFamilia(String fechaInicioStr, String fechaFinStr) throws SQLException {
        List<VentaFamiliaDTO> lista = new ArrayList<>();

        try {
            // Convertimos de formato pantalla (dd/MM/yyyy) a formato BD (yyyy-MM-dd)
            DateTimeFormatter formatoPantalla = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formatoBD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate dIni = LocalDate.parse(fechaInicioStr, formatoPantalla);
            LocalDate dFin = LocalDate.parse(fechaFinStr, formatoPantalla);

            String fechaInicioBD = dIni.format(formatoBD);
            String fechaFinBD = dFin.format(formatoBD);

            String sql = "SELECT v.\"VFAMILI\", "
                    + "COALESCE(f.\"FAMILINOM\", 'FAMILIA SIN NOMBRE') AS nombre_familia, "
                    + "COUNT(*) as n_ventas, "
                    + "SUM(v.\"VPXTOTAL\") as total_venta "
                    + "FROM \"pventas_dbf\" v "
                    + "LEFT JOIN \"pfamili_dbf\" f ON v.\"VFAMILI\" = f.\"FAMILICOD\" "
                    + "WHERE v.\"VFECHA\" BETWEEN ? AND ? "
                    + "GROUP BY v.\"VFAMILI\", f.\"FAMILINOM\" "
                    + "ORDER BY total_venta DESC";

            try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, fechaInicioBD);
                pstmt.setString(2, fechaFinBD);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int codFamilia = rs.getInt("VFAMILI");
                        String nombreFamilia = rs.getString("nombre_familia");
                        int nVentas = rs.getInt("n_ventas");
                        double totalVenta = rs.getDouble("total_venta");

                        lista.add(new VentaFamiliaDTO(codFamilia, nombreFamilia, nVentas, totalVenta));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al formatear fechas o consultar DAO: " + e.getMessage());
            throw new SQLException(e);
        }

        return lista;
    }
}
