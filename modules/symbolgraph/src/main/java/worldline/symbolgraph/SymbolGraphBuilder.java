package worldline.symbolgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Joins public namespaces without treating any external name as semantic proof. */
public final class SymbolGraphBuilder {
    public SymbolGraph build(TinyMapping inventory, TinyMapping nostalgia) {
        requireFirstNamespace(inventory);
        requireFirstNamespace(nostalgia);
        int client = inventory.namespace("clientOfficial");
        int server = inventory.namespace("serverOfficial");
        int named = nostalgia.namespace("named");
        Set<SymbolKey> keys = new TreeSet<SymbolKey>();
        keys.addAll(inventory.symbols().keySet());
        keys.addAll(nostalgia.symbols().keySet());
        List<SymbolRecord> records = new ArrayList<SymbolRecord>();
        for (SymbolKey key : keys) {
            TinySymbol official = inventory.symbols().get(key);
            TinySymbol external = nostalgia.symbols().get(key);
            records.add(new SymbolRecord(key,
                    alias(official, client), alias(official, server), alias(external, named),
                    "", "", official != null, external != null));
        }
        return new SymbolGraph(records);
    }

    private static String alias(TinySymbol symbol, int namespace) {
        return symbol == null ? "" : symbol.name(namespace);
    }

    private static void requireFirstNamespace(TinyMapping mapping) {
        List<String> namespaces = mapping.namespaces();
        if (namespaces.isEmpty() || !"intermediary".equals(namespaces.get(0))) {
            throw new IllegalArgumentException("first namespace must be intermediary");
        }
    }
}
