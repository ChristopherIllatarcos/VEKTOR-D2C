package modelo;

public class FamiliaDTO {

    private String codigo;
    private String descripcion;
    private double ivaAdic;

    public FamiliaDTO(String codigo, String descripcion, double ivaAdic) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.ivaAdic = ivaAdic;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getIvaAdic() {
        return ivaAdic;
    }
}
