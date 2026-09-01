package worldline.testkit;

import java.util.ArrayList;
import java.util.List;

/** Public verifier for canonical native special world-render observations. */
public final class NativeWorldBlockRenderFixture {
    private NativeWorldBlockRenderFixture() { }

    public static NativeWorldBlockRenderEvidence verify(NativeWorldBlockRenderPlan plan,
            List<NativeWorldBlockRenderObservation> observations) {
        if (plan == null || observations == null) throw new NullPointerException("world render");
        require(observations.size() == plan.subjects().size(),
                "native world-render observation census drift");
        List<NativeWorldBlockRenderObservation> verified = new ArrayList<>();
        for (int index = 0; index < plan.subjects().size(); index++) {
            NativeWorldBlockRenderSubject expected = plan.subjects().get(index);
            NativeWorldBlockRenderObservation observed = observations.get(index);
            require(observed != null, "native world-render observation is absent");
            require(expected.subject().equals(observed.subject())
                    && expected.legacyId() == observed.legacyId()
                    && expected.metadata() == observed.metadata(),
                    "native world-render subject identity drift: " + expected.subject());
            require(expected.renderType() == observed.renderType()
                    && NativeWorldBlockRenderSubject.supportsSpecialRoute(observed.renderType()),
                    "native special world route drift: " + expected.subject());
            require(observed.geometryPixels() > 0,
                    "native world renderer emitted no geometry: " + expected.subject());
            verified.add(observed);
        }
        return new NativeWorldBlockRenderEvidence(plan.family(), verified);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
