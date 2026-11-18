package ast;

// (1) Nodo Raíz del Programa
public class NodoPrograma extends Nodo {
    private String nombre;
    private Nodo bloque;

    public NodoPrograma(String nombre, Nodo bloque) {
        this.nombre = nombre;
        this.bloque = bloque;
    }

    @Override
    public Object evaluar() {
        if(bloque != null) {
            return bloque.evaluar();
        }
        return null;
    }
}