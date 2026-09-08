package worldline.testkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.testapi.NativeEntityRenderSubject;
import worldline.testapi.NativeEntityRenderObservation;

/** Public acceptance boundary for an exact ordered family of concrete native entity renders. */
public final class NativeEntityRenderFixture {
    private NativeEntityRenderFixture() { }

    public static NativeEntityRenderEvidence verify(List<NativeEntityRenderSubject> subjects,
            List<NativeEntityRenderObservation> observations) {
        if (subjects == null || observations == null) throw new NullPointerException("entity render");
        require(!subjects.isEmpty() && subjects.size() == observations.size(), "render census drift");
        List<NativeEntityRenderObservation> accepted = new ArrayList<NativeEntityRenderObservation>();
        Set<String> unique = new HashSet<String>();
        for (int index = 0; index < subjects.size(); index++) {
            NativeEntityRenderSubject subject = subjects.get(index);
            NativeEntityRenderObservation observation = observations.get(index);
            require(subject != null && unique.add(subject.subject()), "duplicate render subject");
            require(observation != null && subject.canonical().equals(
                    observation.subject().canonical()), "render subject identity drift");
            require(observation.geometryPixels() > 0, "renderer emitted no geometry");
            accepted.add(observation);
        }
        return new NativeEntityRenderEvidence(accepted);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
