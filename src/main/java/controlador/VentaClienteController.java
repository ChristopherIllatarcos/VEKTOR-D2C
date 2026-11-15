package controlador;

import dao.VentaClienteDAO;
import modelo.VentaClienteDTO;
import java.sql.SQLException;
import java.util.List;

public class VentaClienteController {

    private VentaClienteDAO dao;

    public VentaClienteController() {
        this.dao = new VentaClienteDAO();
    }

    public List<VentaClienteDTO> generarInforme(String fechaInicial, String fechaFinal) {
        try {
            return dao.obtenerVentasPorCliente(fechaInicial, fechaFinal);
        } catch (SQLException e) {
            System.err.println("Error al generar informe de ventas por cliente: " + e.getMessage());
            return null;
        }
    }
}
