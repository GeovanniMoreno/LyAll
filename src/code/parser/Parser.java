package parser;

import java.util.ArrayList;
import java.util.List;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import semantic.SymbolTable;
import ast.Expresion;
import ast.Expresion.*;
import ast.Sentencia;
import ast.Sentencia.*;

/**
 * Analizador Sintactico y Semantico
 */
public class Parser {
    private final Lexer lexer;
    private Token current;
    private final SymbolTable symbolTable;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.nexToken();
        this.symbolTable = new SymbolTable();
    }

    private void match(TokenType type) {
        if (current.getAtributo() == type) {
            current = lexer.nexToken();
        } else {
            throw new RuntimeException("ERROR esperabamos: " + type + " y tenemos: " + current.getLexema());
        }
    }

    public Programa parse() {
        return parseProgram();
    }

    // (1) program → PROGRAM identifier ; block .
    public Programa parseProgram() {
        match(TokenType.PROGRAM);
        String nombrePrgm = current.getLexema();
        match(TokenType.ID);
        match(TokenType.SEMICOLON);

        Bloque bloque = parseBlock();

        match(TokenType.DOT);
        return new Programa(nombrePrgm, bloque);
    }

    // (2) block → declaration_part statement_part
    public Bloque parseBlock() {
        List<Declaracion> declaraciones = parseDeclPart();
        List<Sentencia> sentencias = parseStamentPart();
        return new Bloque(declaraciones, sentencias);
    }

    // (3) declaration_part → VAR declaration_list | ε
    public List<Declaracion> parseDeclPart() {
        if (current.getAtributo() == TokenType.VAR) {
            match(TokenType.VAR);
            return parseDeclList();
        }
        return List.of();
    }

    // (4) declaration_list → declaration ; declaration_list | declaration
    public List<Declaracion> parseDeclList() {
        List<Declaracion> lista = new ArrayList<>();
        lista.add(parseDeclaration());

        while (current.getAtributo() == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON);
            if (current.getAtributo() != TokenType.ID) {
                break;
            }
            lista.add(parseDeclaration());
        }
        return lista;
    }

    // (5) declaration → identifier_list : type
    public Declaracion parseDeclaration() {
        List<String> ids = parseIdentList();
        match(TokenType.COLON);
        TokenType tipoDato = parseType();

        for (String id : ids) {
            symbolTable.declarar(id, tipoDato.toString());
        }
        return new Declaracion(ids, tipoDato);
    }

    // (6) identifier_list → identifier , identifier_list | identifier
    public List<String> parseIdentList() {
        List<String> ids = new ArrayList<>();
        ids.add(current.getLexema());
        match(TokenType.ID);

        while (current.getAtributo() == TokenType.COMMA) {
            match(TokenType.COMMA);
            ids.add(current.getLexema());
            match(TokenType.ID);
        }
        return ids;
    }

    // (7) type → INTEGER | REAL | BOOLEAN
    public TokenType parseType() {
        TokenType revType = current.getAtributo();
        return switch (revType) {
            case INTEGER, REAL, BOOLEAN -> {
                match(revType);
                yield revType;
            }
            default -> throw new RuntimeException("ERROR tipo de dato no valido '" + current.getLexema() + "'");
        };
    }

    // (8) statement_part → BEGIN statement_list END
    public List<Sentencia> parseStamentPart() {
        match(TokenType.BEGIN);
        List<Sentencia> sl = parseStamentList();
        match(TokenType.END);
        return sl;
    }

    // (9) statement_list → statement ; statement_list | statement
    public List<Sentencia> parseStamentList() {
        List<Sentencia> lista = new ArrayList<>();
        lista.add(parseStatement());

        while (current.getAtributo() == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON);
            if (current.getAtributo() == TokenType.END) {
                break;
            }
            lista.add(parseStatement());
        }
        return lista;
    }

    // (10) statement → assignment | if_statement | while_statement | compound_statement | read_statement | write_statement
    public Sentencia parseStatement() {
        return switch (current.getAtributo()) {
            case ID -> {
                if (current.getLexema().equalsIgnoreCase("INPUT")) yield parseReadStmt();
                if (current.getLexema().equalsIgnoreCase("PRINT")) yield parseWriteStmt();
                yield parseAssignment();
            }
            case IF    -> parseIfStmt();
            case WHILE -> parseWhileStmt();
            case BEGIN -> parseCompoundStmt();
            case READ  -> parseReadStmt();
            case WRITE -> parseWriteStmt();
            default    -> throw new RuntimeException("ERROR sentencia no valida cerca de '" + current.getLexema() + "'");
        };
    }

    // (11) assignment → identifier := expression
    private Asignacion parseAssignment() {
        String id = current.getLexema();
        match(TokenType.ID);

        if (!symbolTable.contains(id)) {
            throw new RuntimeException("ERROR la variable '" + id + "' no ha sido declarada.");
        }

        match(TokenType.ASSIGN);
        Expresion expr = parseExpression();
        return new Asignacion(id, expr);
    }

    // (12) if_statement → IF expression THEN statement ELSE statement
    private If parseIfStmt() {
        match(TokenType.IF);
        Expresion condicion = parseExpression();
        match(TokenType.THEN);
        Sentencia entoncesStmt = parseStatement();

        Sentencia sinoStmt = null;
        if (current.getAtributo() == TokenType.ELSE) {
            match(TokenType.ELSE);
            sinoStmt = parseStatement();
        }
        return new If(condicion, entoncesStmt, sinoStmt);
    }

    // (13) while_statement → WHILE expression DO statement
    private While parseWhileStmt() {
        match(TokenType.WHILE);
        Expresion condicion = parseExpression();
        match(TokenType.DO);
        Sentencia cuerpo = parseStatement();
        return new While(condicion, cuerpo);
    }

    // (14) compound_statement → BEGIN statement_list END
    private Compuesta parseCompoundStmt() {
        return new Compuesta(parseStamentPart());
    }

    // (15) read_statement → READ ( identifier_list )
    private Read parseReadStmt() {
        if (current.getAtributo() == TokenType.READ || current.getLexema().equalsIgnoreCase("INPUT")) {
            current = lexer.nexToken();
        } else {
            throw new RuntimeException("ERROR se esperaba READ o INPUT.");
        }

        match(TokenType.LPARENT);
        List<String> ids = parseIdentList();

        for (String varName : ids) {
            if (!symbolTable.contains(varName)) {
                throw new RuntimeException("ERROR la variable '" + varName + "' en READ/INPUT no esta declarada.");
            }
        }

        match(TokenType.RPARENT);
        return new Read(ids);
    }

    // (16) write_statement → WRITE ( write_list )
    private Write parseWriteStmt() {
        if (current.getAtributo() == TokenType.WRITE || current.getLexema().equalsIgnoreCase("PRINT")) {
            current = lexer.nexToken();
        } else {
            throw new RuntimeException("ERROR se esperaba WRITE o PRINT.");
        }

        match(TokenType.LPARENT);
        List<Expresion> items = parseWriteList();
        match(TokenType.RPARENT);
        return new Write(items);
    }

    // (17) write_list → write_item , write_list | write_item
    private List<Expresion> parseWriteList() {
        List<Expresion> items = new ArrayList<>();
        items.add(parseWriteItem());

        while (current.getAtributo() == TokenType.COMMA) {
            match(TokenType.COMMA);
            items.add(parseWriteItem());
        }
        return items;
    }

    // (18) write_item → expression | string_literal
    private Expresion parseWriteItem() {
        if (current.getAtributo() == TokenType.STRING_LITERAL) {
            String texto = current.getLexema();
            match(TokenType.STRING_LITERAL);
            return new Texto(texto);
        }
        return parseExpression();
    }

    // (19) expression → simple_expression relop simple_expression | simple_expression
    private Expresion parseExpression() {
        Expresion izq = parseSimpleExpression();

        if (isRelop(current.getAtributo())) {
            String op = current.getLexema();
            match(current.getAtributo());
            Expresion der = parseSimpleExpression();
            return new Binaria(op, izq, der);
        }
        return izq;
    }

    private boolean isRelop(TokenType type) {
        return type == TokenType.EQUAL   || type == TokenType.NOT_EQUAL     ||
               type == TokenType.LESS    || type == TokenType.LESS_OR_EQUAL ||
               type == TokenType.GREATER || type == TokenType.GREATER_OR_EQUAL;
    }

    // (20) simple_expression → term addop term | term
    private Expresion parseSimpleExpression() {
        Expresion izq = parseTerm();

        while (current.getAtributo() == TokenType.SUM ||
               current.getAtributo() == TokenType.SUB ||
               current.getAtributo() == TokenType.OR) {
            String op = current.getLexema();
            match(current.getAtributo());
            Expresion der = parseTerm();
            izq = new Binaria(op, izq, der);
        }
        return izq;
    }

    // (21) term → factor mulop factor | factor
    private Expresion parseTerm() {
        Expresion izq = parseFactor();

        while (current.getAtributo() == TokenType.MUL ||
               current.getAtributo() == TokenType.DIV ||
               current.getAtributo() == TokenType.AND) {
            String op = current.getLexema();
            match(current.getAtributo());
            Expresion der = parseFactor();
            izq = new Binaria(op, izq, der);
        }
        return izq;
    }

    // (22) factor → identifier | number | ( expression )
    // (22) factor → identifier | number | boolean | ( expression )
    private Expresion parseFactor() {
        if (current.getAtributo() == TokenType.ID) {
            String name = current.getLexema();
            if (!symbolTable.contains(name)) {
                throw new RuntimeException("ERROR la variable '" + name + "' no esta declarada.");
            }
            match(TokenType.ID);
            return new Identificador(name);

        } else if (current.getAtributo() == TokenType.INT_LITERAL) {
            long val = Long.parseLong(current.getLexema());
            match(TokenType.INT_LITERAL);
            return new Entero(val);

        } else if (current.getAtributo() == TokenType.FLOAT_LITERAL) {
            double val = Double.parseDouble(current.getLexema());
            match(TokenType.FLOAT_LITERAL);
            return new Real(val);

        } else if (current.getAtributo() == TokenType.BOOLEAN_LITERAL) {
            boolean val = Boolean.parseBoolean(current.getLexema().toLowerCase());
            match(TokenType.BOOLEAN_LITERAL);
            return new Booleano(val);

        } else if (current.getAtributo() == TokenType.LPARENT) {
            match(TokenType.LPARENT);
            Expresion expr = parseExpression();
            match(TokenType.RPARENT);
            return expr;

        } else {
            throw new RuntimeException("ERROR se esperaba un identificador, literal o '(' y se encontro '" + current.getLexema() + "'");
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}