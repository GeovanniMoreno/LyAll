package ConPascal;
import java.util.HashMap;
import java.util.Map;


public class Symbol {
    private String name;
    private String tokenType;

    public Symbol(String name, String tokenType){
        this.name = name;
        this.tokenType = tokenType;
    }

    public String getName(){
        return name;
    }

    public String getTokenType(){
        return tokenType;
    }

    @Override 
    public String toString(){
        return "Symbol {" + "name= " + name + ", tokenType= " + tokenType + '}';
    }
}

class SymbolTable {
    private Map<String, Symbol> table;

    public SymbolTable(){
        table = new HashMap<>();
    }

    public void declarar(String name, String tokenType){
        if (table.containsKey(name.toUpperCase())) {
            return;
        }
        table.put(name.toUpperCase(), new Symbol(name, tokenType));
    }

    // Buscar variable. Retorna null si no existe (error de variable no declarada)
    public Symbol lookup(String name) {
        return table.get(name.toUpperCase());
    }

    public boolean contains(String name) {
        return table.containsKey(name.toUpperCase());
    }

    public void printTable() {
        System.out.println("\n=== TABLA DE SÍMBOLOS ===");
        for (Symbol s : table.values()) {
            System.out.println(s);
        }
    }
}
