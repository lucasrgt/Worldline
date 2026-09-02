package worldline.testkit;
import worldline.testapi.NativeBlockRenderObservation;
import worldline.testapi.NativeBlockRenderPlan;
import worldline.testapi.NativeBlockRenderSubject;

import java.util.ArrayList;
import java.util.List;

/** Public verifier for canonical native client block-render observations. */
public final class NativeBlockRenderFixture {
    private NativeBlockRenderFixture() { }

    public static NativeBlockRenderEvidence verify(NativeBlockRenderPlan plan,
            List<NativeBlockRenderObservation> observations) {
        if (plan == null || observations == null) throw new NullPointerException("native render");
        require(observations.size() == plan.subjects().size(),
                "native render observation census drift");
        List<NativeBlockRenderObservation> verified =
                new ArrayList<NativeBlockRenderObservation>();
        for (int index = 0; index < plan.subjects().size(); index++) {
            NativeBlockRenderSubject expected = plan.subjects().get(index);
            NativeBlockRenderObservation observed = observations.get(index);
            require(observed != null, "native render observation is absent");
            require(expected.subject().equals(observed.subject())
                    && expected.legacyId() == observed.legacyId()
                    && expected.metadata() == observed.metadata(),
                    "native render subject identity drift: " + expected.subject());
            require(expected.renderType() == observed.renderType(),
                    "native render type drift: " + expected.subject());
            require(NativeBlockRenderSubject.supports3d(observed.renderType()),
                    "native 3D inventory route is absent: " + expected.subject());
            require(observed.geometryPixels() > 0,
                    "native render emitted no geometry: " + expected.subject());
            verified.add(observed);
        }
        return new NativeBlockRenderEvidence(plan.family(), verified);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
