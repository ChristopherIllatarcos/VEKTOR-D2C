package modelo;

public class InventarioItemDTO {

    private int codigo;
    private String descripcion;
    private String familiaNombre;
    private double stockActual;
    private double costoUnitario;

    public InventarioItemDTO(int codigo, String descripcion, String familiaNombre, String stockActual, String costoUnitario) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.familiaNombre = familiaNombre;

        // Conversión segura manejando nulos y espacios
        try {
            this.stockActual = Double.parseDouble(stockActual != null ? stockActual.trim() : "0");
        } catch (NumberFormatException e) {
            this.stockActual = 0.0;
        }

        try {
            this.costoUnitario = Double.parseDouble(costoUnitario != null ? costoUnitario.trim() : "0");
        } catch (NumberFormatException e) {
            this.costoUnitario = 0.0;
        }
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFamiliaNombre() {
        return familiaNombre;
    }

    public double getStockActual() {
        return stockActual;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public double getValorTotal() {
        return stockActual * costoUnitario;
    }
}
