package ast;

// (11) Nodo Asignacion
public class NodoAsignacion extends Nodo {
    private char op;
    private String id;
    private Nodo der;

    public NodoAsignacion(char op, String id, Nodo der) {
        this.op = op;
        this.id = id;
        this.der = der;
    }

    @Override
    public Object evaluar() {
        if(der != null){
            der.evaluar();
        }
        return null;
    }
}
