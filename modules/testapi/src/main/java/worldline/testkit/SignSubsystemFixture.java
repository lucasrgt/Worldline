package worldline.testkit;

import java.util.Arrays;
import java.util.List;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;
import worldline.api.RemoteItemStack;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the complete server-visible standing-sign and wall-sign subsystem. */
public final class SignSubsystemFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STANDING = new BlockState(63, 4);
    private static final BlockState WALL = new BlockState(68, 5);
    private static final List<Integer> DOMAIN = Arrays.asList(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    private SignSubsystemFixture() { }

    public static SignSubsystemEvidence execute(SignSubsystemScenario scenario) {
        if (scenario == null) throw new NullPointerException("sign subsystem scenario");
        SignSubsystemObservation observed = scenario.observe();
        if (observed == null) throw new IllegalStateException("sign observation is absent");
        require(observed.standingMetadata().equals(DOMAIN),
                "standing-sign metadata domain drifted");
        require(observed.placedStanding().equals(STANDING)
                && observed.placedWall().equals(WALL), "sign gameplay placement drifted");
        require(observed.signCountBefore() == 20
                && observed.signCountAfterFirstPlace() == 19,
                "sign placement consumption drifted");
        require(observed.directBrokenFrom().legacyId() == 63
                && observed.directBrokenTo().equals(AIR), "standing-sign break drifted");
        require(observed.directDrop().equals(new RemoteItemStack(323, 1, 0)),
                "standing-sign direct drop drifted");
        require(observed.persistedStanding().equals(STANDING)
                && observed.persistedWall().equals(WALL), "sign persisted state drifted");
        require(observed.persistedStandingText().equals(observed.standingText())
                && observed.persistedWallText().equals(observed.wallText()),
                "sign Packet130 text persistence drifted");
        require(observed.collisions().equals(Arrays.asList(
                MovementDisposition.UNCHALLENGED, MovementDisposition.UNCHALLENGED)),
                "sign passable collision envelope drifted");
        require(observed.blockLight().equals(Arrays.asList(0, 0))
                && observed.skyLight().equals(Arrays.asList(15, 15)),
                "sign light transport drifted");
        require(observed.tickWindow() >= 240 && observed.tickStanding().equals(STANDING)
                && observed.tickWall().equals(WALL), "sign bounded tick stability drifted");
        require(observed.unsupportedStanding().equals(AIR)
                && observed.unsupportedWall().equals(AIR), "sign support invalidation drifted");
        require(observed.finalStanding().equals(AIR) && observed.finalWall().equals(AIR),
                "sign final persisted air drifted");
        require(observed.boundary() == ReloadBoundary.FRESH_LOGIN && observed.reloads() == 2,
                "sign fresh-login boundary drifted");
        require(WorldlineBehavior.require("sign-subsystem") == WorldlineWorldBehaviors.SIGN_SUBSYSTEM,
                "sign-subsystem behavior registration drifted");
        return new SignSubsystemEvidence(observed);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
