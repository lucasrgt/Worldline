package worldline.symbolgraph;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Exact first-namespace set comparison; aliases never alter symbol identity. */
public final class MappingAudit {
    public Report compare(TinyMapping inventory, TinyMapping named) {
        requireIntermediary(inventory);
        requireIntermediary(named);
        Map<SymbolKind, Difference> differences = new EnumMap<SymbolKind, Difference>(SymbolKind.class);
        for (SymbolKind kind : SymbolKind.values()) {
            Set<SymbolKey> inventoryKeys = keys(inventory, kind);
            Set<SymbolKey> namedKeys = keys(named, kind);
            Set<SymbolKey> missing = new TreeSet<SymbolKey>(inventoryKeys);
            missing.removeAll(namedKeys);
            Set<SymbolKey> extra = new TreeSet<SymbolKey>(namedKeys);
            extra.removeAll(inventoryKeys);
            differences.put(kind, new Difference(inventoryKeys.size(), namedKeys.size(), missing, extra));
        }
        return new Report(differences);
    }

    private static void requireIntermediary(TinyMapping mapping) {
        if (mapping.namespaces().isEmpty() || !"intermediary".equals(mapping.namespaces().get(0))) {
            throw new IllegalArgumentException("first namespace must be intermediary");
        }
    }

    private static Set<SymbolKey> keys(TinyMapping mapping, SymbolKind kind) {
        Set<SymbolKey> keys = new TreeSet<SymbolKey>();
        for (SymbolKey key : mapping.symbols().keySet()) if (key.kind() == kind) keys.add(key);
        return keys;
    }

    public static final class Difference {
        private final int inventoryCount;
        private final int namedCount;
        private final Set<SymbolKey> missing;
        private final Set<SymbolKey> extra;

        Difference(int inventoryCount, int namedCount, Set<SymbolKey> missing, Set<SymbolKey> extra) {
            this.inventoryCount = inventoryCount;
            this.namedCount = namedCount;
            this.missing = Collections.unmodifiableSet(new TreeSet<SymbolKey>(missing));
            this.extra = Collections.unmodifiableSet(new TreeSet<SymbolKey>(extra));
        }

        public int inventoryCount() { return inventoryCount; }
        public int namedCount() { return namedCount; }
        public Set<SymbolKey> missing() { return missing; }
        public Set<SymbolKey> extra() { return extra; }
    }

    public static final class Report {
        private final Map<SymbolKind, Difference> differences;

        Report(Map<SymbolKind, Difference> differences) {
            this.differences = Collections.unmodifiableMap(
                    new EnumMap<SymbolKind, Difference>(differences));
        }

        public Difference difference(SymbolKind kind) { return differences.get(kind); }

        public String render() {
            StringBuilder text = new StringBuilder("kind\tinventory\tnamed\tmissing\textra\n");
            for (SymbolKind kind : SymbolKind.values()) {
                Difference value = difference(kind);
                text.append(kind.name().toLowerCase()).append('\t')
                        .append(value.inventoryCount()).append('\t').append(value.namedCount()).append('\t')
                        .append(value.missing().size()).append('\t').append(value.extra().size()).append('\n');
            }
            return text.toString();
        }
    }
}
