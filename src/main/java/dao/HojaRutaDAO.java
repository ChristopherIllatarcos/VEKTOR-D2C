package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HojaRutaDAO {

    public List<Object[]> buscarHojasRuta(String fechaIni, String fechaFin, String filtroBusqueda) {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT 
                COALESCE("VFECHA"::text, '') AS fecha,
                COALESCE("VGUIA"::text, '') AS guia,
                COALESCE("VDESPANUM"::text, '') AS numdespacho,
                COALESCE("VCLIENTE", 'CLIENTE GENERAL') AS cliente,
                COALESCE("VPATENTE"::text, 'SIN PATENTE') AS patente,
                COALESCE("VCHOFER", 'CHOFER NO ASIGNADO') AS chofer,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("VPXTOTAL"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS total
            FROM pventas_dbf
            WHERE (? = '' OR "VFECHA" >= ?)
              AND (? = '' OR "VFECHA" <= ?)
              AND (? = '' 
                   OR UPPER(COALESCE("VCLIENTE", '')) LIKE UPPER(?) 
                   OR UPPER(COALESCE("VGUIA"::text, '')) LIKE UPPER(?)
                   OR UPPER(COALESCE("VDESPANUM"::text, '')) LIKE UPPER(?)
                   OR UPPER(COALESCE("VPATENTE"::text, '')) LIKE UPPER(?)
                   OR UPPER(COALESCE("VCHOFER", '')) LIKE UPPER(?))
            ORDER BY "VFECHA" DESC, "VGUIA" DESC
        """;

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
            ps.setString(10, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("fecha").trim(),
                        rs.getString("guia").trim(),
                        rs.getString("numdespacho").trim(),
                        rs.getString("cliente").trim(),
                        rs.getString("patente").trim(),
                        rs.getString("chofer").trim(),
                        rs.getDouble("total")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public boolean insertarHojaRuta(String fecha, String guia, String numDespacho, String cliente, String patente, String chofer) {
        String sql = """
            INSERT INTO pventas_dbf ("VFECHA", "VGUIA", "VDESPANUM", "VCLIENTE", "VPATENTE", "VCHOFER")
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setString(2, guia);
            ps.setString(3, numDespacho);
            ps.setString(4, cliente);
            ps.setString(5, patente);
            ps.setString(6, chofer);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean actualizarHojaRuta(String guiaOriginal, String fecha, String guia, String numDespacho, String cliente, String patente, String chofer) {
        String sql = """
            UPDATE pventas_dbf 
            SET "VFECHA" = ?, "VGUIA" = ?, "VDESPANUM" = ?, "VCLIENTE" = ?, "VPATENTE" = ?, "VCHOFER" = ?
            WHERE "VGUIA" = ?
        """;
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setString(2, guia);
            ps.setString(3, numDespacho);
            ps.setString(4, cliente);
            ps.setString(5, patente);
            ps.setString(6, chofer);
            ps.setString(7, guiaOriginal);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean eliminarHojaRuta(String guia) {
        String sql = "DELETE FROM pventas_dbf WHERE \"VGUIA\" = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, guia);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}