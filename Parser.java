package ConPascal;

import java.util.List;

/**
 * Analizador Sintáctico y Semántico para Pascal (Reglas 1 a 25).
 */
public class Parser {
    private Lexer lexer;
    private Token current;
    private SymbolTable symbolTable;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.nexToken();
        this.symbolTable = new SymbolTable();
    }

    private void match(TokenType type) {
        if(current.atributo == type) {
            current = lexer.nexToken();
        }else {
            throw new RuntimeException("ERROR esperabamos: " + type + "y tenemos: " + current.lexema);
        }
    }

    public Nodo evaluar() {
        return parseProgram();
    }

    // (1) program → PROGRAM identifier ; block .
    public Nodo parseProgram() {
        match(TokenType.PROGRAM);
        
        String nombrePrgm = current.lexema;
        match(TokenType.ID);
        
        match(TokenType.SEMICOLON);

        Nodo bloque = parseBlock();

        match(TokenType.DOT);
    return new NodoPrograma(nombrePrgm, bloque);
    }

    // (2) block → declaration_part statement_part
    public Nodo parseBlock() {
        Nodo d = parseDeclPart();
        Nodo s = parseStamentPart();
    return new NodoBlock(d, s);
    }

    // (3) declaration_part → VAR declaration_list | ε
    public Nodo parseDeclPart() {
        if(current.atributo == TokenType.VAR) {
            match(TokenType.VAR);
            
            return parseDeclList();
        }
    //Aquí hay que checar si esta bien que el vacío se devuelva así
    return null;
    }

    // (4) declaration_list → declaration ; declaration_list | declaration
    public Nodo parseDeclList() {
        Nodo decl = parseDeclaration();

        if(current.atributo == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON);
            
            if(current.atributo != TokenType.ID) {
                return decl;
            }
            
            Nodo dL = parseDeclList();
            return new NodoDeclList(decl, dL);
        }
        return decl;
    }

    // (5) declaration → identifier_list : type
    public Nodo parseDeclaration() {
        Nodo idenLis = parseIdentLis();
        
        match(TokenType.COLON);
        
        Nodo type = parseType();

        //Guardar variables en la Tabla de Símbolos
        List<String> ids = (List<String>) idenLis.evaluar();
        TokenType tipoDato = (TokenType) type.evaluar();

        for(String id : ids){
            symbolTable.declarar(id, tipoDato.toString());
        }
    return new NodoDeclaration(idenLis, type);
    }

    // (6) identifier_list → identifier , identifier_list | identifier
    public Nodo parseIdentLis() {
        String identifier = current.lexema;
        match(TokenType.ID);

        Nodo restoLista = null;

        if(current.atributo == TokenType.COMMA){
            match(TokenType.COMMA);

            restoLista = parseIdentLis();
        }

        //devolver un IdenList, aunque solo exista un identificador
        return new NodoIdenList(
            new NodoIdentifier(identifier),
            restoLista
        );
    }


    // (7) type → INTEGER | REAL | BOOLEAN
    public Nodo parseType() {
        TokenType revType = current.atributo;
        
        switch(revType) {
            case TokenType.INTEGER: match(TokenType.INTEGER);   return new NodoType(revType);
            case TokenType.REAL:    match(TokenType.REAL);      return new NodoType(revType);
            case TokenType.BOOLEAN: match(TokenType.BOOLEAN);   return new NodoType(revType);
            default: throw new RuntimeException("ERROR tipo de dato no válido '" + current.lexema + "'");
        }
    }

    // (8) statement_part → BEGIN statement_list END
    public Nodo parseStamentPart() {
        match(TokenType.BEGIN);
        
        Nodo sl = parseStamentList();
        
        match(TokenType.END);
        return new NodoStamentPart(sl);
    }

    // (9) statement_list → statement ; statement_list | statement
    public Nodo parseStamentList() {
        Nodo stmt = parseStatement();

        if(current.atributo == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON);
            
            if(current.atributo == TokenType.END) {
                return stmt;
            }
            Nodo sl = parseStamentList();
            return new NodoStmtLis(stmt, sl);
        }
        return stmt;
    }

    // (10) statement → assignment | if_statement | while_statement | compound_statement | read_statement | write_statement
    public Nodo parseStatement() {
        switch (current.atributo) {
            case TokenType.ID:
                if(current.lexema.equalsIgnoreCase("INPUT")) return parseReadStmt();
                if(current.lexema.equalsIgnoreCase("PRINT")) return parseWriteStmt();
                    return parseAssignment();
            case TokenType.IF:      return parseIfStmt();
            case TokenType.WHILE:   return parseWhileStmt();
            case TokenType.BEGIN:   return parseCompoundStmt();
            case TokenType.READ:    return parseReadStmt();
            case TokenType.WRITE:   return parseWriteStmt();
            default: throw new RuntimeException("ERROR sentencia no válida cerca de '" + current.lexema + "'");
        }
    }

    // (11) assignment → identifier := expression
    private Nodo parseAssignment() {
        String id = current.lexema;
        match(TokenType.ID);

        if(!symbolTable.contains(id)) {
            throw new RuntimeException("ERROR la variable '" + id + "' no ha sido declarada.");
        }

        match(TokenType.ASSIGN);
        
        Nodo expr = parseExpression();
        return new NodoAsignación('=', id, expr);
    }

    // (12) if_statement → IF expression THEN statement ELSE statement
    private Nodo parseIfStmt() {
        match(TokenType.IF);
        
        Nodo condicion = parseExpression();
        
        match(TokenType.THEN);
        
        Nodo entoncesStmt = parseStatement();

        Nodo sinoStmt = null;
        
        if(current.atributo == TokenType.ELSE) {
            match(TokenType.ELSE);
            sinoStmt = parseStatement();
        }
        return new NodoIf(condicion, entoncesStmt, sinoStmt);
    }

    // (13) while_statement → WHILE expression DO statement
    private Nodo parseWhileStmt() {
        match(TokenType.WHILE);
        
        Nodo condicion = parseExpression();
        
        match(TokenType.DO);
        
        Nodo cuerpo = parseStatement();
        return new NodoWhile(condicion, cuerpo);
    }

    // (14) compound_statement → BEGIN statement_list END
    private Nodo parseCompoundStmt() {
        return parseStamentPart();
    }

    // (15) read_statement → (READ | INPUT) ( identifier_list )
    private Nodo parseReadStmt() {
        if(current.atributo == TokenType.READ || current.lexema.equalsIgnoreCase("INPUT")){
            current = lexer.nexToken();
        }else {
            throw new RuntimeException("ERROR se esperaba READ o INPUT.");
        }

        match(TokenType.LPARENT);
        Nodo idsNodo = parseIdentLis();

        List<String> ids = (List<String>) idsNodo.evaluar();
        for(String varName : ids) {
            if(!symbolTable.contains(varName)){
                throw new RuntimeException("ERROR la variable '" + varName + "' en READ/INPUT no está declarada.");
            }
        }

        match(TokenType.RPARENT);
        return new NodoRead(idsNodo);
    }

    // (16) write_statement → (WRITE | PRINT) ( write_list )
    private Nodo parseWriteStmt() {
        if(current.atributo == TokenType.WRITE || current.lexema.equalsIgnoreCase("PRINT")){
            current = lexer.nexToken();
        }else {
            throw new RuntimeException("ERROR se esperaba WRITE o PRINT.");
        }

        match(TokenType.LPARENT);
        
        Nodo listNodo = parseWriteList();
        
        match(TokenType.RPARENT);
        return new NodoWrite(listNodo);
    }

    // (17) write_list → write_item , write_list | write_item
    private Nodo parseWriteList() {
        Nodo item = parseWriteItem();
        if(current.atributo == TokenType.COMMA){
            match(TokenType.COMMA);
            
            Nodo resto = parseWriteList();
            return new NodoWriteList(item, resto);
        }
        return item;
    }

    // (18) write_item → expression | string_literal
    private Nodo parseWriteItem() {
        if(current.atributo == TokenType.STRING_LITERAL){
            String texto = current.lexema;
            match(TokenType.STRING_LITERAL);
            
            return new NodoTexto(texto);
        }
        return parseExpression();
    }

    // (19) expression → simple_expression relop simple_expression | simple_expression
    private Nodo parseExpression() {
        Nodo izq = parseSimpleExpression();

        if(isRelop(current.atributo)){
            String op = current.lexema;
            match(current.atributo);
            
            Nodo der = parseSimpleExpression();
            return new NodoOperador(op, izq, der);
        }
        return izq;
    }

    private boolean isRelop(TokenType type) {
        return type == TokenType.EQUAL   || type == TokenType.NOT_EQUAL     ||
               type == TokenType.LESS    || type == TokenType.LESS_OR_EQUAL ||
               type == TokenType.GREATER || type == TokenType.GREATER_OR_EQUAL;
    }

    // (20) simple_expression → term addop term | term
    private Nodo parseSimpleExpression() {
        Nodo izq = parseTerm();

        while(current.atributo == TokenType.SUM || current.atributo == TokenType.SUB || current.atributo == TokenType.OR){
            String op = current.lexema;
            match(current.atributo);
            
            Nodo der = parseTerm();
            
            izq = new NodoOperador(op, izq, der);
        }
        return izq;
    }

    // (21) term → factor mulop factor | factor
    private Nodo parseTerm() {
        Nodo izq = parseFactor();

        while(current.atributo == TokenType.MUL || current.atributo == TokenType.DIV || current.atributo == TokenType.AND){
            String op = current.lexema;
            match(current.atributo);
            
            Nodo der = parseFactor();
            
            izq = new NodoOperador(op, izq, der);
        }
        return izq;
    }

    // (22) factor → identifier | number | ( expression )
    private Nodo parseFactor() {
        if(current.atributo == TokenType.ID){
            String name = current.lexema;

            if(!symbolTable.contains(name)){
                throw new RuntimeException("ERROR la variable '" + name + "' no esta declarada.");
            }

            match(TokenType.ID);
            return new NodoIdentifier(name);
            
        }else if(current.atributo == TokenType.INT_LITERAL || current.atributo == TokenType.FLOAT_LITERAL) {
            float val = Float.parseFloat(current.lexema);
            match(current.atributo);
            
            return new NodoNumérico(val);
            
        }else if(current.atributo == TokenType.LPARENT){
            match(TokenType.LPARENT);
            
            Nodo expr = parseExpression();
            
            match(TokenType.RPARENT);
            return expr;
            
        }else{
            throw new RuntimeException("ERROR se esperaba un identificador, número o '(' y se encontró '" + current.lexema + "'");
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}