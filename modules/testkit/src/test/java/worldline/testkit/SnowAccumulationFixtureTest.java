package worldline.testkit;

import worldline.api.BlockState;

final class SnowAccumulationFixtureTest {
    private SnowAccumulationFixtureTest() { }
    static void execute() {
        SnowAccumulationFixture.Evidence first = evidence(2);
        SnowAccumulationFixture.Evidence second = evidence(5);
        require(first.equals(second) && first.before().equals(new BlockState(0, 0))
                && first.after().equals(new BlockState(78, 0)) && first.maximumPasses() == 8,
                "snow accumulation evidence is not equatable across successful draws");
    }
    private static SnowAccumulationFixture.Evidence evidence(int success) {
        return SnowAccumulationFixture.verify(8, (snowfall, pass) ->
                new SnowAccumulationFixture.Observation(
                        new BlockState(snowfall && pass >= success ? 78 : 0, 0), true,
                        snowfall, 0));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
