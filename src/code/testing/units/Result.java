package testing.units;

import testing.Tester.DispatchTarget;

public class Result {
    public String fuente;
    public DispatchTarget modulo;
    private final String descripcion;
    private final boolean estatus;

    public Result(String desc, boolean status) {
        this.fuente = "Indefinida";
        this.modulo = DispatchTarget.ALL;
        this.descripcion = desc;
        this.estatus = status;
    }

    public String fuente() { return fuente; }
    public DispatchTarget modulo() { return modulo; }
    public String descripcion() { return descripcion; }
    public boolean estatus() { return estatus; }
}