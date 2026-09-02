package worldline.testapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Public row for one bounded support-dependent random-tick spread contract. */
public final class BlockRandomTickSpreadScenario {
    private final String id;
    private final List<BlockConformanceCase> claims;
    private final BlockState state, supportState;
    private final List<BlockPosition> sourceSupports, targets;
    private final BlockPosition control, supportToBreak, lightProbe;
    private final BlockLifecycleSlot placementSlot, breakSlot;
    private final BlockCollisionProbe collision;
    private final int blockLight, skyLight, windowTicks, maxWindows, breakTicks, observationTicks;

    public BlockRandomTickSpreadScenario(String id, List<BlockConformanceCase> claims,
            BlockState state, BlockState supportState, List<BlockPosition> sourceSupports,
            List<BlockPosition> targets, BlockPosition control, BlockPosition supportToBreak,
            BlockPosition lightProbe, BlockLifecycleSlot placementSlot,
            BlockLifecycleSlot breakSlot, BlockCollisionProbe collision, int blockLight,
            int skyLight, int windowTicks, int maxWindows, int breakTicks,
            int observationTicks) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid random-tick spread scenario id");
        }
        this.claims = claims(claims);
        this.id = id; this.state = nonAir(state, "state");
        this.supportState = nonAir(supportState, "supportState");
        this.sourceSupports = positions(sourceSupports, "source supports");
        this.targets = positions(targets, "targets");
        this.control = Objects.requireNonNull(control, "control");
        this.supportToBreak = Objects.requireNonNull(supportToBreak, "supportToBreak");
        this.lightProbe = Objects.requireNonNull(lightProbe, "lightProbe");
        this.placementSlot = Objects.requireNonNull(placementSlot, "placementSlot");
        this.breakSlot = Objects.requireNonNull(breakSlot, "breakSlot");
        this.collision = Objects.requireNonNull(collision, "collision");
        if (collision.expected() != BlockCollisionExpectation.PASSABLE) {
            throw new IllegalArgumentException("spread collision must be passable");
        }
        if (blockLight < 0 || blockLight > 15 || skyLight < 0 || skyLight > 15
                || windowTicks < 1 || windowTicks > 1_200 || maxWindows < 1 || maxWindows > 80
                || breakTicks < 0 || breakTicks > 200
                || observationTicks < 1 || observationTicks > 1_200) {
            throw new IllegalArgumentException("invalid random-tick spread bounds");
        }
        this.blockLight = blockLight; this.skyLight = skyLight;
        this.windowTicks = windowTicks; this.maxWindows = maxWindows; this.breakTicks = breakTicks;
        this.observationTicks = observationTicks;
        if (!sourceSupports.contains(supportToBreak)
                || !BlockFace.UP.adjacent(supportToBreak).equals(lightProbe)) {
            throw new IllegalArgumentException("spread causal probe is not a source");
        }
    }

    public String id() { return id; }
    public String subject() { return claims.get(0).profile().subject(); }
    public List<BlockConformanceCase> claims() { return claims; }
    public BlockConformanceCase claim(String template) {
        return claims.stream().filter(value -> value.template().id().equals(template))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "missing random-tick spread claim " + template));
    }
    public BlockState state() { return state; }
    public BlockState supportState() { return supportState; }
    public List<BlockPosition> sourceSupports() { return sourceSupports; }
    public List<BlockPosition> sources() {
        List<BlockPosition> values = new ArrayList<BlockPosition>();
        for (BlockPosition support : sourceSupports) values.add(BlockFace.UP.adjacent(support));
        return Collections.unmodifiableList(values);
    }
    public List<BlockPosition> targets() { return targets; }
    public BlockPosition control() { return control; }
    public BlockPosition supportToBreak() { return supportToBreak; }
    public BlockPosition sourceToRemove() { return BlockFace.UP.adjacent(supportToBreak); }
    public BlockPosition lightProbe() { return lightProbe; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public BlockLifecycleSlot breakSlot() { return breakSlot; }
    public BlockCollisionProbe collision() { return collision; }
    public int blockLight() { return blockLight; }
    public int skyLight() { return skyLight; }
    public int windowTicks() { return windowTicks; }
    public int maxWindows() { return maxWindows; }
    public int breakTicks() { return breakTicks; }
    public int observationTicks() { return observationTicks; }

    private static List<BlockConformanceCase> claims(List<BlockConformanceCase> values) {
        if (values == null || values.size() != 5) throw new IllegalArgumentException(
                "random-tick spread requires five claims");
        Set<String> expected = new HashSet<String>(Arrays.asList("state-domain",
                "collision-shape", "light-behavior", "tick-policy", "neighbor-response"));
        String subject = values.get(0).profile().subject();
        for (BlockConformanceCase value : values) if (value == null
                || !subject.equals(value.profile().subject())
                || !expected.remove(value.template().id())) {
            throw new IllegalArgumentException("invalid random-tick spread claims");
        }
        if (!expected.isEmpty()) throw new IllegalArgumentException("missing spread claims");
        return Collections.unmodifiableList(new ArrayList<BlockConformanceCase>(values));
    }
    private static List<BlockPosition> positions(List<BlockPosition> values, String role) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(Objects::isNull)
                || new HashSet<BlockPosition>(values).size() != values.size()) {
            throw new IllegalArgumentException("invalid " + role);
        }
        return Collections.unmodifiableList(new ArrayList<BlockPosition>(values));
    }
    private static BlockState nonAir(BlockState value, String role) {
        Objects.requireNonNull(value, role);
        if (value.legacyId() == 0) throw new IllegalArgumentException(role + " is air");
        return value;
    }
}
