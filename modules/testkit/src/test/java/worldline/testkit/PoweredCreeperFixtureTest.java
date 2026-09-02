package worldline.testkit;

import worldline.api.scenario.PoweredCreeperActions;
import worldline.api.scenario.PoweredCreeperEvidence;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineHostileBehaviors;

final class PoweredCreeperFixtureTest {
    private PoweredCreeperFixtureTest() { }

    static void execute() {
        Fake actions = new Fake();
        PoweredCreeperEvidence first = PoweredCreeperFixture.exercise(actions, actions::tick);
        PoweredCreeperEvidence second = PoweredCreeperFixture.exercise(new Fake(), () -> { });
        PoweredCreeperFixture.compare(first, second);
        require(actions.ticks == 1 && first.identityPreserved() && first.heldPowered(),
                "powered-creeper action evidence drifted");
        WorldlineBehavior behavior = WorldlineHostileBehaviors.POWERED_CREEPER;
        require(WorldlineBehavior.require("powered-creeper") == behavior,
                "powered-creeper behavior registration drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static final class Fake implements PoweredCreeperActions {
        private boolean powered;
        private int ticks;

        @Override public PoweredCreeperEvidence.Trial strike() {
            PoweredCreeperEvidence.CreeperState before = state(false);
            powered = true;
            return new PoweredCreeperEvidence.Trial(before, state(false),
                    new PoweredCreeperEvidence.LightningStrike(8, 8, 65, 8, true, true),
                    state(true));
        }

        @Override public PoweredCreeperEvidence.CreeperState current() {
            return state(powered);
        }

        void tick() {
            ticks++;
        }

        private static PoweredCreeperEvidence.CreeperState state(boolean powered) {
            return new PoweredCreeperEvidence.CreeperState(7, 8, 65, 8, powered);
        }
    }
}
