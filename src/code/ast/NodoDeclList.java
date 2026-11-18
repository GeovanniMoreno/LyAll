package ast;


// (4) Nodo Lista de Declaraciones
public class NodoDeclList extends Nodo {
    private Nodo decl;
    private Nodo dL;

    public NodoDeclList(Nodo decl, Nodo dL) {
        this.decl = decl;
        this.dL = dL;
    }

    @Override
    public Object evaluar() {
        if(decl != null) decl.evaluar();
        if(dL != null) dL.evaluar();
        return null;
    }
}