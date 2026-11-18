package ast;

// (9) Nodo Lista de Sentencias
public class NodoStmtLis extends Nodo {
    private Nodo stmt;
    private Nodo sl;

    public NodoStmtLis(Nodo stmt, Nodo sl) {
        this.stmt = stmt;
        this.sl = sl;
    }

    @Override
    public Object evaluar() {
        if(stmt != null) stmt.evaluar();
        if(sl != null) sl.evaluar();
        return null;
    }
}
