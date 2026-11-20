package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.DetalleDocumentoDTO;

public class DocumentoDAO {

    private static final Logger LOGGER = Logger.getLogger(DocumentoDAO.class.getName());

    public List<DetalleDocumentoDTO> obtenerDetalleDocumento(String tipoDoc, int folio) throws SQLException {
        if (tipoDoc == null || tipoDoc.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento no puede ser nulo o vacío.");
        }
        if (folio <= 0) {
            throw new IllegalArgumentException("El número de folio debe ser mayor que cero.");
        }

        String tipoLimpio = tipoDoc.trim().toUpperCase();
        String columnaFolio;

        switch (tipoLimpio) {
            case "FAC" ->
                columnaFolio = "v.\"VFACTURA\"";
            case "BOL" ->
                columnaFolio = "v.\"VBOLETA\"";
            case "GUI" ->
                columnaFolio = "v.\"VGUIA\"";
            case "VAL" ->
                columnaFolio = "v.\"VVALE\"";
            case "COT" ->
                columnaFolio = "v.\"VCOTIZA\"";
            default ->
                throw new IllegalArgumentException("Tipo de documento no reconocido: " + tipoLimpio);
        }

        String sql = "SELECT d.\"DCODIGO\", d.\"DDESCRIP\", d.\"DCANTIDAD\", d.\"DPRECIO\", d.\"DTOTAL\" "
                + "FROM \"pdetalle_dbf\" d "
                + "JOIN \"pventas_dbf\" v ON CAST(d.\"DPEDIDO\" AS VARCHAR) = CAST(v.\"VCODIGO\" AS VARCHAR) "
                + "WHERE CAST(" + columnaFolio + " AS VARCHAR) = ?";

        List<DetalleDocumentoDTO> lista = new ArrayList<>();

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, String.valueOf(folio));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString("DCODIGO");
                    String descrip = rs.getString("DDESCRIP");

                    DetalleDocumentoDTO dto = new DetalleDocumentoDTO(
                            codigo != null ? codigo.trim() : "",
                            descrip != null ? descrip.trim() : "",
                            rs.getDouble("DCANTIDAD"),
                            rs.getDouble("DPRECIO"),
                            rs.getDouble("DTOTAL")
                    );
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalle del documento tipo " + tipoLimpio + " y folio " + folio, e);
            throw e;
        }

        return lista;
    }
}
