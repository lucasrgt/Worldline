package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Public data row for one causally exercised static collision envelope. */
public final class BlockCollisionScenario {
    private final String id;
    private final BlockConformanceCase claim;
    private final BlockLifecycleSlot placementSlot;
    private final float yaw, pitch;
    private final List<BlockCollisionPlacement> placements;
    private final List<BlockCollisionProbe> probes;

    public BlockCollisionScenario(String id, BlockConformanceCase claim,
            BlockLifecycleSlot placementSlot, float yaw, float pitch,
            List<BlockCollisionPlacement> placements, List<BlockCollisionProbe> probes) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid collision scenario id");
        }
        if (claim == null || !claim.template().id().equals("collision-shape")) {
            throw new IllegalArgumentException("claim does not target collision-shape");
        }
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("invalid placement look");
        }
        if (placements == null || placements.isEmpty()
                || placements.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("collision placements are empty");
        }
        if (probes == null || probes.isEmpty()
                || probes.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("collision probes are empty");
        }
        Set<String> ids = new HashSet<String>();
        for (BlockCollisionProbe probe : probes) if (!ids.add(probe.id())) {
            throw new IllegalArgumentException("duplicate collision probe: " + probe.id());
        }
        this.id = id; this.claim = claim;
        this.placementSlot = java.util.Objects.requireNonNull(placementSlot, "placementSlot");
        this.yaw = yaw; this.pitch = pitch;
        this.placements = Collections.unmodifiableList(
                new ArrayList<BlockCollisionPlacement>(placements));
        this.probes = Collections.unmodifiableList(new ArrayList<BlockCollisionProbe>(probes));
    }

    public String id() { return id; }
    public String subject() { return claim.profile().subject(); }
    public BlockConformanceCase claim() { return claim; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public List<BlockCollisionPlacement> placements() { return placements; }
    public List<BlockCollisionProbe> probes() { return probes; }
}
