package ast;

// (2) Nodo Bloque
public class NodoBlock extends Nodo {
    private Nodo declaracion;
    private Nodo stament;

    public NodoBlock(Nodo declaracion, Nodo stament) {
        this.declaracion = declaracion;
        this.stament = stament;
    }

    @Override
    public Object evaluar() {
        if(declaracion != null) declaracion.evaluar();
        if(stament != null) return stament.evaluar();
        return null;
    }
}