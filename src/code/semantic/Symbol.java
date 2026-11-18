package semantic;

public class Symbol {
    private final String name;
    private final String tokenType;
    private Object value; // Valor en tiempo de ejecucion (Interprete)

    // --- Metadatos para Analisis Semantico y Optimizacion ---
    private boolean usada = false;
    private int escrituras = 0;
    private Object valorConstante = null;
    private boolean esConstanteCompilacion = false;

    public Symbol(String name, String tokenType) {
        this.name = name;
        this.tokenType = tokenType;
    }

    public String getName() { return name; }
    public String getTokenType() { return tokenType; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public void registrarLectura() {
        this.usada = true;
    }

    public void registrarEscritura(boolean esLiteralConstante, Object valorCalculado) {
        this.escrituras++;
        if (this.escrituras == 1 && esLiteralConstante) {
            this.esConstanteCompilacion = true;
            this.valorConstante = valorCalculado;
        } else {
            // Si se modifica mas de una vez o depende de entrada dinamica (READ), pierde su estado constante
            this.esConstanteCompilacion = false;
            this.valorConstante = null;
        }
    }

    public boolean isUsada() { return usada; }
    public int getEscrituras() { return escrituras; }
    public boolean isConstanteCompilacion() { return esConstanteCompilacion; }
    public Object getValorConstante() { return valorConstante; }

    @Override
    public String toString() {
        return String.format(
            "Symbol {name=%s, type=%s, value=%s, usada=%b, escrituras=%d, const=%s}",
            name, tokenType, value, usada, escrituras,
            esConstanteCompilacion ? valorConstante : "N/A"
        );
    }
}