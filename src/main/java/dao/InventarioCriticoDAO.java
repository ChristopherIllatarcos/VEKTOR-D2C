package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InventarioCriticoDAO {

    public List<Object[]> buscarInventarioCritico(String filtroBusqueda) {
        List<Object[]> lista = new ArrayList<>();

        // CORREGIDO: "PXFAMILIA"::text para evitar que el COALESCE intente castear '' a entero
        String sql = """
            SELECT 
                COALESCE("PXBARRA"::text, '') AS barra,
                COALESCE("PXFAMILIA"::text, '') AS familia,
                COALESCE("PXNOMBRE", 'PRODUCTO SIN DESCRIPCION') AS nombre,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCOSTO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS costo,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK1"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS stock,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCRITICO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS critico
            FROM pproinv_dbf
            WHERE 
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK1"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) <=
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCRITICO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0)
                AND (? = '' 
                       OR UPPER(COALESCE("PXNOMBRE", '')) LIKE UPPER(?) 
                       OR UPPER(COALESCE("PXBARRA"::text, '')) LIKE UPPER(?))
            ORDER BY "PXSTOCK1" ASC, "PXNOMBRE" ASC
        """;

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = filtroBusqueda == null ? "" : filtroBusqueda.trim();
            ps.setString(1, filtro);
            ps.setString(2, "%" + filtro + "%");
            ps.setString(3, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String barra = rs.getString("barra").trim();
                    String familia = rs.getString("familia").trim();
                    String nombre = rs.getString("nombre").trim();
                    double costo = rs.getDouble("costo");
                    double stock = rs.getDouble("stock");
                    double critico = rs.getDouble("critico");

                    lista.add(new Object[]{
                        barra,
                        familia,
                        nombre,
                        costo,
                        stock,
                        critico
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public boolean insertarItemCritico(String barra, String familiaStr, String nombre, String costo, String stock1, String critico) {
        String sql = """
            INSERT INTO pproinv_dbf ("PXBARRA", "PXFAMILIA", "PXNOMBRE", "PXCOSTO", "PXSTOCK1", "PXCRITICO")
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, barra);

            int familia = 0;
            if (familiaStr != null && !familiaStr.trim().isEmpty()) {
                try {
                    familia = Integer.parseInt(familiaStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(2, familia);

            ps.setString(3, nombre);
            ps.setString(4, costo);
            ps.setString(5, stock1);
            ps.setString(6, critico);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean actualizarItemCritico(String barraOriginal, String nuevoBarra, String familiaStr, String nombre, String costo, String stock1, String critico) {
        String sql = """
            UPDATE pproinv_dbf 
            SET "PXBARRA" = ?, "PXFAMILIA" = ?, "PXNOMBRE" = ?, "PXCOSTO" = ?, "PXSTOCK1" = ?, "PXCRITICO" = ?
            WHERE "PXBARRA" = ?
        """;
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoBarra);

            int familia = 0;
            if (familiaStr != null && !familiaStr.trim().isEmpty()) {
                try {
                    familia = Integer.parseInt(familiaStr.trim());
                } catch (Exception ignored) {
                }
            }
            ps.setInt(2, familia);

            ps.setString(3, nombre);
            ps.setString(4, costo);
            ps.setString(5, stock1);
            ps.setString(6, critico);
            ps.setString(7, barraOriginal);

            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean eliminarItemCritico(String barra) {
        String sql = "DELETE FROM pproinv_dbf WHERE \"PXBARRA\" = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, barra);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
