package worldline.testkit;

import java.util.List;

/** Locks the universal native 3D inventory-render evidence contract. */
public final class NativeBlockRenderFixtureTest {
    private NativeBlockRenderFixtureTest() { }

    public static void main(String[] arguments) {
        NativeBlockRenderSubject cube = subject(1, 0);
        NativeBlockRenderSubject stairs = subject(53, 10);
        NativeBlockRenderPlan plan = new NativeBlockRenderPlan(
                "native-3d-inventory-render", List.of(cube, stairs));
        NativeBlockRenderEvidence evidence = NativeBlockRenderFixture.verify(plan, List.of(
                observation(cube, 2048, "a".repeat(64), 6),
                observation(stairs, 1536, "b".repeat(64), 12)));
        require(evidence.observations().size() == 2, "native render census drifted");
        require(evidence.canonical().contains(
                "claim.2=b1.7.3:block/053#native-render|ARCHETYPE"),
                "native render claim is absent");
        reject(() -> new NativeBlockRenderSubject("b1.7.3:block/006", 6, 0, 1));
        reject(() -> NativeBlockRenderFixture.verify(plan, List.of(
                observation(stairs, 1536, "b".repeat(64), 12),
                observation(cube, 2048, "a".repeat(64), 6))));
        NativeWorldBlockRenderFixtureTest.main(arguments);
        System.out.println("NativeBlockRenderFixtureTest passed");
    }

    private static NativeBlockRenderSubject subject(int id, int renderType) {
        return new NativeBlockRenderSubject(String.format("b1.7.3:block/%03d", id),
                id, 0, renderType);
    }

    private static NativeBlockRenderObservation observation(NativeBlockRenderSubject subject,
            int pixels, String signature, int drawCalls) {
        return new NativeBlockRenderObservation(subject.subject(), subject.legacyId(),
                subject.metadata(), subject.renderType(), pixels, signature,
                "draw-calls=" + drawCalls + ",pixels=" + pixels);
    }

    private static void reject(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("invalid native render contract was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
