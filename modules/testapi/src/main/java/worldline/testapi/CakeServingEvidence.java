package worldline.testapi;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockState;

/** Equatable canonical evidence for the complete cake-serving contract. */
public final class CakeServingEvidence {
    private final CakeServingObservation observation;

    public CakeServingEvidence(CakeServingObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public CakeServingObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.cake-serving-evidence.v1\n");
        value.append("subject=b1.7.3:block/092\n");
        value.append("claims=state-domain,collision-shape,light-behavior,tick-policy,neighbor-response\n");
        value.append("states=").append(states(observation.states())).append('\n');
        value.append("health=").append(join(observation.health())).append('\n');
        value.append("collision-lane-milli=").append(observation.collisionLaneMilli()).append('\n');
        value.append("collision-travel-milli=").append(observation.collisionTravelMilli()).append('\n');
        value.append("collisions=").append(join(observation.collisions())).append('\n');
        value.append("block-light=").append(join(observation.blockLight())).append('\n');
        value.append("sky-light=").append(join(observation.skyLight())).append('\n');
        value.append("tick=").append(observation.tickWindow()).append(':')
                .append(state(observation.tickBefore())).append("->")
                .append(state(observation.tickAfter())).append('\n');
        value.append("reload=").append(observation.boundary()).append('x')
                .append(observation.reloads()).append(':')
                .append(state(observation.reloaded())).append('\n');
        value.append("support=").append(state(observation.supported())).append("->")
                .append(state(observation.unsupported())).append('\n');
        return value.append("persisted=").append(state(observation.persisted())).append('\n')
                .toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof CakeServingEvidence
                && observation.equals(((CakeServingEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }

    private static String states(List<BlockState> values) {
        StringBuilder result = new StringBuilder();
        for (BlockState state : values) {
            if (result.length() > 0) result.append("->");
            result.append(state(state));
        }
        return result.toString();
    }

    private static String join(List<?> values) {
        StringBuilder result = new StringBuilder();
        for (Object value : values) {
            if (result.length() > 0) result.append("->");
            result.append(value);
        }
        return result.toString();
    }

    private static String state(BlockState value) {
        return value.legacyId() + ":" + value.metadata();
    }
}
