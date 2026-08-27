package worldline.testkit;

import worldline.api.BlockState;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

final class SnowLayerNonstackingFixtureTest {
    private SnowLayerNonstackingFixtureTest() {
    }

    static void execute() {
        SnowLayerNonstackingFixture.Evidence first = evidence(2);
        SnowLayerNonstackingFixture.Evidence second = evidence(5);
        require(first.equals(second)
                && first.before().equals(new BlockState(0, 0))
                && first.after().equals(new BlockState(78, 0))
                && first.above().equals(new BlockState(0, 0))
                && first.columnSnowCount() == 1
                && first.maximumFormationPasses() == 8
                && first.maximumSettlingPasses() == 4,
                "snow layer evidence is not equatable");
        require(WorldlineBehavior.require("snow-layer-nonstacking")
                        == WorldlineWorldBehaviors.SNOW_LAYER_NONSTACKING,
                "snow layer behavior registration drifted");
        fail(() -> SnowLayerNonstackingFixture.verify(8, 4,
                (snowfall, pass) -> new SnowLayerNonstackingFixture.Observation(
                        new BlockState(snowfall ? 78 : 0, 0),
                        new BlockState(snowfall ? 78 : 0, 0), snowfall ? 2 : 0,
                        true, snowfall, 0)));
    }

    private static SnowLayerNonstackingFixture.Evidence evidence(int success) {
        return SnowLayerNonstackingFixture.verify(8, 4, (snowfall, pass) -> {
            boolean formed = snowfall && pass >= success;
            return new SnowLayerNonstackingFixture.Observation(
                    new BlockState(formed ? 78 : 0, 0),
                    new BlockState(0, 0), formed ? 1 : 0,
                    true, snowfall, 0);
        });
    }

    private static void fail(Runnable action) {
        try {
            action.run();
            throw new AssertionError("stacked snow layer was accepted");
        } catch (IllegalStateException expected) {
        }
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
