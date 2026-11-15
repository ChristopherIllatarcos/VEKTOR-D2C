package modelo;

public class DetalleDocumentoDTO {

    private String codigoProducto;
    private String descripcion;
    private double cantidad;
    private double precioUnitario;
    private double totalLinea;

    public DetalleDocumentoDTO(String codigoProducto, String descripcion, double cantidad, double precioUnitario, double totalLinea) {
        this.codigoProducto = codigoProducto;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalLinea = totalLinea;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getTotalLinea() {
        return totalLinea;
    }
}
