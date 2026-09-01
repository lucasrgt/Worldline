package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobSpawn;

/** One causally killed slime parent and the Packet24 children observed around it. */
public final class SlimeSplitObservation {
    private final RemoteMobSpawn parent;
    private final int parentSize;
    private final RemoteMobDeath death;
    private final List<RemoteMobSpawn> children;

    public SlimeSplitObservation(RemoteMobSpawn parent, int parentSize,
            RemoteMobDeath death, List<RemoteMobSpawn> children) {
        if (parentSize < 1 || children == null) throw new IllegalArgumentException("slime split");
        this.parent = Objects.requireNonNull(parent, "parent");
        this.parentSize = parentSize;
        this.death = Objects.requireNonNull(death, "death");
        List<RemoteMobSpawn> copy = new ArrayList<RemoteMobSpawn>();
        for (RemoteMobSpawn child : children) copy.add(Objects.requireNonNull(child, "child"));
        this.children = Collections.unmodifiableList(copy);
    }

    public RemoteMobSpawn parent() { return parent; }
    public int parentSize() { return parentSize; }
    public RemoteMobDeath death() { return death; }
    public List<RemoteMobSpawn> children() { return children; }
}
