package dao;

import conexion.Conexion;
import modelo.ComisionVendedorDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComisionDAO {

    public List<ComisionVendedorDTO> obtenerComisionesPorFecha(String fechaInicio, String fechaFin) throws SQLException {
        List<ComisionVendedorDTO> lista = new ArrayList<>();

        // Consulta haciendo JOIN con pvende_dbf para traernos el nombre real del vendedor
        String sql = "SELECT COUNT(*) as n_ventas, v.\"VVENDE\", COALESCE(e.\"VENDENOM\", 'VENDEDOR ID: ' || v.\"VVENDE\") as nombre_vendedor, SUM(v.\"VPXTOTAL\") as total_venta "
                + "FROM \"pventas_dbf\" v "
                + "LEFT JOIN \"pvende_dbf\" e ON v.\"VVENDE\" = e.\"VENDECOD\" "
                + "WHERE v.\"VFECHA\" BETWEEN ? AND ? "
                + "GROUP BY v.\"VVENDE\", e.\"VENDENOM\" "
                + "ORDER BY total_venta DESC";

        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, fechaFin);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int codVende = rs.getInt("VVENDE");
                    int nVentas = rs.getInt("n_ventas");
                    String nombreVendedor = rs.getString("nombre_vendedor");
                    double totalVenta = rs.getDouble("total_venta");

                    double comision = 0.0;
                    double participacion = 0.0; // Se calcula o se deja en 0 por mientras

                    lista.add(new ComisionVendedorDTO(codVende, nVentas, nombreVendedor, totalVenta, comision, participacion));
                }
            }
        }

        return lista;
    }

    public List<modelo.DetalleVendedorDTO> obtenerDetalleVentasPorVendedor(int codigoVendedor, String fechaInicio, String fechaFin) throws SQLException {
        List<modelo.DetalleVendedorDTO> lista = new ArrayList<>();

        // Añadimos ORDER BY "VCODIGO" DESC para que los documentos más recientes salgan primero
        String sql = "SELECT \"VFECHA\", \"VCLIENTE\", \"VPXTOTAL\", \"VFACTURA\", \"VBOLETA\", \"VGUIA\", \"VVALE\", \"VCOTIZA\" "
                + "FROM \"pventas_dbf\" "
                + "WHERE \"VVENDE\" = ? AND \"VFECHA\" BETWEEN ? AND ? "
                + "ORDER BY \"VCODIGO\" DESC";

        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, codigoVendedor);
            pstmt.setString(2, fechaInicio);
            pstmt.setString(3, fechaFin);

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
        return lista;
    }

    public List<modelo.DetalleVendedorDTO> obtenerDetallePorFolio(int codigoVendedor, String filtroFolio) throws SQLException {
        List<modelo.DetalleVendedorDTO> lista = new ArrayList<>();

        boolean filtrarFolio = filtroFolio != null && !filtroFolio.trim().isEmpty();

        // Armamos la consulta dinámica según si se ingresó un folio o no
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
        return lista;
    }
}
