package ast;

import java.util.List;
import java.util.ArrayList;

// (6) Nodo Lista de Identificadores (Retorna List<String>)
public class NodoIdenList extends Nodo {
    private Nodo iden;
    private Nodo restoLista;

    public NodoIdenList(Nodo iden, Nodo restoLista) {
        this.iden = iden;
        this.restoLista = restoLista;
    }

    @Override
    public Object evaluar() {
        List<String> lista = new ArrayList<>();
        lista.add((String) iden.evaluar());
        if(restoLista != null){
            Object res = restoLista.evaluar();
            if(res instanceof List){
                lista.addAll((List<String>) res);
            }else if(res instanceof String){
                lista.add((String) res);
            }
        }
        return lista;
    }
}