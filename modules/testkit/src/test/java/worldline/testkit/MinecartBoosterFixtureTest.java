package worldline.testkit;

import worldline.api.RemoteObjectMovement;
import worldline.api.RemoteObjectSpawn;

final class MinecartBoosterFixtureTest {
    private MinecartBoosterFixtureTest() { }
    static void execute() {
        RemoteObjectSpawn driver = new RemoteObjectSpawn(4, 10, 144, 2331, 144, 0, 0, 0, 0);
        RemoteObjectSpawn booster = new RemoteObjectSpawn(5, 10, 176, 2331, 144, 0, 0, 0, 0);
        RemoteObjectMovement driverMove = new RemoteObjectMovement(4, 31,
                144, 2331, 144, 144, 2331, 148, 0, 0);
        RemoteObjectMovement boosterMove = new RemoteObjectMovement(5, 33,
                176, 2331, 144, 176, 2331, 146, 0, 0);
        MinecartBoosterFixture.Evidence first = MinecartBoosterFixture.observe(
                driver, booster, driverMove, boosterMove, 0, 1);
        MinecartBoosterFixture.Evidence second = MinecartBoosterFixture.observe(
                driver, booster, driverMove, boosterMove, 0, 1);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.lateralFixed() == 32 && first.driverForward()
                && first.boosterForward(), "minecart booster evidence drifted");
        fail(() -> MinecartBoosterFixture.observe(driver, booster,
                driverMove, boosterMove, 0, -1));
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid booster evidence accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
