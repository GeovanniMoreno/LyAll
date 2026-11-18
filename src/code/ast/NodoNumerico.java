package ast;

// Nodo Numerico
public class NodoNumerico extends Nodo {
    private float valor;

    public NodoNumerico(float valor) {
        this.valor = valor;
    }

    @Override
    public Object evaluar() {
        return this.valor;
    }
}