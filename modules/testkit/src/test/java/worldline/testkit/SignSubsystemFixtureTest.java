package worldline.testkit;

import java.util.Arrays;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteSignText;

public final class SignSubsystemFixtureTest {
    private SignSubsystemFixtureTest() { }

    public static void main(String[] arguments) { execute(); }

    static void execute() {
        SignSubsystemEvidence first = SignSubsystemFixture.execute(
                SignSubsystemFixtureTest::observation);
        SignSubsystemEvidence second = SignSubsystemFixture.execute(
                SignSubsystemFixtureTest::observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "sign subsystem evidence is not equatable");
        require(first.canonical().contains("standing-domain=0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15")
                && first.canonical().contains("inventory=20->19")
                && first.canonical().contains("reload=FRESH_LOGINx2|final=0:0+0:0"),
                "sign subsystem canonical evidence drifted");
        expectFailure(() -> SignSubsystemFixture.execute(() -> invalidTick()));
        System.out.println("sign subsystem fixture tests passed");
    }

    private static SignSubsystemObservation observation() {
        BlockPosition standingCell = new BlockPosition(4, 72, 4);
        BlockPosition wallCell = new BlockPosition(5, 72, 5);
        RemoteSignText standingText = new RemoteSignText(
                standingCell, "Stand", "sign", "TestKit", "ok");
        RemoteSignText wallText = new RemoteSignText(
                wallCell, "Wall", "sign", "TestKit", "ok");
        return observation(240, standingText, wallText);
    }

    private static SignSubsystemObservation invalidTick() {
        SignSubsystemObservation valid = observation();
        return new SignSubsystemObservation(valid.standingMetadata(), valid.placedStanding(),
                valid.placedWall(), valid.signCountBefore(), valid.signCountAfterFirstPlace(),
                valid.directBrokenFrom(), valid.directBrokenTo(), valid.directDrop(),
                valid.standingText(), valid.wallText(), valid.persistedStanding(),
                valid.persistedWall(), valid.persistedStandingText(),
                valid.persistedWallText(), valid.collisions(), valid.blockLight(),
                valid.skyLight(), 120, valid.tickStanding(), valid.tickWall(),
                valid.unsupportedStanding(), valid.unsupportedWall(), valid.finalStanding(),
                valid.finalWall(), valid.boundary(), valid.reloads());
    }

    private static SignSubsystemObservation observation(int ticks,
            RemoteSignText standingText, RemoteSignText wallText) {
        BlockState air = state(0, 0), standing = state(63, 4), wall = state(68, 5);
        return new SignSubsystemObservation(Arrays.asList(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                standing, wall, 20, 19, state(63, 0), air,
                new RemoteItemStack(323, 1, 0), standingText, wallText,
                standing, wall, standingText, wallText,
                Arrays.asList(MovementDisposition.UNCHALLENGED,
                        MovementDisposition.UNCHALLENGED), Arrays.asList(0, 0),
                Arrays.asList(15, 15), ticks, standing, wall, air, air, air, air,
                ReloadBoundary.FRESH_LOGIN, 2);
    }

    private static BlockState state(int id, int metadata) { return new BlockState(id, metadata); }
    private static void expectFailure(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("sign subsystem drift was accepted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
