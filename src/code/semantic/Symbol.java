package semantic;

public class Symbol {
    private final String name;
    private final String tokenType;
    private Object value;

    public Symbol(String name, String tokenType) {
        this.name = name;
        this.tokenType = tokenType;
        this.value = null;
    }

    public String getName() {
        return name;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Symbol {name=" + name + ", tokenType=" + tokenType + ", value=" + value + "}";
    }
}