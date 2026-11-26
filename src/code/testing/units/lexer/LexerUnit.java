package testing.units.lexer;

import java.util.ArrayList;
import java.util.List;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import testing.Tester.DispatchTarget;
import testing.units.Result;
import testing.units.Test;

public class LexerUnit extends Test {

    public LexerUnit() {
        super();
        this.pertenencia = DispatchTarget.LEXER;
    }

    // Metodo auxiliar para tokenizar una cadena completa hasta EOF
    private List<Token> tokenizar(String entrada) {
        Lexer lexer = new Lexer(entrada);
        List<Token> tokens = new ArrayList<>();
        Token actual;
        do {
            actual = lexer.nexToken();
            tokens.add(actual);
        } while (actual.getAtributo() != TokenType.EOF);
        return tokens;
    }

    private Result test_entradaVaciaYSoloEspacios() {
        List<Token> tokensVacio = tokenizar("");
        List<Token> tokensEspacios = tokenizar("   \n\t\r\n  ");

        boolean ok = tokensVacio.size() == 1 && tokensVacio.get(0).getAtributo() == TokenType.EOF
                  && tokensEspacios.size() == 1 && tokensEspacios.get(0).getAtributo() == TokenType.EOF;

        return ok
            ? new Result("Entrada vacia y espacios retornan EOF", true)
            : new Result("Se esperaba unicamente EOF para entradas vacias", false);
    }

    private Result test_palabrasReservadasExactas() {
        String entrada = "PROGRAM VAR BEGIN END IF THEN ELSE WHILE DO INTEGER REAL BOOLEAN READ WRITE OR AND TRUE FALSE";
        List<TokenType> esperados = List.of(
            TokenType.PROGRAM, TokenType.VAR, TokenType.BEGIN, TokenType.END,
            TokenType.IF, TokenType.THEN, TokenType.ELSE, TokenType.WHILE,
            TokenType.DO, TokenType.INTEGER, TokenType.REAL, TokenType.BOOLEAN,
            TokenType.READ, TokenType.WRITE, TokenType.OR, TokenType.AND,
            TokenType.BOOLEAN_LITERAL, TokenType.BOOLEAN_LITERAL, TokenType.EOF
        );

        List<TokenType> obtenidos = tokenizar(entrada).stream()
            .map(Token::getAtributo)
            .toList();

        return esperados.equals(obtenidos)
            ? new Result("Todas las palabras reservadas reconocidas", true)
            : new Result("Discrepancia en keywords. Obtenido: " + obtenidos, false);
    }

    private Result test_sensibilidadMayusculasYPrefijos() {
        // Las keywords son puras mayusculas; minusculas o prefijos deben ser ID
        String entrada = "program iffy WHILE_1 radio_2 volumen_total true";
        List<TokenType> esperados = List.of(
            TokenType.ID, TokenType.ID, TokenType.ID,
            TokenType.ID, TokenType.ID, TokenType.ID, TokenType.EOF
        );

        List<TokenType> obtenidos = tokenizar(entrada).stream()
            .map(Token::getAtributo)
            .toList();

        return esperados.equals(obtenidos)
            ? new Result("Identificadores y sensibilidad de mayusculas validados", true)
            : new Result("Fallo diferenciando IDs de keywords: " + obtenidos, false);
    }

    private Result test_literalesNumericosYPuntoFinal() {
        // Valida enteros, flotantes y el caso borde '5.' al final del programa (INT + DOT)
        String entrada = "42 3.1416 100. 7.abc";
        List<TokenType> esperados = List.of(
            TokenType.INT_LITERAL,
            TokenType.FLOAT_LITERAL,
            TokenType.INT_LITERAL, TokenType.DOT,
            TokenType.INT_LITERAL, TokenType.DOT, TokenType.ID,
            TokenType.EOF
        );

        List<Token> tokens = tokenizar(entrada);
        List<TokenType> obtenidos = tokens.stream().map(Token::getAtributo).toList();

        boolean lexemaFloatOk = tokens.get(1).getLexema().equals("3.1416");

        return (esperados.equals(obtenidos) && lexemaFloatOk)
            ? new Result("Enteros, flotantes y punto final diferenciados", true)
            : new Result("Error en literales numericos: " + obtenidos, false);
    }

    private Result test_operadoresCompuestosYSimples() {
        String entrada = "+ - * / = := : <> <= < >= > ( ) ; , .";
        List<TokenType> esperados = List.of(
            TokenType.SUM, TokenType.SUB, TokenType.MUL, TokenType.DIV,
            TokenType.EQUAL, TokenType.ASSIGN, TokenType.COLON,
            TokenType.NOT_EQUAL, TokenType.LESS_OR_EQUAL, TokenType.LESS,
            TokenType.GREATER_OR_EQUAL, TokenType.GREATER,
            TokenType.LPARENT, TokenType.RPARENT, TokenType.SEMICOLON,
            TokenType.COMMA, TokenType.DOT, TokenType.EOF
        );

        List<TokenType> obtenidos = tokenizar(entrada).stream()
            .map(Token::getAtributo)
            .toList();

        return esperados.equals(obtenidos)
            ? new Result("Operadores simples y compuestos reconocidos", true)
            : new Result("Error en operadores. Obtenido: " + obtenidos, false);
    }

    private Result test_operadoresPegadosSinEspacios() {
        String entrada = "x:=a+b*2<>10;";
        List<TokenType> esperados = List.of(
            TokenType.ID, TokenType.ASSIGN, TokenType.ID, TokenType.SUM,
            TokenType.ID, TokenType.MUL, TokenType.INT_LITERAL,
            TokenType.NOT_EQUAL, TokenType.INT_LITERAL, TokenType.SEMICOLON,
            TokenType.EOF
        );

        List<TokenType> obtenidos = tokenizar(entrada).stream()
            .map(Token::getAtributo)
            .toList();

        return esperados.equals(obtenidos)
            ? new Result("Agrupacion de tokens correcta en cadenas sin espacios", true)
            : new Result("Fallo al separar tokens pegados: " + obtenidos, false);
    }

    private Result test_cadenasTextoValidas() {
        String entrada = "\"Hola mundo\" \"\"";
        List<Token> tokens = tokenizar(entrada);

        boolean ok = tokens.size() == 3
                  && tokens.get(0).getAtributo() == TokenType.STRING_LITERAL
                  && tokens.get(0).getLexema().equals("\"Hola mundo\"")
                  && tokens.get(1).getAtributo() == TokenType.STRING_LITERAL
                  && tokens.get(1).getLexema().equals("\"\"");

        return ok
            ? new Result("Cadenas normales y vacias reconocidas", true)
            : new Result("Error al leer STRING_LITERAL", false);
    }

    private Result test_errorCadenaSinCerrar() {
        try {
            tokenizar("WRITE(\"Cadena sin cerrar);");
            return new Result("Debio lanzar excepcion por string sin cerrar", false);
        } catch (RuntimeException e) {
            boolean mensajeEsperado = e.getMessage().contains("String no cerrado");
            return mensajeEsperado
                ? new Result("Excepcion capturada: " + e.getMessage(), true)
                : new Result("Mensaje de excepcion inesperado: " + e.getMessage(), false);
        }
    }

    private Result test_errorCaracterIlegal() {
        try {
            tokenizar("VAR @x : INTEGER;");
            return new Result("Debio lanzar excepcion por caracter ilegal '@'", false);
        } catch (RuntimeException e) {
            boolean mensajeEsperado = e.getMessage().contains("Caracter ilegal");
            return mensajeEsperado
                ? new Result("Excepcion capturada: " + e.getMessage(), true)
                : new Result("Mensaje de excepcion inesperado: " + e.getMessage(), false);
        }
    }
}