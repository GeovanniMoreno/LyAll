package testing.units.ast;

import ast.Evaluador;
import ast.Expresion.*;
import ast.Sentencia.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lexer.TokenType;
import semantic.Symbol;
import semantic.SymbolTable;
import testing.Tester.DispatchTarget;
import testing.units.Result;
import testing.units.Test;

public class AstUnit extends Test {

    public AstUnit() {
        super();
        this.pertenencia = DispatchTarget.AST;
    }

    private Result test_igualdadEstructuralDeNodos() {
        // Los records de Expresion y Sentencia deben soportar igualdad por valor
        var expr1 = new Binaria("+", new Identificador("x"), new Entero(10));
        var expr2 = new Binaria("+", new Identificador("x"), new Entero(10));
        var expr3 = new Binaria("-", new Identificador("x"), new Entero(10));

        var stmt1 = new Asignacion("x", expr1);
        var stmt2 = new Asignacion("x", expr2);

        boolean ok = expr1.equals(expr2)
                  && !expr1.equals(expr3)
                  && stmt1.equals(stmt2);

        return ok
            ? new Result("Igualdad estructural de nodos Expresion y Sentencia verificada", true)
            : new Result("Fallo en equals/hashCode de los records del AST", false);
    }

    private Result test_evaluacionAritmeticaYDivisionReal() {
        SymbolTable tabla = new SymbolTable();
        Evaluador eval = new Evaluador(tabla);

        // 5 + 3 * 2 = 11 (Long)
        Object resEntero = eval.evaluar(
            new Binaria("+", new Entero(5), new Binaria("*", new Entero(3), new Entero(2)))
        );

        // En Pascal, 10 / 4 siempre produce un REAL (Double: 2.5)
        Object resDivision = eval.evaluar(
            new Binaria("/", new Entero(10), new Entero(4))
        );

        // Suma mixta: INTEGER + REAL -> Double (7.5)
        Object resMixto = eval.evaluar(
            new Binaria("+", new Entero(5), new Real(2.5))
        );

        boolean ok = (resEntero instanceof Long l && l == 11L)
                  && (resDivision instanceof Double d && d == 2.5)
                  && (resMixto instanceof Double m && m == 7.5);

        return ok
            ? new Result("Evaluacion aritmetica, division real (/) y promocion mixta correctas", true)
            : new Result("Error en tipos o valores calculados: " + resEntero + ", " + resDivision + ", " + resMixto, false);
    }

    private Result test_promocionImplicitaEnAsignacion() {
        SymbolTable tabla = new SymbolTable();
        Evaluador eval = new Evaluador(tabla);

        var prog = new Programa("Promo", new Bloque(
            List.of(new Declaracion(List.of("precio"), TokenType.REAL)),
            List.of(new Asignacion("precio", new Entero(42)))
        ));

        eval.ejecutar(prog);
        Symbol sym = tabla.lookup("precio");

        boolean ok = sym != null
                  && sym.getValue() instanceof Double d
                  && d == 42.0;

        return ok
            ? new Result("Promocion de Long a Double al asignar a variable REAL", true)
            : new Result("No se promovio el valor asignado: " + (sym != null ? sym.getValue() : "null"), false);
    }

    private Result test_operadoresRelacionalesYLogicos() {
        SymbolTable tabla = new SymbolTable();
        Evaluador eval = new Evaluador(tabla);

        // (10 >= 10) AND (5 <> 3) OR FALSE -> true
        var cond = new Binaria("OR",
            new Binaria("AND",
                new Binaria(">=", new Entero(10), new Entero(10)),
                new Binaria("<>", new Entero(5), new Entero(3))
            ),
            new Booleano(false)
        );

        // Comparacion de igualdad entre Long(5) y Double(5.0) -> true
        var igualdadMixta = new Binaria("=", new Entero(5), new Real(5.0));

        boolean ok = Boolean.TRUE.equals(eval.evaluar(cond))
                  && Boolean.TRUE.equals(eval.evaluar(igualdadMixta));

        return ok
            ? new Result("Operadores relacionales, logicos e igualdad mixta evaluados", true)
            : new Result("Fallo en evaluacion booleana", false);
    }

    private Result test_ejecucionIfElseYWhile() {
        // Calcula suma de 1 a 4 en 'acum' (10) y clasifica en 'mayor' (true)
        SymbolTable tabla = new SymbolTable();
        Evaluador eval = new Evaluador(tabla);

        var prog = new Programa("Flujo", new Bloque(
            List.of(
                new Declaracion(List.of("i", "acum"), TokenType.INTEGER),
                new Declaracion(List.of("mayor"), TokenType.BOOLEAN)
            ),
            List.of(
                new Asignacion("i", new Entero(1)),
                new Asignacion("acum", new Entero(0)),
                new While(
                    new Binaria("<=", new Identificador("i"), new Entero(4)),
                    new Compuesta(List.of(
                        new Asignacion("acum", new Binaria("+", new Identificador("acum"), new Identificador("i"))),
                        new Asignacion("i", new Binaria("+", new Identificador("i"), new Entero(1)))
                    ))
                ),
                new If(
                    new Binaria(">", new Identificador("acum"), new Entero(5)),
                    new Asignacion("mayor", new Booleano(true)),
                    new Asignacion("mayor", new Booleano(false))
                )
            )
        ));

        eval.ejecutar(prog);

        boolean ok = Long.valueOf(10L).equals(tabla.lookup("acum").getValue())
                  && Boolean.TRUE.equals(tabla.lookup("mayor").getValue());

        return ok
            ? new Result("Ejecucion de While, Compuesta e If-Else verificada", true)
            : new Result("Estado final inesperado tras ejecutar estructuras de control", false);
    }

    private Result test_entradaSalidaReadYWrite() {
        InputStream stdinOriginal = System.in;
        PrintStream stdoutOriginal = System.out;

        try {
            // Simulamos entrada por consola: "25\n"
            System.setIn(new ByteArrayInputStream("25\n".getBytes(StandardCharsets.UTF_8)));
            ByteArrayOutputStream salidaCapturada = new ByteArrayOutputStream();
            System.setOut(new PrintStream(salidaCapturada));

            SymbolTable tabla = new SymbolTable();
            Evaluador eval = new Evaluador(tabla);

            var prog = new Programa("IO", new Bloque(
                List.of(new Declaracion(List.of("edad"), TokenType.INTEGER)),
                List.of(
                    new Read(List.of("edad")),
                    new Write(List.of(new Texto("\"Edad: \""), new Identificador("edad")))
                )
            ));

            eval.ejecutar(prog);
            String impreso = salidaCapturada.toString(StandardCharsets.UTF_8).trim();

            return impreso.equals("Edad: 25")
                ? new Result("Read y Write (con recorte de comillas) funcionando", true)
                : new Result("Salida inesperada en Write: '" + impreso + "'", false);
        } finally {
            System.setIn(stdinOriginal);
            System.setOut(stdoutOriginal);
        }
    }

    private Result test_errorVariableSinInicializar() {
        SymbolTable tabla = new SymbolTable();
        Evaluador eval = new Evaluador(tabla);

        var prog = new Programa("SinInit", new Bloque(
            List.of(new Declaracion(List.of("x"), TokenType.INTEGER)),
            List.of(new Asignacion("x", new Binaria("+", new Identificador("x"), new Entero(1))))
        ));

        try {
            eval.ejecutar(prog);
            return new Result("Debio fallar al leer variable no inicializada", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("antes de ser inicializada");
            return ok
                ? new Result("Excepcion detectada: " + e.getMessage(), true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }

    private Result test_errorDivisionPorCero() {
        SymbolTable tabla = new SymbolTable();
        Evaluador eval = new Evaluador(tabla);

        try {
            eval.evaluar(new Binaria("/", new Entero(10), new Entero(0)));
            return new Result("Debio fallar al dividir entre cero", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("Division por cero");
            return ok
                ? new Result("Excepcion detectada: " + e.getMessage(), true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }
}