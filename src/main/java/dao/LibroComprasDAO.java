package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LibroComprasDAO {

    public List<Object[]> buscarLibroCompras(String fechaInicioStr, String fechaFinStr, String nombreProveedor) {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
            SELECT 
                c."VFECHA" AS fecha,
                c."VFACTURA" AS factura,
                c."VCLIENTE" AS rut,
                COALESCE(p."PRVDIGIT"::text, '') AS dv,
                COALESCE(p."PRVNOMBRE", 'PROVEEDOR SIN NOMBRE') AS proveedor,
                COALESCE(MAX(c."VCONDIX"), '') AS condicion,
                SUM(
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM(c."VPXCANT"), '.', ''), ',', '.'), '') AS NUMERIC), 0) *
                    COALESCE(CAST(NULLIF(REPLACE(REPLACE(TRIM(c."VPXPRECIO"), '.', ''), ',', '.'), '') AS NUMERIC), 0)
                ) AS total_compra
            FROM pcompra_dbf c
            LEFT JOIN pproveed_dbf p ON c."VCLIENTE" = p."PRVRUT"
            WHERE (? = '' OR UPPER(COALESCE(p."PRVNOMBRE", '')) LIKE UPPER(?))
            GROUP BY c."VFECHA", c."VFACTURA", c."VCLIENTE", p."PRVDIGIT", p."PRVNOMBRE"
            ORDER BY c."VFACTURA" ASC
        """;

        DateTimeFormatter fmtUsuario = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fIni = parsearFechaFlexible(fechaInicioStr);
        LocalDate fFin = parsearFechaFlexible(fechaFinStr);

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            String filtroProv = nombreProveedor == null ? "" : nombreProveedor.trim();
            ps.setString(1, filtroProv);
            ps.setString(2, "%" + filtroProv + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fechaBD = rs.getString("fecha") != null ? rs.getString("fecha").trim() : "";
                    LocalDate fechaDoc = parsearFechaFlexible(fechaBD);

                    // Filtrar por rango de fechas
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
                    long factura = rs.getLong("factura");
                    long rut = rs.getLong("rut");
                    String dv = rs.getString("dv").trim();
                    String rutCompleto = dv.isEmpty() ? String.valueOf(rut) : rut + "-" + dv;
                    String proveedor = rs.getString("proveedor").trim();
                    String condicion = rs.getString("condicion").trim();

                    double total = rs.getDouble("total_compra");
                    double neto = total / 1.19;
                    double ivaMonto = total - neto;

                    lista.add(new Object[]{
                        fechaMostrar,
                        factura,
                        rutCompleto,
                        proveedor,
                        condicion,
                        neto,
                        ivaMonto,
                        total
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
