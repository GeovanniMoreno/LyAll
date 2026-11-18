package ast;

// (16) Nodo WRITE / PRINT
public class NodoWrite extends Nodo {
    private Nodo lista;

    public NodoWrite(Nodo lista) {
        this.lista = lista;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}