package worldline.testkit;

import worldline.api.scenario.PoweredCreeperActions;
import worldline.api.scenario.PoweredCreeperEvidence;

/** Reusable action and comparison fixture for a causal powered-creeper trial. */
public final class PoweredCreeperFixture {
    private PoweredCreeperFixture() { }

    public static PoweredCreeperEvidence exercise(
            PoweredCreeperActions actions, Runnable observationTick) {
        if (actions == null) {
            throw new IllegalArgumentException("missing powered-creeper actions");
        }
        return actions.exercise(observationTick);
    }

    public static void compare(
            PoweredCreeperEvidence expected, PoweredCreeperEvidence observed) {
        if (expected == null || !expected.equals(observed)) {
            throw new IllegalStateException("powered-creeper evidence mismatch");
        }
    }
}
