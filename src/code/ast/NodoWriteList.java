package ast;

// (17) Nodo Lista de Items a Imprimir
public class NodoWriteList extends Nodo {
    private Nodo item, resto;

    public NodoWriteList(Nodo item, Nodo resto) {
        this.item = item;
        this.resto = resto;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}
