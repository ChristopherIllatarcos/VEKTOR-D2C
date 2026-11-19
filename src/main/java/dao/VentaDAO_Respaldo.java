package dao;

import conexion.Conexion;
import static conexion.Conexion.getConnection;
import java.util.List;
import modelo.ItemVentaDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.VentaDTO;

public class VentaDAO_Respaldo {

    // Método exclusivo para guardar la venta en la base de datos
    public boolean guardarVenta(int folioVenta, int codigoCliente, String fechaActual, int codigoVendedor, List<ItemVentaDTO> items) {

        // Convertimos la fecha de pantalla (dd/MM/yyyy) a formato BD (yyyy-MM-dd)
        String fechaActualBD = fechaActual;
        try {
            if (fechaActual != null && !fechaActual.trim().isEmpty()) {
                java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                java.time.LocalDate d = java.time.LocalDate.parse(fechaActual.trim(), formatoPantalla);
                fechaActualBD = d.format(formatoBD);
            }
        } catch (Exception e) {
        }

        String sql = "INSERT INTO pventas_dbf (\"VCODIGO\", \"VCLIENTE\", \"VFECHA\", \"VPXBARRA\", \"VPXCANT\", \"VPXPRECIO\", \"VPXTOTAL\", \"VVENDE\", \"VFAMILI\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Consulta usando la tabla real pproduc_dbf, PXBARRA y PXFAMILIA
        String sqlBuscaFamilia = "SELECT \"PXFAMILIA\" FROM \"pproduc_dbf\" WHERE \"PXBARRA\" = ?";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); PreparedStatement pstmtFamilia = conn.prepareStatement(sqlBuscaFamilia)) {

            for (ItemVentaDTO item : items) {

                // 1. Buscamos el código de familia del producto en pproduc_dbf por su barra
                int codigoFamilia = 0;
                pstmtFamilia.setString(1, item.getBarra());
                try (ResultSet rsFam = pstmtFamilia.executeQuery()) {
                    if (rsFam.next()) {
                        codigoFamilia = rsFam.getInt(1);
                    }
                }

                // 2. Seteamos los parámetros del INSERT de la venta (usando la fecha convertida a la BD)
                pstmt.setInt(1, folioVenta);
                pstmt.setInt(2, codigoCliente);
                pstmt.setString(3, fechaActualBD); // <-- Fecha formateada correctamente (yyyy-MM-dd)
                pstmt.setString(4, item.getBarra());
                pstmt.setInt(5, item.getCantidad());
                pstmt.setDouble(6, item.getPrecioUnitario());
                pstmt.setInt(7, (int) item.getTotalItem());
                pstmt.setInt(8, codigoVendedor);
                pstmt.setInt(9, codigoFamilia); // <-- Inyectamos la familia encontrada

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println("Error en VentaDAO al guardar venta: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Método en tu DAO para eliminar el pago por número de recibo
    public boolean eliminarPagoPorRecibo(String numeroRecibo) {
        // Usamos CTEFOLIO o CTENUMERO dependiendo de cuál campo use la oficina para el "N° Recibo"
        String sql = "DELETE FROM pctacte_dbf WHERE TRIM(CAST(\"CTEFOLIO\" AS TEXT)) = ?";

        try (PreparedStatement pstmt = Conexion.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, numeroRecibo);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPagoPorFolio(String nroFolio) {
        String sql = "DELETE FROM pctacte_dbf WHERE TRIM(CAST(\"CTEFOLIO\" AS TEXT)) = ?";

        try (PreparedStatement pstmt = Conexion.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, nroFolio);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<VentaDTO> obtenerVentas(Connection con, String filtroTexto) {
        List<VentaDTO> lista = new ArrayList<>();
        String filtro = "%" + (filtroTexto != null ? filtroTexto.trim() : "") + "%";

        // Mantenemos tu consulta SQL con el ORDER BY inteligente que soporta ambos formatos
        String sql = "SELECT \"VCODIGO\", \"VCLIENTE\", \"VFECHA\", \"VPXBARRA\", \"VPXCANT\", "
                + "\"VPXPRECIO\", \"VPXDESCTO\", \"VPXTOTAL\", \"VBOLETA\", \"VFACTURA\", "
                + "\"VGUIA\", \"VVALE\", \"VNOTCRE\", \"VCAJA\" "
                + "FROM pventas_dbf "
                + "WHERE UPPER(COALESCE(\"VCLIENTE\", '')) LIKE UPPER(?) "
                + "   OR COALESCE(CAST(\"VBOLETA\" AS TEXT), '') LIKE ? "
                + "   OR COALESCE(CAST(\"VFACTURA\" AS TEXT), '') LIKE ? "
                + "   OR COALESCE(CAST(\"VCODIGO\" AS TEXT), '') LIKE ? "
                + "ORDER BY CASE "
                + "    WHEN \"VFECHA\" ~ '^\\d{4}-\\d{2}-\\d{2}' THEN TO_DATE(\"VFECHA\", 'YYYY-MM-DD') "
                + "    WHEN \"VFECHA\" ~ '^\\d{2}/\\d{2}/\\d{4}' THEN TO_DATE(\"VFECHA\", 'DD/MM/YYYY') "
                + "    ELSE NULL "
                + "END DESC NULLS LAST, \"VCODIGO\" DESC LIMIT 200";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            ps.setString(4, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                java.time.format.DateTimeFormatter formatoBD = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                java.time.format.DateTimeFormatter formatoPantalla = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

                while (rs.next()) {
                    VentaDTO v = new VentaDTO();
                    v.setVcodigo(rs.getLong("VCODIGO"));
                    v.setVcliente(rs.getString("VCLIENTE") != null ? rs.getString("VCLIENTE") : "");

                    // Formateamos la fecha de la BD (yyyy-MM-dd) a Pantalla (dd/MM/yyyy)
                    String fecDb = rs.getString("VFECHA");
                    String fecFormateada = fecDb != null ? fecDb.trim() : "";
                    try {
                        if (fecDb != null && !fecDb.trim().isEmpty()) {
                            if (fecDb.length() > 10) {
                                fecDb = fecDb.substring(0, 10);
                            }
                            java.time.LocalDate parsed = java.time.LocalDate.parse(fecDb, formatoBD);
                            fecFormateada = parsed.format(formatoPantalla);
                        }
                    } catch (Exception ignored) {
                        // Si viene con otro formato (ej: barras), lo deja como viene
                        fecFormateada = fecDb;
                    }

                    v.setVfecha(fecFormateada);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public boolean eliminarVenta(Connection con, long vcodigo) {
        String sql = "DELETE FROM pventas_dbf WHERE \"VCODIGO\" = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, vcodigo);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean actualizarVenta(Connection con, VentaDTO v) {
        String sql = "UPDATE pventas_dbf SET "
                + "\"VCLIENTE\" = ?, \"VFECHA\" = ?, \"VBOLETA\" = ?, "
                + "\"VFACTURA\" = ?, \"VGUIA\" = ?, \"VPXTOTAL\" = ? "
                + "WHERE \"VCODIGO\" = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getVcliente());
            ps.setString(2, v.getVfecha());
            ps.setString(3, v.getVboleta());
            ps.setString(4, v.getVfactura());
            ps.setString(5, v.getVguia());
            ps.setDouble(6, v.getVpxtotal());
            ps.setLong(7, v.getVcodigo());
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<modelo.VentaClienteDTO> obtenerVentasPorCliente(String fechaIni, String fechaFin) {
        List<modelo.VentaClienteDTO> lista = new ArrayList<>();

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
                + "END) BETWEEN TO_DATE(?, 'DD/MM/YYYY') AND TO_DATE(?, 'DD/MM/YYYY') "
                + "GROUP BY TRIM(v.\"VCLIENTE\") "
                + "ORDER BY total_ventas DESC";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fechaIni);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.VentaClienteDTO dto = new modelo.VentaClienteDTO(
                            rs.getString("cod_cliente"),
                            rs.getString("nom_cliente"),
                            rs.getInt("cant_docs"),
                            rs.getDouble("total_ventas")
                    );
                    lista.add(dto);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }

}
