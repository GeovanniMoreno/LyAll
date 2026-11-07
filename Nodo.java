package ConPascal;

//import ConPascal.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base abstracta para los nodos del Árbol de Sintaxis Abstracta (AST).
 */
public abstract class Nodo {
    public abstract Object evaluar();
}

// (1) Nodo Raíz del Programa
class NodoPrograma extends Nodo {
    private String nombre;
    private Nodo bloque;

    public NodoPrograma(String nombre, Nodo bloque) {
        this.nombre = nombre;
        this.bloque = bloque;
    }

    @Override
    public Object evaluar() {
        if(bloque != null) {
            return bloque.evaluar();
        }
        return null;
    }
}

// (2) Nodo Bloque
class NodoBlock extends Nodo {
    private Nodo declaracion;
    private Nodo stament;

    public NodoBlock(Nodo declaracion, Nodo stament) {
        this.declaracion = declaracion;
        this.stament = stament;
    }

    @Override
    public Object evaluar() {
        if(declaracion != null) declaracion.evaluar();
        if(stament != null) return stament.evaluar();
        return null;
    }
}

// (3) Nodo Parte de Declaración
class NodoDeclPart extends Nodo {
    private Nodo listaVariables;

    public NodoDeclPart(Nodo listaVariables) {
        this.listaVariables = listaVariables;
    }
    
    @Override
    public Object evaluar() {
        if(listaVariables != null){
            return listaVariables.evaluar();
        }
        return null;
    }
}

// (4) Nodo Lista de Declaraciones
class NodoDeclList extends Nodo {
    private Nodo decl;
    private Nodo dL;

    public NodoDeclList(Nodo decl, Nodo dL) {
        this.decl = decl;
        this.dL = dL;
    }

    @Override
    public Object evaluar() {
        if(decl != null) decl.evaluar();
        if(dL != null) dL.evaluar();
        return null;
    }
}

// (5) Nodo Declaración
class NodoDeclaration extends Nodo {
    private Nodo identLis;
    private Nodo type;

    public NodoDeclaration(Nodo identLis, Nodo type) {
        this.identLis = identLis;
        this.type = type;
    }

    @Override
    public Object evaluar() {
        return identLis.evaluar();
    }
}

// (6) Nodo Lista de Identificadores (Retorna List<String>)
class NodoIdenList extends Nodo {
    private Nodo iden;
    private Nodo restoLista;

    public NodoIdenList(Nodo iden, Nodo restoLista) {
        this.iden = iden;
        this.restoLista = restoLista;
    }

    @Override
    public Object evaluar() {
        List<String> lista = new ArrayList<>();
        lista.add((String) iden.evaluar());
        if(restoLista != null){
            Object res = restoLista.evaluar();
            if(res instanceof List){
                lista.addAll((List<String>) res);
            }else if(res instanceof String){
                lista.add((String) res);
            }
        }
        return lista;
    }
}

// Nodo Identificador
class NodoIdentifier extends Nodo {
    private String lexema;

    public NodoIdentifier(String lexema) {
        this.lexema = lexema;
    }

    @Override
    public Object evaluar() {
        return this.lexema;
    }
}

// (7) Nodo Tipo de Dato
class NodoType extends Nodo {
    private TokenType type;

    public NodoType(TokenType type) {
        this.type = type;
    }

    @Override
    public Object evaluar() {
        return this.type;
    }
}

// (8) Nodo Parte de Sentencias
class NodoStamentPart extends Nodo {
    private Nodo sl;

    public NodoStamentPart(Nodo sl) {
        this.sl = sl;
    }

    @Override
    public Object evaluar() {
        if(sl != null){
            sl.evaluar();
        }
        return null;
    }
}

// (9) Nodo Lista de Sentencias
class NodoStmtLis extends Nodo {
    private Nodo stmt;
    private Nodo sl;

    public NodoStmtLis(Nodo stmt, Nodo sl) {
        this.stmt = stmt;
        this.sl = sl;
    }

    @Override
    public Object evaluar() {
        if(stmt != null) stmt.evaluar();
        if(sl != null) sl.evaluar();
        return null;
    }
}

// (10) Nodo Sentencia Genérica
class NodoStatement extends Nodo {
    private Nodo stmt;

    public NodoStatement(Nodo stmt) {
        this.stmt = stmt;
    }

    @Override
    public Object evaluar() {
        if(stmt != null){
            stmt.evaluar();
        }
        return null;
    }
}

// (11) Nodo Asignación
class NodoAsignación extends Nodo {
    private char op;
    private String id;
    private Nodo der;

    public NodoAsignación(char op, String id, Nodo der) {
        this.op = op;
        this.id = id;
        this.der = der;
    }

    @Override
    public Object evaluar() {
        if(der != null){
            der.evaluar();
        }
        return null;
    }
}

// (12) Nodo IF
class NodoIf extends Nodo {
    private Nodo condicion, entoncesStmt, sinoStmt;

    public NodoIf(Nodo c, Nodo e, Nodo s) {
        this.condicion = c;
        this.entoncesStmt = e;
        this.sinoStmt = s;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}

// (13) Nodo WHILE
class NodoWhile extends Nodo {
    private Nodo condicion, cuerpo;

    public NodoWhile(Nodo c, Nodo b) {
        this.condicion = c;
        this.cuerpo = b;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}

// (15) Nodo READ / INPUT
class NodoRead extends Nodo {
    private Nodo ids;

    public NodoRead(Nodo ids) {
        this.ids = ids;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}

// (16) Nodo WRITE / PRINT
class NodoWrite extends Nodo {
    private Nodo lista;

    public NodoWrite(Nodo lista) {
        this.lista = lista;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}

// (17) Nodo Lista de Items a Imprimir
class NodoWriteList extends Nodo {
    private Nodo item, resto;

    public NodoWriteList(Nodo item, Nodo resto) {
        this.item = item;
        this.resto = resto;
    }

    @Override
    public Object evaluar() {
        return null;
    }
}

// (18) Nodo Texto Literal
class NodoTexto extends Nodo {
    private String texto;

    public NodoTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public Object evaluar() {
        return this.texto;
    }
}

// Nodo Numérico
class NodoNumérico extends Nodo {
    private float valor;

    public NodoNumérico(float valor) {
        this.valor = valor;
    }

    @Override
    public Object evaluar() {
        return this.valor;
    }
}

// Operaciones Matemáticas y Lógicas (Reglas 19 a 25)
class NodoOperador extends Nodo {
    private String operador;
    private Nodo izq, der;

    public NodoOperador(String operador, Nodo izq, Nodo der) {
        this.operador = operador;
        this.izq = izq;
        this.der = der;
    }

    @Override
    public Object evaluar() {
        switch (this.operador) {
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