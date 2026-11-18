package ast;

import lexer.TokenType;

// (7) Nodo Tipo de Dato
public class NodoType extends Nodo {
    private TokenType type;

    public NodoType(TokenType type) {
        this.type = type;
    }

    @Override
    public Object evaluar() {
        return this.type;
    }
}