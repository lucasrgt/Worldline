package worldline.b173;

/** Runs isolated deterministic branches from one immutable checkpoint. */
public final class B173Experiments {
    private B173Experiments() {}

    public static B173Comparison compare(B173Checkpoint checkpoint,
            B173Hypothesis baseline, B173Hypothesis alternative, int ticks) {
        if (checkpoint == null || baseline == null || alternative == null) {
            throw new NullPointerException("checkpoint and hypotheses are required");
        }
        if (ticks < 0) throw new IllegalArgumentException("ticks must not be negative");
        return new B173Comparison(run(checkpoint, baseline, ticks),
                run(checkpoint, alternative, ticks));
    }

    private static B173Observation run(B173Checkpoint checkpoint,
            B173Hypothesis hypothesis, int ticks) {
        B173Runtime runtime = checkpoint.restore();
        try {
            hypothesis.apply(runtime);
            runtime.tick(ticks);
            return runtime.observe();
        } finally {
            runtime.close();
        }
    }
}
