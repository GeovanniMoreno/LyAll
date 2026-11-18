package ast;

// (15) Nodo READ / INPUT
public class NodoRead extends Nodo {
    private Nodo ids;

    public NodoRead(Nodo ids) {
        this.ids = ids;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}