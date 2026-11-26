package testing;

import java.util.ArrayList;
import java.util.List;

import testing.units.Result;
import testing.units.lexer.LexerUnit;
import testing.units.loader.LoaderUnit;
import testing.units.parser.ParserUnit;
import testing.units.ast.AstUnit;
import testing.units.semantic.SymbolTableUnit;
import testing.units.codegen.CuadruplosUnit;
import testing.units.codegen.AsmUnit;

public class Tester {
    private static final String RESET = "\u001B[0m";
    private static final String RED   = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";

    public static void main(String[] args) {
        DispatchTarget objetivo = DispatchTarget.ALL;
        if (args.length > 0) {
            for (String arg : args) {
                if (!arg.equals("-b")) {
                    objetivo = DispatchTarget.valueOf(arg.toUpperCase());
                }
            }
        }

        var resultados = despacharTests(objetivo);
        printResults(resultados);
    }

    public enum DispatchTarget {
        LOADER,
        LEXER,
        PARSER,
        AST,
        SYMBOL_TABLE,
        CODE_GENERATOR,
        IR,
        ASM,
        ALL
    }

    public static List<Result> despacharTests(DispatchTarget objetivo) {
        try {
            return switch (objetivo) {
                case LOADER -> new LoaderUnit()           .ejecutar();
                case LEXER -> new LexerUnit()             .ejecutar();
                case PARSER -> new ParserUnit()           .ejecutar();
                case AST -> new AstUnit()                 .ejecutar();
                case SYMBOL_TABLE -> new SymbolTableUnit().ejecutar();
                case IR -> new CuadruplosUnit()           .ejecutar();
                case ASM -> new AsmUnit()                 .ejecutar();
                case CODE_GENERATOR -> {
                    List<Result> codegen = new ArrayList<>();
                    codegen.addAll(new CuadruplosUnit().ejecutar());
                    codegen.addAll(new AsmUnit()       .ejecutar());
                    yield codegen;
                }
                case ALL -> {
                    List<Result> todos = new ArrayList<>();
                    todos.addAll(new LoaderUnit()     .ejecutar());
                    todos.addAll(new LexerUnit()      .ejecutar());
                    todos.addAll(new ParserUnit()     .ejecutar());
                    todos.addAll(new AstUnit()        .ejecutar());
                    todos.addAll(new SymbolTableUnit().ejecutar());
                    todos.addAll(new CuadruplosUnit() .ejecutar());
                    todos.addAll(new AsmUnit()        .ejecutar());
                    yield todos;
                }
            };
        } catch (Exception e) {
            System.err.println("Error de invocacion de tests:");
            e.printStackTrace();
            return List.of();
        }
    }

    public static void printResults(List<Result> resultados) {
        int anchoColModulo = 15;
        int anchoColTest = 42;
        int anchoColEstado = 10;
        int anchoTotal = anchoColModulo + anchoColTest + anchoColEstado + 6; // +6 por los dos separadores " | "

        System.out.println("\n=== RESULTADOS DEL TESTING ===");
        System.out.printf("%-" + anchoColModulo + "s | %-" + anchoColTest + "s | %-" + anchoColEstado + "s%n",
                "MODULO", "TEST", "ESTADO");
        System.out.println("=".repeat(anchoTotal));

        for (Result result : resultados) {
            String color = result.estatus() ? GREEN : RED;
            String textoEstado = result.estatus() ? "PASS" : "FAIL";

            System.out.printf("%-" + anchoColModulo + "s | %-" + anchoColTest + "s | %s%-" + anchoColEstado + "s%s%n",
                    result.modulo(), result.fuente(), color, textoEstado, RESET);

            if (!result.estatus()) {
                System.out.println(RED + "  -> Descripcion: " + result.descripcion() + RESET);
            }
        }
        System.out.println("=".repeat(anchoTotal));
    }
}