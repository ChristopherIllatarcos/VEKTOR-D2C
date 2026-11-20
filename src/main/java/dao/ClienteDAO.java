package dao;

import conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.ClienteDTO;
import modelo.EstadisticasClienteDTO;

public class ClienteDAO {

    private static final Logger LOGGER = Logger.getLogger(ClienteDAO.class.getName());

    public List<Object[]> cargarClientesDAO(String filtro) {
        List<Object[]> listaClientes = new ArrayList<>();

        String sql = "SELECT (\"CLIRUT\" || '-' || \"CLIDIGIT\") AS rut_completo, "
                + "\"CLINOMBRE\", \"CLIDIREC\", \"CLICOMUNA\", \"CLIZONA\", \"CLIFONO1\", \"CLICIUDAD\" "
                + "FROM pcliente_dbf "
                + "WHERE (\"CLIRUT\" || '-' || \"CLIDIGIT\") ILIKE ? OR \"CLINOMBRE\" ILIKE ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String parametro = "%" + (filtro != null ? filtro.trim() : "") + "%";
            pstmt.setString(1, parametro);
            pstmt.setString(2, parametro);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[]{
                        valorSeguro(rs.getString("rut_completo")),
                        valorSeguro(rs.getString("CLINOMBRE")),
                        valorSeguro(rs.getString("CLIDIREC")),
                        valorSeguro(rs.getString("CLICOMUNA")),
                        valorSeguro(rs.getString("CLIZONA")),
                        valorSeguro(rs.getString("CLIFONO1")),
                        valorSeguro(rs.getString("CLICIUDAD"))
                    };
                    listaClientes.add(fila);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los clientes", e);
        }

        return listaClientes;
    }

    public EstadisticasClienteDTO obtenerEstadisticasPorRut(String rutParam) {
        EstadisticasClienteDTO stats = null;

        String rutLimpio = rutParam;
        if (rutParam != null && rutParam.contains("-")) {
            rutLimpio = rutParam.split("-")[0];
        }

        int rutInt = 0;
        try {
            String digitos = rutLimpio != null ? rutLimpio.replaceAll("[^0-9]", "").trim() : "";
            if (!digitos.isEmpty()) {
                rutInt = Integer.parseInt(digitos);
            }
        } catch (NumberFormatException e) {
            rutInt = 0;
        }

        if (rutInt <= 0) {
            return null;
        }

        String sqlCliente = "SELECT \"CLIRUT\", \"CLIDIGIT\", \"CLINOMBRE\", \"CLISALDO\" "
                + "FROM pcliente_dbf "
                + "WHERE TRIM(\"CLIRUT\")::text = ?";

        String sqlCompras = "SELECT COUNT(DISTINCT \"VCODIGO\"), "
                + "COALESCE(SUM(CAST(NULLIF(REPLACE(CAST(\"VPXPRECIO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                + "COALESCE(AVG(CAST(NULLIF(REPLACE(CAST(\"VPXPRECIO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                + "COALESCE(MAX(CAST(NULLIF(REPLACE(CAST(\"VPXPRECIO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                + "TO_CHAR(MAX(TO_DATE(NULLIF(TRIM(CAST(\"VFECHA\" AS TEXT)), ''), 'DD-MM-YYYY')), 'DD-MM-YYYY') "
                + "FROM pventas_dbf WHERE TRIM(CAST(\"VCLIENTE\" AS text)) = ?";

        String sqlPagos = "SELECT COUNT(*), "
                + "COALESCE(SUM(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                + "COALESCE(AVG(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                + "COALESCE(MAX(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC)), 0), "
                + "TO_CHAR(MAX(TO_DATE(NULLIF(TRIM(CAST(\"CTEFECHA\" AS TEXT)), ''), 'DD-MM-YYYY')), 'DD-MM-YYYY') "
                + "FROM pctacte_dbf "
                + "WHERE TRIM(CAST(\"CTERUT\" AS text)) = ? "
                + "AND COALESCE(CAST(NULLIF(REPLACE(CAST(\"CTEABONO\" AS TEXT), ',', '.'), '') AS NUMERIC), 0) > 0";

        try (Connection conn = Conexion.getConnection()) {

            try (PreparedStatement pstmt = conn.prepareStatement(sqlCliente)) {
                pstmt.setString(1, String.valueOf(rutInt));
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        stats = new EstadisticasClienteDTO();
                        String rutNumStr = valorSeguro(rs.getString("CLIRUT"));
                        String digito = valorSeguro(rs.getString("CLIDIGIT"));

                        stats.setRut(!digito.isEmpty() ? rutNumStr + "-" + digito : rutNumStr);
                        stats.setNombre(valorSeguro(rs.getString("CLINOMBRE")));
                        BigDecimal saldo = rs.getBigDecimal("CLISALDO");
                        stats.setSaldoActual(saldo != null ? saldo.doubleValue() : 0.0);
                    }
                }
            }

            if (stats != null) {
                try (PreparedStatement pstmtC = conn.prepareStatement(sqlCompras)) {
                    pstmtC.setString(1, String.valueOf(rutInt));
                    try (ResultSet rsC = pstmtC.executeQuery()) {
                        if (rsC.next()) {
                            stats.setVecesCompro(rsC.getInt(1));
                            stats.setTotalCompro(rsC.getDouble(2));
                            stats.setPromCompro(rsC.getDouble(3));
                            stats.setMaxCompro(rsC.getDouble(4));
                            String fecCompra = valorSeguro(rsC.getString(5));
                            stats.setUltimaCompra(!fecCompra.isEmpty() ? fecCompra : "N/A");
                        }
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Aviso al consultar compras del cliente", ex);
                }

                try (PreparedStatement pstmtP = conn.prepareStatement(sqlPagos)) {
                    pstmtP.setString(1, String.valueOf(rutInt));
                    try (ResultSet rsP = pstmtP.executeQuery()) {
                        if (rsP.next()) {
                            stats.setVecesPago(rsP.getInt(1));
                            stats.setTotalPago(rsP.getDouble(2));
                            stats.setPromPago(rsP.getDouble(3));
                            stats.setMaxPago(rsP.getDouble(4));
                            String fecPago = valorSeguro(rsP.getString(5));
                            stats.setUltimoPago(!fecPago.isEmpty() ? fecPago : "N/A");
                        }
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Aviso al consultar pagos del cliente", ex);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas del cliente", e);
        }

        return stats;
    }

    public List<ClienteDTO> obtenerTodosLosClientes() {
        List<ClienteDTO> lista = new ArrayList<>();

        String sql = "SELECT \"CLIRUT\", \"CLIDIGIT\", \"CLINOMBRE\", \"CLISALDO\", \"CLICREDITO\" FROM pcliente_dbf";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ClienteDTO cliente = new ClienteDTO();
                String rut = valorSeguro(rs.getString("CLIRUT"));
                String digit = valorSeguro(rs.getString("CLIDIGIT"));
                cliente.setRut(!digit.isEmpty() ? rut + "-" + digit : rut);
                cliente.setNombres(valorSeguro(rs.getString("CLINOMBRE")));
                cliente.setSaldoActual(rs.getBigDecimal("CLISALDO") != null ? rs.getBigDecimal("CLISALDO") : BigDecimal.ZERO);
                cliente.setCupo(rs.getBigDecimal("CLICREDITO") != null ? rs.getBigDecimal("CLICREDITO") : BigDecimal.ZERO);

                lista.add(cliente);
            }

            lista.sort(Comparator.comparing(ClienteDTO::getNombres, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los clientes", e);
        }

        return lista;
    }

    public boolean eliminarCliente(String rutParam) {
        if (rutParam == null || rutParam.trim().isEmpty()) {
            LOGGER.warning("Intento de eliminar cliente con RUT nulo o vacío.");
            return false;
        }

        String rutParteNumero = rutParam;
        String digitoVerificador = "";

        if (rutParam.contains("-")) {
            String[] partes = rutParam.split("-");
            rutParteNumero = partes[0].trim();
            if (partes.length > 1) {
                digitoVerificador = partes[1].replaceAll("[^0-9kK]", "").trim().toUpperCase();
            }
        }

        rutParteNumero = rutParteNumero.replaceAll("[^0-9]", "").trim();
        if (rutParteNumero.isEmpty()) {
            LOGGER.warning("Intento de eliminar cliente con RUT numérico inválido.");
            return false;
        }

        String sql = "DELETE FROM pcliente_dbf WHERE TRIM(\"CLIRUT\")::text = ? AND UPPER(TRIM(\"CLIDIGIT\")) = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rutParteNumero);
            pstmt.setString(2, digitoVerificador);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar el cliente de la base de datos", e);
            return false;
        }
    }

    public boolean insertarCliente(ClienteDTO cliente) {
        if (cliente == null) {
            LOGGER.warning("Intento de insertar cliente nulo.");
            return false;
        }

        String cliNombreFinal = valorSeguro(cliente.getNombres()).toUpperCase();
        String rutParam = cliente.getRut();
        String rutLimpio = extraerNumeroRut(rutParam);
        String dvLimpio = extraerDigitoRut(cliente.getDigitoVerificador(), rutParam);

        if (rutLimpio.isEmpty() || cliNombreFinal.isEmpty()) {
            LOGGER.warning("Intento de insertar cliente con RUT o nombre vacío.");
            return false;
        }

        String sql = "INSERT INTO pcliente_dbf (\"CLIRUT\", \"CLIDIGIT\", \"CLINOMBRE\", \"CLISALDO\", \"CLICREDITO\") "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT (\"CLIRUT\") DO UPDATE SET "
                + "\"CLIDIGIT\" = EXCLUDED.\"CLIDIGIT\", "
                + "\"CLINOMBRE\" = EXCLUDED.\"CLINOMBRE\", "
                + "\"CLISALDO\" = EXCLUDED.\"CLISALDO\", "
                + "\"CLICREDITO\" = EXCLUDED.\"CLICREDITO\"";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rutLimpio);
            pstmt.setString(2, dvLimpio);
            pstmt.setString(3, cliNombreFinal);
            pstmt.setBigDecimal(4, cliente.getSaldoActual() != null ? cliente.getSaldoActual() : BigDecimal.ZERO);
            pstmt.setBigDecimal(5, cliente.getCupo() != null ? cliente.getCupo() : BigDecimal.ZERO);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar cliente", e);
            return false;
        }
    }

    public boolean actualizarCliente(ClienteDTO cliente) {
        if (cliente == null) {
            LOGGER.warning("Intento de actualizar cliente nulo.");
            return false;
        }

        String cliNombreFinal = valorSeguro(cliente.getNombres()).toUpperCase();
        String rutLimpio = extraerNumeroRut(cliente.getRut());

        if (rutLimpio.isEmpty() || cliNombreFinal.isEmpty()) {
            LOGGER.warning("Intento de actualizar cliente con RUT o nombre vacío.");
            return false;
        }

        String sql = "UPDATE pcliente_dbf SET "
                + "\"CLINOMBRE\" = ?, "
                + "\"CLISALDO\" = ?, "
                + "\"CLICREDITO\" = ? "
                + "WHERE TRIM(\"CLIRUT\")::text = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliNombreFinal);
            pstmt.setBigDecimal(2, cliente.getSaldoActual() != null ? cliente.getSaldoActual() : BigDecimal.ZERO);
            pstmt.setBigDecimal(3, cliente.getCupo() != null ? cliente.getCupo() : BigDecimal.ZERO);
            pstmt.setString(4, rutLimpio);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar cliente", e);
            return false;
        }
    }

    public ClienteDTO obtenerClientePorRut(String rutParam) {
        String rutLimpio = extraerNumeroRut(rutParam);
        if (rutLimpio.isEmpty()) {
            return null;
        }

        String sql = "SELECT \"CLIRUT\", \"CLIDIGIT\", \"CLINOMBRE\", \"CLISALDO\", \"CLICREDITO\" "
                + "FROM pcliente_dbf WHERE TRIM(\"CLIRUT\")::text = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rutLimpio);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ClienteDTO cliente = new ClienteDTO();
                    cliente.setRut(valorSeguro(rs.getString("CLIRUT")));
                    cliente.setDigitoVerificador(valorSeguro(rs.getString("CLIDIGIT")));
                    cliente.setNombres(valorSeguro(rs.getString("CLINOMBRE")));
                    cliente.setSaldoActual(rs.getBigDecimal("CLISALDO") != null ? rs.getBigDecimal("CLISALDO") : BigDecimal.ZERO);
                    cliente.setCupo(rs.getBigDecimal("CLICREDITO") != null ? rs.getBigDecimal("CLICREDITO") : BigDecimal.ZERO);

                    return cliente;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener cliente por RUT", e);
        }

        return null;
    }

    private String extraerNumeroRut(String rutParam) {
        if (rutParam == null || rutParam.trim().isEmpty()) {
            return "";
        }
        String parte = rutParam.contains("-") ? rutParam.split("-")[0] : rutParam;
        return parte.replaceAll("[^0-9]", "").trim();
    }

    private String extraerDigitoRut(String dvParam, String rutParam) {
        if (dvParam != null && !dvParam.trim().isEmpty()) {
            return dvParam.replaceAll("[^0-9kK]", "").trim().toUpperCase();
        }
        if (rutParam != null && rutParam.contains("-")) {
            String[] partes = rutParam.split("-");
            if (partes.length > 1) {
                return partes[1].replaceAll("[^0-9kK]", "").trim().toUpperCase();
            }
        }
        return "";
    }

    private String valorSeguro(String texto) {
        return texto != null ? texto.trim() : "";
    }
}
