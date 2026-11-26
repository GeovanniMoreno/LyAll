package testing.units.semantic;

import ast.Expresion.*;
import ast.Sentencia.*;
import java.util.List;
import lexer.TokenType;
import semantic.AnalizadorSemantico;
import semantic.Symbol;
import semantic.SymbolTable;
import testing.Tester.DispatchTarget;
import testing.units.Result;
import testing.units.Test;

public class SymbolTableUnit extends Test {

    public SymbolTableUnit() {
        super();
        this.pertenencia = DispatchTarget.SYMBOL_TABLE;
    }

    private Result test_declaracionYBusquedaInsensibleAMayusculas() {
        SymbolTable tabla = new SymbolTable();
        tabla.declarar("totalVentas", "REAL");

        boolean ok = tabla.contains("totalVentas")
                  && tabla.contains("TOTALVENTAS")
                  && tabla.contains("totalventas")
                  && !tabla.contains("inexistente")
                  && tabla.lookup("TOTALVENTAS") != null
                  && tabla.lookup("TOTALVENTAS").getTokenType().equals("REAL");

        return ok
            ? new Result("Insercion y lookup case-insensitive funcionando", true)
            : new Result("Fallo en busqueda insensible a mayusculas en SymbolTable", false);
    }

    private Result test_noSobrescribeSimboloExistenteEnTabla() {
        SymbolTable tabla = new SymbolTable();
        tabla.declarar("x", "INTEGER");
        tabla.lookup("x").setValue(99L);

        // SymbolTable.declarar ignora si ya existe la clave en mayusculas
        tabla.declarar("X", "REAL");
        Symbol sym = tabla.lookup("x");

        boolean ok = sym.getTokenType().equals("INTEGER")
                  && Long.valueOf(99L).equals(sym.getValue());

        return ok
            ? new Result("SymbolTable conserva el tipo y valor original ante duplicado", true)
            : new Result("SymbolTable sobrescribio el simbolo existente", false);
    }

    private Result test_metadatosDeLecturaYEscrituraEnSymbol() {
        Symbol sym = new Symbol("pi", "REAL");

        // Primera escritura con literal constante -> esConstanteCompilacion = true
        sym.registrarEscritura(true, 3.14);
        boolean eraConstante = sym.isConstanteCompilacion()
                            && Double.valueOf(3.14).equals(sym.getValorConstante())
                            && sym.getEscrituras() == 1;

        // Segunda escritura -> pierde estado constante
        sym.registrarEscritura(true, 6.28);
        sym.registrarLectura();

        boolean dejoDeSerConstante = !sym.isConstanteCompilacion()
                                  && sym.getValorConstante() == null
                                  && sym.getEscrituras() == 2
                                  && sym.isUsada();

        return (eraConstante && dejoDeSerConstante)
            ? new Result("Tracking de lecturas, escrituras y constantes en Symbol correcto", true)
            : new Result("Error en metadatos de Symbol: " + sym, false);
    }

    private Result test_semanticoDetectaVariableRedeclarada() {
        SymbolTable tabla = new SymbolTable();
        tabla.declarar("x", "INTEGER");
        AnalizadorSemantico semantico = new AnalizadorSemantico(tabla);

        var prog = new Programa("Redecl", new Bloque(
            List.of(
                new Declaracion(List.of("x"), TokenType.INTEGER),
                new Declaracion(List.of("X"), TokenType.REAL)
            ),
            List.of()
        ));

        try {
            semantico.analizar(prog);
            return new Result("Debio detectar redeclaracion de variable 'X'", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("ya fue declarada");
            return ok
                ? new Result("Redeclaracion detectada: " + e.getMessage(), true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }

    private Result test_semanticoCompatibilidadDeTiposEnAsignacion() {
        SymbolTable tabla = new SymbolTable();
        tabla.declarar("entero", "INTEGER");
        tabla.declarar("real", "REAL");
        AnalizadorSemantico semantico = new AnalizadorSemantico(tabla);

        // Válido: REAL := INTEGER (promoción implícita)
        var asignacionValida = new Asignacion("real", new Identificador("entero"));
        semantico.analizar(asignacionValida);

        // Inválido: INTEGER := REAL
        var asignacionInvalida = new Asignacion("entero", new Identificador("real"));
        try {
            semantico.analizar(asignacionInvalida);
            return new Result("Debio rechazar asignacion de REAL a INTEGER", false);
        } catch (RuntimeException e) {
            boolean ok = e.getMessage().contains("No se puede asignar");
            return ok
                ? new Result("Promocion valida aceptada y perdida de precision rechazada", true)
                : new Result("Mensaje inesperado: " + e.getMessage(), false);
        }
    }

    private Result test_semanticoInferenciaYCondicionesBooleanas() {
        SymbolTable tabla = new SymbolTable();
        tabla.declarar("a", "INTEGER");
        tabla.declarar("b", "INTEGER");
        AnalizadorSemantico semantico = new AnalizadorSemantico(tabla);

        // a / b debe inferirse como REAL
        TokenType tipoDiv = semantico.verificarExpresion(
            new Binaria("/", new Identificador("a"), new Identificador("b"))
        );

        // IF (a + b) THEN ... debe fallar porque la condicion no es BOOLEAN
        var ifInvalido = new If(
            new Binaria("+", new Identificador("a"), new Identificador("b")),
            new Asignacion("a", new Entero(1)),
            null
        );

        try {
            semantico.analizar(ifInvalido);
            return new Result("Debio exigir expresion BOOLEAN en condicion de IF", false);
        } catch (RuntimeException e) {
            boolean ok = tipoDiv == TokenType.REAL && e.getMessage().contains("debe ser BOOLEAN");
            return ok
                ? new Result("Inferencia de division REAL y validacion de IF booleano correctas", true)
                : new Result("Error en inferencia o mensaje: " + e.getMessage(), false);
        }
    }
}