package worldline.testapi;

import worldline.api.MobObservationSession;

/** Driver callback for one bounded small-slime kill and slimeball observation attempt. */
@FunctionalInterface
public interface SlimeDropAttempt {
    SlimeDropObservation attempt(MobObservationSession session, int attempt);
}
