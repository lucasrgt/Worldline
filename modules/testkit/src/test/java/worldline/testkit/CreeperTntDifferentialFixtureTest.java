package worldline.testkit;

final class CreeperTntDifferentialFixtureTest {
    private CreeperTntDifferentialFixtureTest() { }
    static void execute() {
        CreeperTntDifferentialFixture.Evidence first =
                CreeperTntDifferentialFixture.observe(3F, 4F);
        CreeperTntDifferentialFixture.Evidence second =
                CreeperTntDifferentialFixture.observe(3F, 4F);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.creeperStrength() == 3 && first.tntStrength() == 4
                && first.delta() == 1 && first.tntStronger(),
                "creeper/TNT differential evidence drifted");
        fail(() -> CreeperTntDifferentialFixture.observe(4F, 4F));
        fail(() -> CreeperTntDifferentialFixture.observe(3F, 5F));
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid explosion differential accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
