package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Public data row for one causally exercised reachable metadata domain. */
public final class BlockStateDomainScenario {
    private static final BlockState DEFAULT_SUPPORT = new BlockState(1, 0);
    private final String id;
    private final BlockConformanceCase claim;
    private final BlockLifecycleSlot placementSlot;
    private final List<BlockState> domain;
    private final List<BlockStateDomainStep> steps;
    private final int observationTicks;
    private final BlockState supportState;

    public BlockStateDomainScenario(String id, BlockConformanceCase claim,
            BlockLifecycleSlot placementSlot, List<BlockState> domain,
            List<BlockStateDomainStep> steps, int observationTicks) {
        this(id, claim, placementSlot, domain, steps, observationTicks, DEFAULT_SUPPORT);
    }

    public BlockStateDomainScenario(String id, BlockConformanceCase claim,
            BlockLifecycleSlot placementSlot, List<BlockState> domain,
            List<BlockStateDomainStep> steps, int observationTicks,
            BlockState supportState) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,127}")) {
            throw new IllegalArgumentException("invalid state-domain scenario id");
        }
        if (claim == null || !claim.template().id().equals("state-domain")) {
            throw new IllegalArgumentException("claim does not target state-domain");
        }
        if (domain == null || domain.isEmpty() || domain.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("state domain is empty");
        }
        if (steps == null || steps.isEmpty() || steps.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("state-domain steps are empty");
        }
        if (observationTicks < 1) throw new IllegalArgumentException("invalid observation ticks");
        List<BlockState> domainCopy = new ArrayList<BlockState>(domain);
        Set<BlockState> distinct = new HashSet<BlockState>(domainCopy);
        if (distinct.size() != domainCopy.size()) throw new IllegalArgumentException(
                "duplicate state-domain value");
        Set<BlockState> exercised = new HashSet<BlockState>();
        Set<String> stepIds = new HashSet<String>();
        for (BlockStateDomainStep step : steps) {
            if (!stepIds.add(step.id())) throw new IllegalArgumentException(
                    "duplicate state-domain step: " + step.id());
            for (BlockStateObservation observation : step.observations()) {
                exercised.add(observation.state());
            }
        }
        if (!exercised.equals(distinct)) throw new IllegalArgumentException(
                "declared state domain differs from exercised states");
        if (supportState == null || supportState.legacyId() == 0) {
            throw new IllegalArgumentException("invalid state-domain support state");
        }
        this.id = id;
        this.claim = claim;
        this.placementSlot = java.util.Objects.requireNonNull(placementSlot, "placementSlot");
        this.domain = Collections.unmodifiableList(domainCopy);
        this.steps = Collections.unmodifiableList(new ArrayList<BlockStateDomainStep>(steps));
        this.observationTicks = observationTicks;
        this.supportState = supportState;
    }

    public String id() { return id; }
    public String subject() { return claim.profile().subject(); }
    public BlockConformanceCase claim() { return claim; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public List<BlockState> domain() { return domain; }
    public List<BlockStateDomainStep> steps() { return steps; }
    public int observationTicks() { return observationTicks; }
    public BlockState supportState() { return supportState; }

    public Map<BlockPosition, BlockState> finalStates() {
        Map<BlockPosition, BlockState> result = new LinkedHashMap<BlockPosition, BlockState>();
        for (BlockStateDomainStep step : steps) for (BlockStateObservation observation
                : step.observations()) result.put(observation.position(), observation.state());
        return Collections.unmodifiableMap(result);
    }
}
