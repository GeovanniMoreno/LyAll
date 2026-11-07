package ConPascal;

/* -----------------------------------------------------------------------------
Para no modificar el Lexer y Parser que ya estaban conectados con el match
hice este nuevo programa donde vamos a agregar la gramática extendida, la tabla
de símbolos y lo que va a transformarse en TAC para de ahí pasar a ASM
----------------------------------------------------------------------------- 
public class StartAnalyzer {
    public static void main (String[] args){
        System.out.println("Leyendo codigo fuente: \n");
        System.out.println(Loader.read("src/ConPascal/CódigoFuente.pas"));
    }
}
*/

public class StartAnalyzer {
    public static void main(String[] args) {
        System.out.println("Leyendo codigo fuente...\n");
        String codigoFuente = Loader.read("src/ConPascalGIO/CódigoFuente.pas");
        System.out.println(codigoFuente);

        System.out.println("Iniciando analisis sintactico y semantico...");
        Lexer lexer = new Lexer(codigoFuente);
        Parser parser = new Parser(lexer);

        try {
            Nodo arbolAST = parser.evaluar();
            System.out.println("\nAnalisis sintactico y semantico completado CON EXITO");
            
            //Muestra el contenido de la Tabla de Símbolos
            parser.getSymbolTable().printTable();
        } catch (Exception e) {
            System.err.println("\n" + e.getMessage());
        }
    }
}