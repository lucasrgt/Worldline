package worldline.test;

import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineEvidence;
import worldline.api.WorldlineEvidenceDiff;

/** TestKit assertion that compares implementation evidence with a frozen vanilla behavior pin. */
public final class BehaviorExpectation {
    private final WorldlineEvidence actual;

    BehaviorExpectation(WorldlineEvidence actual) {
        if (actual == null) throw new NullPointerException("evidence");
        this.actual = actual;
    }

    public void toMatchVanilla(WorldlineBehavior behavior, String signal, String signature) {
        match(WorldlineEvidence.pin(behavior, signal, signature));
    }

    public void toMatchVanilla(String behaviorOrAtlas, String signal, String signature) {
        match(WorldlineEvidence.pin(behaviorOrAtlas, signal, signature));
    }

    public void toMatchEvidence(WorldlineEvidence expected) {
        if (expected == null) throw new NullPointerException("expected evidence");
        match(expected);
    }

    private void match(WorldlineEvidence expected) {
        WorldlineEvidenceDiff difference = expected.compare(actual);
        if (difference.diverged()) throw new WorldlineAssertionError(
                "behavior evidence diverged\n" + difference.render(), expected, actual);
    }
}
