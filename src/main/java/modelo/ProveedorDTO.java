package modelo;

public class ProveedorDTO {

    private String codigo;
    private String nombre;
    private int rut;
    private String direccion;
    private String ciudad;
    private int fono;

    public ProveedorDTO(String codigo, String nombre, int rut, String direccion, String ciudad, int fono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.rut = rut;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.fono = fono;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getRut() {
        return rut;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public int getFono() {
        return fono;
    }
}
