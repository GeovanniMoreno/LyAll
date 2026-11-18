package ast;

// Nodo Identificador
public class NodoIdentifier extends Nodo {
    private String lexema;

    public NodoIdentifier(String lexema) {
        this.lexema = lexema;
    }

    @Override
    public Object evaluar() {
        return this.lexema;
    }
}
