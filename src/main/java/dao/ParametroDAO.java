package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import conexion.Conexion;
import java.util.ArrayList;
import java.util.List;

public class ParametroDAO {

    // Método para obtener los valores actuales desde PostgreSQL y mostrarlos en el diálogo
    public static String[] cargarParametros() {
        String[] valores = new String[5]; // [boleta, factura, guia, iva, fecha]
        String sql = "SELECT folio_boleta, folio_factura, folio_guia, iva, fecha_sistema FROM parametros WHERE id = 1";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                valores[0] = String.valueOf(rs.getInt("folio_boleta"));
                valores[1] = String.valueOf(rs.getInt("folio_factura"));
                valores[2] = String.valueOf(rs.getInt("folio_guia"));
                valores[3] = String.valueOf(rs.getDouble("iva"));
                valores[4] = rs.getString("fecha_sistema");
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar los parámetros desde la base de datos: " + e.getMessage(),
                    "Error SQL",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return valores;
    }

    // Método para actualizar los valores en PostgreSQL
    public static boolean actualizar(int boleta, int factura, int guia, double iva, String fecha) {
        String sql = "UPDATE parametros SET folio_boleta = ?, folio_factura = ?, folio_guia = ?, iva = ?, fecha_sistema = ? WHERE id = 1";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, boleta);
            pstmt.setInt(2, factura);
            pstmt.setInt(3, guia);
            pstmt.setDouble(4, iva);
            pstmt.setString(5, fecha);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al actualizar los parámetros en la base de datos: " + e.getMessage(),
                    "Error SQL",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Obtiene los folios de pparam_dbf y los transforma en una lista para la tabla visual
    public List<Object[]> obtenerFoliosParaTabla() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT \"pboleta\", \"pfactura\", \"pguia\" FROM pparam_dbf LIMIT 1";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                long boleta = rs.getLong("pboleta");
                long factura = rs.getLong("pfactura");
                long guia = rs.getLong("pguia");

                // Mapeamos cada uno como una fila en el foliador retro
                lista.add(new Object[]{"BOL", "BOLETA DE VENTA", String.format("%08d", boleta), "ACTIVO"});
                lista.add(new Object[]{"FAC", "FACTURA DE VENTA / COMPRA", String.format("%08d", factura), "ACTIVO"});
                lista.add(new Object[]{"GUI", "GUÍA DE DESPACHO BODEGA", String.format("%08d", guia), "ACTIVO"});
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener folios de parámetros: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // Actualiza el folio correspondiente según el código del documento (BOL, FAC, GUI)
    public boolean actualizarFolio(String codigoDoc, long nuevoFolio) {
        String columna = "";
        if (codigoDoc.equals("BOL")) {
            columna = "pboleta";
        } else if (codigoDoc.equals("FAC")) {
            columna = "pfactura";
        } else if (codigoDoc.equals("GUI")) {
            columna = "pguia";
        } else {
            return false;
        }

        String sql = "UPDATE pparam_dbf SET \"" + columna + "\" = ?";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, nuevoFolio);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar folio en base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Obtiene la Cámara Key desde la base de datos
    public String obtenerCamaraKey() {
        String sql = "SELECT \"pcamara\" FROM pparam_dbf LIMIT 1";
        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String clave = rs.getString("pcamara");
                return (clave != null && !clave.trim().isEmpty()) ? clave.trim() : "KEY-9988-CV2026";
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener Cámara Key: " + e.getMessage());
        }
        return "KEY-9988-CV2026"; // Valor por defecto si está vacío
    }

    // Actualiza la Cámara Key en la base de datos
    public boolean actualizarCamaraKey(String nuevaClave) {
        String sql = "UPDATE pparam_dbf SET \"pcamara\" = ?";
        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevaClave);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar Cámara Key: " + e.getMessage());
            return false;
        }
    }
}
