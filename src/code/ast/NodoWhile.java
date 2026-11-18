package ast;

// (13) Nodo WHILE
public class NodoWhile extends Nodo {
    private Nodo condicion, cuerpo;

    public NodoWhile(Nodo c, Nodo b) {
        this.condicion = c;
        this.cuerpo = b;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}
