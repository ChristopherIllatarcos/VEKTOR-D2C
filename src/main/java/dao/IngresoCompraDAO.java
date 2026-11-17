package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class IngresoCompraDAO {

    public List<Object[]> buscarCompras(String fechaIni, String fechaFin, String filtroBusqueda) {
        List<Object[]> lista = new ArrayList<>();

        // CORREGIDO: VCLIENTE::text para evitar que falle si la columna es entera en la BD
        String sql = """
            SELECT 
                COALESCE("VFECHA"::text, '') AS fecha,
                COALESCE("VFACTURA"::text, '') AS factura,
                COALESCE("VCLIENTE"::text, 'PROVEEDOR GENERAL') AS proveedor,
                COALESCE("VPXBARRA"::text, '') AS barra,
                COALESCE("VPXNOM", 'PRODUCTO SIN DESCRIPCION') AS nombre,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("VPXCANT"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS cantidad,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("VPXPRECIO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS costo,
                (
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("VPXCANT"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) *
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("VPXPRECIO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0)
                ) AS total
            FROM pcompra_dbf
            WHERE (? = '' OR "VFECHA" >= ?)
              AND (? = '' OR "VFECHA" <= ?)
              AND (? = '' 
                   OR UPPER(COALESCE("VPXNOM", '')) LIKE UPPER(?) 
                   OR UPPER(COALESCE("VPXBARRA"::text, '')) LIKE UPPER(?)
                   OR UPPER(COALESCE("VFACTURA"::text, '')) LIKE UPPER(?)
                   OR UPPER(COALESCE("VCLIENTE"::text, '')) LIKE UPPER(?))
            ORDER BY "VFECHA" DESC, "VFACTURA" DESC
        """;

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            String fIni = fechaIni == null ? "" : fechaIni.trim();
            String fFin = fechaFin == null ? "" : fechaFin.trim();
            String filtro = filtroBusqueda == null ? "" : filtroBusqueda.trim();

            ps.setString(1, fIni);
            ps.setString(2, fIni);
            ps.setString(3, fFin);
            ps.setString(4, fFin);
            ps.setString(5, filtro);
            ps.setString(6, "%" + filtro + "%");
            ps.setString(7, "%" + filtro + "%");
            ps.setString(8, "%" + filtro + "%");
            ps.setString(9, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fecha = rs.getString("fecha").trim();
                    String factura = rs.getString("factura").trim();
                    String proveedor = rs.getString("proveedor").trim();
                    String barra = rs.getString("barra").trim();
                    String nombre = rs.getString("nombre").trim();
                    double cantidad = rs.getDouble("cantidad");
                    double costo = rs.getDouble("costo");
                    double total = rs.getDouble("total");

                    lista.add(new Object[]{
                        fecha,
                        factura,
                        proveedor,
                        barra,
                        nombre,
                        cantidad,
                        costo,
                        total
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public boolean insertarCompra(String fecha, String facturaStr, String clienteStr, String condStr, String barra, String nombre, String cantidad, String costo) {
        String sql = """
            INSERT INTO pcompra_dbf ("VFECHA", "VFACTURA", "VCLIENTE", "VCONDI", "VPXBARRA", "VPXNOM", "VPXCANT", "VPXPRECIO")
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fecha);

            int factura = 0;
            if (facturaStr != null && !facturaStr.trim().isEmpty()) {
                try {
                    factura = Integer.parseInt(facturaStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(2, factura);

            int cliente = 0;
            if (clienteStr != null && !clienteStr.trim().isEmpty()) {
                try {
                    cliente = Integer.parseInt(clienteStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(3, cliente);

            int cond = 0;
            if (condStr != null && !condStr.trim().isEmpty()) {
                try {
                    cond = Integer.parseInt(condStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(4, cond);

            ps.setString(5, barra);
            ps.setString(6, nombre);
            ps.setString(7, cantidad);
            ps.setString(8, costo);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean actualizarCompra(String facturaOriginalStr, String barraOriginal, String fecha, String facturaStr, String clienteStr, String condStr, String barra, String nombre, String cantidad, String costo) {
        String sql = """
            UPDATE pcompra_dbf 
            SET "VFECHA" = ?, "VFACTURA" = ?, "VCLIENTE" = ?, "VCONDI" = ?, "VPXBARRA" = ?, "VPXNOM" = ?, "VPXCANT" = ?, "VPXPRECIO" = ?
            WHERE "VFACTURA" = ? AND "VPXBARRA" = ?
        """;
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fecha);

            int factura = 0;
            if (facturaStr != null && !facturaStr.trim().isEmpty()) {
                try {
                    factura = Integer.parseInt(facturaStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(2, factura);

            int cliente = 0;
            if (clienteStr != null && !clienteStr.trim().isEmpty()) {
                try {
                    cliente = Integer.parseInt(clienteStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(3, cliente);

            int cond = 0;
            if (condStr != null && !condStr.trim().isEmpty()) {
                try {
                    cond = Integer.parseInt(condStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(4, cond);

            ps.setString(5, barra);
            ps.setString(6, nombre);
            ps.setString(7, cantidad);
            ps.setString(8, costo);

            int facturaOriginal = 0;
            if (facturaOriginalStr != null && !facturaOriginalStr.trim().isEmpty()) {
                try {
                    facturaOriginal = Integer.parseInt(facturaOriginalStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(9, facturaOriginal);
            ps.setString(10, barraOriginal);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean eliminarCompra(String facturaStr, String barra) {
        String sql = "DELETE FROM pcompra_dbf WHERE \"VFACTURA\" = ? AND \"VPXBARRA\" = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            // Blindaje para convertir VFACTURA a entero
            int factura = 0;
            if (facturaStr != null && !facturaStr.trim().isEmpty()) {
                try {
                    factura = Integer.parseInt(facturaStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }
            ps.setInt(1, factura);
            ps.setString(2, barra);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
