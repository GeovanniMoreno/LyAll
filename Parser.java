package ConPascal;

/*
PARSER CON LA GRMÁTICA MAS LARGA
 */
public class Parser {
    private Lexer lexer;
    private Token current;
    private Symbol symbolTable;
    
    public Parser (Lexer lexer){
        this.lexer = lexer;
        this.current = lexer.nexToken();
        //this.symbolTable = new Symbol();
    }
    
    //Método de parser con match
    private void match (TokenType type){
        if(current.atributo == type){
            current = lexer.nexToken();
        }else {
            throw new RuntimeException("ERROR esperabamos: " + type + "y tenemos: " + current.lexema);
        }
    }
    
    public Nodo evaluar(){
        //Igual que en el otro parser, el primer método corresponde a la primera regla
        return parseProgram();
    }
    
    //(1)program → PROGRAM identifier ; block.
    public Nodo parseProgram(){
        match(TokenType.PROGRAM);
        
        String nombrePrgm = current.lexema;
        match(TokenType.ID);
        
        match(TokenType.SEMICOLON);
        
        Nodo bloque = parseBlock();
        
        match(TokenType.DOT);
    return new NodoPrograma(nombrePrgm, bloque);   
    }
    
    //(2)block → declaration_part statement_part
    public Nodo parseBlock(){
        Nodo d = parseDeclPart();
        Nodo s = parseStamentPart();
    return new NodoBlock(d, s);
    }
    
    //(3)declaration_part → VAR declaration_list | ε
    public Nodo parseDeclPart(){
        if(current.atributo == TokenType.VAR){
           match(TokenType.VAR);
           
           Nodo dL = parseDeclList();
        return new NodoDeclPart(dL);
        }
    //Aquí hay que checar si esta bien que el vacío se devuelva así
    return null;
    }
    
    //(4)declaration_list → declaration ; declaration_list  | declaration
    public Nodo parseDeclList(){
        Nodo decl = parseDeclaration();
        
        if(current.atributo == TokenType.SEMICOLON){
            match(TokenType.SEMICOLON);
            
            Nodo dL = parseDeclList();
            while(dL !=null){
                /*Aquí debe volver a evaluar con parseDeclList y agregar a una lista
                  o a la tabla de símbolos la declaración encontrada
                */
                return new NodoDeclList(decl, dL);
            }
        }
        return parseDeclaration();
    }
    
    //(5)declaration → identifier_list : type
    public Nodo parseDeclaration(){
        Nodo idenLis = parseIdentLis();
        
        match(TokenType.COLON);
        
        Nodo type = parseType();
    return new NodoDeclaration(idenLis, type);
    }
    
    //(6)identifier_list → identifier , identifier_list | identifier
    public Nodo parseIdentLis(){
        String identifier = current.lexema;
        match(TokenType.ID);
        
        if(current.atributo == TokenType.COMMA){
            match(TokenType.COMMA);
            Nodo idenLis = parseIdentLis();
            
            while(idenLis != null){
            /*Igual que en la regla 4, mientras se siga encontrando un identificador
            volver a evaluarlo con parseIdentLis() y guardarlo
            */
            return new NodoIdenList(idenLis);
            }
        }
        return new NodoIdentifier(identifier);
    }
    
    //(7)type → INTEGER | REAL | BOOLEAN
    public Nodo parseType(){
        /*
        Algo debe de encargarse de unir el idendifier con su tipo
        */
        TokenType revType = current.atributo;
        switch(revType){
            case TokenType.INTEGER: match(TokenType.INTEGER); return new NodoType(revType);
            case TokenType.REAL: match(TokenType.REAL); return new NodoType(revType);
            case TokenType.BOOLEAN: match(TokenType.BOOLEAN); return new NodoType(revType);
            default: throw new RuntimeException("ERROR falta declarar el tipo de la variable");
        }
    }
    
    //(8)statement_part → BEGIN statement_list END
    public Nodo parseStamentPart(){
        match(TokenType.BEGIN);
        
        Nodo sl = parseStamentList();
        
        match(TokenType.END);
        return new NodoStamentPart(sl);
    }
    
    //(9)statement_list → statement ; statement_list  | statement
    public Nodo parseStamentList(){
        Nodo stmt = parseStatement();
        
        if(current.atributo == TokenType.SEMICOLON){
            match(TokenType.SEMICOLON);
            
            Nodo sl = parseStamentList();
            while(sl != null){
                /*
                    Igual necesito acomodar estos while
                */
            }
        return new NodoStmtLis(stmt, sl);    
        }
        return new NodoStatement(stmt);
    }
    
    //(10)statement → assignment | if_statement  | while_statement  | compound_statement | read_statement | write_statement
    public Nodo parseStatement(){
        switch(current.atributo){
            case TokenType.ID: return parseAssignment();
            case TokenType.IF: return parseIfStmt();
            case TokenType.WHILE: return parseWhileStmt();
            case TokenType.BEGIN: return parseCompoundStmt();
            case TokenType.READ: return parseReadStmt();
            case TokenType.WRITE: return parseWriteStmt();
            default: throw new RuntimeException("ERROR algo no va donde debe o falta");
        }
    }

//(11)assignment → identifier := expression
    private Nodo parseAssignment() {return null;}
//(12)if_statement → IF expression THEN statement ELSE statement
    private Nodo parseIfStmt() {return null;}
//(13)while_statement → WHILE expression DO statement
    private Nodo parseWhileStmt() { return null;}
//(14)compound_statement → BEGIN statement_list END
    private Nodo parseCompoundStmt() {return null;}
//(15)read_statement → READ ( identifier_list )
    private Nodo parseReadStmt() {return null;}
//(16)write_statement → WRITE ( write_list )
    private Nodo parseWriteStmt() {return null;}
    
    





//NODOS PARA CREAR EL ÁRBOL, respete el evaluar() para no confundirme, pero se le puede cambiar el nombre
abstract class Nodo{
    /*NOTA: Como lo modificamos a object es necesario especificar que es lo que se devuelve cada que se 
         tiene que devolver, pero no se que tan útil sea eso o si nos va a dar muchos problemas
    */
    public abstract Object evaluar();
}

//Nodo raíz o lo principal (Regla 1)
class NodoPrograma extends Nodo{
    private String nombre;
    private Nodo bloque;
    
    public NodoPrograma(String nombre, Nodo bloque){
        this.nombre = nombre;
        this.bloque = bloque;
    }
    
    @Override
    public Object evaluar(){
        return bloque.evaluar();
    }
}

//Regla 2
class NodoBlock extends Nodo{
    private Nodo declaración;
    private Nodo stament;
    
    public NodoBlock(Nodo declaración, Nodo stament){
        this.declaración = declaración;
        this.stament = stament;
    }
    
    @Override
    public Object evaluar(){
        return declaración.evaluar();
    }
}

//Regla 3
class NodoDeclPart extends Nodo{
    private Nodo listaVariables;
    
    public NodoDeclPart(Nodo listaVariables){
        this.listaVariables = listaVariables;
    }
    
    @Override
    public Object evaluar(){
        return listaVariables.evaluar();
    }
}

//Regla 4
class NodoDeclList extends Nodo{
    private Nodo declL;
    private Nodo dL;
    
    public NodoDeclList(Nodo declL, Nodo dL){
        this.declL = declL;
        this.dL = dL;
    }
    
    @Override
    public Object evaluar(){
        return declL.evaluar();
    }
}

//Regla 5
class NodoDeclaration extends Nodo{
    private Nodo identLis;
    private Nodo type;
    
    public NodoDeclaration(Nodo identLis, Nodo type){
        this.identLis = identLis;
        this.type = type;
    }
    
    @Override
    public Object evaluar(){
        return identLis.evaluar();
    }
}

//Regla 6
class NodoIdenList extends Nodo{
    private Nodo idenList;
    
    public NodoIdenList(Nodo idenList){
        this.idenList = idenList;
    }
    
    @Override
    public Object evaluar(){
        return idenList.evaluar();
    }
}

//Es el identifier de la regla 6, solo necesitamos guardar el nombre del idetificador
class NodoIdentifier extends Nodo{
    String lexema;
    
    public NodoIdentifier(String lexema){
        this.lexema = lexema;
    }
    
    public Object evaluar(){
        //Aquí no recuerdo porque regresa 0
        return 0;
    }
}

//Regla 7
class NodoType extends Nodo{
    TokenType type;
    
    public NodoType(TokenType type){
        this.type = type;
    }
    
    public Object evaluar(){
    //Repeti el 0 de identifier porque segun mi lógica no se evalua, solo se guarda el typo que define el usuario (programa.pas)
        return 0;
    }
}

//Regla 8
class NodoStamentPart extends Nodo{
    private Nodo sl;
    
    public NodoStamentPart(Nodo sl){
        this.sl = sl;
    }
    
    @Override
    public Object evaluar(){
        return sl.evaluar();
    }
}

//Regla 9
class NodoStmtLis extends Nodo{
    private Nodo stmt;
    private Nodo sl;
    
    public NodoStmtLis(Nodo stmt, Nodo sl){
        this.sl = sl;
        this.stmt = stmt;
    }
    
    @Override
    public Object evaluar(){
        return stmt.evaluar();
    }
}

//Parte de la 10, no se si realmente se ocupen tantos nodos (osea si pero no)
class NodoStatement extends Nodo{
    private Nodo stmt;
    
    public NodoStatement(Nodo stmt){
        this.stmt = stmt;
    }
    
    @Override
    public Object evaluar(){
        return stmt.evaluar();
    }
}


//Coservamos NodoNumérico porque seguimos utilizando numeros para los cálculos
class NodoNumérico extends Nodo{
    private float valor;
    
    public NodoNumérico(float valor){
        this.valor = valor;
    }
    
    @Override
    public Object evaluar(){
        //Regresamos este valor que ya trae el tipo
        return this.valor;
    }
}

//Al nodo operador solo le agregamos los nuevos operadores que están en la gramática
class NodoOperador extends Nodo{
    private String operador;    
    private Nodo izq, der;
    
    public NodoOperador(String operador, Nodo izq, Nodo der){
        this.operador = operador;
        this.izq = izq;
        this.der = der;
    }
    
    @Override
    public Object evaluar(){
        switch(this.operador){
            case "+"    :  return (float)  this.izq.evaluar()  +   (float)  this.der.evaluar();
            case "-"    :  return (float)  this.izq.evaluar()  -   (float)  this.der.evaluar();
            case "*"    :  return (float)  this.izq.evaluar()  *   (float)  this.der.evaluar();
            case "/"    :  return (float)  this.izq.evaluar()  /   (float)  this.der.evaluar();
            case "OR"   :  return (boolean)this.izq.evaluar()  ||  (boolean)this.der.evaluar();
            case "AND"  :  return (boolean)this.izq.evaluar()  &&  (boolean)this.der.evaluar();
            case "="    :  return          this.izq.evaluar()  ==           this.der.evaluar();
            case "<>"   :  return          this.izq.evaluar()  !=           this.der.evaluar();
            case "<"    :  return (float)  this.izq.evaluar()  <   (float)  this.der.evaluar();
            case "<="   :  return (float)  this.izq.evaluar()  <=  (float)  this.der.evaluar();
            case ">"    :  return (float)  this.izq.evaluar()  >   (float)  this.der.evaluar();
            case ">="   :  return (float)  this.izq.evaluar()  >=  (float)  this.der.evaluar();
            default     :  throw new RuntimeException("Operador ilegal...");    
        }
    }      
}



//Seguimos asignando
class NodoAsignación extends Nodo{
    String id;
    char op;
    Nodo der;
    
    public NodoAsignación(char op, String id, Nodo der){
        this.op = op;
        this.id = id;
        this.der = der;
    }
    public Object evaluar(){
        return der.evaluar();
    }
}

}
