package worldline.testkit;

import worldline.api.BlockState;

final class IceFormationFixtureTest {
    private IceFormationFixtureTest() { }
    static void execute() {
        IceFormationFixture.Evidence first = IceFormationFixture.verify(8, (lit, pass) ->
                new IceFormationFixture.Observation(new BlockState(lit ? 9 : pass < 2 ? 9 : 79, 0),
                        true, lit ? 15 : 0));
        IceFormationFixture.Evidence second = IceFormationFixture.verify(8, (lit, pass) ->
                new IceFormationFixture.Observation(new BlockState(lit ? 9 : pass < 4 ? 9 : 79, 0),
                        true, lit ? 15 : 0));
        require(first.equals(second) && first.before().equals(new BlockState(9, 0))
                && first.after().equals(new BlockState(79, 0)) && first.maximumPasses() == 8,
                "ice formation evidence is not equatable across successful draws");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
