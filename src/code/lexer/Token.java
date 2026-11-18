package lexer;

public class Token{
    String lexema;
    TokenType atributo;
    
    public Token(String lexema, TokenType atributo){
        this.lexema = lexema;
        this.atributo = atributo;
    }

    public String getLexema() {
        return lexema;
    }

    public TokenType getAtributo() {
        return atributo;
    }
}