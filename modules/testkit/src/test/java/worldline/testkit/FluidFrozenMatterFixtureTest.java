package worldline.testkit;

/** Contract test for stable fluid and frozen-matter evidence. */
public final class FluidFrozenMatterFixtureTest {
    private FluidFrozenMatterFixtureTest() { }

    public static void execute() {
        FluidFrozenMatterEvidence first = FluidFrozenMatterFixture.execute(
                FluidFrozenMatterFixtureTest::observation);
        FluidFrozenMatterEvidence second = FluidFrozenMatterFixture.execute(
                FluidFrozenMatterFixtureTest::observation);
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("claims=21|"))
            throw new AssertionError("fluid and frozen-matter evidence is unstable");
        rejects(() -> FluidFrozenMatterFixture.execute(() ->
                new FluidFrozenMatterObservation("wrong", observation().sponge(),
                        observation().snowLayer(), observation().ice(), observation().snowBlock())));
    }

    private static FluidFrozenMatterObservation observation() {
        return new FluidFrozenMatterObservation(
                "8+10:item-route+consumed,8+9+10+11:break-removed+drop-none,10:meta0-15",
                "19:rate10+direct-tick-stable+neighbor-stable",
                "78:rate10+dark-tick-stable+support-stable+support-loss-air",
                "79:meta0+break-to-water+drop-none+full-collision+neighbor-stable",
                "80:random-enrolled+direct-tick-stable");
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
            throw new AssertionError("invalid fluid and frozen-matter evidence was accepted");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }
}
