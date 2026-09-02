package worldline.api;

import worldline.api.scenario.PoweredCreeperEvidence;

final class PoweredCreeperEvidenceTest {
    private PoweredCreeperEvidenceTest() { }

    static void run() {
        PoweredCreeperEvidence first = valid();
        PoweredCreeperEvidence second = valid();
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || first.cellX() != 8 || first.cellY() != 65 || first.cellZ() != 8
                || !first.initiallyUnpowered() || !first.strikeObserved()
                || !first.strikeAtCreeper() || !first.identityPreserved()
                || !first.powered() || !first.heldPowered()) {
            throw new AssertionError("powered-creeper evidence drifted");
        }
        fail(() -> capture(state(7, false), state(7, true), strike(8), state(7, true)));
        fail(() -> capture(state(7, false), state(7, false), strike(7), state(7, true)));
        fail(() -> capture(state(7, false), state(7, false), strike(8), state(9, true)));
    }

    private static PoweredCreeperEvidence valid() {
        return capture(state(7, false), state(7, false), strike(8), state(7, true));
    }

    private static PoweredCreeperEvidence capture(PoweredCreeperEvidence.CreeperState before,
            PoweredCreeperEvidence.CreeperState prerequisite,
            PoweredCreeperEvidence.LightningStrike strike,
            PoweredCreeperEvidence.CreeperState after) {
        PoweredCreeperEvidence.Trial trial =
                new PoweredCreeperEvidence.Trial(before, prerequisite, strike, after);
        return PoweredCreeperEvidence.capture(trial, after);
    }

    private static PoweredCreeperEvidence.CreeperState state(int entity, boolean powered) {
        return new PoweredCreeperEvidence.CreeperState(entity, 8, 65, 8, powered);
    }

    private static PoweredCreeperEvidence.LightningStrike strike(int entity) {
        return new PoweredCreeperEvidence.LightningStrike(entity, 8, 65, 8, true, true);
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid powered-creeper evidence accepted");
        } catch (IllegalStateException expected) {
            return;
        }
    }
}
