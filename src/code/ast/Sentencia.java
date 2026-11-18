package ast;

import java.util.List;
import lexer.TokenType;

public sealed interface Sentencia {
    record Programa(String nombre, Bloque bloque) implements Sentencia {}
    record Bloque(List<Declaracion> declaraciones, List<Sentencia> sentencias) implements Sentencia {}
    record Declaracion(List<String> ids, TokenType tipo) implements Sentencia {}
    record Asignacion(String id, Expresion valor) implements Sentencia {}
    record If(Expresion condicion, Sentencia entonces, Sentencia sino) implements Sentencia {}
    record While(Expresion condicion, Sentencia cuerpo) implements Sentencia {}
    record Compuesta(List<Sentencia> sentencias) implements Sentencia {}
    record Read(List<String> ids) implements Sentencia {}
    record Write(List<Expresion> items) implements Sentencia {}
}