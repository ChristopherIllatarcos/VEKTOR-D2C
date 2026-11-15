package modelo;

public class DetalleVendedorDTO {

    private String tipoDocumento;
    private int folio;
    private String fecha;
    private String cliente;
    private double total;

    public DetalleVendedorDTO(String tipoDocumento, int folio, String fecha, String cliente, double total) {
        this.tipoDocumento = tipoDocumento;
        this.folio = folio;
        this.fecha = fecha;
        this.cliente = cliente;
        this.total = total;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public int getFolio() {
        return folio;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public double getTotal() {
        return total;
    }
}
