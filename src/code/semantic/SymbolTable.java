package semantic;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
    private Map<String, Symbol> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void declarar(String name, String tokenType) {
        if(table.containsKey(name.toUpperCase())){
            return;
        }
        table.put(name.toUpperCase(), new Symbol(name, tokenType));
    }
    
    //Buscar variable. Retorna null si no existe (error de variable no declarada)
    public Symbol lookup(String name) {
        return table.get(name.toUpperCase());
    }

    public boolean contains(String name) {
        return table.containsKey(name.toUpperCase());
    }

    public void printTable() {
        System.out.println("\n ------ TABLA DE SIMBOLOS -------");
        for (Symbol s : table.values()) {
            System.out.println(s);
        }
    }
}
