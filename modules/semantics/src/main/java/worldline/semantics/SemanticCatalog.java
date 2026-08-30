package worldline.semantics;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.api.SemanticMapping;

/**
 * Fail-closed catalog of b1.7.3 semantic mappings. Construction requires every
 * declared category role exactly once, known confidence, and unique symbols.
 */
public final class SemanticCatalog {
    private final List<SemanticMapping> mappings;
    private final Map<String, SemanticMapping> byRole;
    private final Map<String, List<SemanticMapping>> byCategory;

    private SemanticCatalog(List<SemanticMapping> mappings) {
        this.mappings = mappings;
        this.byRole = indexRoles(mappings);
        this.byCategory = indexCategories(mappings);
        verifyComplete();
    }

    public static SemanticCatalog standard() {
        List<SemanticMapping> all = new ArrayList<SemanticMapping>();
        all.addAll(ClockSemantics.mappings());
        all.addAll(RngSemantics.mappings());
        all.addAll(InputSemantics.mappings());
        all.addAll(TickSemantics.mappings());
        all.addAll(FilesystemSemantics.mappings());
        all.addAll(NetworkSemantics.mappings());
        all.addAll(DedicatedServerSemantics.mappings());
        all.addAll(SchedulerSemantics.mappings());
        all.addAll(WorldSemantics.mappings());
        all.addAll(WorldgenSemantics.mappings());
        all.addAll(BlockSemantics.mappings());
        all.addAll(BlockTickSemantics.mappings());
        all.addAll(FluidSemantics.mappings());
        all.addAll(LightSemantics.mappings());
        all.addAll(WeatherSemantics.mappings());
        all.addAll(MobAiSemantics.mappings());
        all.addAll(DimensionSemantics.mappings());
        all.addAll(TileEntitySemantics.mappings());
        all.addAll(PlayerSemantics.mappings());
        all.addAll(EntitySemantics.mappings());
        all.addAll(ChunkSemantics.mappings());
        all.addAll(InventorySemantics.mappings());
        all.addAll(ItemSemantics.mappings());
        all.addAll(RecipeSemantics.mappings());
        all.addAll(GuiSemantics.mappings());
        all.addAll(RenderSemantics.mappings());
        all.addAll(AudioSemantics.mappings());
        all.addAll(ResourceSemantics.mappings());
        all.addAll(PersistenceSemantics.mappings());
        all.addAll(SaveSemantics.mappings());
        all.addAll(LifecycleSemantics.mappings());
        all.addAll(LabSemantics.mappings());
        all.addAll(DomainSemantics.mappings());
        all.addAll(RedstoneSemantics.mappings());
        return of(all);
    }

    public static SemanticCatalog of(List<SemanticMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) throw new IllegalArgumentException("mappings");
        List<SemanticMapping> copy = new ArrayList<SemanticMapping>();
        for (SemanticMapping mapping : mappings) {
            if (mapping == null) throw new NullPointerException("mapping");
            if (!mapping.known()) throw new IllegalArgumentException("unknown mapping " + mapping.role());
            copy.add(mapping);
        }
        Collections.sort(copy, new Comparator<SemanticMapping>() {
            @Override public int compare(SemanticMapping left, SemanticMapping right) {
                int category = left.category().compareTo(right.category());
                return category != 0 ? category : left.role().compareTo(right.role());
            }
        });
        return new SemanticCatalog(Collections.unmodifiableList(copy));
    }

    public SemanticMapping role(String role) {
        SemanticMapping mapping = byRole.get(role);
        if (mapping == null) throw new IllegalArgumentException("unknown role " + role);
        return mapping;
    }

    public List<SemanticMapping> category(String category) {
        List<SemanticMapping> mappings = byCategory.get(category);
        if (mappings == null) throw new IllegalArgumentException("unknown category " + category);
        return mappings;
    }

    public SemanticMapping symbol(String owner, String name) {
        SemanticMapping found = null;
        for (SemanticMapping mapping : mappings) {
            if (mapping.owner().equals(owner) && mapping.name().equals(name)) {
                if (found != null) throw new IllegalStateException("ambiguous symbol " + owner + "." + name);
                found = mapping;
            }
        }
        if (found == null) throw new IllegalArgumentException("unknown symbol " + owner + "." + name);
        return found;
    }

    public List<SemanticMapping> mappings() { return mappings; }
    public List<String> categories() { return SemanticRoles.categories(); }
    public int size() { return mappings.size(); }

    public String canonical() {
        StringBuilder text = new StringBuilder();
        text.append("format=1\n");
        text.append("categories=").append(SemanticRoles.categories().size()).append('\n');
        text.append("roles=").append(size()).append('\n');
        for (SemanticMapping mapping : mappings) text.append(mapping.canonical()).append('\n');
        return text.toString();
    }

    public String sha256() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format("%02x", value & 255));
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    public String render() {
        StringBuilder text = new StringBuilder();
        text.append("categories=").append(SemanticRoles.categories().size()).append('\n');
        text.append("roles=").append(size()).append('\n');
        text.append("complete=true\n");
        text.append("sha256=").append(sha256()).append('\n');
        for (SemanticMapping mapping : mappings) {
            text.append(mapping.category()).append('.').append(mapping.role()).append('=')
                    .append(mapping.owner()).append('.').append(mapping.name())
                    .append(" confidence=").append(mapping.confidence()).append('\n');
        }
        return text.toString();
    }

    private void verifyComplete() {
        for (String category : SemanticRoles.categories()) {
            List<String> required = SemanticRoles.required(category);
            List<SemanticMapping> found = byCategory.get(category);
            if (found == null || found.size() != required.size()) {
                throw new IllegalArgumentException("incomplete category " + category);
            }
            for (SemanticMapping mapping : found) {
                if (!required.contains(mapping.role())) {
                    throw new IllegalArgumentException("unexpected role " + mapping.role());
                }
            }
        }
        if (mappings.size() != SemanticRoles.roleCount()) {
            throw new IllegalArgumentException("catalog is not complete");
        }
        Map<String, SemanticMapping> symbols = new LinkedHashMap<String, SemanticMapping>();
        for (SemanticMapping mapping : mappings) {
            String key = mapping.owner() + "." + mapping.name();
            if (symbols.put(key, mapping) != null) {
                throw new IllegalArgumentException("duplicate symbol " + key);
            }
        }
    }

    private static Map<String, SemanticMapping> indexRoles(List<SemanticMapping> mappings) {
        Map<String, SemanticMapping> index = new LinkedHashMap<String, SemanticMapping>();
        for (SemanticMapping mapping : mappings) {
            if (index.put(mapping.role(), mapping) != null) {
                throw new IllegalArgumentException("duplicate role " + mapping.role());
            }
        }
        return Collections.unmodifiableMap(index);
    }

    private static Map<String, List<SemanticMapping>> indexCategories(List<SemanticMapping> mappings) {
        Map<String, List<SemanticMapping>> index = new LinkedHashMap<String, List<SemanticMapping>>();
        for (String category : SemanticRoles.categories()) {
            index.put(category, new ArrayList<SemanticMapping>());
        }
        for (SemanticMapping mapping : mappings) {
            List<SemanticMapping> bucket = index.get(mapping.category());
            if (bucket == null) throw new IllegalArgumentException("unknown category " + mapping.category());
            bucket.add(mapping);
        }
        Map<String, List<SemanticMapping>> frozen = new LinkedHashMap<String, List<SemanticMapping>>();
        for (Map.Entry<String, List<SemanticMapping>> entry : index.entrySet()) {
            frozen.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(frozen);
    }
}
