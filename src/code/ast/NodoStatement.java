package ast;

// (10) Nodo Sentencia Generica
public class NodoStatement extends Nodo {
    private Nodo stmt;

    public NodoStatement(Nodo stmt) {
        this.stmt = stmt;
    }

    @Override
    public Object evaluar() {
        if(stmt != null){
            stmt.evaluar();
        }
        return null;
    }
}
