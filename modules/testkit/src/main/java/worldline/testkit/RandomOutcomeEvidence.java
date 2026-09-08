package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.testapi.RandomOutcomeObservation;

/** Equatable, complete finite-draw evidence; no stochastic trial counts are hidden. */
public final class RandomOutcomeEvidence {
    private final List<RandomOutcomeObservation> rows;
    RandomOutcomeEvidence(List<RandomOutcomeObservation> rows) {
        this.rows = Collections.unmodifiableList(new ArrayList<RandomOutcomeObservation>(rows));
    }
    public List<RandomOutcomeObservation> observations() { return rows; }
    public String canonical() {
        StringBuilder text = new StringBuilder("schema=worldline.native-random-outcomes.v1\n");
        for (RandomOutcomeObservation row : rows) text.append(row.canonical()).append('\n');
        return text.toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof RandomOutcomeEvidence && canonical().equals(((RandomOutcomeEvidence) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
}
