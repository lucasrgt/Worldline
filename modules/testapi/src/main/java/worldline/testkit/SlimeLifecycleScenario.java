package worldline.testkit;

/** Driver-neutral scenes and bounded attempts for the complete slime mini-subsystem. */
public interface SlimeLifecycleScenario {
    SlimeMotionObservation observeMotion(SlimeMotionScene scene);
    SlimeSplitObservation attemptSplit(int attempt);
    SlimeDropObservation attemptDrop(int attempt);
}
