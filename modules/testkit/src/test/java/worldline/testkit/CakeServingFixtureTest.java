package worldline.testkit;
import worldline.testapi.CakeServingEvidence;
import worldline.testapi.CakeServingFixture;
import worldline.testapi.CakeServingObservation;

import java.util.Arrays;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;

public final class CakeServingFixtureTest {
    private CakeServingFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
    }

    static void execute() {
        CakeServingObservation observation = observation();
        CakeServingEvidence first = CakeServingFixture.execute(() -> observation);
        CakeServingEvidence second = CakeServingFixture.execute(() -> observation());
        if (!first.equals(second) || first.hashCode() != second.hashCode()
                || !first.canonical().contains("states=92:0->92:1->92:2->92:3->92:4->92:5->0:0")
                || !first.canonical().contains("reload=FRESH_LOGINx2:92:3")) {
            throw new AssertionError("cake serving evidence is not stable");
        }
        expectFailure(() -> CakeServingFixture.execute(() -> badCollision()));
        System.out.println("cake serving fixture tests passed");
    }

    private static CakeServingObservation observation() {
        return new CakeServingObservation(Arrays.asList(state(92, 0), state(92, 1),
                state(92, 2), state(92, 3), state(92, 4), state(92, 5), state(0, 0)),
                Arrays.asList(1, 4, 7, 10, 13, 16, 19), Arrays.asList(
                        MovementDisposition.CORRECTED, MovementDisposition.CORRECTED,
                        MovementDisposition.UNCHALLENGED, MovementDisposition.UNCHALLENGED,
                        MovementDisposition.UNCHALLENGED, MovementDisposition.UNCHALLENGED),
                0, 1_000, Arrays.asList(0, 0, 0, 0, 0, 0),
                Arrays.asList(15, 15, 15, 15, 15, 15), 200, state(92, 3),
                state(92, 3), state(92, 3), state(92, 0), state(0, 0), state(0, 0),
                ReloadBoundary.FRESH_LOGIN, 2);
    }

    private static CakeServingObservation badCollision() {
        CakeServingObservation valid = observation();
        return new CakeServingObservation(valid.states(), valid.health(), Arrays.asList(
                MovementDisposition.CORRECTED, MovementDisposition.CORRECTED,
                MovementDisposition.CORRECTED, MovementDisposition.UNCHALLENGED,
                MovementDisposition.UNCHALLENGED, MovementDisposition.UNCHALLENGED),
                0, 1_000, valid.blockLight(), valid.skyLight(), valid.tickWindow(),
                valid.tickBefore(), valid.tickAfter(), valid.reloaded(), valid.supported(),
                valid.unsupported(), valid.persisted(), valid.boundary(), valid.reloads());
    }

    private static BlockState state(int id, int metadata) {
        return new BlockState(id, metadata);
    }

    private static void expectFailure(Runnable action) {
        try { action.run(); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("cake serving drift was accepted");
    }
}
