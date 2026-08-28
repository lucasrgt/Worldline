package worldline.testkit;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockState;

/** Equatable canonical evidence for the paired moving-fluid lifecycle contract. */
public final class FlowingFluidLifecycleEvidence {
    private final FlowingFluidLifecycleObservation observation;

    FlowingFluidLifecycleEvidence(FlowingFluidLifecycleObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public FlowingFluidLifecycleObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.flowing-fluid-lifecycle-evidence.v1\n");
        value.append("verified-claims=water:state-domain+save-reload+collision-shape+light-behavior")
                .append("+tick-policy+neighbor-response,lava:save-reload+collision-shape")
                .append("+light-behavior+tick-policy+neighbor-response\n");
        value.append("partial-claims=lava:state-domain-overworld-only\n");
        row(value, "water", observation.water());
        row(value, "lava", observation.lava());
        return value.append("reload=").append(observation.boundary()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof FlowingFluidLifecycleEvidence
                && observation.equals(((FlowingFluidLifecycleEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }

    private static void row(StringBuilder value, String role, FlowingFluidObservation row) {
        value.append(role).append('=').append(row.movingId()).append("|domain=")
                .append(join(row.metadataDomain())).append("|first-flow-tick=")
                .append(row.firstFlowTick()).append("|neighbor=").append(state(row.blocked()))
                .append("->").append(state(row.recomputed())).append("|collision=")
                .append(row.passable() ? "PASSABLE" : "BLOCKED").append("|light=")
                .append(row.opacity()).append(':').append(row.emission()).append(':')
                .append(row.blockLight()).append(':').append(row.skyLight()).append("|persist=")
                .append(state(row.saved())).append("->").append(state(row.reloaded())).append('\n');
    }

    private static String join(List<Integer> values) {
        StringBuilder result = new StringBuilder();
        for (Integer value : values) {
            if (result.length() > 0) result.append(',');
            result.append(value);
        }
        return result.toString();
    }

    private static String state(BlockState value) {
        return value.legacyId() + ":" + value.metadata();
    }
}
