import ast.*;
import lexer.*;
import parser.*;


public class Main {
    public static void main(String[] args) {
        System.out.println("Leyendo codigo fuente...\n");
        String codigoFuente = Loader.read("src/tests/Fuente.pas");
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