import ast.Evaluador;
import ast.Sentencia.Programa;
import codegen.Cuadruplo;
import codegen.GeneradorASM;
import codegen.GeneradorCuadruplos;
import java.util.List;
import lexer.Lexer;
import lexer.Loader;
import parser.Parser;
import semantic.AnalizadorSemantico;

public class Main {
    public static void main(String[] args) {
        System.out.println("Leyendo codigo fuente...\n");
        String codigoFuente = Loader.read("src/tests/Cuadruplos.pas");
        System.out.println(codigoFuente);

        Lexer lexer = new Lexer(codigoFuente);
        Parser parser = new Parser(lexer);
        GeneradorASM generadorASM = new GeneradorASM();

        try {
            // 1. Analisis Sintactico y construccion del AST
            Programa arbolAST = parser.parse();
            System.out.println("Analisis sintactico completado CON EXITO.");

            // 2. Analisis Semantico (Validacion de tipos y declaraciones)
            AnalizadorSemantico semantico = new AnalizadorSemantico(parser.getSymbolTable());
            semantico.analizar(arbolAST);
            System.out.println("Analisis semantico completado CON EXITO.");

            // 3. Generacion de codigo intermedio
            System.out.println("\n--- GENERANDO CUADRUPLOS ---");
            GeneradorCuadruplos generador = new GeneradorCuadruplos();
            List<Cuadruplo> cuadruplos = generador.generar(arbolAST);
                for (Cuadruplo c : cuadruplos) {
                    System.out.println(c);
                }
                
            // 4. Ejecucion (Interprete)
            System.out.println("\n--- EJECUTANDO PROGRAMA ---");
            Evaluador evaluador = new Evaluador(parser.getSymbolTable());
            evaluador.ejecutar(arbolAST);

            System.out.println("\n--- GENERANDO ASM ---");
            generadorASM.generar(cuadruplos);

            parser.getSymbolTable().printTable();
        } catch (Exception e) {
            System.err.println("\n" + e.getMessage());
        }
    }
}