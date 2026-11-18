package ast;

// (18) Nodo Texto Literal
public class NodoTexto extends Nodo {
    private String texto;

    public NodoTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public Object evaluar() {
        return this.texto;
    }
}
