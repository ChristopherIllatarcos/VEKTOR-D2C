package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RotacionVentasDAO {

    public List<Object[]> buscarRotacionVentas(String fechaInicioStr, String fechaFinStr, String filtroProducto) {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT 
                "PXBARRA" AS barra,
                "PXFAMILIA" AS familia,
                "PXFECHA" AS fecha,
                "PXNOMBRE" AS nombre,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXCOSTO"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS costo,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXROTAC"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS rotacion_cant,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXROTACTT"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS rotacion_total,
                COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM("PXMEDIA"::text), '.', ''), ',', '.'), '') AS NUMERIC), 0) AS media
            FROM pprorot_dbf
            WHERE (? = '' 
                   OR UPPER(COALESCE("PXNOMBRE", '')) LIKE UPPER(?) 
                   OR UPPER(COALESCE("PXBARRA"::text, '')) LIKE UPPER(?))
            ORDER BY rotacion_cant DESC, "PXNOMBRE" ASC
        """;

        DateTimeFormatter fmtUsuario = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fIni = parsearFechaFlexible(fechaInicioStr);
        LocalDate fFin = parsearFechaFlexible(fechaFinStr);

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = filtroProducto == null ? "" : filtroProducto.trim();
            ps.setString(1, filtro);
            ps.setString(2, "%" + filtro + "%");
            ps.setString(3, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fechaBD = rs.getString("fecha") != null ? rs.getString("fecha").trim() : "";
                    LocalDate fechaDoc = parsearFechaFlexible(fechaBD);

                    if (fIni != null) {
                        if (fechaDoc == null || fechaDoc.isBefore(fIni)) {
                            continue;
                        }
                    }
                    if (fFin != null) {
                        if (fechaDoc == null || fechaDoc.isAfter(fFin)) {
                            continue;
                        }
                    }

                    String fechaMostrar = (fechaDoc != null) ? fechaDoc.format(fmtUsuario) : fechaBD;
                    String barra = rs.getString("barra") != null ? rs.getString("barra").trim() : "";
                    String familia = rs.getString("familia") != null ? rs.getString("familia").trim() : "";
                    String nombre = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
                    double costo = rs.getDouble("costo");
                    double rotacionCant = rs.getDouble("rotacion_cant");
                    double rotacionTotal = rs.getDouble("rotacion_total");
                    double media = rs.getDouble("media");

                    lista.add(new Object[]{
                        barra,
                        familia,
                        fechaMostrar,
                        nombre,
                        costo,
                        rotacionCant,
                        rotacionTotal,
                        media
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lista;
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
