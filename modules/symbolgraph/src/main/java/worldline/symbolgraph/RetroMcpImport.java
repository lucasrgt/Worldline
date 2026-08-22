package worldline.symbolgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Imports RetroMCP names only after exact official owner/name/descriptor resolution. */
public final class RetroMcpImport {
    public Result apply(SymbolGraph graph, TinyMapping intermediary, TinyMapping retroMcp) {
        requireFirst(intermediary, "intermediary");
        requireFirst(retroMcp, "named");
        Map<SymbolKey, String> clientAliases = new LinkedHashMap<SymbolKey, String>();
        Map<SymbolKey, String> serverAliases = new LinkedHashMap<SymbolKey, String>();
        Set<String> unmatched = new TreeSet<String>();
        importSide(intermediary, retroMcp, "clientOfficial", "client", clientAliases, unmatched);
        importSide(intermediary, retroMcp, "serverOfficial", "server", serverAliases, unmatched);
        Set<SymbolKey> matched = new TreeSet<SymbolKey>(clientAliases.keySet());
        matched.addAll(serverAliases.keySet());
        Set<SymbolKey> differences = new TreeSet<SymbolKey>();
        for (SymbolKey key : clientAliases.keySet()) {
            String server = serverAliases.get(key);
            if (server != null && !clientAliases.get(key).equals(server)) differences.add(key);
        }
        SymbolGraph enriched = graph.withRetroMcp(clientAliases, serverAliases);
        Set<SymbolKey> missing = new TreeSet<SymbolKey>();
        for (SymbolRecord record : enriched.records()) {
            if (record.inventoryPresent() && record.retroMcpClient().isEmpty()
                    && record.retroMcpServer().isEmpty()) missing.add(record.key());
        }
        return new Result(enriched, matched.size(), unmatched, differences, missing);
    }

    private static void importSide(TinyMapping intermediary, TinyMapping retroMcp,
            String intermediaryNamespace, String retroNamespace, Map<SymbolKey, String> aliases,
            Set<String> unmatched) {
        int inventorySide = intermediary.namespace(intermediaryNamespace);
        int retroSide = retroMcp.namespace(retroNamespace);
        Map<String, String> inventoryClasses = classes(intermediary, 0, inventorySide);
        Map<String, String> retroClasses = classes(retroMcp, 0, retroSide);
        Map<OfficialSymbolKey, SymbolKey> officialIndex = index(intermediary, inventorySide, inventoryClasses);
        for (TinySymbol symbol : retroMcp.symbols().values()) {
            String officialName = symbol.name(retroSide);
            if (officialName.isEmpty()) continue;
            OfficialSymbolKey official = officialKey(symbol, officialName, retroClasses);
            if (official == null) continue;
            SymbolKey target = officialIndex.get(official);
            if (target == null) {
                unmatched.add(retroNamespace + "|" + official.canonical());
                continue;
            }
            String prior = aliases.put(target, symbol.name(0));
            if (prior != null && !prior.equals(symbol.name(0))) {
                throw new IllegalArgumentException("ambiguous same-side RetroMCP alias for " + target);
            }
        }
    }

    private static Map<OfficialSymbolKey, SymbolKey> index(TinyMapping mapping, int side,
            Map<String, String> classes) {
        Map<OfficialSymbolKey, SymbolKey> index = new HashMap<OfficialSymbolKey, SymbolKey>();
        for (TinySymbol symbol : mapping.symbols().values()) {
            String officialName = symbol.name(side);
            if (officialName.isEmpty()) continue;
            OfficialSymbolKey official = officialKey(symbol, officialName, classes);
            if (official == null) continue;
            SymbolKey prior = index.put(official, symbol.key());
            if (prior != null && !prior.equals(symbol.key())) {
                throw new IllegalArgumentException("ambiguous official identity: " + official);
            }
        }
        return index;
    }

    private static OfficialSymbolKey officialKey(TinySymbol symbol, String officialName,
            Map<String, String> classes) {
        SymbolKey key = symbol.key();
        if (key.kind() == SymbolKind.CLASS) {
            return new OfficialSymbolKey(key.kind(), "", officialName, "");
        }
        String owner = classes.get(key.owner());
        if (owner == null || owner.isEmpty()) return null;
        return new OfficialSymbolKey(key.kind(), owner, officialName,
                DescriptorNames.remap(key.descriptor(), classes));
    }

    private static Map<String, String> classes(TinyMapping mapping, int source, int target) {
        Map<String, String> classes = new HashMap<String, String>();
        for (TinySymbol symbol : mapping.symbols().values()) {
            if (symbol.key().kind() == SymbolKind.CLASS) {
                classes.put(symbol.name(source), symbol.name(target));
            }
        }
        return classes;
    }

    private static void requireFirst(TinyMapping mapping, String expected) {
        if (mapping.namespaces().isEmpty() || !expected.equals(mapping.namespaces().get(0))) {
            throw new IllegalArgumentException("first namespace must be " + expected);
        }
    }

    public static final class Result {
        private final SymbolGraph graph;
        private final int matched;
        private final Set<String> unmatched;
        private final Set<SymbolKey> nameDifferences;
        private final Set<SymbolKey> missing;

        Result(SymbolGraph graph, int matched, Set<String> unmatched,
                Set<SymbolKey> nameDifferences, Set<SymbolKey> missing) {
            this.graph = graph;
            this.matched = matched;
            this.unmatched = Collections.unmodifiableSet(new TreeSet<String>(unmatched));
            this.nameDifferences = Collections.unmodifiableSet(
                    new TreeSet<SymbolKey>(nameDifferences));
            this.missing = Collections.unmodifiableSet(new TreeSet<SymbolKey>(missing));
        }

        public SymbolGraph graph() { return graph; }
        public int matched() { return matched; }
        public Set<String> unmatched() { return unmatched; }
        public Set<SymbolKey> nameDifferences() { return nameDifferences; }
        public Set<SymbolKey> missing() { return missing; }
    }
}
