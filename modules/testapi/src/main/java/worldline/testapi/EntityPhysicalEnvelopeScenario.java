package worldline.testapi;

import java.util.List;

/** Captures canonical physical envelopes from one native entity registry. */
public interface EntityPhysicalEnvelopeScenario {
    List<EntityPhysicalEnvelopeObservation> observe();
}
