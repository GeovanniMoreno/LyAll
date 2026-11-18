package ast;

import java.util.Scanner;
import ast.Expresion.*;
import ast.Sentencia.*;
import semantic.Symbol;
import semantic.SymbolTable;

public class Evaluador {
    private final SymbolTable symbolTable;
    private final Scanner scanner;

    public Evaluador(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scanner = new Scanner(System.in);
    }

    public void ejecutar(Sentencia stmt) {
        switch (stmt) {
            case Programa(var nombre, var bloque) -> ejecutar(bloque);

            case Bloque(var declaraciones, var sentencias) -> {
                declaraciones.forEach(this::ejecutar);
                sentencias.forEach(this::ejecutar);
            }

            case Declaracion(var ids, var tipo) -> {
                for (String id : ids) {
                    symbolTable.declarar(id, tipo.toString());
                }
            }

            case Asignacion(var id, var expr) -> {
                Symbol sym = obtenerSimbolo(id);
                Object valor = evaluar(expr);
                // Si la variable es REAL y se asigna un entero (Long), lo promovemos a Double
                if (sym.getTokenType().equals("REAL") && valor instanceof Long l) {
                    valor = l.doubleValue();
                }
                sym.setValue(valor);
            }

            case Compuesta(var sentencias) -> sentencias.forEach(this::ejecutar);

            case If(var condicion, var entonces, var sino) -> {
                if (evaluarBooleano(condicion)) {
                    ejecutar(entonces);
                } else if (sino != null) {
                    ejecutar(sino);
                }
            }

            case While(var condicion, var cuerpo) -> {
                while (evaluarBooleano(condicion)) {
                    ejecutar(cuerpo);
                }
            }

            case Read(var ids) -> {
                for (String id : ids) {
                    Symbol sym = obtenerSimbolo(id);
                    String tipo = sym.getTokenType();
                    switch (tipo) {
                        case "INTEGER" -> sym.setValue(scanner.nextLong());
                        case "REAL"    -> sym.setValue(scanner.nextDouble());
                        case "BOOLEAN" -> sym.setValue(scanner.nextBoolean());
                        default        -> throw new RuntimeException("Tipo no soportado en READ: " + tipo);
                    }
                }
            }

            case Write(var items) -> {
                for (Expresion item : items) {
                    Object val = evaluar(item);
                    if (val instanceof String s && s.startsWith("\"") && s.endsWith("\"")) {
                        System.out.print(s.substring(1, s.length() - 1));
                    } else {
                        System.out.print(val);
                    }
                }
                System.out.println();
            }
        }
    }

    public Object evaluar(Expresion expr) {
        return switch (expr) {
            case Entero(var valor)   -> valor;
            case Real(var valor)     -> valor;
            case Booleano(var valor) -> valor;
            case Texto(var valor)    -> valor;
            case Identificador(var nombre) -> {
                Symbol sym = obtenerSimbolo(nombre);
                if (sym.getValue() == null) {
                    throw new RuntimeException("ERROR: Variable '" + nombre + "' usada antes de ser inicializada.");
                }
                yield sym.getValue();
            }
            case Binaria(var op, var izq, var der) -> evaluarBinaria(op, izq, der);
        };
    }

    private Object evaluarBinaria(String op, Expresion izqExpr, Expresion derExpr) {
        Object izq = evaluar(izqExpr);
        Object der = evaluar(derExpr);

        return switch (op) {
            case "+"  -> operarAritmetica(op, izq, der);
            case "-"  -> operarAritmetica(op, izq, der);
            case "*"  -> operarAritmetica(op, izq, der);
            case "/"  -> operarAritmetica(op, izq, der);
            case "<"  -> compararNumeros(op, izq, der);
            case "<=" -> compararNumeros(op, izq, der);
            case ">"  -> compararNumeros(op, izq, der);
            case ">=" -> compararNumeros(op, izq, der);
            case "OR"  -> (boolean) izq || (boolean) der;
            case "AND" -> (boolean) izq && (boolean) der;
            case "="   -> compararIgualdad(izq, der);
            case "<>"  -> !compararIgualdad(izq, der);
            default    -> throw new RuntimeException("Operador ilegal: " + op);
        };
    }

    private Object operarAritmetica(String op, Object izq, Object der) {
        Number n1 = (Number) izq;
        Number n2 = (Number) der;

        // La division (/) en Pascal siempre produce un REAL (double)
        if (op.equals("/")) {
            double d2 = n2.doubleValue();
            if (d2 == 0.0) throw new RuntimeException("ERROR: Division por cero.");
            return n1.doubleValue() / d2;
        }

        // Suma, resta y multiplicacion entre enteros se mantienen como INTEGER (Long)
        if (n1 instanceof Long l1 && n2 instanceof Long l2) {
            return switch (op) {
                case "+" -> l1 + l2;
                case "-" -> l1 - l2;
                case "*" -> l1 * l2;
                default  -> throw new IllegalStateException();
            };
        }

        // Si al menos uno es Real (Double), el resultado es REAL (Double)
        double d1 = n1.doubleValue();
        double d2 = n2.doubleValue();
        return switch (op) {
            case "+" -> d1 + d2;
            case "-" -> d1 - d2;
            case "*" -> d1 * d2;
            default  -> throw new IllegalStateException();
        };
    }

    private boolean compararNumeros(String op, Object izq, Object der) {
        double d1 = ((Number) izq).doubleValue();
        double d2 = ((Number) der).doubleValue();
        return switch (op) {
            case "<"  -> d1 < d2;
            case "<=" -> d1 <= d2;
            case ">"  -> d1 > d2;
            case ">=" -> d1 >= d2;
            default   -> throw new IllegalStateException();
        };
    }

    private boolean compararIgualdad(Object izq, Object der) {
        if (izq instanceof Number n1 && der instanceof Number n2) {
            return n1.doubleValue() == n2.doubleValue();
        }
        return izq.equals(der);
    }

    private boolean evaluarBooleano(Expresion expr) {
        Object res = evaluar(expr);
        if (res instanceof Boolean b) return b;
        throw new RuntimeException("ERROR: Se esperaba una expresion booleana, se obtuvo: " + res);
    }

    private Symbol obtenerSimbolo(String id) {
        Symbol sym = symbolTable.lookup(id);
        if (sym == null) {
            throw new RuntimeException("ERROR: Variable '" + id + "' no declarada.");
        }
        return sym;
    }
}