package worldline.testkit;

/** Contract test for stable redstone signal-consumer evidence. */
public final class RedstoneSignalConsumersFixtureTest {
    private RedstoneSignalConsumersFixtureTest() { }

    public static void execute() {
        RedstoneSignalConsumersEvidence first = RedstoneSignalConsumersFixture.execute(
                RedstoneSignalConsumersFixtureTest::observation);
        RedstoneSignalConsumersEvidence second = RedstoneSignalConsumersFixture.execute(
                RedstoneSignalConsumersFixtureTest::observation);
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("claims=18|"))
            throw new AssertionError("redstone signal-consumer evidence is unstable");
        rejects(() -> RedstoneSignalConsumersFixture.execute(() ->
                new RedstoneSignalConsumersObservation("wrong", observation().shapes(),
                        observation().light(), observation().ticks(), observation().neighbors())));
    }

    private static RedstoneSignalConsumersObservation observation() {
        return new RedstoneSignalConsumersObservation(
                "46:0+1,55:0-15,84:0+1",
                "46:full,55:passable",
                "46:255/0,55:0/0",
                "23:rate4+unpowered-stable,25+46+50+55+84:rate10+noop",
                "23:powered-schedule,25:rising-edge,46:powered-prime,55:support-loss,84:noop");
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid redstone signal-consumer evidence was accepted");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }
}
