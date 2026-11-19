package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccesoDAO {

    // Carga los registros directamente desde la tabla keys_dbf de PostgreSQL
    public List<Object[]> obtenerAccesosParaTabla() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT \"A_MODULO\", \"A_RUT\", \"A_CLAVE\", \"A_USUARIO\" FROM keys_dbf ORDER BY \"A_MODULO\" ASC";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String modulo = rs.getString("A_MODULO");
                String rut = rs.getString("A_RUT");
                String clave = rs.getString("A_CLAVE");
                String usuario = rs.getString("A_USUARIO");

                lista.add(new Object[]{
                    modulo != null ? modulo.trim() : "",
                    rut != null ? rut.trim() : "",
                    usuario != null ? usuario.trim() : "",
                    clave != null ? clave.trim() : ""
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener accesos desde keys_dbf: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // Inserta un nuevo permiso / clave de acceso en keys_dbf
    public boolean insertarAcceso(String modulo, String rut, String usuario, String clave) {
        String sql = "INSERT INTO keys_dbf (\"A_MODULO\", \"A_RUT\", \"A_USUARIO\", \"A_CLAVE\") VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, modulo.toUpperCase());
            pstmt.setString(2, rut.toUpperCase());
            pstmt.setString(3, usuario.toUpperCase());
            pstmt.setString(4, clave);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar en keys_dbf: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualiza la clave de acceso en keys_dbf filtrando por módulo y rut
    public boolean actualizarClaveAcceso(String modulo, String rut, String nuevaClave) {
        String sql = "UPDATE keys_dbf SET \"A_CLAVE\" = ? WHERE \"A_MODULO\" = ? AND \"A_RUT\" = ?";
        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevaClave);
            pstmt.setString(2, modulo);
            pstmt.setString(3, rut);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar keys_dbf: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
