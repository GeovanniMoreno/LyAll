package codegen;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GeneradorASM {
    private Set<String> variables = new LinkedHashSet<>();

    // Generador principal
    public void generar(List<Cuadruplo> cuadruplos) {
        variables.clear();

        // Identificar variables y temporales
        for (Cuadruplo c : cuadruplos) {
            switch (c.operador()) {
                case "+":
                case "-":
                case "*":
                case "/":
                case "<":
                case ">":
                case "<=":
                case ">=":
                case "<>":
                case "AND":
                case "OR":
                    registrar(c.arg1());
                    registrar(c.arg2());
                    registrar(c.resultado());
                    break;
                case "=":
                    registrar(c.arg1());
                    registrar(c.arg2());
                    registrar(c.resultado());
                    break;
                case "READ":
                    registrar(c.arg1());
                    break;
                case "WRITE":
                    if (!c.arg1().startsWith("'")
                            && !c.arg1().startsWith("\"")) {
                        registrar(c.arg1());
                    }
                    break;
                case "IF_FALSE":
                    registrar(c.arg1());
                    break;
                case "GOTO":
                case "LABEL":
                    break;
            }
        }

        // Declarar variables
        if (!variables.isEmpty()) {
            System.out.println("section .bss");
            for (String variable : variables) {
                System.out.println(
                    "    " + nombreASM(variable) + " resq 1"
                );
            }
        }

        // Inicio del programa
        System.out.println("\nsection .text");
        System.out.println("global _start");
        System.out.println("\n_start:");

        // Traducir cuádruplos
        for (Cuadruplo c : cuadruplos) {
            generarCuadruplo(c);
        }

        // Finalizar programa
        System.out.println("\n    mov rax, 60");
        System.out.println("    xor rdi, rdi");
        System.out.println("    syscall");
    }

    // Traducir cada operador
    private void generarCuadruplo(Cuadruplo c) {
        String op = c.operador();
        switch (op) {
            case "+":
                cargar(c.arg1(), "rax");
                cargar(c.arg2(), "rbx");
                System.out.println("    add rax, rbx");
                guardar(c.resultado(), "rax");
                break;
            case "-":
                cargar(c.arg1(), "rax");
                cargar(c.arg2(), "rbx");
                System.out.println("    sub rax, rbx");
                guardar(c.resultado(), "rax");
                break;
            case "*":
                cargar(c.arg1(), "rax");
                cargar(c.arg2(), "rbx");
                System.out.println("    imul rax, rbx");
                guardar(c.resultado(), "rax");
                break;
            case "/":
                cargar(c.arg1(), "rax");
                cargar(c.arg2(), "rbx");
                System.out.println("    cqo");
                System.out.println("    idiv rbx");
                guardar(c.resultado(), "rax");
                break;
            case "=":
                if (c.arg2().equals("-")) {
                    cargar(c.arg1(), "rax");
                    guardar(c.resultado(), "rax");
                } else {
                    comparar(c, "sete");
                }
                break;
            case "<":
                comparar(c, "setl");
                break;
            case ">":
                comparar(c, "setg");
                break;
            case "<=":
                comparar(c, "setle");
                break;
            case ">=":
                comparar(c, "setge");
                break;
            case "<>":
                comparar(c, "setne");
                break;
            case "AND":
                logica(c, "and");
                break;
            case "OR":
                logica(c, "or");
                break;
            case "IF_FALSE":
                cargar(c.arg1(), "rax");
                System.out.println("    test rax, rax");
                System.out.println("    jz " + c.resultado());
                break;
            case "GOTO":
                System.out.println("    jmp " + c.resultado());
                break;
            case "LABEL":
                System.out.println(c.resultado() + ":");
                break;
            case "READ":
                System.out.println("    ; READ pendiente: " + c.arg1());
                break;
            case "WRITE":
                System.out.println("    ; WRITE pendiente: " + c.arg1());
                break;
            default:
                System.out.println("    ; Operador no implementado: " + op);
                break;
        }
    }

    // Comparaciones
    private void comparar(Cuadruplo c, String instruccion) {
        cargar(c.arg1(), "rax");
        cargar(c.arg2(), "rbx");
        System.out.println("    cmp rax, rbx");
        System.out.println("    " + instruccion + " al");
        System.out.println("    movzx rax, al");
        guardar(c.resultado(), "rax");
    }

    // Operaciones lógicas
    private void logica(Cuadruplo c, String instruccion) {
        cargar(c.arg1(), "rax");
        cargar(c.arg2(), "rbx");
        System.out.println("    " + instruccion + " rax, rbx");
        System.out.println("    test rax, rax");
        System.out.println("    setne al");
        System.out.println("    movzx rax, al");
        guardar(c.resultado(), "rax");
    }

    // Cargar un valor en un registro
    private void cargar(String valor, String registro) {
        if (valor.matches("-?\\d+")) {
            System.out.println("    mov " + registro + ", " + valor);
        } else if (valor.equalsIgnoreCase("true")) {
            System.out.println("    mov " + registro + ", 1");
        } else if (valor.equalsIgnoreCase("false")) {
            System.out.println("    mov " + registro + ", 0");
        } else if (valor.matches("-?\\d+\\.\\d+")) {
            System.out.println("    ; REAL pendiente: " + valor);
        } else {
            System.out.println(
                "    mov " + registro + ", [rel " + nombreASM(valor) + "]"
            );
        }
    }

    // Guardar resultado
    private void guardar(String variable, String registro) {
        System.out.println(
            "    mov [rel " + nombreASM(variable) + "], " + registro
        );
    }

    // Registrar variables y temporales
    private void registrar(String valor) {
        if (valor == null || valor.equals("-")) {
            return;
        }
        if (valor.matches("-?\\d+")
                || valor.equalsIgnoreCase("true")
                || valor.equalsIgnoreCase("false")
                || valor.matches("-?\\d+\\.\\d+")) {
            return;
        }
        variables.add(valor);
    }
    // Convertir nombre a identificador ASM
    private String nombreASM(String nombre) {
        return "v_" + nombre.toUpperCase()
                .replaceAll("[^A-Z0-9_]", "_");
    }
}
