package worldline.testkit;

import java.util.Objects;
import worldline.api.MobObservationSession;

/** Connects public mob observation to controlled slime motion, split and drop callbacks. */
public final class SlimeObservationLifecycleScenario implements SlimeLifecycleScenario {
    private final MobObservationSession session;
    private final SlimeMotionAction motion;
    private final SlimeSplitAttempt split;
    private final SlimeDropAttempt drop;

    public SlimeObservationLifecycleScenario(MobObservationSession session,
            SlimeMotionAction motion, SlimeSplitAttempt split, SlimeDropAttempt drop) {
        this.session = Objects.requireNonNull(session, "session");
        this.motion = Objects.requireNonNull(motion, "motion");
        this.split = Objects.requireNonNull(split, "split");
        this.drop = Objects.requireNonNull(drop, "drop");
    }

    @Override public SlimeMotionObservation observeMotion(SlimeMotionScene scene) {
        return motion.observe(session, scene);
    }

    @Override public SlimeSplitObservation attemptSplit(int attempt) {
        return split.attempt(session, attempt);
    }

    @Override public SlimeDropObservation attemptDrop(int attempt) {
        return drop.attempt(session, attempt);
    }
}
