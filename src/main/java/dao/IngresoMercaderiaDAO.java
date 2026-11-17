package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class IngresoMercaderiaDAO {

    public List<Object[]> buscarIngresos(String fechaInicioStr, String fechaFinStr, String filtroBusqueda) {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT 
                COALESCE("PXFECHA", '') AS fecha,
                COALESCE("PXBARRA"::text, '') AS barra,
                COALESCE("PXNOMBRE", 'PRODUCTO SIN DESCRIPCION') AS nombre,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK1"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS cantidad,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCOSTO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS costo,
                (
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXSTOCK1"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) *
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCOSTO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0)
                ) AS total
            FROM pproinv_dbf
            WHERE (? = '' 
                   OR UPPER(COALESCE("PXNOMBRE", '')) LIKE UPPER(?) 
                   OR UPPER(COALESCE("PXBARRA"::text, '')) LIKE UPPER(?))
            ORDER BY "PXFECHA" DESC, "PXNOMBRE" ASC
        """;

        DateTimeFormatter fmtUsuario = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fIni = parsearFechaFlexible(fechaInicioStr);
        LocalDate fFin = parsearFechaFlexible(fechaFinStr);

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = filtroBusqueda == null ? "" : filtroBusqueda.trim();
            ps.setString(1, filtro);
            ps.setString(2, "%" + filtro + "%");
            ps.setString(3, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fechaBD = rs.getString("fecha").trim();
                    LocalDate fechaDoc = parsearFechaFlexible(fechaBD);

                    if (fIni != null && (fechaDoc == null || fechaDoc.isBefore(fIni))) {
                        continue;
                    }
                    if (fFin != null && (fechaDoc == null || fechaDoc.isAfter(fFin))) {
                        continue;
                    }

                    String fechaMostrar = (fechaDoc != null) ? fechaDoc.format(fmtUsuario) : fechaBD;
                    String barra = rs.getString("barra").trim();
                    String nombre = rs.getString("nombre").trim();
                    double cantidad = rs.getDouble("cantidad");
                    double costo = rs.getDouble("costo");
                    double total = rs.getDouble("total");

                    lista.add(new Object[]{
                        fechaMostrar,
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

    public boolean insertarIngreso(String fecha, String barra, String nombre, double stock1, double costo) {
        String sql = """
            INSERT INTO pproinv_dbf ("PXFECHA", "PXBARRA", "PXNOMBRE", "PXSTOCK1", "PXCOSTO")
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setString(2, barra);
            ps.setString(3, nombre);
            ps.setDouble(4, stock1);
            ps.setDouble(5, costo);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean actualizarIngreso(String barraOriginal, String fecha, String nuevoBarra, String nombre, double stock1, double costo) {
        String sql = """
            UPDATE pproinv_dbf 
            SET "PXFECHA" = ?, "PXBARRA" = ?, "PXNOMBRE" = ?, "PXSTOCK1" = ?, "PXCOSTO" = ?
            WHERE "PXBARRA" = ?
        """;
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setString(2, nuevoBarra);
            ps.setString(3, nombre);
            ps.setDouble(4, stock1);
            ps.setDouble(5, costo);
            ps.setString(6, barraOriginal);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean eliminarIngreso(String barra) {
        String sql = "DELETE FROM pproinv_dbf WHERE \"PXBARRA\" = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, barra);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private LocalDate parsearFechaFlexible(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String limpio = texto.trim();
        String[] patrones = {"dd/MM/yyyy", "dd-MM-yyyy", "ddMMyyyy", "yyyy-MM-dd", "yyyy/MM/dd"};
        for (String p : patrones) {
            try {
                return LocalDate.parse(limpio, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
