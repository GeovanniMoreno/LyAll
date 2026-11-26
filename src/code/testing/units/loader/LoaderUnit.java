package testing.units.loader;

import java.util.List;
import java.lang.reflect.InvocationTargetException;

import lexer.Loader;
import testing.units.Test;
import testing.units.Result;
import testing.Tester.DispatchTarget;



public class LoaderUnit extends Test {
    String path;
    List<Result> resultados;

    private enum Codigo {
        FUENTE,
        CUADRUPLOS,
    }

    private String getPathFromCode(Codigo codigo){
        return switch(codigo) {
            case FUENTE -> "src/tests/Fuente.pas";
            case CUADRUPLOS -> "src/tests/Cuadruplos.pas";
        };
    }


    public LoaderUnit() throws IllegalAccessException, InvocationTargetException{
        super();
        this.path = getPathFromCode(Codigo.FUENTE);
        this.pertenencia = DispatchTarget.LOADER; 
    }  

    private Result test_leerCodigo(){
        String descripcion = "Lectura y carga exitosa";
        boolean pasado = true;

        String salida = Loader.read(path);

        if (salida == null){
            descripcion = "Error en la busqueda o lectura del archivo: " + path;
            pasado = false;
        }

        return new Result(descripcion, pasado);
    }
}
