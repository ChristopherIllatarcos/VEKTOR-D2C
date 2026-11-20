package dao;

import conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.InventarioItemDTO;

public class ProductoVentaDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductoVentaDAO.class.getName());

    // 1. Búsqueda para el buscador flotante de ventas
    public List<Object[]> buscarProductosYVendedorDAO(String textoBusqueda) {
        List<Object[]> lista = new ArrayList<>();
        String filtro = textoBusqueda != null ? textoBusqueda.trim() : "";

        String sql = "SELECT \"PXBARRA\", \"PXNOMBRE\", \"PXILA\", \"PXVENTA0\" "
                + "FROM pproduc_dbf "
                + "WHERE UPPER(\"PXBARRA\") LIKE UPPER(?) OR UPPER(\"PXNOMBRE\") LIKE UPPER(?)";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String parametro = "%" + filtro + "%";
            pstmt.setString(1, parametro);
            pstmt.setString(2, parametro);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[4];
                    fila[0] = valorSeguro(rs.getString("PXBARRA"));
                    fila[1] = valorSeguro(rs.getString("PXNOMBRE"));
                    fila[2] = valorSeguro(rs.getString("PXILA"));
                    fila[3] = parsearDecimalSeguro(rs.getString("PXVENTA0")).doubleValue();
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar productos", e);
        }
        return lista;
    }

    // 2. Obtener todos los productos para la tabla de registro
    public List<Object[]> obtenerTodosLosProductos() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT \"PXBARRA\", \"PXNOMBRE\", \"PXILA\", \"PXVENTA0\" FROM pproduc_dbf";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = valorSeguro(rs.getString("PXBARRA"));
                fila[1] = valorSeguro(rs.getString("PXNOMBRE"));
                fila[2] = valorSeguro(rs.getString("PXILA"));
                fila[3] = valorSeguro(rs.getString("PXVENTA0"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los productos", e);
        }
        return lista;
    }

    // Obtener un producto específico por su código de barra
    public Map<String, Object> obtenerProductoPorBarra(String barra) {
        Map<String, Object> producto = new HashMap<>();
        String barraLimpia = barra != null ? barra.trim() : "";
        if (barraLimpia.isEmpty()) {
            return producto;
        }

        String sql = "SELECT \"PXBARRA\", \"PXNOMBRE\", \"PXILA\", \"PXVENTA0\" FROM pproduc_dbf WHERE \"PXBARRA\" = ?";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barraLimpia);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto.put("barra", valorSeguro(rs.getString("PXBARRA")));
                    producto.put("nombre", valorSeguro(rs.getString("PXNOMBRE")));
                    producto.put("ila", valorSeguro(rs.getString("PXILA")));
                    producto.put("precio", valorSeguro(rs.getString("PXVENTA0")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener producto por barra", e);
        }
        return producto;
    }

    // Insertar un nuevo producto con validación básica
    public boolean insertarProducto(String barra, String nombre, double precio) {
        String barraLimpia = barra != null ? barra.trim() : "";
        String nombreLimpio = nombre != null ? nombre.trim() : "";

        if (barraLimpia.isEmpty() || nombreLimpio.isEmpty()) {
            LOGGER.warning("Intento de insertar producto con barra o nombre vacío.");
            return false;
        }
        if (Double.isNaN(precio) || Double.isInfinite(precio) || precio < 0) {
            LOGGER.warning("Intento de insertar producto con precio inválido.");
            return false;
        }

        String sql = "INSERT INTO pproduc_dbf (\"PXBARRA\", \"PXNOMBRE\", \"PXVENTA0\") VALUES (?, ?, ?)";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barraLimpia);
            pstmt.setString(2, nombreLimpio);
            pstmt.setString(3, BigDecimal.valueOf(precio).toPlainString().replace(".", ","));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar producto", e);
            return false;
        }
    }

    // Actualizar producto usando precio numérico
    public boolean actualizarProducto(String codigoOriginal, String barra, String nombre, double precio) {
        String originalLimpio = codigoOriginal != null ? codigoOriginal.trim() : "";
        String barraLimpia = barra != null ? barra.trim() : "";
        String nombreLimpio = nombre != null ? nombre.trim() : "";

        if (originalLimpio.isEmpty() || barraLimpia.isEmpty() || nombreLimpio.isEmpty()) {
            LOGGER.warning("Intento de actualizar producto con campos obligatorios vacíos.");
            return false;
        }
        if (Double.isNaN(precio) || Double.isInfinite(precio) || precio < 0) {
            LOGGER.warning("Intento de actualizar producto con precio inválido.");
            return false;
        }

        String sql = "UPDATE pproduc_dbf SET \"PXBARRA\" = ?, \"PXNOMBRE\" = ?, \"PXVENTA0\" = ? WHERE \"PXBARRA\" = ?";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barraLimpia);
            pstmt.setString(2, nombreLimpio);
            pstmt.setString(3, BigDecimal.valueOf(precio).toPlainString().replace(".", ","));
            pstmt.setString(4, originalLimpio);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar producto", e);
            return false;
        }
    }

    // Sobrecarga para formularios que reciben el precio como texto
    public boolean actualizarProducto(String nuevaBarra, String nombre, String precioStr, String barraOriginal) {
        if (precioStr == null || precioStr.trim().isEmpty()) {
            LOGGER.warning("Intento de actualizar producto con precio vacío.");
            return false;
        }

        try {
            BigDecimal precioDecimal = parsearDecimalEstricto(precioStr);
            if (precioDecimal.compareTo(BigDecimal.ZERO) < 0) {
                LOGGER.warning("Intento de actualizar producto con precio negativo.");
                return false;
            }
            return actualizarProducto(barraOriginal, nuevaBarra, nombre, precioDecimal.doubleValue());
        } catch (NumberFormatException e) {
            LOGGER.warning(() -> "Precio no válido al actualizar producto: " + precioStr);
            return false;
        }
    }

    public List<Object[]> obtenerProductosParaTabla() {
        List<Object[]> listaProductos = new ArrayList<>();
        String sql = "SELECT \"PXNOMBRE\", \"PXBARRA\", \"PXVENTA0\" FROM pproduc_dbf ORDER BY \"PXNOMBRE\" ASC";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String desc = valorSeguro(rs.getString("PXNOMBRE"));
                String barra = valorSeguro(rs.getString("PXBARRA"));
                BigDecimal precio = parsearDecimalSeguro(rs.getString("PXVENTA0"));

                listaProductos.add(new Object[]{desc, barra, precio});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener productos para la tabla", e);
        }
        return listaProductos;
    }

    public boolean eliminarProducto(String barra) {
        String barraLimpia = barra != null ? barra.trim() : "";
        if (barraLimpia.isEmpty()) {
            LOGGER.warning("Intento de eliminar producto con barra vacía.");
            return false;
        }

        String sql = "DELETE FROM pproduc_dbf WHERE \"PXBARRA\" = ?";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barraLimpia);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto", e);
            return false;
        }
    }

    public List<InventarioItemDTO> listarInventario() {
        List<InventarioItemDTO> listaItems = new ArrayList<>();
        String sql = "SELECT \"PXBARRA\", \"PXNOMBRE\", \"PXILA\", \"PXSTOCK\", \"PXVENTA0\" FROM pproduc_dbf ORDER BY \"PXNOMBRE\" ASC";

        try (Connection conn = Conexion.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String codigoStr = valorSeguro(rs.getString("PXBARRA"));
                String descripcion = valorSeguro(rs.getString("PXNOMBRE"));
                String familiaRaw = valorSeguro(rs.getString("PXILA"));
                String familia = familiaRaw.isEmpty() ? "GENERAL" : familiaRaw;

                double precioFinal = parsearDecimalSeguro(rs.getString("PXVENTA0")).doubleValue();
                double stockFinal = parsearDecimalSeguro(rs.getString("PXSTOCK")).doubleValue();

                int codigo = 0;
                if (!codigoStr.isEmpty()) {
                    String soloDigitos = codigoStr.replaceAll("[^0-9]", "");
                    if (!soloDigitos.isEmpty()) {
                        try {
                            codigo = Integer.parseInt(soloDigitos);
                        } catch (NumberFormatException ignored) {
                            codigo = 0;
                        }
                    }
                }

                InventarioItemDTO item = new InventarioItemDTO(
                        codigo,
                        descripcion,
                        familia,
                        BigDecimal.valueOf(stockFinal).toPlainString(),
                        BigDecimal.valueOf(precioFinal).toPlainString()
                );
                listaItems.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar inventario", e);
        }
        return listaItems;
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor.trim() : "";
    }

    private BigDecimal parsearDecimalEstricto(String texto) {
        String limpio = texto.trim().replace(".", "").replace(",", ".");
        return new BigDecimal(limpio);
    }

    private BigDecimal parsearDecimalSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return parsearDecimalEstricto(texto);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
