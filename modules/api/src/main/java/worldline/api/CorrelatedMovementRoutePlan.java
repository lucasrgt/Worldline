package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** One bounded explicit-fallback route and its caller-owned correlation. */
public final class CorrelatedMovementRoutePlan {
    private final Object correlation;
    private final List<MovementAlternative> alternatives;

    public CorrelatedMovementRoutePlan(Object correlation, List<MovementAlternative> alternatives) {
        this.correlation = Objects.requireNonNull(correlation, "correlation");
        if (alternatives == null || alternatives.isEmpty() || alternatives.size() > 32)
            throw new IllegalArgumentException("invalid movement alternatives");
        ArrayList<MovementAlternative> copy = new ArrayList<>(alternatives.size());
        for (MovementAlternative alternative : alternatives) {
            if (alternative == null) throw new IllegalArgumentException("null movement alternative");
            copy.add(alternative);
        }
        this.alternatives = Collections.unmodifiableList(copy);
    }

    public Object correlation() { return correlation; }
    public List<MovementAlternative> alternatives() { return alternatives; }
}
