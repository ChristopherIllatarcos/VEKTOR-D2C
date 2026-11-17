package modelo;

public class ParametrosDTO {

    private long boleta;
    private long factura;
    private long guia;
    private double iva;
    private String fecha;

    public ParametrosDTO(long boleta, long factura, long guia, double iva, String fecha) {
        this.boleta = boleta;
        this.factura = factura;
        this.guia = guia;
        this.iva = iva;
        this.fecha = fecha;
    }

    public long getBoleta() {
        return boleta;
    }

    public void setBoleta(long boleta) {
        this.boleta = boleta;
    }

    public long getFactura() {
        return factura;
    }

    public void setFactura(long factura) {
        this.factura = factura;
    }

    public long getGuia() {
        return guia;
    }

    public void setGuia(long guia) {
        this.guia = guia;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
