package dao;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.InventarioItemDTO;

public class InventarioDAO {

    public List<InventarioItemDTO> obtenerInventario(String fechaCorte, boolean verCeros) throws SQLException {
        List<InventarioItemDTO> lista = new ArrayList<>();

        // Usamos comillas dobles para respetar exactamente los nombres de las columnas en PostgreSQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT \"PXCODIGO\", \"PXNOMBRE\", \"PXFAMILIAN\", \"PXSTOCK\", \"PXCOSTO\" ");
        sql.append("FROM \"pproduc_dbf\" ");
        if (!verCeros) {
            sql.append("WHERE CAST(NULLIF(TRIM(\"PXSTOCK\"), '') AS DECIMAL(12,2)) > 0 ");
        }
        sql.append("ORDER BY \"PXCODIGO\" ASC");

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString()); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                InventarioItemDTO item = new InventarioItemDTO(
                        rs.getInt("PXCODIGO"),
                        rs.getString("PXNOMBRE"),
                        rs.getString("PXFAMILIAN"),
                        rs.getString("PXSTOCK"),
                        rs.getString("PXCOSTO")
                );
                lista.add(item);
            }
        }
        return lista;
    }
}
