package ast;

// (3) Nodo Parte de Declaracion
public class NodoDeclPart extends Nodo {
    private Nodo listaVariables;

    public NodoDeclPart(Nodo listaVariables) {
        this.listaVariables = listaVariables;
    }
    
    @Override
    public Object evaluar() {
        if(listaVariables != null){
            return listaVariables.evaluar();
        }
        return null;
    }
}
