package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockState;

/** Public data row for one causally exercised static light-transport profile. */
public final class BlockLightScenario {
    private static final BlockState DEFAULT_SUPPORT = new BlockState(1, 0);
    private final String id;
    private final BlockConformanceCase claim;
    private final BlockLifecycleSlot placementSlot;
    private final float yaw, pitch;
    private final List<BlockLightPlacement> placements;
    private final List<BlockLightProbe> probes;
    private final BlockState supportState;

    public BlockLightScenario(String id, BlockConformanceCase claim,
            BlockLifecycleSlot placementSlot, float yaw, float pitch,
            List<BlockLightPlacement> placements, List<BlockLightProbe> probes) {
        this(id, claim, placementSlot, yaw, pitch, placements, probes, DEFAULT_SUPPORT);
    }

    public BlockLightScenario(String id, BlockConformanceCase claim,
            BlockLifecycleSlot placementSlot, float yaw, float pitch,
            List<BlockLightPlacement> placements, List<BlockLightProbe> probes,
            BlockState supportState) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid light scenario id");
        }
        if (claim == null || !claim.template().id().equals("light-behavior")) {
            throw new IllegalArgumentException("claim does not target light-behavior");
        }
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("invalid light placement look");
        }
        if (placements == null || placements.isEmpty()
                || placements.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("light placements are empty");
        }
        if (probes == null || probes.isEmpty()
                || probes.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("light probes are empty");
        }
        Set<String> ids = new HashSet<String>();
        for (BlockLightProbe probe : probes) if (!ids.add(probe.id())) {
            throw new IllegalArgumentException("duplicate light probe: " + probe.id());
        }
        if (supportState == null || supportState.legacyId() == 0) {
            throw new IllegalArgumentException("invalid light support state");
        }
        this.id = id; this.claim = claim;
        this.placementSlot = java.util.Objects.requireNonNull(placementSlot, "placementSlot");
        this.yaw = yaw; this.pitch = pitch;
        this.placements = Collections.unmodifiableList(new ArrayList<BlockLightPlacement>(placements));
        this.probes = Collections.unmodifiableList(new ArrayList<BlockLightProbe>(probes));
        this.supportState = supportState;
    }

    public String id() { return id; }
    public String subject() { return claim.profile().subject(); }
    public BlockConformanceCase claim() { return claim; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public List<BlockLightPlacement> placements() { return placements; }
    public List<BlockLightProbe> probes() { return probes; }
    public BlockState supportState() { return supportState; }
}
