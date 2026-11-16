package dao;

import modelo.FamiliaDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FamiliaDAO {

    public List<FamiliaDTO> listarFamilias() {
        List<FamiliaDTO> lista = new ArrayList<>();
        String sql = "SELECT \"FAMILICOD\", \"FAMILINOM\", \"FAMIPOR100\" FROM pfamili_dbf ORDER BY \"FAMILICOD\" ASC";

        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String codigo = rs.getString("FAMILICOD");
                String descripcion = rs.getString("FAMILINOM");
                String ivaStr = rs.getString("FAMIPOR100");

                double ivaAdic = 0.0;
                if (ivaStr != null && !ivaStr.trim().isEmpty()) {
                    try {
                        String limpio = ivaStr.trim().replace(".", "").replace(",", ".");
                        ivaAdic = Double.parseDouble(limpio);
                    } catch (Exception e) {
                        ivaAdic = 0.0;
                    }
                }

                lista.add(new FamiliaDTO(
                        codigo != null ? codigo.trim() : "",
                        descripcion != null ? descripcion.trim() : "",
                        ivaAdic
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar familias: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // --- NUEVO: INSERTAR ---
    public boolean insertar(FamiliaDTO f) {
        String sql = "INSERT INTO pfamili_dbf (\"FAMILICOD\", \"FAMILINOM\", \"FAMIPOR100\") VALUES (?, ?, ?)";
        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Convertimos el código a Integer porque la columna en la BD es de tipo int4
            int codigoInt = 0;
            try {
                if (f.getCodigo() != null && !f.getCodigo().trim().isEmpty()) {
                    codigoInt = Integer.parseInt(f.getCodigo().trim());
                }
            } catch (NumberFormatException e) {
                codigoInt = 0;
            }

            pstmt.setInt(1, codigoInt);
            pstmt.setString(2, f.getDescripcion());
            pstmt.setDouble(3, f.getIvaAdic());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar familia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- NUEVO: MODIFICAR ---
    public boolean modificar(FamiliaDTO f) {
        String sql = "UPDATE pfamili_dbf SET \"FAMILINOM\" = ?, \"FAMIPOR100\" = ? WHERE \"FAMILICOD\" = ?";
        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Convertimos el código a Integer para que coincida con el tipo de dato de la columna en la BD
            int codigoInt = 0;
            try {
                if (f.getCodigo() != null && !f.getCodigo().trim().isEmpty()) {
                    codigoInt = Integer.parseInt(f.getCodigo().trim());
                }
            } catch (NumberFormatException e) {
                codigoInt = 0;
            }

            pstmt.setString(1, f.getDescripcion());
            pstmt.setDouble(2, f.getIvaAdic());
            pstmt.setInt(3, codigoInt); // Parámetro del WHERE convertido a entero

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al modificar familia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(String codigo) {
        String sql = "DELETE FROM pfamili_dbf WHERE \"FAMILICOD\" = ?";
        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Convertimos el código a Integer para que coincida con el tipo int4 de la base de datos
            int codigoInt = 0;
            try {
                if (codigo != null && !codigo.trim().isEmpty()) {
                    codigoInt = Integer.parseInt(codigo.trim());
                }
            } catch (NumberFormatException e) {
                codigoInt = 0;
            }

            pstmt.setInt(1, codigoInt);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar familia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
