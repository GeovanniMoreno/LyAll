package ast;

public sealed interface Expresion {
    record Entero(long valor) implements Expresion {}
    record Real(double valor) implements Expresion {}
    record Booleano(boolean valor) implements Expresion {}
    record Texto(String valor) implements Expresion {}
    record Identificador(String nombre) implements Expresion {}
    record Binaria(String operador, Expresion izq, Expresion der) implements Expresion {}
}