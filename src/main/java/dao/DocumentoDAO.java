package dao;

import conexion.Conexion;
import modelo.DetalleDocumentoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentoDAO {

    public List<DetalleDocumentoDTO> obtenerDetalleDocumento(String tipoDoc, int folio) throws SQLException {
        List<DetalleDocumentoDTO> lista = new ArrayList<>();

        // Seleccionamos la columna exacta de pventas_dbf según el tipo de documento
        String columnaFolio = "";
        switch (tipoDoc) {
            case "FAC" -> columnaFolio = "v.\"VFACTURA\"";
            case "BOL" -> columnaFolio = "v.\"VBOLETA\"";
            case "GUI" -> columnaFolio = "v.\"VGUIA\"";
            case "VAL" -> columnaFolio = "v.\"VVALE\"";
            case "COT" -> columnaFolio = "v.\"VCOTIZA\"";
            default -> throw new SQLException("Tipo de documento no reconocido: " + tipoDoc);
        }

        // Conversión explícita CAST para evitar el conflicto entre numeric y varchar
        String sql = "SELECT d.\"DCODIGO\", d.\"DDESCRIP\", d.\"DCANTIDAD\", d.\"DPRECIO\", d.\"DTOTAL\" "
                + "FROM \"pdetalle_dbf\" d "
                + "JOIN \"pventas_dbf\" v ON CAST(d.\"DPEDIDO\" AS VARCHAR) = CAST(v.\"VCODIGO\" AS VARCHAR) "
                + "WHERE CAST(" + columnaFolio + " AS VARCHAR) = ?";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, String.valueOf(folio));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetalleDocumentoDTO dto = new DetalleDocumentoDTO(
                            rs.getString("DCODIGO"),
                            rs.getString("DDESCRIP"),
                            rs.getDouble("DCANTIDAD"),
                            rs.getDouble("DPRECIO"),
                            rs.getDouble("DTOTAL")
                    );
                    lista.add(dto);
                }
            }
        }
        return lista;
    }
}
