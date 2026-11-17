package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import modelo.ParametrosDTO;

public class ParametrosDAO {

    // Método para obtener los parámetros actuales de la BD
    public ParametrosDTO obtenerParametros(Connection con) {
        ParametrosDTO p = new ParametrosDTO(0, 0, 0, 19.0, "");

        String sql = "SELECT pboleta, pfactura, pguia, piva, pfecha FROM pparam_dbf LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                p.setBoleta(rs.getLong("pboleta"));
                p.setFactura(rs.getLong("pfactura"));
                p.setGuia(rs.getLong("pguia"));
                p.setIva(rs.getDouble("piva"));
                p.setFecha(rs.getString("pfecha"));
            }
        } catch (Exception ex) {
            System.out.println("Aviso: No se pudo leer pparam_dbf (usando valores por defecto): " + ex.getMessage());
        }
        return p;
    }

    // Método para guardar o actualizar los parámetros en la BD
    public boolean guardarParametros(Connection con, ParametrosDTO p) {
        String sqlUpdate = "UPDATE pparam_dbf SET pboleta = ?, pfactura = ?, pguia = ?, piva = ?, pfecha = ?";

        try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
            ps.setLong(1, p.getBoleta());
            ps.setLong(2, p.getFactura());
            ps.setLong(3, p.getGuia());
            ps.setDouble(4, p.getIva());
            ps.setString(5, p.getFecha());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                return true;
            } else {
                // Si la tabla estaba totalmente vacía, hacemos un INSERT de respaldo
                String sqlInsert = "INSERT INTO pparam_dbf (pboleta, pfactura, pguia, piva, pfecha) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    psIns.setLong(1, p.getBoleta());
                    psIns.setLong(2, p.getFactura());
                    psIns.setLong(3, p.getGuia());
                    psIns.setDouble(4, p.getIva());
                    psIns.setString(5, p.getFecha());
                    return psIns.executeUpdate() > 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
