package worldline.atlas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import worldline.extension.ExtensionCapabilities;
import worldline.semantics.SemanticCatalog;

/** Immutable Atlas index. Construction validates the closed schema. */
public final class AtlasStore {
    private final List<AtlasRecord> records;
    private final Map<String, AtlasRecord> byId;
    private final String canonical;

    private AtlasStore(List<AtlasRecord> records, String canonical) {
        this.records = records;
        this.byId = index(records);
        this.canonical = canonical;
    }

    public static AtlasStore standard(Path root) {
        return AtlasSources.load(root);
    }

    /** Loads the canonical repository Atlas and discovers extensions from another project root. */
    public static AtlasStore standard(Path root, Path extensionRoot) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = AtlasStore.class.getClassLoader();
        return standard(root, extensionRoot, loader, ExtensionCapabilities.of(
                ExtensionCapabilities.TESTKIT_V1, ExtensionCapabilities.ATLAS_V1,
                ExtensionCapabilities.CUSTOM_CONTRACT_V1));
    }

    /** Explicit discovery overload for isolated external class loaders and capability hosts. */
    public static AtlasStore standard(Path root, Path extensionRoot, ClassLoader loader,
            ExtensionCapabilities capabilities) {
        return AtlasSources.load(root, extensionRoot, loader, capabilities);
    }

    public static AtlasStore of(List<AtlasRecord> records, SemanticCatalog catalog, Path root) {
        if (records == null || records.isEmpty()) throw new IllegalArgumentException("records");
        List<AtlasRecord> copy = new ArrayList<AtlasRecord>();
        for (AtlasRecord record : records) {
            if (record == null) throw new NullPointerException("record");
            copy.add(record);
        }
        Collections.sort(copy, new Comparator<AtlasRecord>() {
            @Override public int compare(AtlasRecord left, AtlasRecord right) {
                return left.id().compareTo(right.id());
            }
        });
        List<AtlasRecord> frozen = Collections.unmodifiableList(copy);
        AtlasValidator.validate(frozen, catalog, root);
        return new AtlasStore(frozen, document(frozen));
    }

    public List<AtlasRecord> records() { return records; }

    public AtlasRecord get(String id) {
        AtlasRecord record = byId.get(id);
        if (record == null) throw new IllegalArgumentException("unknown atlas id " + id);
        return record;
    }

    public List<AtlasRecord> kind(String kind) {
        String canonical = AtlasKind.parse(kind);
        List<AtlasRecord> found = new ArrayList<AtlasRecord>();
        for (AtlasRecord record : records) {
            if (canonical.equals(record.kind())) found.add(record);
        }
        return Collections.unmodifiableList(found);
    }

    public int size() { return records.size(); }
    public String canonical() { return canonical; }
    public String sha256() { return AtlasHashes.sha256(canonical); }

    public List<AtlasRecord> search(String term) {
        if (term == null || term.isEmpty()) throw new IllegalArgumentException("term");
        String needle = term.toLowerCase(Locale.US);
        List<AtlasRecord> found = new ArrayList<AtlasRecord>();
        for (AtlasRecord record : records) {
            if (haystack(record).contains(needle)) found.add(record);
        }
        return Collections.unmodifiableList(found);
    }

    private static String haystack(AtlasRecord record) {
        StringBuilder text = new StringBuilder();
        text.append(record.id()).append('\n').append(record.subject()).append('\n');
        for (String item : record.evidence()) text.append(item).append('\n');
        for (String item : record.refs()) text.append(item).append('\n');
        return text.toString().toLowerCase(Locale.US);
    }

    private static String document(List<AtlasRecord> records) {
        StringBuilder text = new StringBuilder();
        text.append(AtlasSchema.STORE).append('\n');
        text.append("records=").append(records.size()).append('\n');
        for (AtlasRecord record : records) text.append(record.canonical());
        return text.toString();
    }

    private static Map<String, AtlasRecord> index(List<AtlasRecord> records) {
        Map<String, AtlasRecord> index = new LinkedHashMap<String, AtlasRecord>();
        for (AtlasRecord record : records) {
            if (index.put(record.id(), record) != null) {
                throw new IllegalArgumentException("duplicate id " + record.id());
            }
        }
        return Collections.unmodifiableMap(index);
    }
}
