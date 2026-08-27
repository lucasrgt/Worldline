package worldline.testkit;

import java.util.Objects;

/** One reusable behavior dimension expanded over every registered block profile. */
public final class BlockConformanceTemplate {
    private final String id;
    private final ConformanceLayer defaultLayer;

    public BlockConformanceTemplate(String id, ConformanceLayer defaultLayer) {
        if (id == null || !id.matches("[a-z][a-z0-9-]{0,62}")) {
            throw new IllegalArgumentException("id");
        }
        this.id = id;
        this.defaultLayer = Objects.requireNonNull(defaultLayer, "defaultLayer");
    }

    public String id() {
        return id;
    }

    public ConformanceLayer defaultLayer() {
        return defaultLayer;
    }
}
