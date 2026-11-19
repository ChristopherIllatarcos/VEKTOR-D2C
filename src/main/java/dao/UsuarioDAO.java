package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Obtiene todos los usuarios para mostrar en la tabla retro
    public List<Object[]> obtenerUsuariosParaTabla() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT \"URUT\", \"UNOMBRE\", \"ULOCAL\", \"ULABOR\", \"UNIVEL\", \"UPASS\" FROM usuario_dbf ORDER BY \"UNOMBRE\" ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String rut = rs.getString("URUT");
                String nombre = rs.getString("UNOMBRE");
                String local = rs.getString("ULOCAL");
                String labor = rs.getString("ULABOR");
                String nivel = rs.getString("UNIVEL");
                String pass = rs.getString("UPASS");

                lista.add(new Object[]{
                    rut != null ? rut.trim() : "",
                    nombre != null ? nombre.trim() : "",
                    local != null ? local.trim() : "",
                    labor != null ? labor.trim() : "",
                    nivel != null ? nivel.trim() : "",
                    pass != null ? pass.trim() : ""
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios desde usuario_dbf: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // Inserta un nuevo usuario en la base de datos
    public boolean insertarUsuario(String rut, String nombre, String pass, String local, String labor, String nivel) {
        String sql = "INSERT INTO usuario_dbf (\"URUT\", \"UNOMBRE\", \"UPASS\", \"ULOCAL\", \"ULABOR\", \"UNIVEL\") VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rut.toUpperCase());
            pstmt.setString(2, nombre.toUpperCase());
            pstmt.setString(3, pass);
            pstmt.setString(4, local.toUpperCase());
            pstmt.setString(5, labor.toUpperCase());
            pstmt.setString(6, nivel.toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario en usuario_dbf: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualiza los datos de un usuario existente filtrando por su RUT
    public boolean actualizarUsuario(String rutOriginal, String nombre, String pass, String local, String labor, String nivel) {
        String sql = "UPDATE usuario_dbf SET \"UNOMBRE\" = ?, \"UPASS\" = ?, \"ULOCAL\" = ?, \"ULABOR\" = ?, \"UNIVEL\" = ? WHERE \"URUT\" = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre.toUpperCase());
            pstmt.setString(2, pass);
            pstmt.setString(3, local.toUpperCase());
            pstmt.setString(4, labor.toUpperCase());
            pstmt.setString(5, nivel.toUpperCase());
            pstmt.setString(6, rutOriginal);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario en usuario_dbf: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}