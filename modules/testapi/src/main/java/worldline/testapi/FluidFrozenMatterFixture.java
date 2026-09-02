package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable Beta 1.7.3 fluid and frozen-matter lifecycle matrix. */
public final class FluidFrozenMatterFixture {
    private static final String FLUIDS =
            "8+10:item-route+consumed,8+9+10+11:break-removed+drop-none,10:meta0-15";
    private static final String SPONGE = "19:rate10+direct-tick-stable+neighbor-stable";
    private static final String SNOW_LAYER =
            "78:rate10+dark-tick-stable+support-stable+support-loss-air";
    private static final String ICE =
            "79:meta0+break-to-water+drop-none+full-collision+neighbor-stable";
    private static final String SNOW_BLOCK = "80:random-enrolled+direct-tick-stable";

    private FluidFrozenMatterFixture() { }

    public static FluidFrozenMatterEvidence execute(FluidFrozenMatterScenario scenario) {
        FluidFrozenMatterObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(FLUIDS, actual.fluids(), "fluid lifecycle");
        expect(SPONGE, actual.sponge(), "sponge lifecycle");
        expect(SNOW_LAYER, actual.snowLayer(), "snow-layer lifecycle");
        expect(ICE, actual.ice(), "ice lifecycle");
        expect(SNOW_BLOCK, actual.snowBlock(), "snow-block lifecycle");
        if (WorldlineBehavior.require("fluid-frozen-matter-subsystem")
                != WorldlineWorldBehaviors.FLUID_FROZEN_MATTER_SUBSYSTEM)
            throw new IllegalStateException("fluid and frozen-matter registration drifted");
        return new FluidFrozenMatterEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
