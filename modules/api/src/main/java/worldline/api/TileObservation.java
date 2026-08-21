package worldline.api;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Immutable, neutral observation of a loaded tile entity. */
public final class TileObservation {
    private final BlockPosition position;
    private final String type;
    private final Map<String, String> fields;

    public TileObservation(BlockPosition position, String type, Map<String, String> fields) {
        if (position == null || fields == null) throw new NullPointerException();
        if (type == null || !type.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("tile type");
        TreeMap<String, String> copy = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty() || entry.getValue() == null)
                throw new IllegalArgumentException("tile field");
            copy.put(entry.getKey(), entry.getValue());
        }
        this.position = position; this.type = type;
        this.fields = Collections.unmodifiableMap(copy);
    }
    public BlockPosition position() { return position; }
    public String type() { return type; }
    public Map<String, String> fields() { return fields; }
    public String field(String name) {
        if (!fields.containsKey(name)) throw new IllegalArgumentException("unknown tile field " + name);
        return fields.get(name);
    }
}
