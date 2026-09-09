package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.testapi.RandomOutcomeObservation;

/** Mutation checks prevent favorable-only sampling, reordered draws and invented loot evidence. */
public final class RandomOutcomeFixtureTest {
    private RandomOutcomeFixtureTest() { }
    public static void execute() {
        List<RandomOutcomeObservation> rows = RandomOutcomeFixture.expectations();
        RandomOutcomeEvidence evidence = RandomOutcomeFixture.verify(rows);
        require(evidence.equals(RandomOutcomeFixture.verify(new ArrayList<>(rows))), "evidence equality");
        reject(() -> RandomOutcomeFixture.verify(rows.subList(1, rows.size())));
        List<RandomOutcomeObservation> reordered = new ArrayList<>(rows);
        Collections.swap(reordered, 0, 1); reject(() -> RandomOutcomeFixture.verify(reordered));
        for (int index : new int[] {0, 10, 11, 510, 2410, rows.size() - 1}) {
            List<RandomOutcomeObservation> altered = new ArrayList<>(rows);
            RandomOutcomeObservation row = rows.get(index);
            altered.set(index, new RandomOutcomeObservation(row.key(), row.draws(), row.counter() + 1,
                    row.result(), row.item(), row.count(), row.dead()));
            reject(() -> RandomOutcomeFixture.verify(altered));
        }
        List<RandomOutcomeObservation> loot = new ArrayList<>(rows);
        RandomOutcomeObservation row = rows.get(10);
        loot.set(10, new RandomOutcomeObservation(row.key(), row.draws(), row.counter(), 1, 264, 1, true));
        reject(() -> RandomOutcomeFixture.verify(loot));
        require(evidence.observations().size() == 3228, "finite draw census");
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid finite draw evidence accepted");
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
