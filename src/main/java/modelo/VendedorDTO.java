package modelo;

public class VendedorDTO {

    private int codigo;
    private String nombre;
    private double comision;

    public VendedorDTO(int codigo, String nombre, double comision) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.comision = comision;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getComision() {
        return comision;
    }
}
