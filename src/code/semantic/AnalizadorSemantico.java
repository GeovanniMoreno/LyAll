package semantic;

import java.util.HashSet;
import java.util.Set;

import ast.Expresion;
import ast.Expresion.*;
import ast.Sentencia;
import ast.Sentencia.*;
import lexer.TokenType;

public class AnalizadorSemantico {
    private final SymbolTable symbolTable;
    private final Set<String> variablesDeclaradas;

    public AnalizadorSemantico(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.variablesDeclaradas = new HashSet<>();
    }

    public void analizar(Sentencia stmt) {
        switch (stmt) {
            case Programa(var nombre, var bloque) -> analizar(bloque);

            case Bloque(var declaraciones, var sentencias) -> {
                declaraciones.forEach(this::analizar);
                sentencias.forEach(this::analizar);
            }

            case Declaracion(var ids, var tipo) -> {
                for (String id : ids) {
                    String clave = id.toUpperCase();
                    if (!variablesDeclaradas.add(clave)) {
                        throw new RuntimeException("ERROR SEMANTICO: La variable '" + id + "' ya fue declarada.");
                    }
                }
            }

            case Asignacion(var id, var expr) -> {
                TokenType tipoVar = obtenerTipoVariable(id);
                TokenType tipoExpr = verificarExpresion(expr);

                if (!esAsignable(tipoVar, tipoExpr)) {
                    throw new RuntimeException(
                        "ERROR SEMANTICO: No se puede asignar un valor de tipo " + tipoExpr +
                        " a la variable '" + id + "' de tipo " + tipoVar + "."
                    );
                }
            }

            case Compuesta(var sentencias) -> sentencias.forEach(this::analizar);

            case If(var condicion, var entonces, var sino) -> {
                TokenType tipoCond = verificarExpresion(condicion);
                if (tipoCond != TokenType.BOOLEAN) {
                    throw new RuntimeException("ERROR SEMANTICO: La condicion del IF debe ser BOOLEAN, se encontro " + tipoCond + ".");
                }
                analizar(entonces);
                if (sino != null) {
                    analizar(sino);
                }
            }

            case While(var condicion, var cuerpo) -> {
                TokenType tipoCond = verificarExpresion(condicion);
                if (tipoCond != TokenType.BOOLEAN) {
                    throw new RuntimeException("ERROR SEMANTICO: La condicion del WHILE debe ser BOOLEAN, se encontro " + tipoCond + ".");
                }
                analizar(cuerpo);
            }

            case Read(var ids) -> {
                for (String id : ids) {
                    obtenerTipoVariable(id); // Verifica que exista en la tabla
                }
            }

            case Write(var items) -> {
                for (Expresion item : items) {
                    verificarExpresion(item);
                }
            }
        }
    }

    public TokenType verificarExpresion(Expresion expr) {
        return switch (expr) {
            case Entero(var valor)   -> TokenType.INTEGER;
            case Real(var valor)     -> TokenType.REAL;
            case Booleano(var valor) -> TokenType.BOOLEAN;
            case Texto(var valor)    -> TokenType.STRING_LITERAL;
            case Identificador(var nombre) -> obtenerTipoVariable(nombre);
            case Binaria(var op, var izq, var der) -> verificarBinaria(op, izq, der);
        };
    }

    private TokenType verificarBinaria(String op, Expresion izqExpr, Expresion derExpr) {
        TokenType t1 = verificarExpresion(izqExpr);
        TokenType t2 = verificarExpresion(derExpr);

        return switch (op) {
            case "+", "-", "*" -> {
                if (!esNumerico(t1) || !esNumerico(t2)) {
                    throw new RuntimeException("ERROR SEMANTICO: El operador '" + op + "' requiere operandos numericos, se encontro " + t1 + " y " + t2 + ".");
                }
                yield (t1 == TokenType.REAL || t2 == TokenType.REAL) ? TokenType.REAL : TokenType.INTEGER;
            }
            case "/" -> {
                if (!esNumerico(t1) || !esNumerico(t2)) {
                    throw new RuntimeException("ERROR SEMANTICO: El operador '/' requiere operandos numericos, se encontro " + t1 + " y " + t2 + ".");
                }
                yield TokenType.REAL;
            }
            case "<", "<=", ">", ">=" -> {
                if (!esNumerico(t1) || !esNumerico(t2)) {
                    throw new RuntimeException("ERROR SEMANTICO: El operador relacional '" + op + "' requiere operandos numericos, se encontro " + t1 + " y " + t2 + ".");
                }
                yield TokenType.BOOLEAN;
            }
            case "AND", "OR" -> {
                if (t1 != TokenType.BOOLEAN || t2 != TokenType.BOOLEAN) {
                    throw new RuntimeException("ERROR SEMANTICO: El operador logico '" + op + "' requiere operandos BOOLEAN, se encontro " + t1 + " y " + t2 + ".");
                }
                yield TokenType.BOOLEAN;
            }
            case "=", "<>" -> {
                boolean ambosNumericos = esNumerico(t1) && esNumerico(t2);
                boolean mismoTipo = (t1 == t2);
                if (!ambosNumericos && !mismoTipo) {
                    throw new RuntimeException("ERROR SEMANTICO: No se pueden comparar tipos incompatibles (" + t1 + " y " + t2 + ") con '" + op + "'.");
                }
                yield TokenType.BOOLEAN;
            }
            default -> throw new RuntimeException("ERROR SEMANTICO: Operador desconocido '" + op + "'.");
        };
    }

    private boolean esNumerico(TokenType tipo) {
        return tipo == TokenType.INTEGER || tipo == TokenType.REAL;
    }

    private boolean esAsignable(TokenType destino, TokenType origen) {
        if (destino == origen) return true;
        // Promocion implicita permitida: INTEGER -> REAL
        return destino == TokenType.REAL && origen == TokenType.INTEGER;
    }

    private TokenType obtenerTipoVariable(String id) {
        Symbol sym = symbolTable.lookup(id);
        if (sym == null) {
            throw new RuntimeException("ERROR SEMANTICO: La variable '" + id + "' no ha sido declarada.");
        }
        return TokenType.valueOf(sym.getTokenType());
    }
}