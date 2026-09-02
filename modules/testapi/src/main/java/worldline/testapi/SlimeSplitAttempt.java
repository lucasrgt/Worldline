package worldline.testapi;

import worldline.api.MobObservationSession;

/** Driver callback for one bounded parent-kill and child-observation attempt. */
@FunctionalInterface
public interface SlimeSplitAttempt {
    SlimeSplitObservation attempt(MobObservationSession session, int attempt);
}
