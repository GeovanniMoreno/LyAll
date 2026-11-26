package testing.units;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import testing.Tester.DispatchTarget;

public abstract class Test {
    protected String nombre;
    protected DispatchTarget pertenencia;
    private final String TEST_PREFIX = "test_";

    public Test(){
        this.nombre = Test.class.getSimpleName();
    }

    public List<Result> ejecutar() {
        var tests = getTests();
        List<Result> results = new ArrayList<>();

        for (Method test : tests) {
            test.setAccessible(true);
            Result result;
            try {
                result = (Result) test.invoke(this);
            } catch (InvocationTargetException e) {
                Throwable causa = e.getCause();
                result = new Result("Excepcion inesperada: " + causa.getClass().getSimpleName() + " - " + causa.getMessage(), false);
            } catch (Exception e) {
                result = new Result("Error de reflexion: " + e.getMessage(), false);
            }
            result.fuente = test.getName().replaceFirst("^" + TEST_PREFIX, "");
            result.modulo = this.pertenencia;
            results.add(result);
        }
        return results;
    }

    protected List<Method> getTests(){
        Method[] methods = this.getClass().getDeclaredMethods();
        List<Method> tests = new ArrayList<>();

        for (Method method : methods) {
            if(method.getName().startsWith(TEST_PREFIX))
                tests.add(method);
        }

        return tests;
    }

}
