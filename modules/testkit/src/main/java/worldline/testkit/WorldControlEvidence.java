package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Equatable public evidence shared by the local-world block, player and inventory GUI consumers. */
public final class WorldControlEvidence {
    private final List<String> observations;
    public WorldControlEvidence(List<String> observations) {
        if (observations == null) throw new NullPointerException("observations");
        this.observations = Collections.unmodifiableList(new ArrayList<String>(observations));
        for (String row : this.observations)
            if (row == null || row.isEmpty() || row.contains("\n"))
                throw new IllegalArgumentException("invalid observation");
    }
    public List<String> observations() { return observations; }
    public String canonical() { return "schema=worldline.world-controls.v1|" + String.join("|", observations); }
    @Override public boolean equals(Object other) {
        return other instanceof WorldControlEvidence
                && observations.equals(((WorldControlEvidence) other).observations);
    }
    @Override public int hashCode() { return observations.hashCode(); }
}
