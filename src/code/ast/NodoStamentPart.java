package ast;

// (8) Nodo Parte de Sentencias
public class NodoStamentPart extends Nodo {
    private Nodo sl;

    public NodoStamentPart(Nodo sl) {
        this.sl = sl;
    }

    @Override
    public Object evaluar() {
        if(sl != null){
            sl.evaluar();
        }
        return null;
    }
}