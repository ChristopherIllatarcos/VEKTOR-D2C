package modelo;

public class VentaClienteDTO {

    String codigoCliente;
    String nombreCliente;
    int cantidadDocumentos;
    double totalVentas;

    public VentaClienteDTO(String codigoCliente, String nombreCliente, int cantidadDocumentos, double totalVentas) {
        this.codigoCliente = codigoCliente;
        this.nombreCliente = nombreCliente;
        this.cantidadDocumentos = cantidadDocumentos;
        this.totalVentas = totalVentas;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public int getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    public double getTotalVentas() {
        return totalVentas;
    }
}
