package worldline.testkit;

import worldline.api.MobObservationSession;

/** Driver callback that executes one controlled slime-motion environment. */
@FunctionalInterface
public interface SlimeMotionAction {
    SlimeMotionObservation observe(MobObservationSession session, SlimeMotionScene scene);
}
