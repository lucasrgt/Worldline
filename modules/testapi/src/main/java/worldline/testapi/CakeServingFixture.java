package worldline.testapi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;

/** Validates one complete six-serving cake lifecycle against the public contract. */
public final class CakeServingFixture {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final List<Integer> HEALTH = Collections.unmodifiableList(
            Arrays.asList(1, 4, 7, 10, 13, 16, 19));
    private static final List<MovementDisposition> COLLISIONS = Collections.unmodifiableList(
            Arrays.asList(MovementDisposition.CORRECTED, MovementDisposition.CORRECTED,
                    MovementDisposition.UNCHALLENGED, MovementDisposition.UNCHALLENGED,
                    MovementDisposition.UNCHALLENGED, MovementDisposition.UNCHALLENGED));

    private CakeServingFixture() { }

    public static CakeServingEvidence execute(CakeServingScenario scenario) {
        if (scenario == null) throw new NullPointerException("cake serving scenario");
        CakeServingObservation observed = scenario.observe();
        if (observed == null) throw new IllegalStateException("cake observation is absent");
        require(observed.states().equals(states()), "cake serving metadata progression drifted");
        require(observed.health().equals(HEALTH), "cake serving health progression drifted");
        require(observed.collisionLaneMilli() == 0
                && observed.collisionTravelMilli() == 1_000,
                "cake collision probe geometry drifted");
        require(observed.collisions().equals(COLLISIONS),
                "cake progressive collision envelope drifted");
        require(observed.blockLight().equals(repeated(0)), "cake emitted light drifted");
        require(observed.skyLight().equals(repeated(15)), "cake skylight transport drifted");
        BlockState partial = new BlockState(92, 3);
        require(observed.tickWindow() >= 200 && observed.tickBefore().equals(partial)
                && observed.tickAfter().equals(partial), "cake idle stability drifted");
        require(observed.reloaded().equals(partial)
                && observed.boundary() == ReloadBoundary.FRESH_LOGIN
                && observed.reloads() == 2, "cake fresh-login boundary drifted");
        require(observed.supported().equals(new BlockState(92, 0))
                && observed.unsupported().equals(AIR), "cake support invalidation drifted");
        require(observed.persisted().equals(AIR), "cake final persisted state drifted");
        return new CakeServingEvidence(observed);
    }

    private static List<BlockState> states() {
        return Arrays.asList(new BlockState(92, 0), new BlockState(92, 1),
                new BlockState(92, 2), new BlockState(92, 3), new BlockState(92, 4),
                new BlockState(92, 5), AIR);
    }

    private static List<Integer> repeated(int value) {
        return Arrays.asList(value, value, value, value, value, value);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
