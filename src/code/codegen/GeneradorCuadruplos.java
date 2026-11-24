
package codegen;

import ast.Expresion;
import ast.Expresion.Binaria;
import ast.Expresion.Booleano;
import ast.Expresion.Entero;
import ast.Expresion.Identificador;
import ast.Expresion.Real;
import ast.Expresion.Texto;
import ast.Sentencia;
import ast.Sentencia.Asignacion;
import ast.Sentencia.Bloque;
import ast.Sentencia.Compuesta;
import ast.Sentencia.Declaracion;
import ast.Sentencia.If;
import ast.Sentencia.Programa;
import ast.Sentencia.Read;
import ast.Sentencia.While;
import ast.Sentencia.Write;
import java.util.ArrayList;
import java.util.List;

public class GeneradorCuadruplos {

    private final List<Cuadruplo> cuadruplos = new ArrayList<>();

    private int contadorTemporales = 1;
    private int contadorEtiquetas = 1;

    // Genera un nuevo temporal: t1, t2, t3...
    private String nuevoTemporal() {
        return "t" + contadorTemporales++;
    }

    // Genera una nueva etiqueta: L1, L2, L3...
    private String nuevaEtiqueta() {
        return "L" + contadorEtiquetas++;
    }

    // Agrega un cuadruplo a la lista
    private void agregar(String operador, String arg1,
                         String arg2, String resultado) {
        cuadruplos.add(
            new Cuadruplo(operador, arg1, arg2, resultado)
        );
    }

    // Punto de entrada del generador
    public List<Cuadruplo> generar(Sentencia programa) {

        cuadruplos.clear();
        contadorTemporales = 1;
        contadorEtiquetas = 1;

        generarSentencia(programa);

        return List.copyOf(cuadruplos);
    }

    // Procesa las diferentes sentencias del AST
    private void generarSentencia(Sentencia stmt) {

        switch (stmt) {
            case Programa(var nombre, var bloque) ->
                generarSentencia(bloque);
            case Bloque(var declaraciones, var sentencias) -> {
                for (Sentencia sentencia : sentencias) {
                    generarSentencia(sentencia);
                }
            }
            case Declaracion(var ids, var tipo) -> {}
            case Asignacion(var id, var valor) -> {
                String resultado = generarExpresion(valor);
                agregar("=", resultado, "-", id);
            }
            case Compuesta(var sentencias) -> {
                for (Sentencia sentencia : sentencias) {
                    generarSentencia(sentencia);
                }
            }
            case If(var condicion, var entonces, var sino) -> {
                String resultadoCondicion =
                    generarExpresion(condicion);
                String etiquetaFin = nuevaEtiqueta();
                if (sino == null) {
                    agregar("IF_FALSE", resultadoCondicion,
                            "-", etiquetaFin);
                    generarSentencia(entonces);
                } else {
                    String etiquetaSino = nuevaEtiqueta();
                    agregar("IF_FALSE", resultadoCondicion,
                            "-", etiquetaSino);
                    generarSentencia(entonces);
                    agregar("GOTO", "-", "-", etiquetaFin);
                    agregar("LABEL", "-", "-", etiquetaSino);
                    generarSentencia(sino);
                }
                agregar("LABEL", "-", "-", etiquetaFin);
            }
            case While(var condicion, var cuerpo) -> {
                String etiquetaInicio = nuevaEtiqueta();
                String etiquetaFin = nuevaEtiqueta();
                agregar("LABEL", "-", "-", etiquetaInicio);
                String resultadoCondicion =
                    generarExpresion(condicion);
                agregar("IF_FALSE", resultadoCondicion,
                        "-", etiquetaFin);
                generarSentencia(cuerpo);
                agregar("GOTO", "-", "-", etiquetaInicio);
                agregar("LABEL", "-", "-", etiquetaFin);
            }
            case Read(var ids) -> {
                for (String id : ids) {
                    agregar("READ", id, "-", "-");
                }
            }
            case Write(var items) -> {
                for (Expresion item : items) {
                    String resultado = generarExpresion(item);
                    agregar("WRITE", resultado, "-", "-");
                }
            }
        }
    }

    // Procesa expresiones y devuelve su resultado
    private String generarExpresion(Expresion expr) {
        return switch (expr) {
            case Entero(var valor) ->
                String.valueOf(valor);
            case Real(var valor) ->
                String.valueOf(valor);
            case Booleano(var valor) ->
                String.valueOf(valor);
            case Texto(var valor) ->
                valor;
            case Identificador(var nombre) ->
                nombre;
            case Binaria(var operador, var izq, var der) -> {
                // Primero procesamos el lado izquierdo
                String arg1 = generarExpresion(izq);
                // Después procesamos el lado derecho
                String arg2 = generarExpresion(der);
                // Creamos un temporal para guardar el resultado
                String temporal = nuevoTemporal();
                agregar(operador, arg1, arg2, temporal);
                yield temporal;
            }
        };
    }

    // Devuelve una copia de los cuádruplos generados
    public List<Cuadruplo> getCuadruplos() {
        return List.copyOf(cuadruplos);
    }
}
