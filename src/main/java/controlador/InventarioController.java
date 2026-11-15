package controlador;

import dao.InventarioDAO;
import modelo.InventarioItemDTO;
import java.sql.SQLException;
import java.util.List;

public class InventarioController {
    private InventarioDAO inventarioDAO;

    public InventarioController() {
        this.inventarioDAO = new InventarioDAO();
    }

    public List<InventarioItemDTO> cargarReporteInventario(String fechaCorte, boolean verCeros) {
        try {
            return inventarioDAO.obtenerInventario(fechaCorte, verCeros);
        } catch (SQLException e) {
            System.err.println("Error al obtener inventario en DAO: " + e.getMessage());
            return null;
        }
    }
}