package semantic;

import java.util.HashMap;
import java.util.Map;

public class Symbol {
    private String name;
    private String tokenType;

    public Symbol(String name, String tokenType) {
        this.name = name;
        this.tokenType = tokenType;
    }

    public String getName() {
        return name;
    }

    public String getTokenType() {
        return tokenType;
    }

    @Override
    public String toString() {
        return "Symbol {name=" + name + ", tokenType=" + tokenType + "}";
    }
}