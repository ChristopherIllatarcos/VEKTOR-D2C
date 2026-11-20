package dao;

import conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.DocumentoCarteraDTO;
import modelo.EstadisticasClienteDTO;

/**
 *
 * @author caill
 */
public class CuentaCorrienteDAO {

    private static final Logger LOGGER = Logger.getLogger(CuentaCorrienteDAO.class.getName());
    private static final DateTimeFormatter FORMATO_BD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_PANTALLA_BARRA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_PANTALLA_GUION = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // --- CONSULTAR ESTADÍSTICAS DEL CLIENTE ---
    public EstadisticasClienteDTO obtenerEstadisticas(Connection con, String rut) {
        EstadisticasClienteDTO stats = new EstadisticasClienteDTO();
        if (con == null || rut == null || rut.trim().isEmpty()) {
            return stats;
        }

        String rutLimpio = rut.trim();
        stats.setRut(rutLimpio);

        try {
            // 1. Compras ordenadas cronológicamente
            String sqlCompras = "SELECT COUNT(DISTINCT \"VCODIGO\"), "
                    + "COALESCE(SUM(CAST(NULLIF(REPLACE(CAST(\"VPXPRECIO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                    + "COALESCE(AVG(CAST(NULLIF(REPLACE(CAST(\"VPXPRECIO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                    + "COALESCE(MAX(CAST(NULLIF(REPLACE(CAST(\"VPXPRECIO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                    + "TO_CHAR(MAX(TO_DATE(NULLIF(TRIM(CAST(\"VFECHA\" AS TEXT)), ''), 'YYYY-MM-DD')), 'DD-MM-YYYY') "
                    + "FROM pventas_dbf WHERE TRIM(CAST(\"VCLIENTE\" AS text)) LIKE ?";

            try (PreparedStatement ps = con.prepareStatement(sqlCompras)) {
                ps.setString(1, "%" + rutLimpio + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setVecesCompro(rs.getInt(1));
                        stats.setTotalCompro(rs.getDouble(2));
                        stats.setPromCompro(rs.getDouble(3));
                        stats.setMaxCompro(rs.getDouble(4));
                        String fec = rs.getString(5);
                        if (fec != null && !fec.trim().isEmpty()) {
                            stats.setUltimaCompra(fec.trim());
                        }
                    }
                }
            }

            // 2. Pagos / Abonos ordenados cronológicamente
            String sqlPagos = "SELECT COUNT(*), "
                    + "COALESCE(SUM(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                    + "COALESCE(AVG(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                    + "COALESCE(MAX(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                    + "TO_CHAR(MAX(TO_DATE(NULLIF(TRIM(CAST(\"CTEFECHA\" AS TEXT)), ''), 'YYYY-MM-DD')), 'DD-MM-YYYY') "
                    + "FROM pctacte_dbf "
                    + "WHERE TRIM(CAST(\"CTERUT\" AS text)) LIKE ? "
                    + "AND COALESCE(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC), 0) > 0";

            try (PreparedStatement ps = con.prepareStatement(sqlPagos)) {
                ps.setString(1, "%" + rutLimpio + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setVecesPago(rs.getInt(1));
                        stats.setTotalPago(rs.getDouble(2));
                        stats.setPromPago(rs.getDouble(3));
                        stats.setMaxPago(rs.getDouble(4));
                        String fec = rs.getString(5);
                        if (fec != null && !fec.trim().isEmpty()) {
                            stats.setUltimoPago(fec.trim());
                        }
                    }
                }
            }

            // 3. Calcular saldo actual basado en Totales
            stats.setSaldoActual(stats.getTotalCompro() - stats.getTotalPago());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas del cliente", e);
        }
        return stats;
    }

    // --- ELIMINAR PAGO / RECIBO POR FOLIO ---
    public boolean eliminarPagoPorFolio(Connection con, String folio) {
        if (con == null || folio == null || folio.trim().isEmpty()) {
            LOGGER.warning("Intento de eliminar pago con conexión nula o folio vacío.");
            return false;
        }

        String sql = "DELETE FROM pctacte_dbf WHERE CAST(\"CTEFOLIO\" AS TEXT) = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, folio.trim());
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar pago por folio", e);
            return false;
        }
    }

    public List<DocumentoCarteraDTO> listarDocumentosCartera(Connection con, String nombreCliente, String fechaInicio, String fechaFin) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String clienteTrim = nombreCliente != null ? nombreCliente.trim() : "";
        if (clienteTrim.contains("-")) {
            clienteTrim = clienteTrim.split("-")[0];
        }
        clienteTrim = clienteTrim.replace(".", "").replace(" ", "");

        String filtroCliente = "%" + clienteTrim + "%";

        String fechaInicioBD = convertirEntradaAFechaBD(fechaInicio);
        String fechaFinBD = convertirEntradaAFechaBD(fechaFin);
        boolean filtrarFechas = !fechaInicioBD.isEmpty() && !fechaFinBD.isEmpty();

        String sql = "SELECT \"CTEFOLIO\", \"CTEFECHA\", \"CTERUT\", \"CTEABONO\" "
                + "FROM pctacte_dbf "
                + "WHERE (UPPER(CAST(\"CTERUT\" AS TEXT)) LIKE UPPER(?) OR ? = '') ";

        if (filtrarFechas) {
            sql += "AND \"CTEFECHA\" BETWEEN ? AND ? ";
        }

        sql += "ORDER BY \"CTEFECHA\" ASC, \"CTEFOLIO\" ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filtroCliente);
            ps.setString(2, clienteTrim);

            if (filtrarFechas) {
                ps.setString(3, fechaInicioBD);
                ps.setString(4, fechaFinBD);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    doc.setFolio(valorSeguro(rs.getString("CTEFOLIO")));
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, ""));
                    doc.setCliente(valorSeguro(rs.getString("CTERUT")));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));
                    doc.setEstado("ACTIVO");
                    lista.add(doc);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar documentos en cartera", e);
        }
        return lista;
    }

    public List<Object[]> obtenerCartolaCliente(String rutCliente) {
        List<Object[]> listaMovimientos = new ArrayList<>();

        if (rutCliente == null || rutCliente.trim().isEmpty()) {
            return listaMovimientos;
        }

        String rutLimpio = rutCliente.replaceAll("[^0-9kK]", "").trim();
        if (rutLimpio.isEmpty()) {
            return listaMovimientos;
        }

        String cuerpoRut = rutLimpio;
        if (rutLimpio.length() > 1) {
            cuerpoRut = rutLimpio.substring(0, rutLimpio.length() - 1);
        }

        String sql = "SELECT \"CTECPTDOC\", \"CTEDOCUMEN\", \"CTEFECHA\", \"CTEVENCE\", "
                + "\"CTECREDITO\", \"CTEABONO\", "
                + "(COALESCE(\"CTECREDITO\", 0) - COALESCE(\"CTEABONO\", 0)) AS saldo "
                + "FROM pctacte_dbf WHERE REPLACE(REPLACE(REPLACE(\"CTERUT\", '.', ''), '-', ''), ' ', '') = ? "
                + "OR REPLACE(REPLACE(REPLACE(\"CTERUT\", '.', ''), '-', ''), ' ', '') = ? "
                + "ORDER BY "
                + "   CASE "
                + "       WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                + "       THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'YYYY-MM-DD') "
                + "       WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                + "       THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'DD-MM-YYYY') "
                + "       ELSE '1900-01-01'::date "
                + "   END DESC, \"CTECOMPROB\" DESC";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rutLimpio);
            pstmt.setString(2, cuerpoRut);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String tipoDoc = valorSeguro(rs.getString("CTECPTDOC"));
                    String nroDoc = valorSeguro(rs.getString("CTEDOCUMEN"));
                    String fecEmisionFormateada = formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, "");
                    String fecVencFormateada = formatearFechaBDAPantalla(rs.getString("CTEVENCE"), FORMATO_PANTALLA_BARRA, "-");

                    double cargo = parsearMontoSeguro(rs.getObject("CTECREDITO"));
                    double abono = parsearMontoSeguro(rs.getObject("CTEABONO"));
                    double saldo = parsearMontoSeguro(rs.getObject("saldo"));

                    listaMovimientos.add(new Object[]{
                        !tipoDoc.isEmpty() ? tipoDoc : "DOC",
                        !nroDoc.isEmpty() ? nroDoc : "-",
                        fecEmisionFormateada,
                        fecVencFormateada,
                        cargo,
                        abono,
                        saldo
                    });
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error en CuentaCorrienteDAO.obtenerCartolaCliente", ex);
        }

        return listaMovimientos;
    }

    // Método para consultar los detalles de un pago por su folio
    public DocumentoCarteraDTO buscarDetallePorFolio(Connection con, String folio) {
        if (con == null || folio == null || folio.trim().isEmpty()) {
            return null;
        }

        String folioLimpio = folio.trim();
        DocumentoCarteraDTO doc = null;
        String sql = "SELECT \"CTEFECHA\", \"CTERUT\", \"CTEABONO\" FROM pctacte_dbf WHERE CAST(\"CTEFOLIO\" AS TEXT) = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, folioLimpio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doc = new DocumentoCarteraDTO();
                    doc.setFolio(folioLimpio);
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, ""));
                    doc.setCliente(valorSeguro(rs.getString("CTERUT")));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar detalle por folio", e);
        }
        return doc;
    }

    public List<DocumentoCarteraDTO> buscarRecibosPorFiltro(Connection con, String filtro) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String textoOriginal = filtro != null ? filtro.trim() : "";
        String filtroBusqueda = textoOriginal;

        if (filtroBusqueda.contains("-")) {
            filtroBusqueda = filtroBusqueda.split("-")[0].replaceAll("[^0-9]", "");
        } else {
            filtroBusqueda = filtroBusqueda.replaceAll("[^0-9]", "");
            if (filtroBusqueda.isEmpty()) {
                filtroBusqueda = textoOriginal;
            }
        }

        String sql = "SELECT \"CTEFOLIO\", \"CTEFECHA\", \"CTERUT\", \"CTEABONO\" FROM pctacte_dbf "
                + "WHERE CAST(\"CTERUT\" AS TEXT) LIKE ? OR CAST(\"CTEFOLIO\" AS TEXT) LIKE ? "
                + "ORDER BY "
                + "   CASE "
                + "       WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                + "       THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'YYYY-MM-DD') "
                + "       WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                + "       THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'DD-MM-YYYY') "
                + "       ELSE '1900-01-01'::date "
                + "   END DESC LIMIT 100";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String parametroBusqueda = filtroBusqueda.isEmpty() ? "%" : "%" + filtroBusqueda + "%";
            ps.setString(1, parametroBusqueda);
            ps.setString(2, parametroBusqueda);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    doc.setFolio(valorSeguro(rs.getString("CTEFOLIO")));
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, ""));
                    doc.setCliente(valorSeguro(rs.getString("CTERUT")));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));
                    lista.add(doc);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar recibos por filtro", ex);
        }
        return lista;
    }

    // --- LISTADO DE CUENTAS POR COBRAR ALFABETICO CON JOIN A CLIENTES ---
    public List<DocumentoCarteraDTO> obtenerCuentasPorCobrarAlfabetico(Connection con, String filtroTexto) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String filtro = filtroTexto != null ? filtroTexto.trim() : "";

        String sql = "SELECT p.\"CTERUT\", p.\"CTEFOLIO\", p.\"CTEFECHA\", p.\"CTEABONO\", c.\"CLINOMBRE\" "
                + "FROM pctacte_dbf p "
                + "LEFT JOIN pcliente_dbf c ON CAST(p.\"CTERUT\" AS TEXT) = CAST(c.\"CLIRUT\" AS TEXT) "
                + "WHERE UPPER(COALESCE(c.\"CLINOMBRE\", '')) LIKE UPPER(?) "
                + "   OR CAST(p.\"CTERUT\" AS TEXT) LIKE ? "
                + "   OR CAST(p.\"CTEFOLIO\" AS TEXT) LIKE ? "
                + "ORDER BY c.\"CLINOMBRE\" ASC, p.\"CTEFECHA\" DESC LIMIT 200";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String parametro = "%" + filtro + "%";

            ps.setString(1, parametro);
            ps.setString(2, parametro);
            ps.setString(3, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    String nombreCliente = valorSeguro(rs.getString("CLINOMBRE"));
                    String rutCliente = valorSeguro(rs.getString("CTERUT"));

                    doc.setCliente(!nombreCliente.isEmpty() ? nombreCliente + " (" + rutCliente + ")" : rutCliente);
                    doc.setFolio(valorSeguro(rs.getString("CTEFOLIO")));
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, ""));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));

                    lista.add(doc);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener cuentas por cobrar alfabético", ex);
        }
        return lista;
    }

    // --- 1. MODO RESUMEN: Ordenado puramente por fecha más reciente ---
    public List<DocumentoCarteraDTO> obtenerCuentasResumen(Connection con, String filtroTexto) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String filtro = filtroTexto != null ? filtroTexto.trim() : "";

        String sql = "SELECT c.\"CLINOMBRE\", p.\"CTERUT\", "
                + "       COUNT(p.\"CTEFOLIO\") AS total_docs, "
                + "       SUM(p.\"CTEABONO\") AS suma_abonos, "
                + "       MAX(p.\"CTEFECHA\") AS max_fecha "
                + "FROM pctacte_dbf p "
                + "LEFT JOIN pcliente_dbf c ON CAST(p.\"CTERUT\" AS TEXT) = CAST(c.\"CLIRUT\" AS TEXT) "
                + "WHERE UPPER(COALESCE(c.\"CLINOMBRE\", '')) LIKE UPPER(?) "
                + "   OR CAST(p.\"CTERUT\" AS TEXT) LIKE ? "
                + "GROUP BY c.\"CLINOMBRE\", p.\"CTERUT\" "
                + "ORDER BY "
                + "   CASE "
                + "       WHEN TRIM(CAST(MAX(p.\"CTEFECHA\") AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                + "       THEN TO_DATE(TRIM(CAST(MAX(p.\"CTEFECHA\") AS TEXT)), 'YYYY-MM-DD') "
                + "       WHEN TRIM(CAST(MAX(p.\"CTEFECHA\") AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                + "       THEN TO_DATE(TRIM(CAST(MAX(p.\"CTEFECHA\") AS TEXT)), 'DD-MM-YYYY') "
                + "       ELSE '1900-01-01'::date "
                + "   END DESC LIMIT 200";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String parametro = "%" + filtro + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    String nombreCrudo = rs.getString("CLINOMBRE");
                    String rut = valorSeguro(rs.getString("CTERUT"));

                    String nombreFormateado = formatearNombreApellidosPrimero(nombreCrudo);

                    doc.setCliente(!nombreFormateado.isEmpty()
                            ? nombreFormateado + " (" + rut + ")"
                            : rut);
                    doc.setFolio(rs.getInt("total_docs") + " docs");
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("max_fecha"), FORMATO_PANTALLA_BARRA, "---"));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("suma_abonos")));
                    lista.add(doc);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener cuentas resumen", ex);
        }
        return lista;
    }

    // --- 2. MODO DETALLE: Ordenado puramente por fecha de documento descendente ---
    public List<DocumentoCarteraDTO> obtenerCuentasDetalle(Connection con, String filtroTexto) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String filtro = filtroTexto != null ? filtroTexto.trim() : "";

        String sql = "SELECT p.\"CTERUT\", p.\"CTEFOLIO\", p.\"CTEFECHA\", p.\"CTEABONO\", c.\"CLINOMBRE\" "
                + "FROM pctacte_dbf p "
                + "LEFT JOIN pcliente_dbf c ON CAST(p.\"CTERUT\" AS TEXT) = CAST(c.\"CLIRUT\" AS TEXT) "
                + "WHERE UPPER(COALESCE(c.\"CLINOMBRE\", '')) LIKE UPPER(?) "
                + "   OR CAST(p.\"CTERUT\" AS TEXT) LIKE ? "
                + "   OR CAST(p.\"CTEFOLIO\" AS TEXT) LIKE ? "
                + "ORDER BY "
                + "   CASE "
                + "       WHEN TRIM(CAST(p.\"CTEFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                + "       THEN TO_DATE(TRIM(CAST(p.\"CTEFECHA\" AS TEXT)), 'YYYY-MM-DD') "
                + "       WHEN TRIM(CAST(p.\"CTEFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                + "       THEN TO_DATE(TRIM(CAST(p.\"CTEFECHA\" AS TEXT)), 'DD-MM-YYYY') "
                + "       ELSE '1900-01-01'::date "
                + "   END DESC LIMIT 200";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String parametro = "%" + filtro + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);
            ps.setString(3, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    String nombreCrudo = rs.getString("CLINOMBRE");
                    String rut = valorSeguro(rs.getString("CTERUT"));

                    String nombreFormateado = formatearNombreApellidosPrimero(nombreCrudo);

                    doc.setCliente(!nombreFormateado.isEmpty()
                            ? nombreFormateado + " (" + rut + ")"
                            : rut);
                    doc.setFolio(valorSeguro(rs.getString("CTEFOLIO")));
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, "---"));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));
                    lista.add(doc);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener cuentas detalle", ex);
        }
        return lista;
    }

    // Método auxiliar unificado para poner los apellidos siempre primero
    private String formatearNombreApellidosPrimero(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            return "";
        }

        String limpio = nombreOriginal.trim().replaceAll("\\s+", " ");
        String[] partes = limpio.split(" ");

        if (partes.length == 2) {
            return partes[1] + " " + partes[0];
        } else if (partes.length == 3) {
            return partes[1] + " " + partes[2] + " " + partes[0];
        } else if (partes.length >= 4) {
            String apellido2 = partes[partes.length - 1];
            String apellido1 = partes[partes.length - 2];

            StringBuilder nombresRestantes = new StringBuilder();
            for (int i = 0; i < partes.length - 2; i++) {
                nombresRestantes.append(partes[i]).append(" ");
            }
            return apellido1 + " " + apellido2 + " " + nombresRestantes.toString().trim();
        }

        return limpio;
    }

    // --- 3. CARTOLA POR FECHA: Ordenada alfabéticamente por apellido y rango de fechas ---
    public List<DocumentoCarteraDTO> obtenerCartolaPorFecha(Connection con, String rutCliente, String fechaDesde, String fechaHasta) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null || rutCliente == null || rutCliente.trim().isEmpty()) {
            return lista;
        }

        String fechaDesdeBD = convertirEntradaAFechaBD(fechaDesde);
        String fechaHastaBD = convertirEntradaAFechaBD(fechaHasta);
        boolean filtrarFechas = !fechaDesdeBD.isEmpty() && !fechaHastaBD.isEmpty();

        String sql = "SELECT p.\"CTERUT\", p.\"CTEFOLIO\", p.\"CTEFECHA\", p.\"CTEABONO\", c.\"CLINOMBRE\" "
                + "FROM pctacte_dbf p "
                + "LEFT JOIN pcliente_dbf c ON CAST(p.\"CTERUT\" AS TEXT) = CAST(c.\"CLIRUT\" AS TEXT) "
                + "WHERE CAST(p.\"CTERUT\" AS TEXT) = ? ";

        if (filtrarFechas) {
            sql += "  AND ( "
                    + "      CASE "
                    + "          WHEN TRIM(CAST(p.\"CTEFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                    + "          THEN TO_DATE(TRIM(CAST(p.\"CTEFECHA\" AS TEXT)), 'YYYY-MM-DD') "
                    + "          WHEN TRIM(CAST(p.\"CTEFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                    + "          THEN TO_DATE(TRIM(CAST(p.\"CTEFECHA\" AS TEXT)), 'DD-MM-YYYY') "
                    + "          ELSE NULL "
                    + "      END "
                    + "  ) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ";
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rutCliente.trim());

            if (filtrarFechas) {
                ps.setString(2, fechaDesdeBD);
                ps.setString(3, fechaHastaBD);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    String nombreCrudo = rs.getString("CLINOMBRE");
                    String rut = valorSeguro(rs.getString("CTERUT"));

                    String nombreFormateado = formatearNombreApellidosPrimero(nombreCrudo);

                    doc.setCliente(!nombreFormateado.isEmpty()
                            ? nombreFormateado + " (" + rut + ")"
                            : rut);
                    doc.setFolio(valorSeguro(rs.getString("CTEFOLIO")));
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, "---"));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));

                    lista.add(doc);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener cartola por fecha", ex);
        }
        return lista;
    }

    private String convertirEntradaAFechaBD(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            return "";
        }
        String limpia = fechaTexto.trim();
        if (limpia.length() > 10) {
            limpia = limpia.substring(0, 10);
        }

        DateTimeFormatter[] formatosEntrada = {
            FORMATO_PANTALLA_BARRA,
            FORMATO_PANTALLA_GUION,
            FORMATO_BD
        };

        for (DateTimeFormatter fmt : formatosEntrada) {
            try {
                LocalDate fecha = LocalDate.parse(limpia, fmt);
                return fecha.format(FORMATO_BD);
            } catch (DateTimeParseException ignored) {
            }
        }
        return "";
    }

    private String formatearFechaBDAPantalla(String fechaBD, DateTimeFormatter destino, String valorPorDefecto) {
        String defecto = valorPorDefecto != null ? valorPorDefecto : "";
        if (fechaBD == null || fechaBD.trim().isEmpty()) {
            return defecto;
        }
        String limpia = fechaBD.trim();
        if (limpia.length() > 10) {
            limpia = limpia.substring(0, 10);
        }

        DateTimeFormatter[] formatosPosibles = {
            FORMATO_BD,
            FORMATO_PANTALLA_GUION,
            FORMATO_PANTALLA_BARRA
        };

        for (DateTimeFormatter fmt : formatosPosibles) {
            try {
                LocalDate parsed = LocalDate.parse(limpia, fmt);
                return parsed.format(destino);
            } catch (DateTimeParseException ignored) {
            }
        }
        return defecto.isEmpty() ? limpia : defecto;
    }

    private double parsearMontoSeguro(Object valor) {
        if (valor == null) {
            return 0.0;
        }
        if (valor instanceof Number num) {
            return num.doubleValue();
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return 0.0;
        }
        try {
            String limpio = texto.replace(" ", "");
            if (limpio.contains(",") && limpio.contains(".")) {
                limpio = limpio.replace(".", "").replace(",", ".");
            } else if (limpio.contains(",")) {
                limpio = limpio.replace(",", ".");
            }
            return new BigDecimal(limpio).doubleValue();
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String valorSeguro(String texto) {
        return texto != null ? texto.trim() : "";
    }

    public List<DocumentoCarteraDTO> listarDocumentosCarteraNormalizado(Connection con, String nombreCliente, String fechaInicio, String fechaFin) {
        List<DocumentoCarteraDTO> lista = new ArrayList<>();
        if (con == null) {
            return lista;
        }

        String clienteTrim = nombreCliente != null ? nombreCliente.trim() : "";
        if (clienteTrim.contains("-")) {
            clienteTrim = clienteTrim.split("-")[0];
        }
        clienteTrim = clienteTrim.replace(".", "").replace(" ", "");

        String filtroCliente = "%" + clienteTrim + "%";

        String fechaInicioBD = convertirEntradaAFechaBD(fechaInicio);
        String fechaFinBD = convertirEntradaAFechaBD(fechaFin);
        boolean filtrarFechas = !fechaInicioBD.isEmpty() && !fechaFinBD.isEmpty();

        String sql = "SELECT \"CTEFOLIO\", \"CTEFECHA\", \"CTERUT\", \"CTEABONO\" "
                + "FROM pctacte_dbf "
                + "WHERE (UPPER(CAST(\"CTERUT\" AS TEXT)) LIKE UPPER(?) OR ? = '') ";

        if (filtrarFechas) {
            sql += "AND ( "
                    + "    CASE "
                    + "        WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                    + "        THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'YYYY-MM-DD') "
                    + "        WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                    + "        THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'DD-MM-YYYY') "
                    + "        ELSE NULL "
                    + "    END "
                    + ") BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') ";
        }

        sql += "ORDER BY "
                + "   CASE "
                + "       WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{4}-\\d{2}-\\d{2}$' "
                + "       THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'YYYY-MM-DD') "
                + "       WHEN TRIM(CAST(\"CTEFECHA\" AS TEXT)) ~ '^\\d{2}-\\d{2}-\\d{4}$' "
                + "       THEN TO_DATE(TRIM(CAST(\"CTEFECHA\" AS TEXT)), 'DD-MM-YYYY') "
                + "       ELSE '1900-01-01'::date "
                + "   END ASC, \"CTEFOLIO\" ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filtroCliente);
            ps.setString(2, clienteTrim);

            if (filtrarFechas) {
                ps.setString(3, fechaInicioBD);
                ps.setString(4, fechaFinBD);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoCarteraDTO doc = new DocumentoCarteraDTO();
                    doc.setFolio(valorSeguro(rs.getString("CTEFOLIO")));
                    doc.setFecha(formatearFechaBDAPantalla(rs.getString("CTEFECHA"), FORMATO_PANTALLA_BARRA, ""));
                    doc.setCliente(valorSeguro(rs.getString("CTERUT")));
                    doc.setMonto(parsearMontoSeguro(rs.getObject("CTEABONO")));
                    doc.setEstado("ACTIVO");
                    lista.add(doc);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar documentos en cartera normalizado", e);
        }
        return lista;
    }
}
