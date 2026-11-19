package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import modelo.ItemVentaDTO;
import modelo.VentaClienteDTO;
import modelo.VentaDTO;

public class VentaDAO {

    private static final DateTimeFormatter FORMATO_PANTALLA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_BD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String normalizarFechaAFormatoBD(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return fecha;
        }
        String limpia = fecha.trim();
        if (limpia.length() > 10) {
            limpia = limpia.substring(0, 10);
        }
        try {
            return LocalDate.parse(limpia, FORMATO_BD).format(FORMATO_BD);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(limpia, FORMATO_PANTALLA).format(FORMATO_BD);
        } catch (DateTimeParseException ignored) {
        }
        return limpia;
    }

    private String formatearFechaAPantalla(String fecDb) {
        if (fecDb == null || fecDb.trim().isEmpty()) {
            return "";
        }
        String limpia = fecDb.trim();
        if (limpia.length() > 10) {
            limpia = limpia.substring(0, 10);
        }
        try {
            return LocalDate.parse(limpia, FORMATO_BD).format(FORMATO_PANTALLA);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(limpia, FORMATO_PANTALLA).format(FORMATO_PANTALLA);
        } catch (DateTimeParseException ignored) {
        }
        return fecDb.trim();
    }

    // Sobrecarga transaccional: permite participar en una transacción externa (commit/rollback)
    public boolean guardarVenta(Connection conn, int folioVenta, int codigoCliente, String fechaActual,
            int codigoVendedor, List<ItemVentaDTO> items) throws SQLException {
        if (conn == null) {
            throw new SQLException("La conexión proporcionada es nula.");
        }
        if (items == null || items.isEmpty()) {
            return false;
        }

        String fechaActualBD = normalizarFechaAFormatoBD(fechaActual);

        String sqlInsert = "INSERT INTO pventas_dbf (\"VCODIGO\", \"VCLIENTE\", \"VFECHA\", \"VPXBARRA\", \"VPXCANT\", \"VPXPRECIO\", \"VPXTOTAL\", \"VVENDE\", \"VFAMILI\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlBuscaFamilia = "SELECT \"PXFAMILIA\" FROM \"pproduc_dbf\" WHERE \"PXBARRA\" = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert);
             PreparedStatement pstmtFamilia = conn.prepareStatement(sqlBuscaFamilia)) {

            for (ItemVentaDTO item : items) {
                int codigoFamilia = 0;
                pstmtFamilia.setString(1, item.getBarra());
                try (ResultSet rsFam = pstmtFamilia.executeQuery()) {
                    if (rsFam.next()) {
                        codigoFamilia = rsFam.getInt(1);
                    }
                }

                pstmt.setInt(1, folioVenta);
                pstmt.setInt(2, codigoCliente);
                pstmt.setString(3, fechaActualBD);
                pstmt.setString(4, item.getBarra());
                pstmt.setInt(5, item.getCantidad());
                pstmt.setDouble(6, item.getPrecioUnitario());
                pstmt.setDouble(7, item.getTotalItem());
                pstmt.setInt(8, codigoVendedor);
                pstmt.setInt(9, codigoFamilia);

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;
        }
    }

    // Método estándar para llamadas independientes
    public boolean guardarVenta(int folioVenta, int codigoCliente, String fechaActual,
            int codigoVendedor, List<ItemVentaDTO> items) {
        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);
            boolean ok = guardarVenta(conn, folioVenta, codigoCliente, fechaActual, codigoVendedor, items);
            if (ok) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("Error en VentaDAO al guardar venta: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean eliminarRegistroCuentaCorrientePorFolio(String folio) {
        if (folio == null || folio.trim().isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM pctacte_dbf WHERE TRIM(CAST(\"CTEFOLIO\" AS TEXT)) = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, folio.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar registro en pctacte_dbf: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPagoPorRecibo(String numeroRecibo) {
        return eliminarRegistroCuentaCorrientePorFolio(numeroRecibo);
    }

    public boolean eliminarPagoPorFolio(String nroFolio) {
        return eliminarRegistroCuentaCorrientePorFolio(nroFolio);
    }

    public List<VentaDTO> obtenerVentas(Connection con, String filtroTexto) {
        List<VentaDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String filtro = "%" + (filtroTexto != null ? filtroTexto.trim() : "") + "%";

        String sql = "SELECT \"VCODIGO\", \"VCLIENTE\", \"VFECHA\", \"VPXBARRA\", \"VPXCANT\", "
                + "\"VPXPRECIO\", \"VPXDESCTO\", \"VPXTOTAL\", \"VBOLETA\", \"VFACTURA\", "
                + "\"VGUIA\", \"VVALE\", \"VNOTCRE\", \"VCAJA\" "
                + "FROM pventas_dbf "
                + "WHERE UPPER(COALESCE(\"VCLIENTE\", '')) LIKE UPPER(?) "
                + "   OR COALESCE(CAST(\"VBOLETA\" AS TEXT), '') LIKE ? "
                + "   OR COALESCE(CAST(\"VFACTURA\" AS TEXT), '') LIKE ? "
                + "   OR COALESCE(CAST(\"VCODIGO\" AS TEXT), '') LIKE ? "
                + "ORDER BY CASE "
                + "    WHEN \"VFECHA\" ~ '^\\d{4}-\\d{2}-\\d{2}' THEN TO_DATE(SUBSTRING(\"VFECHA\", 1, 10), 'YYYY-MM-DD') "
                + "    WHEN \"VFECHA\" ~ '^\\d{2}/\\d{2}/\\d{4}' THEN TO_DATE(SUBSTRING(\"VFECHA\", 1, 10), 'DD/MM/YYYY') "
                + "    ELSE NULL "
                + "END DESC NULLS LAST, \"VCODIGO\" DESC LIMIT 200";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            ps.setString(4, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaDTO v = new VentaDTO();
                    v.setVcodigo(rs.getLong("VCODIGO"));
                    v.setVcliente(rs.getString("VCLIENTE") != null ? rs.getString("VCLIENTE") : "");
                    v.setVfecha(formatearFechaAPantalla(rs.getString("VFECHA")));
                    v.setVpxbarra(rs.getString("VPXBARRA") != null ? rs.getString("VPXBARRA") : "");
                    v.setVpxcant(rs.getString("VPXCANT") != null ? rs.getString("VPXCANT") : "");
                    v.setVpxprecio(rs.getString("VPXPRECIO") != null ? rs.getString("VPXPRECIO") : "");
                    v.setVpxdescto(rs.getString("VPXDESCTO") != null ? rs.getString("VPXDESCTO") : "");
                    v.setVpxtotal(rs.getDouble("VPXTOTAL"));
                    v.setVboleta(rs.getString("VBOLETA") != null ? rs.getString("VBOLETA") : "");
                    v.setVfactura(rs.getString("VFACTURA") != null ? rs.getString("VFACTURA") : "");
                    v.setVguia(rs.getString("VGUIA") != null ? rs.getString("VGUIA") : "");
                    v.setVvale(rs.getDouble("VVALE"));
                    v.setVnotcre(rs.getString("VNOTCRE") != null ? rs.getString("VNOTCRE") : "");
                    v.setVcaja(rs.getDouble("VCAJA"));
                    lista.add(v);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error al obtener ventas: " + ex.getMessage());
            ex.printStackTrace();
        }
        return lista;
    }

    public boolean eliminarVenta(Connection con, long vcodigo) throws SQLException {
        if (con == null) {
            throw new SQLException("La conexión proporcionada es nula.");
        }
        String sql = "DELETE FROM pventas_dbf WHERE \"VCODIGO\" = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, vcodigo);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarVenta(Connection con, VentaDTO v) {
        if (con == null || v == null) {
            return false;
        }
        String sql = "UPDATE pventas_dbf SET "
                + "\"VCLIENTE\" = ?, \"VFECHA\" = ?, \"VBOLETA\" = ?, "
                + "\"VFACTURA\" = ?, \"VGUIA\" = ?, \"VPXTOTAL\" = ? "
                + "WHERE \"VCODIGO\" = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getVcliente());
            ps.setString(2, normalizarFechaAFormatoBD(v.getVfecha()));
            ps.setString(3, v.getVboleta());
            ps.setString(4, v.getVfactura());
            ps.setString(5, v.getVguia());
            ps.setDouble(6, v.getVpxtotal());
            ps.setLong(7, v.getVcodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error al actualizar venta: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public List<VentaClienteDTO> obtenerVentasPorCliente(String fechaIni, String fechaFin) {
        List<VentaClienteDTO> lista = new ArrayList<>();

        String fIni = normalizarFechaAFormatoBD(fechaIni);
        String fFin = normalizarFechaAFormatoBD(fechaFin);

        String sql = "SELECT "
                + "    CASE "
                + "        WHEN COALESCE(NULLIF(TRIM(v.\"VCLIENTE\"), ''), '0') = '0' THEN '0' "
                + "        WHEN MAX(TRIM(c.\"CLIRUT\")) IS NOT NULL AND COALESCE(MAX(TRIM(c.\"CLIDIGIT\")), '') <> '' "
                + "            THEN MAX(TRIM(c.\"CLIRUT\")) || '-' || MAX(TRIM(c.\"CLIDIGIT\")) "
                + "        ELSE TRIM(v.\"VCLIENTE\") "
                + "    END AS cod_cliente, "
                + "    CASE "
                + "        WHEN COALESCE(NULLIF(TRIM(v.\"VCLIENTE\"), ''), '0') = '0' THEN 'CLIENTE CONTADO / GENÉRICO' "
                + "        ELSE COALESCE(MAX(TRIM(c.\"CLINOMBRE\")), 'CLIENTE NO REGISTRADO (' || TRIM(v.\"VCLIENTE\") || ')') "
                + "    END AS nom_cliente, "
                + "    COUNT(DISTINCT COALESCE(NULLIF(TRIM(v.\"VFACTURA\"), ''), NULLIF(TRIM(v.\"VBOLETA\"), ''), NULLIF(TRIM(v.\"VGUIA\"), ''), CAST(v.\"VCODIGO\" AS TEXT))) AS cant_docs, "
                + "    COALESCE(SUM(CASE "
                + "        WHEN COALESCE(v.\"VPXTOTAL\", 0) > 0 THEN v.\"VPXTOTAL\" "
                + "        ELSE CAST(REPLACE(COALESCE(NULLIF(TRIM(v.\"VPXPRECIO\"), ''), '0'), ',', '.') AS NUMERIC) "
                + "    END), 0) AS total_ventas "
                + "FROM pventas_dbf v "
                + "LEFT JOIN pcliente_dbf c ON "
                + "    UPPER(REPLACE(REPLACE(TRIM(v.\"VCLIENTE\"), '.', ''), '-', '')) IN ( "
                + "        UPPER(TRIM(c.\"CLIRUT\")), "
                + "        UPPER(TRIM(c.\"CLIRUT\") || COALESCE(TRIM(c.\"CLIDIGIT\"), '')) "
                + "    ) "
                + "WHERE (CASE "
                + "    WHEN v.\"VFECHA\" ~ '^\\d{4}-\\d{2}-\\d{2}' THEN TO_DATE(SUBSTRING(v.\"VFECHA\", 1, 10), 'YYYY-MM-DD') "
                + "    WHEN v.\"VFECHA\" ~ '^\\d{2}-\\d{2}-\\d{4}' THEN TO_DATE(SUBSTRING(v.\"VFECHA\", 1, 10), 'DD-MM-YYYY') "
                + "    WHEN v.\"VFECHA\" ~ '^\\d{2}/\\d{2}/\\d{4}' THEN TO_DATE(SUBSTRING(v.\"VFECHA\", 1, 10), 'DD/MM/YYYY') "
                + "    ELSE NULL "
                + "END) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                + "GROUP BY TRIM(v.\"VCLIENTE\") "
                + "ORDER BY total_ventas DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fIni);
            ps.setString(2, fFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaClienteDTO dto = new VentaClienteDTO(
                            rs.getString("cod_cliente"),
                            rs.getString("nom_cliente"),
                            rs.getInt("cant_docs"),
                            rs.getDouble("total_ventas")
                    );
                    lista.add(dto);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error al obtener ventas por cliente: " + ex.getMessage());
            ex.printStackTrace();
        }
        return lista;
    }
}

