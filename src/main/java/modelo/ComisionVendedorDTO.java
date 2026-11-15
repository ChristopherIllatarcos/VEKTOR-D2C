package modelo;

public class ComisionVendedorDTO {

    private int codigoVendedor; // <- NUEVO
    private int numeroVentas;
    private String nombreVendedor;
    private double ventaTotal;
    private double comision;
    private double participacion;

    public ComisionVendedorDTO(int codigoVendedor, int numeroVentas, String nombreVendedor, double ventaTotal, double comision, double participacion) {
        this.codigoVendedor = codigoVendedor;
        this.numeroVentas = numeroVentas;
        this.nombreVendedor = nombreVendedor;
        this.ventaTotal = ventaTotal;
        this.comision = comision;
        this.participacion = participacion;
    }

    public int getCodigoVendedor() {
        return codigoVendedor;
    }

    public int getNumeroVentas() {
        return numeroVentas;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public double getVentaTotal() {
        return ventaTotal;
    }

    public double getComision() {
        return comision;
    }

    public double getParticipacion() {
        return participacion;
    }
}
