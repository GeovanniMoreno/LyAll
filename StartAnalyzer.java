package ConPascal;

/* -----------------------------------------------------------------------------
Para no modificar el Lexer y Parser que ya estaban conectados con el match
hice este nuevo programa donde vamos a agregar la gramática extendida, la tabla
de símbolos y lo que va a transformarse en TAC para de ahí pasar a ASM
----------------------------------------------------------------------------- */
public class StartAnalyzer {
    public static void main (String[] args){
        System.out.println("Leyendo codigo fuente: \n");
        System.out.println(Loader.read("src/ConPascal/CódigoFuente.pas"));
    }
    
}
