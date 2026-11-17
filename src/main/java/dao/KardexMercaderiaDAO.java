package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class KardexMercaderiaDAO {

    public List<Object[]> buscarKardex(String filtroBusqueda) {
        List<Object[]> lista = new ArrayList<>();

        // Consulta los datos del inventario para armar el kárdex / valorización por producto
        String sql = """
            SELECT 
                COALESCE("PXBARRA"::text, '') AS barra,
                COALESCE("PXFAMILIA"::text, '') AS familia,
                COALESCE("PXNOMBRE", 'PRODUCTO SIN DESCRIPCION') AS nombre,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCOSTO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS costo,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK0"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS stock0,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK1"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS stock1,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK2"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS stock2,
                (
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK1"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) *
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCOSTO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0)
                ) AS total
            FROM pproinv_dbf
            WHERE (? = '' 
                   OR UPPER(COALESCE("PXNOMBRE", '')) LIKE UPPER(?) 
                   OR UPPER(COALESCE("PXBARRA"::text, '')) LIKE UPPER(?))
            ORDER BY "PXNOMBRE" ASC
        """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
                    double stock0 = rs.getDouble("stock0");
                    double stock1 = rs.getDouble("stock1");
                    double stock2 = rs.getDouble("stock2");
                    double total = rs.getDouble("total");

                    lista.add(new Object[]{
                        barra,
                        familia,
                        nombre,
                        costo,
                        stock0,
                        stock1,
                        stock2,
                        total
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }
}