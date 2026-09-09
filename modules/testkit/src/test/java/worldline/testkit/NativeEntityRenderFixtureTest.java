package worldline.testkit;

import java.util.List;
import worldline.testapi.NativeEntityRenderSubject;
import worldline.testapi.NativeEntityRenderObservation;

/** Reject partial, duplicated, mismatched and empty renders at the public boundary. */
public final class NativeEntityRenderFixtureTest {
    private NativeEntityRenderFixtureTest() { }

    public static void main(String[] arguments) {
        NativeEntityRenderSubject item = new NativeEntityRenderSubject(
                "b1.7.3:entity/001", 1, "EntityItem", "ARCHETYPE");
        NativeEntityRenderSubject boat = new NativeEntityRenderSubject(
                "b1.7.3:entity/041", 41, "EntityBoat", "SINGULAR");
        NativeEntityRenderObservation first = frame(item, "RenderItem", 'a');
        NativeEntityRenderObservation second = frame(boat, "RenderBoat", 'b');
        List<NativeEntityRenderSubject> subjects = List.of(item, boat);
        NativeEntityRenderEvidence evidence = NativeEntityRenderFixture.verify(subjects,
                List.of(first, second));
        require(evidence.equals(NativeEntityRenderFixture.verify(subjects, List.of(first, second))),
                "equal frames produce equal evidence");
        require(evidence.canonical().contains("b1.7.3:entity/041#native-render|SINGULAR"),
                "claim identity absent");
        require(!evidence.equals(NativeEntityRenderFixture.verify(subjects,
                List.of(first, frame(boat, "RenderBoat", 'c')))), "changed pixels erased");
        reject(() -> NativeEntityRenderFixture.verify(subjects, List.of(first)));
        reject(() -> NativeEntityRenderFixture.verify(subjects, List.of(second, first)));
        reject(() -> NativeEntityRenderFixture.verify(List.of(item, item), List.of(first, first)));
        reject(() -> new NativeEntityRenderObservation(item, "RenderItem", 0, "a".repeat(64)));
        reject(() -> new NativeEntityRenderSubject("b1.7.3:entity/048", 48, "EntityLiving", "ARCHETYPE"));
        System.out.println("NativeEntityRenderFixtureTest passed");
    }

    private static NativeEntityRenderObservation frame(NativeEntityRenderSubject subject,
            String renderer, char digest) {
        return new NativeEntityRenderObservation(subject, renderer, 100,
                String.valueOf(digest).repeat(64));
    }
    private static void reject(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid native entity render accepted");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
