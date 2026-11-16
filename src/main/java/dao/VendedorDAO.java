package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.VendedorDTO;

public class VendedorDAO {

     // 1. Listar todos los vendedores reemplazando la coma por punto de forma segura
    public List<VendedorDTO> listarVendedores() throws SQLException {
        List<VendedorDTO> lista = new ArrayList<>();
        String sql = "SELECT \"VENDECOD\", \"VENDENOM\", "
                + "COALESCE(CAST(NULLIF(REPLACE(TRIM(CAST(\"VENDEVEN\" AS TEXT)), ',', '.'), '') AS NUMERIC), 0) as comision "
                + "FROM \"pvende_dbf\" ORDER BY \"VENDECOD\" ASC";

        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int codigo = rs.getInt("VENDECOD");
                String nombre = rs.getString("VENDENOM");
                double comision = rs.getDouble("comision");

                lista.add(new VendedorDTO(codigo, nombre, comision));
            }
        } catch (Exception e) {
            System.err.println("Error al listar vendedores: " + e.getMessage());
            throw new SQLException(e);
        }
        return lista;
    }

    // 2. Insertar un nuevo vendedor
    public boolean insertarVendedor(int codigo, String nombre, double comision) throws SQLException {
        String sql = "INSERT INTO \"pvende_dbf\" (\"VENDECOD\", \"VENDENOM\", \"VENDEVEN\") VALUES (?, ?, ?)";
        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            pstmt.setString(2, nombre);
            pstmt.setDouble(3, comision);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 3. Modificar vendedor existente
    public boolean actualizarVendedor(int codigo, String nombre, double comision) throws SQLException {
        String sql = "UPDATE \"pvende_dbf\" SET \"VENDENOM\" = ?, \"VENDEVEN\" = ? WHERE \"VENDECOD\" = ?";
        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setDouble(2, comision);
            pstmt.setInt(3, codigo);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 4. Eliminar vendedor (Supr)
    public boolean eliminarVendedor(int codigo) throws SQLException {
        String sql = "DELETE FROM \"pvende_dbf\" WHERE \"VENDECOD\" = ?";
        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 5. Obtener el nombre del vendedor por su código numérico
    public String obtenerNombreVendedor(int codigo) throws SQLException {
        String nombre = "DESCONOCIDO";
        String sql = "SELECT \"VENDENOM\" FROM \"pvende_dbf\" WHERE \"VENDECOD\" = ?";

        try (Connection conn = conexion.Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, codigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("VENDENOM");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener nombre del vendedor: " + e.getMessage());
            throw new SQLException(e);
        }
        return nombre;
    }
}
