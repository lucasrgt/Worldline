package worldline.testkit;

final class SleepQuorumFixtureTest {
    private SleepQuorumFixtureTest() { }
    static void execute() {
        SleepQuorumFixture.Evidence first = SleepQuorumFixture.await(2, 400, 100,
                tick -> tick % 2 == 0, () -> 2);
        SleepQuorumFixture.Evidence second = SleepQuorumFixture.await(2, 400, 100,
                tick -> true, () -> 2);
        require(first.equals(second) && first.wokenSleepers() == 2 && first.holdTicks() == 400
                && first.probeTicks() == 100 && first.expectedSleepers() == 2,
                "sleep quorum evidence is not equatable");
        require(fails(() -> SleepQuorumFixture.await(2, 400, 100, tick -> false, () -> 2)),
                "an open quorum that collapses must fail");
        require(fails(() -> SleepQuorumFixture.await(2, 400, 100, tick -> true, () -> 1)),
                "a partial wake must fail");
        require(fails(() -> SleepQuorumFixture.await(1, 400, 100, tick -> true, () -> 1)),
                "fewer than two sleepers must fail");
        require(fails(() -> SleepQuorumFixture.await(2, 50, 100, tick -> true, () -> 2)),
                "probe beyond the hold window must fail");
    }
    private static boolean fails(Runnable attempt) {
        try { attempt.run(); return false; }
        catch (RuntimeException expected) { return true; }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
