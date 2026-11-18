package ast;

// (12) Nodo IF
public class NodoIf extends Nodo {
    private Nodo condicion, entoncesStmt, sinoStmt;

    public NodoIf(Nodo c, Nodo e, Nodo s) {
        this.condicion = c;
        this.entoncesStmt = e;
        this.sinoStmt = s;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}
