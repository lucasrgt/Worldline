package worldline.testkit;

import java.util.ArrayList;
import java.util.List;

/** Public verifier for canonical native tile-entity render observations. */
public final class NativeTileEntityRenderFixture {
    private NativeTileEntityRenderFixture() { }

    public static NativeTileEntityRenderEvidence verify(NativeTileEntityRenderPlan plan,
            List<NativeTileEntityRenderObservation> observations) {
        if (plan == null || observations == null) throw new NullPointerException("tile render");
        require(observations.size() == plan.subjects().size(),
                "native tile-render observation census drift");
        List<NativeTileEntityRenderObservation> verified = new ArrayList<>();
        for (int index = 0; index < plan.subjects().size(); index++) {
            NativeTileEntityRenderSubject expected = plan.subjects().get(index);
            NativeTileEntityRenderObservation observed = observations.get(index);
            require(observed != null && expected.canonical().equals(
                    observed.subject().canonical()),
                    "native tile-render subject identity drift: " + expected.subject());
            require(observed.geometryPixels() > 0,
                    "native tile renderer emitted no geometry: " + expected.subject());
            verified.add(observed);
        }
        return new NativeTileEntityRenderEvidence(plan.family(), verified);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
