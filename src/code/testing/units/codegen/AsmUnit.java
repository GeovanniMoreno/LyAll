package testing.units.codegen;

import codegen.Cuadruplo;
import codegen.GeneradorASM;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import testing.Tester.DispatchTarget;
import testing.units.Result;
import testing.units.Test;

public class AsmUnit extends Test {

    public AsmUnit() {
        super();
        this.pertenencia = DispatchTarget.ASM;
    }

    // Metodo auxiliar para capturar el ensamblador impreso en System.out
    private String compilarAAsm(List<Cuadruplo> cuadruplos) {
        PrintStream stdoutOriginal = System.out;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            System.setOut(new PrintStream(buffer));

            GeneradorASM genAsm = new GeneradorASM();
            genAsm.generar(cuadruplos);

            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(stdoutOriginal);
        }
    }

    private Result test_estructuraBaseSinVariables() {
        String asm = compilarAAsm(List.of());

        boolean ok = !asm.contains("section .bss")
                  && asm.contains("section .text")
                  && asm.contains("global _start")
                  && asm.contains("_start:")
                  && asm.contains("mov rax, 60")
                  && asm.contains("xor rdi, rdi")
                  && asm.contains("syscall");

        return ok
            ? new Result("Estructura .text, _start y syscall de salida generados", true)
            : new Result("Falta encabezado o salida del sistema en ASM:\n" + asm, false);
    }

    private Result test_seccionBssYSanitizacionDeNombres() {
        List<Cuadruplo> quads = List.of(
            new Cuadruplo("+", "total_ventas", "10", "t1"),
            new Cuadruplo("=", "true", "-", "flag")
        );
        String asm = compilarAAsm(quads);

        boolean ok = asm.contains("section .bss")
                  && asm.contains("v_TOTAL_VENTAS resq 1")
                  && asm.contains("v_T1 resq 1")
                  && asm.contains("v_FLAG resq 1")
                  && !asm.contains("v_10")
                  && !asm.contains("v_TRUE");

        return ok
            ? new Result("Variables y temporales declarados en .bss excluyendo literales", true)
            : new Result("Error en declaracion de variables .bss:\n" + asm, false);
    }

    private Result test_operacionesAritmeticasYDivision() {
        List<Cuadruplo> quads = List.of(
            new Cuadruplo("+", "a", "5", "t1"),
            new Cuadruplo("/", "t1", "2", "res")
        );
        String asm = compilarAAsm(quads);

        boolean sumaOk = asm.contains("mov rax, [rel v_A]")
                      && asm.contains("mov rbx, 5")
                      && asm.contains("add rax, rbx")
                      && asm.contains("mov [rel v_T1], rax");

        boolean divOk = asm.contains("cqo")
                     && asm.contains("idiv rbx")
                     && asm.contains("mov [rel v_RES], rax");

        return (sumaOk && divOk)
            ? new Result("Instrucciones add, cqo e idiv emitidas correctamente", true)
            : new Result("Traduccion aritmetica incorrecta:\n" + asm, false);
    }

    private Result test_comparacionesYOperadoresLogicos() {
        List<Cuadruplo> quads = List.of(
            new Cuadruplo("<", "a", "b", "t1"),
            new Cuadruplo("AND", "t1", "true", "t2")
        );
        String asm = compilarAAsm(quads);

        boolean compOk = asm.contains("cmp rax, rbx")
                      && asm.contains("setl al")
                      && asm.contains("movzx rax, al");

        boolean logicOk = asm.contains("and rax, rbx")
                       && asm.contains("test rax, rax")
                       && asm.contains("setne al");

        return (compOk && logicOk)
            ? new Result("Comparaciones (cmp/setl) y logica (and/setne) generadas", true)
            : new Result("Error en traduccion relacional o logica:\n" + asm, false);
    }

    private Result test_saltosCondicionalesIncondicionalesYEtiquetas() {
        List<Cuadruplo> quads = List.of(
            new Cuadruplo("LABEL", "-", "-", "L1"),
            new Cuadruplo("IF_FALSE", "cond", "-", "L2"),
            new Cuadruplo("GOTO", "-", "-", "L1"),
            new Cuadruplo("LABEL", "-", "-", "L2")
        );
        String asm = compilarAAsm(quads);

        boolean ok = asm.contains("L1:")
                  && asm.contains("mov rax, [rel v_COND]")
                  && asm.contains("test rax, rax")
                  && asm.contains("jz L2")
                  && asm.contains("jmp L1")
                  && asm.contains("L2:");

        return ok
            ? new Result("Etiquetas, saltos jz y jmp traducidos correctamente", true)
            : new Result("Error en saltos o etiquetas ASM:\n" + asm, false);
    }

    private Result test_instruccionesPendientesEmitenComentarios() {
        List<Cuadruplo> quads = List.of(
            new Cuadruplo("=", "3.14", "-", "pi"),
            new Cuadruplo("READ", "x", "-", "-"),
            new Cuadruplo("WRITE", "x", "-", "-")
        );
        String asm = compilarAAsm(quads);

        boolean ok = asm.contains("; REAL pendiente: 3.14")
                  && asm.contains("; READ pendiente: x")
                  && asm.contains("; WRITE pendiente: x");

        return ok
            ? new Result("Operaciones pendientes (REAL, READ, WRITE) comentadas sin fallar", true)
            : new Result("Faltan comentarios de operaciones pendientes:\n" + asm, false);
    }
}