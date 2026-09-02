package worldline.testkit;
import worldline.testapi.NativeWorldBlockRenderObservation;
import worldline.testapi.NativeWorldBlockRenderPlan;
import worldline.testapi.NativeWorldBlockRenderSubject;

import java.util.List;

/** Contract tests for reusable native special world-render evidence. */
public final class NativeWorldBlockRenderFixtureTest {
    private NativeWorldBlockRenderFixtureTest() { }

    public static void main(String[] arguments) {
        NativeWorldBlockRenderSubject plant = new NativeWorldBlockRenderSubject(
                "b1.7.3:block/006", 6, 0, 1);
        NativeWorldBlockRenderSubject fluid = new NativeWorldBlockRenderSubject(
                "b1.7.3:block/008", 8, 0, 4);
        NativeWorldBlockRenderPlan plan = new NativeWorldBlockRenderPlan(
                "native-special-world-render", List.of(plant, fluid));
        NativeWorldBlockRenderObservation plantFrame = observation(plant, 41, 'a');
        NativeWorldBlockRenderObservation fluidFrame = observation(fluid, 73, 'b');
        NativeWorldBlockRenderEvidence evidence = NativeWorldBlockRenderFixture.verify(
                plan, List.of(plantFrame, fluidFrame));
        require(evidence.observations().size() == 2, "verified census drift");
        require(evidence.canonical().contains(
                "b1.7.3:block/008#native-render|ARCHETYPE"), "claim is absent");
        expectFailure(() -> NativeWorldBlockRenderFixture.verify(plan,
                List.of(fluidFrame, plantFrame)), "reordered observations were accepted");
        expectFailure(() -> new NativeWorldBlockRenderSubject(
                "b1.7.3:block/006", 6, 0, 0), "inventory route was accepted");
        System.out.println("NativeWorldBlockRenderFixtureTest passed");
    }

    private static NativeWorldBlockRenderObservation observation(
            NativeWorldBlockRenderSubject subject, int pixels, char hash) {
        return new NativeWorldBlockRenderObservation(subject.subject(), subject.legacyId(),
                subject.metadata(), subject.renderType(), pixels, String.valueOf(hash).repeat(64),
                "draw-calls=1,pixels=" + pixels);
    }

    private static void expectFailure(Runnable action, String message) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError(message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
