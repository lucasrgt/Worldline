package worldline.symbolgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Compares official class-file identities with one Calamus official namespace. */
public final class OfficialBytecodeAudit {
    public Report compare(OfficialJarInventory official, TinyMapping intermediary,
            String officialNamespace) {
        if (!"intermediary".equals(intermediary.namespaces().get(0))) {
            throw new IllegalArgumentException("first namespace must be intermediary");
        }
        int side = intermediary.namespace(officialNamespace);
        Map<String, String> classes = classAliases(intermediary, side);
        Set<OfficialSymbolKey> mapped = new TreeSet<OfficialSymbolKey>();
        for (TinySymbol symbol : intermediary.symbols().values()) {
            String alias = symbol.name(side);
            if (alias.isEmpty()) continue;
            OfficialSymbolKey key = key(symbol, alias, classes);
            if (key != null && !mapped.add(key)) {
                throw new IllegalArgumentException("ambiguous mapped official symbol: " + key);
            }
        }
        Set<OfficialSymbolKey> missing = new TreeSet<OfficialSymbolKey>(official.symbols());
        missing.removeAll(mapped);
        Set<OfficialSymbolKey> phantom = new TreeSet<OfficialSymbolKey>(mapped);
        phantom.removeAll(official.symbols());
        Set<OfficialSymbolKey> descriptorCandidates = descriptorCandidates(missing, mapped);
        return new Report(official.counts(), counts(mapped), missing, phantom, descriptorCandidates);
    }

    private static OfficialSymbolKey key(TinySymbol symbol, String alias, Map<String, String> classes) {
        SymbolKey key = symbol.key();
        if (key.kind() == SymbolKind.CLASS) return new OfficialSymbolKey(key.kind(), "", alias, "");
        String owner = classes.get(key.owner());
        if (owner == null || owner.isEmpty()) return null;
        return new OfficialSymbolKey(key.kind(), owner, alias,
                DescriptorNames.remap(key.descriptor(), classes));
    }

    private static Map<String, String> classAliases(TinyMapping mapping, int side) {
        Map<String, String> classes = new HashMap<String, String>();
        for (TinySymbol symbol : mapping.symbols().values()) {
            if (symbol.key().kind() == SymbolKind.CLASS) classes.put(symbol.name(0), symbol.name(side));
        }
        return classes;
    }

    private static Set<OfficialSymbolKey> descriptorCandidates(Set<OfficialSymbolKey> missing,
            Set<OfficialSymbolKey> mapped) {
        Set<String> mappedNames = new TreeSet<String>();
        for (OfficialSymbolKey key : mapped) mappedNames.add(nameIdentity(key));
        Set<OfficialSymbolKey> conflicts = new TreeSet<OfficialSymbolKey>();
        for (OfficialSymbolKey key : missing) {
            if (key.kind() != SymbolKind.CLASS && mappedNames.contains(nameIdentity(key))) conflicts.add(key);
        }
        return conflicts;
    }

    private static String nameIdentity(OfficialSymbolKey key) {
        return key.kind().name() + "|" + key.owner() + "|" + key.name();
    }

    private static Map<SymbolKind, Integer> counts(Set<OfficialSymbolKey> symbols) {
        Map<SymbolKind, Integer> counts = new java.util.EnumMap<SymbolKind, Integer>(SymbolKind.class);
        for (SymbolKind kind : SymbolKind.values()) counts.put(kind, Integer.valueOf(0));
        for (OfficialSymbolKey key : symbols) {
            counts.put(key.kind(), Integer.valueOf(counts.get(key.kind()).intValue() + 1));
        }
        return counts;
    }

    public static final class Report {
        private final Map<SymbolKind, Integer> official;
        private final Map<SymbolKind, Integer> mapped;
        private final Set<OfficialSymbolKey> missing;
        private final Set<OfficialSymbolKey> phantom;
        private final Set<OfficialSymbolKey> descriptorCandidates;

        Report(Map<SymbolKind, Integer> official, Map<SymbolKind, Integer> mapped,
                Set<OfficialSymbolKey> missing, Set<OfficialSymbolKey> phantom,
                Set<OfficialSymbolKey> descriptorCandidates) {
            this.official = Collections.unmodifiableMap(official);
            this.mapped = Collections.unmodifiableMap(mapped);
            this.missing = Collections.unmodifiableSet(new TreeSet<OfficialSymbolKey>(missing));
            this.phantom = Collections.unmodifiableSet(new TreeSet<OfficialSymbolKey>(phantom));
            this.descriptorCandidates = Collections.unmodifiableSet(
                    new TreeSet<OfficialSymbolKey>(descriptorCandidates));
        }

        public int official(SymbolKind kind) { return official.get(kind).intValue(); }
        public int mapped(SymbolKind kind) { return mapped.get(kind).intValue(); }
        public int missing(SymbolKind kind) { return count(missing, kind); }
        public int phantom(SymbolKind kind) { return count(phantom, kind); }
        public int descriptorConflictCandidates() { return descriptorCandidates.size(); }
        Set<OfficialSymbolKey> missingSymbols() { return missing; }

        public Map<OfficialSymbolKey, OfficialGapKind> gaps() {
            Set<String> missingOwners = new TreeSet<String>();
            for (OfficialSymbolKey key : missing)
                if (key.kind() == SymbolKind.CLASS) missingOwners.add(key.name());
            Map<OfficialSymbolKey, OfficialGapKind> gaps =
                    new LinkedHashMap<OfficialSymbolKey, OfficialGapKind>();
            for (OfficialSymbolKey key : missing) gaps.put(key, classifyGap(key, missingOwners));
            return Collections.unmodifiableMap(gaps);
        }

        public Map<OfficialGapKind, Integer> gapCounts() {
            Map<OfficialGapKind, Integer> counts =
                    new java.util.EnumMap<OfficialGapKind, Integer>(OfficialGapKind.class);
            for (OfficialGapKind kind : OfficialGapKind.values()) counts.put(kind, Integer.valueOf(0));
            for (OfficialGapKind kind : gaps().values()) {
                counts.put(kind, Integer.valueOf(counts.get(kind).intValue() + 1));
            }
            return Collections.unmodifiableMap(counts);
        }

        public String render(String side) {
            StringBuilder text = new StringBuilder("side\tkind\tofficial\tmapped\tmissing\tphantom\n");
            for (SymbolKind kind : SymbolKind.values()) {
                text.append(side).append('\t').append(kind.name().toLowerCase()).append('\t')
                        .append(official(kind)).append('\t').append(mapped(kind)).append('\t')
                        .append(missing(kind)).append('\t').append(phantom(kind)).append('\n');
            }
            text.append("descriptorConflictCandidates\t")
                    .append(descriptorConflictCandidates()).append('\n');
            for (Map.Entry<OfficialGapKind, Integer> entry : gapCounts().entrySet()) {
                text.append("gap.").append(entry.getKey().name()).append('\t')
                        .append(entry.getValue()).append('\n');
            }
            return text.toString();
        }

        private static OfficialGapKind classifyGap(OfficialSymbolKey key, Set<String> missingOwners) {
            if (key.kind() == SymbolKind.CLASS) return OfficialGapKind.OFFICIAL_ONLY_CLASS;
            if (missingOwners.contains(key.owner())) return OfficialGapKind.OFFICIAL_ONLY_OWNER_MEMBER;
            if ("<init>".equals(key.name())) return OfficialGapKind.CONSTRUCTOR;
            if ("<clinit>".equals(key.name())) return OfficialGapKind.CLASS_INITIALIZER;
            if (key.kind() == SymbolKind.FIELD) return OfficialGapKind.UNMAPPED_FIELD;
            return OfficialGapKind.UNMAPPED_METHOD;
        }

        private static int count(Set<OfficialSymbolKey> symbols, SymbolKind kind) {
            int count = 0;
            for (OfficialSymbolKey key : symbols) if (key.kind() == kind) count++;
            return count;
        }
    }
}
