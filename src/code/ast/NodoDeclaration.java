package ast;

// (5) Nodo Declaracion
public class NodoDeclaration extends Nodo {
    private Nodo identLis;
    private Nodo type;

    public NodoDeclaration(Nodo identLis, Nodo type) {
        this.identLis = identLis;
        this.type = type;
    }

    @Override
    public Object evaluar() {
        return identLis.evaluar();
    }
}
