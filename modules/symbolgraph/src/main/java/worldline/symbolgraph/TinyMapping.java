package worldline.symbolgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TinyMapping {
    private final List<String> namespaces;
    private final Map<SymbolKey, TinySymbol> symbols;

    TinyMapping(List<String> namespaces, List<TinySymbol> symbols) {
        this.namespaces = Collections.unmodifiableList(new ArrayList<String>(namespaces));
        Map<SymbolKey, TinySymbol> indexed = new LinkedHashMap<SymbolKey, TinySymbol>();
        for (TinySymbol symbol : symbols) {
            if (indexed.put(symbol.key(), symbol) != null) {
                throw new IllegalArgumentException("duplicate symbol: " + symbol.key());
            }
        }
        this.symbols = Collections.unmodifiableMap(indexed);
    }

    public List<String> namespaces() { return namespaces; }
    public Map<SymbolKey, TinySymbol> symbols() { return symbols; }

    public int namespace(String name) {
        int index = namespaces.indexOf(name);
        if (index < 0) throw new IllegalArgumentException("unknown namespace: " + name);
        return index;
    }

    public int count(SymbolKind kind) {
        int count = 0;
        for (SymbolKey key : symbols.keySet()) if (key.kind() == kind) count++;
        return count;
    }

    public Map<SymbolKind, Integer> counts() {
        Map<SymbolKind, Integer> counts = new EnumMap<SymbolKind, Integer>(SymbolKind.class);
        for (SymbolKind kind : SymbolKind.values()) counts.put(kind, Integer.valueOf(count(kind)));
        return Collections.unmodifiableMap(counts);
    }
}
