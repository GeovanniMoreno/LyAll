package testing.units.parser;

import ast.Expresion.*;
import ast.Sentencia.*;
import lexer.Lexer;
import lexer.TokenType;
import parser.Parser;
import testing.Tester.DispatchTarget;
import testing.units.Result;
import testing.units.Test;

public class ParserUnit extends Test {

    public ParserUnit() {
        super();
        this.pertenencia = DispatchTarget.PARSER;
    }

    // Metodo auxiliar para compilar codigo fuente hasta obtener el AST raiz
    private Programa parsear(String codigo) {
        Lexer lexer = new Lexer(codigo);
        Parser parser = new Parser(lexer);
        return parser.parse();
    }

    private Result test_programaMinimoValido() {
        // La gramatica exige al menos una sentencia dentro de BEGIN ... END
        String codigo = """
            PROGRAM Minimo;
            VAR x : INTEGER;
            BEGIN
                x := 1;
            END.
            """;
        Programa ast = parsear(codigo);

        boolean ok = ast.nombre().equals("Minimo")
                  && ast.bloque().declaraciones().size() == 1
                  && ast.bloque().sentencias().size() == 1;

        return ok
            ? new Result("Programa minimo con declaracion y sentencia parseado", true)
            : new Result("Estructura incorrecta en programa minimo", false);
    }

    private Result test_declaracionesMultiplesYTipos() {
        String codigo = """
            PROGRAM Decls;
            VAR
                a, b, c : INTEGER;
                precio : REAL;
                activo : BOOLEAN;
            BEGIN
                a := 0;
            END.
            """;
        Programa ast = parsear(codigo);
        var decls = ast.bloque().declaraciones();

        boolean ok = decls.size() == 3
                  && decls.get(0).ids().size() == 3
                  && decls.get(0).tipo() == TokenType.INTEGER
                  && decls.get(1).tipo() == TokenType.REAL
                  && decls.get(2).tipo() == TokenType.BOOLEAN;

        return ok
            ? new Result("Declaraciones simples, multiples y tipos validados", true)
            : new Result("Error al construir nodos Declaracion", false);
    }

    private Result test_precedenciaYAsociatividadOperadores() {
        // 1 + 2 * 3 debe agruparse como 1 + (2 * 3)
        String codigo = """
            PROGRAM Precedencia;
            VAR x : INTEGER;
            BEGIN
                x := 1 + 2 * 3;
            END.
            """;
        Programa ast = parsear(codigo);
        var asig = (Asignacion) ast.bloque().sentencias().get(0);

        if (asig.valor() instanceof Binaria suma
                && suma.operador().equals("+")
                && suma.izq() instanceof Entero izq && izq.valor() == 1
                && suma.der() instanceof Binaria mult
                && mult.operador().equals("*")
                && mult.izq() instanceof Entero m1 && m1.valor() == 2
                && mult.der() instanceof Entero m2 && m2.valor() == 3) {
            return new Result("Precedencia (* sobre +) respetada en el AST", true);
        }
        return new Result("Arbol de expresion mal agrupado: " + asig.valor(), false);
    }

    private Result test_parentesisAlteranPrecedencia() {
        // (1 + 2) * 3 debe agruparse como (1 + 2) * 3
        String codigo = """
            PROGRAM Parentesis;
            VAR x : INTEGER;
            BEGIN
                x := (1 + 2) * 3;
            END.
            """;
        Programa ast = parsear(codigo);
        var asig = (Asignacion) ast.bloque().sentencias().get(0);

        if (asig.valor() instanceof Binaria mult
                && mult.operador().equals("*")
                && mult.izq() instanceof Binaria suma
                && suma.operador().equals("+")
                && mult.der() instanceof Entero der && der.valor() == 3) {
            return new Result("Parentesis agrupan subexpresiones correctamente", true);
        }
        return new Result("Parentesis ignorados en el AST: " + asig.valor(), false);
    }

    private Result test_estructurasControlIfElseYWhile() {
        String codigo = """
            PROGRAM Control;
            VAR i : INTEGER;
            BEGIN
                i := 0;
                IF i < 10 THEN
                    i := i + 1
                ELSE
                    i := 0;
                WHILE i < 5 DO
                BEGIN
                    i := i + 1;
                END;
            END.
            """;
        Programa ast = parsear(codigo);
        var stmts = ast.bloque().sentencias();

        boolean ifOk = stmts.get(1) instanceof If si
                    && si.condicion() instanceof Binaria
                    && si.entonces() instanceof Asignacion
                    && si.sino() instanceof Asignacion;

        boolean whileOk = stmts.get(2) instanceof While bucle
                       && bucle.cuerpo() instanceof Compuesta comp
                       && comp.sentencias().size() == 1;

        return (ifOk && whileOk)
            ? new Result("Sentencias IF-ELSE, WHILE y bloques BEGIN-END validados", true)
            : new Result("Error en nodos de control de flujo", false);
    }

    private Result test_sentenciasEntradaSalidaYAlias() {
        String codigo = """
            PROGRAM IO;
            VAR x : INTEGER;
            BEGIN
                READ(x);
                INPUT(x);
                WRITE("Valor: ", x);
                PRINT(x);
            END.
            """;
        Programa ast = parsear(codigo);
        var stmts = ast.bloque().sentencias();

        boolean ok = stmts.get(0) instanceof Read r1 && r1.ids().contains("x")
                  && stmts.get(1) instanceof Read r2 && r2.ids().contains("x")
                  && stmts.get(2) instanceof Write w1 && w1.items().size() == 2
                  && w1.items().get(0) instanceof Texto
                  && stmts.get(3) instanceof Write;

        return ok
            ? new Result("READ/INPUT y WRITE/PRINT generan nodos Read y Write", true)
            : new Result("Error al parsear sentencias de E/S", false);
    }

    private Result test_errorSintacticoFaltaPuntoFinal() {
        String codigo = """
            PROGRAM SinPunto;
            VAR x : INTEGER;
            BEGIN
                x := 1;
            END
            """;
        try {
            parsear(codigo);
            return new Result("Debio fallar al omitir el '.' final", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("DOT");
            return ok
                ? new Result("Error sintactico detectado: " + e.getMessage(), true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }

    private Result test_errorVariableNoDeclaradaEnParseo() {
        // Parser valida existencia en SymbolTable durante parseAssignment y parseFactor
        String codigo = """
            PROGRAM NoDeclarada;
            BEGIN
                x := 10;
            END.
            """;
        try {
            parsear(codigo);
            return new Result("Debio fallar al asignar a variable no declarada", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("no ha sido declarada");
            return ok
                ? new Result("Detectada variable no declarada: " + e.getMessage(), true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }

    private Result test_errorBloqueVacio() {
        String codigo = "PROGRAM Vacio; BEGIN END.";
        try {
            parsear(codigo);
            return new Result("Debio fallar al encontrar un bloque BEGIN-END vacio", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("sentencia no valida cerca de 'END'");
            return ok
                ? new Result("Bloque vacio rechazado correctamente: " + e.getMessage(), true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }
}