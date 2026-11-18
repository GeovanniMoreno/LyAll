package ast;

// Operaciones Matematicas y Logicas (Reglas 19 a 25)
public class NodoOperador extends Nodo {
    private String operador;
    private Nodo izq, der;

    public NodoOperador(String operador, Nodo izq, Nodo der) {
        this.operador = operador;
        this.izq = izq;
        this.der = der;
    }

    @Override
    public Object evaluar() {
        return switch (this.operador) {
            case "+"   -> (float)   this.izq.evaluar()  +   (float)   this.der.evaluar();
            case "-"   -> (float)   this.izq.evaluar()  -   (float)   this.der.evaluar();
            case "*"   -> (float)   this.izq.evaluar()  *   (float)   this.der.evaluar();
            case "/"   -> (float)   this.izq.evaluar()  /   (float)   this.der.evaluar();
            case "OR"  -> (boolean) this.izq.evaluar()  ||  (boolean) this.der.evaluar();
            case "AND" -> (boolean) this.izq.evaluar()  &&  (boolean) this.der.evaluar();
            case "="   ->           this.izq.evaluar()  ==            this.der.evaluar();
            case "<>"  ->           this.izq.evaluar()  !=            this.der.evaluar();
            case "<"   -> (float)   this.izq.evaluar()  <   (float)   this.der.evaluar();
            case "<="  -> (float)   this.izq.evaluar()  <=  (float)   this.der.evaluar();
            case ">"   -> (float)   this.izq.evaluar()  >   (float)   this.der.evaluar();
            case ">="  -> (float)   this.izq.evaluar()  >=  (float)   this.der.evaluar();
            default    -> throw new RuntimeException("Operador ilegal...");    
        };
    }
}