package modelo;

public class VentaFamiliaDTO {

    private int codigoFamilia;
    private String nombreFamilia;
    private int cantidadVentas;
    private double ventaTotal;

    public VentaFamiliaDTO(int codigoFamilia, String nombreFamilia, int cantidadVentas, double ventaTotal) {
        this.codigoFamilia = codigoFamilia;
        this.nombreFamilia = nombreFamilia;
        this.cantidadVentas = cantidadVentas;
        this.ventaTotal = ventaTotal;
    }

    public int getCodigoFamilia() {
        return codigoFamilia;
    }

    public String getNombreFamilia() {
        return nombreFamilia;
    }

    public int getCantidadVentas() {
        return cantidadVentas;
    }

    public double getVentaTotal() {
        return ventaTotal;
    }
}
