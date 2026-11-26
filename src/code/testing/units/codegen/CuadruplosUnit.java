package testing.units.codegen;

import ast.Expresion.*;
import ast.Sentencia.*;
import codegen.Cuadruplo;
import codegen.GeneradorCuadruplos;
import java.util.List;
import lexer.TokenType;
import testing.Tester.DispatchTarget;
import testing.units.Result;
import testing.units.Test;

public class CuadruplosUnit extends Test {

    public CuadruplosUnit() {
        super();
        this.pertenencia = DispatchTarget.IR;
    }

    private Result test_declaracionNoGeneraCuadruplosYAsignacionSimple() {
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var prog = new Programa("Simple", new Bloque(
            List.of(new Declaracion(List.of("x"), TokenType.INTEGER)),
            List.of(new Asignacion("x", new Entero(10)))
        ));

        List<Cuadruplo> quads = gen.generar(prog);
        List<Cuadruplo> esperados = List.of(
            new Cuadruplo("=", "10", "-", "x")
        );

        return esperados.equals(quads)
            ? new Result("Declaracion omitida y asignacion simple traducida", true)
            : new Result("Cuadruplos inesperados: " + quads, false);
    }

    private Result test_expresionComplejaYTemporalesPostorden() {
        // x := (a + b) * (c - d) -> t1 = a + b, t2 = c - d, t3 = t1 * t2, x = t3
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var expr = new Binaria("*",
            new Binaria("+", new Identificador("a"), new Identificador("b")),
            new Binaria("-", new Identificador("c"), new Identificador("d"))
        );
        var prog = new Programa("Expr", new Bloque(
            List.of(),
            List.of(new Asignacion("x", expr))
        ));

        List<Cuadruplo> quads = gen.generar(prog);
        List<Cuadruplo> esperados = List.of(
            new Cuadruplo("+", "a", "b", "t1"),
            new Cuadruplo("-", "c", "d", "t2"),
            new Cuadruplo("*", "t1", "t2", "t3"),
            new Cuadruplo("=", "t3", "-", "x")
        );

        return esperados.equals(quads)
            ? new Result("Temporales t1..t3 generados en orden postorden", true)
            : new Result("Orden o nombres de temporales incorrectos: " + quads, false);
    }

    private Result test_estructuraIfSinElse() {
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var prog = new Programa("IfSolo", new Bloque(
            List.of(),
            List.of(new If(
                new Binaria("<", new Identificador("x"), new Entero(5)),
                new Asignacion("x", new Entero(0)),
                null
            ))
        ));

        List<Cuadruplo> quads = gen.generar(prog);
        List<Cuadruplo> esperados = List.of(
            new Cuadruplo("<", "x", "5", "t1"),
            new Cuadruplo("IF_FALSE", "t1", "-", "L1"),
            new Cuadruplo("=", "0", "-", "x"),
            new Cuadruplo("LABEL", "-", "-", "L1")
        );

        return esperados.equals(quads)
            ? new Result("Patron IF_FALSE y LABEL para IF sin ELSE validado", true)
            : new Result("Cuadruplos de IF incorrectos: " + quads, false);
    }

    private Result test_estructuraIfConElse() {
        // Nota: etiquetaFin se reserva antes (L1) que etiquetaSino (L2)
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var prog = new Programa("IfElse", new Bloque(
            List.of(),
            List.of(new If(
                new Identificador("cond"),
                new Asignacion("x", new Entero(1)),
                new Asignacion("x", new Entero(2))
            ))
        ));

        List<Cuadruplo> quads = gen.generar(prog);
        List<Cuadruplo> esperados = List.of(
            new Cuadruplo("IF_FALSE", "cond", "-", "L2"),
            new Cuadruplo("=", "1", "-", "x"),
            new Cuadruplo("GOTO", "-", "-", "L1"),
            new Cuadruplo("LABEL", "-", "-", "L2"),
            new Cuadruplo("=", "2", "-", "x"),
            new Cuadruplo("LABEL", "-", "-", "L1")
        );

        return esperados.equals(quads)
            ? new Result("Saltos GOTO y etiquetas L1/L2 en IF-ELSE correctos", true)
            : new Result("Cuadruplos de IF-ELSE incorrectos: " + quads, false);
    }

    private Result test_estructuraBucleWhile() {
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var prog = new Programa("Bucle", new Bloque(
            List.of(),
            List.of(new While(
                new Binaria(">", new Identificador("n"), new Entero(0)),
                new Asignacion("n", new Binaria("-", new Identificador("n"), new Entero(1)))
            ))
        ));

        List<Cuadruplo> quads = gen.generar(prog);
        List<Cuadruplo> esperados = List.of(
            new Cuadruplo("LABEL", "-", "-", "L1"),
            new Cuadruplo(">", "n", "0", "t1"),
            new Cuadruplo("IF_FALSE", "t1", "-", "L2"),
            new Cuadruplo("-", "n", "1", "t2"),
            new Cuadruplo("=", "t2", "-", "n"),
            new Cuadruplo("GOTO", "-", "-", "L1"),
            new Cuadruplo("LABEL", "-", "-", "L2")
        );

        return esperados.equals(quads)
            ? new Result("Ciclo WHILE con etiqueta de retorno L1 y salida L2 validado", true)
            : new Result("Cuadruplos de WHILE incorrectos: " + quads, false);
    }

    private Result test_entradaSalidaReadYWrite() {
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var prog = new Programa("IO", new Bloque(
            List.of(),
            List.of(
                new Read(List.of("a", "b")),
                new Write(List.of(new Texto("\"Suma:\""), new Binaria("+", new Identificador("a"), new Identificador("b"))))
            )
        ));

        List<Cuadruplo> quads = gen.generar(prog);
        List<Cuadruplo> esperados = List.of(
            new Cuadruplo("READ", "a", "-", "-"),
            new Cuadruplo("READ", "b", "-", "-"),
            new Cuadruplo("WRITE", "\"Suma:\"", "-", "-"),
            new Cuadruplo("+", "a", "b", "t1"),
            new Cuadruplo("WRITE", "t1", "-", "-")
        );

        return esperados.equals(quads)
            ? new Result("Cuadruplos READ y WRITE expandidos por argumento", true)
            : new Result("Cuadruplos de E/S incorrectos: " + quads, false);
    }

    private Result test_reinicioDeContadoresEntreEjecuciones() {
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        var prog = new Programa("Reinicio", new Bloque(
            List.of(),
            List.of(new Asignacion("x", new Binaria("+", new Entero(1), new Entero(2))))
        ));

        gen.generar(prog); // Primera corrida usa t1
        List<Cuadruplo> segundaCorrida = gen.generar(prog); // Debe reiniciar a t1 y no acumular

        boolean ok = segundaCorrida.size() == 2
                  && segundaCorrida.get(0).resultado().equals("t1");

        return ok
            ? new Result("Contadores de temporales y lista se reinician en cada generar()", true)
            : new Result("Estado contaminado entre llamadas: " + segundaCorrida, false);
    }
}