package ConPascal;

import java.util.function.Predicate;

public class Lexer {
    final String recibePrograma;
    int posición;
    char caracterActual;
    
    public Lexer(String recibePrograma){
        this.recibePrograma = recibePrograma;
        this.posición = 0;
        caracterActual = (recibePrograma.length() > 0)? recibePrograma.charAt(0) : '\0';
    }
    
    private void advance(){
        this.posición++;
        caracterActual = (posición < recibePrograma.length())? recibePrograma.charAt(posición) : '\0';
    }
    
    private void skipSpace(){
        while(Character.isWhitespace(caracterActual))
            advance();
    }
    
    public Token nexToken(){
        while(caracterActual != '\0'){
            if(Character.isWhitespace(caracterActual)){
                skipSpace();
                continue;
            }
            if(Character.isDigit(caracterActual)){
                return number();
            }else if(Character.isLetter(caracterActual)){
                return identifier();
            }else if(caracterActual == '"'){
                return texto();
            }
            switch(caracterActual){
                case '+': advance(); return new Token("+", TokenType.SUM);
                case '-': advance(); return new Token("-", TokenType.SUB);
                case '*': advance(); return new Token("*", TokenType.MUL);
                case '/': advance(); return new Token("/", TokenType.DIV);
                case '(': advance(); return new Token("(", TokenType.LPARENT);
                case ')': advance(); return new Token(")", TokenType.RPARENT);
                case ';': advance(); return new Token(";", TokenType.SEMICOLON);
                case '=': advance(); return new Token("=", TokenType.EQUAL);
                case '.': advance(); return new Token(".", TokenType.DOT);
                case ',': advance(); return new Token(",", TokenType.COMMA);
                case ':': return assig(); 
                case '<': return relop();
                case '>': return relop2();
                default : throw new RuntimeException("Carácter ilegal [" + caracterActual + "]"); 
            }
        }
        return new Token("", TokenType.EOF);
    }
    
    private Token assig(){
        advance();
        if(caracterActual == '='){
            advance(); 
            return new Token(":=", TokenType.ASSIGN);
        }
        //Aquí no se porque agregas dos puntos que no están en la gramática
        return new Token(":", TokenType.COLON);
    }
    
    private Token relop(){
        advance();
        if(caracterActual == '>'){
            advance();
            return new Token("<>", TokenType.NOT_EQUAL);
        }else if(caracterActual == '='){
            advance();
            return new Token("<=", TokenType.LESS_OR_EQUAL);
        }
        return new Token("<", TokenType.LESS);
    }
    
    private Token relop2(){
        advance();
        if(caracterActual == '='){
            advance();
            return new Token(">=", TokenType.GREATER_OR_EQUAL);
        }
        return new Token(">", TokenType.GREATER);
    }
    
    private String readWhile(Predicate<Character> condición){
        StringBuilder sb = new StringBuilder();
        while(caracterActual != '\0' && condición.test(caracterActual)){
            sb.append(caracterActual);
            advance();
        }
        return sb.toString();
    }
    
    private Token number(){
    //Lo modifique para que reconociera los números con (.) y no se confundiera con el (.) de finPrograma
        StringBuilder sb = new StringBuilder();
        while (caracterActual != '\0' && Character.isDigit(caracterActual)) {
            sb.append(caracterActual);
            advance();
        }        
        
        if (caracterActual == '.' && posición + 1 < recibePrograma.length() 
                && Character.isDigit(recibePrograma.charAt(posición + 1))) {
            sb.append(caracterActual);
            advance();            
            while (caracterActual != '\0' && Character.isDigit(caracterActual)) {
                sb.append(caracterActual);
                advance();
            }
            return new Token(sb.toString(), TokenType.FLOAT_LITERAL);
        }
        return new Token(sb.toString(), TokenType.INT_LITERAL);
    }
    
//Sirve para determinar qué caracteres componen un identificador válido o palabra reservada tras 
//leer la primera letra. Permite identificar nombres como radio1, volumen_total o radio_2.
    private Predicate<Character> isValidChar() {
        return c -> Character.isLetterOrDigit(c) || c == '_';
    }
    
    private Token identifier(){
        String id = readWhile(isValidChar());
        switch(id){
            case "PROGRAM": return new Token(id, TokenType.PROGRAM);
            case "VAR"    : return new Token(id, TokenType.VAR);
            case "BEGIN"  : return new Token(id, TokenType.BEGIN);
            case "END"    : return new Token(id, TokenType.END);
            case "IF"     : return new Token(id, TokenType.IF);
            case "THEN"   : return new Token(id, TokenType.THEN);
            case "ELSE"   : return new Token(id, TokenType.ELSE);
            case "WHILE"  : return new Token(id, TokenType.WHILE);
            case "DO"     : return new Token(id, TokenType.DO);
            case "INTEGER": return new Token(id, TokenType.INTEGER);
            case "REAL"   : return new Token(id, TokenType.REAL);
            case "BOOLEAN": return new Token(id, TokenType.BOOLEAN);
            case "READ"   : return new Token(id,TokenType.READ);
            case "WRITE"  : return new Token(id, TokenType.WRITE);
            case "OR"     : return new Token(id,TokenType.OR);
            case "AND"    : return new Token(id,TokenType.AND);
        
        }
        return new Token(id, TokenType.ID);
    }
    
    private Token texto(){
       StringBuilder sb = new StringBuilder();
        sb.append("\"");
        advance();
        
        while(caracterActual!='\0' && caracterActual!='"'){
            sb.append(caracterActual);
            advance();
        }
        if(caracterActual == '"'){
            sb.append('"');
            advance();
            return new Token(sb.toString(), TokenType.STRING_LITERAL);
        }else
            throw new RuntimeException("String no cerrado");
    }
}

class Token{
    String lexema;
    TokenType atributo;
    
    public Token(String lexema, TokenType atributo){
        this.lexema = lexema;
        this.atributo = atributo;
    }
}

enum TokenType{
    SUM,
    SUB,
    MUL,
    DIV,
    EQUAL,
    ASSIGN,
    NOT_EQUAL,
    LESS,
    LESS_OR_EQUAL,
    GREATER,
    GREATER_OR_EQUAL,
    LPARENT,
    RPARENT,
    SEMICOLON,
    COLON,
    COMMA,
    DOT,
    PROGRAM,
    VAR,
    BEGIN,
    END,
    IF,
    THEN,
    ELSE,
    WHILE,
    DO,
    INTEGER,
    REAL,
    BOOLEAN,
    ID,
    OR,
    AND,
    INT_LITERAL,
    FLOAT_LITERAL,
    BOOLEAN_LITERAL,
    STRING_LITERAL,
    READ,
    WRITE,
    EOF
}


class TestLexer{
    public static void main(String[] args) {
        Lexer demo = new Lexer("""
                               PROGRAM VolumenDeUnaEsfera;
                               VAR
                                   radio, volumen : REAL;
                               BEGIN
                                   WRITE("Introduce un valor para el radio: ");
                                   READ(radio);
                                   
                                   volumen := 3/4 * 3.1415964 * radio*radio*radio;
                               
                                   WRITE("El valor del volumen es: ", volumen);
                               END.""");
       
        while(true){
            Token t = demo.nexToken();
            if(t.atributo == TokenType.EOF)
                break;
            System.out.println(t.atributo + ", " + t.lexema);                
        }
    }
}

