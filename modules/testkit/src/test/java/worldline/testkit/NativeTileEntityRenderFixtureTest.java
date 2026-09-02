package worldline.testkit;
import worldline.testapi.NativeTileEntityRenderObservation;
import worldline.testapi.NativeTileEntityRenderPlan;
import worldline.testapi.NativeTileEntityRenderSubject;

import java.util.List;

/** Contract tests for reusable native tile-entity render evidence. */
public final class NativeTileEntityRenderFixtureTest {
    private NativeTileEntityRenderFixtureTest() { }

    public static void main(String[] arguments) {
        NativeTileEntityRenderSubject moving = subject(36, 5, "moving-piston", "SINGULAR");
        NativeTileEntityRenderSubject standing = subject(63, 0, "sign", "ARCHETYPE");
        NativeTileEntityRenderPlan plan = new NativeTileEntityRenderPlan(
                "native-tile-entity-render", List.of(moving, standing));
        NativeTileEntityRenderObservation movingFrame = observation(moving, 61, 'a');
        NativeTileEntityRenderObservation signFrame = observation(standing, 43, 'b');
        NativeTileEntityRenderEvidence evidence = NativeTileEntityRenderFixture.verify(
                plan, List.of(movingFrame, signFrame));
        require(evidence.canonical().contains(
                "b1.7.3:block/036#native-render|SINGULAR"), "singular claim is absent");
        require(evidence.canonical().contains(
                "b1.7.3:block/063#native-render|ARCHETYPE"), "archetype claim is absent");
        reject(() -> NativeTileEntityRenderFixture.verify(plan,
                List.of(signFrame, movingFrame)));
        reject(() -> subject(68, 2, "moving-piston", "ARCHETYPE"));
        System.out.println("NativeTileEntityRenderFixtureTest passed");
    }

    private static NativeTileEntityRenderSubject subject(int id, int metadata,
            String renderer, String layer) {
        return new NativeTileEntityRenderSubject(String.format("b1.7.3:block/%03d", id),
                id, metadata, renderer, layer);
    }

    private static NativeTileEntityRenderObservation observation(
            NativeTileEntityRenderSubject subject, int pixels, char hash) {
        return new NativeTileEntityRenderObservation(subject, pixels,
                String.valueOf(hash).repeat(64), "draw-calls=1,pixels=" + pixels);
    }

    private static void reject(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid native tile-render contract was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
