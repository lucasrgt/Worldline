package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.testapi.RandomOutcomeObservation;

/** Public acceptance for exhaustive gravel draws, fishing weather/depth and catch-window timing. */
public final class RandomOutcomeFixture {
    private RandomOutcomeFixture() { }
    public static RandomOutcomeEvidence verify(List<RandomOutcomeObservation> observations) {
        List<RandomOutcomeObservation> expected = expectations();
        if (observations == null || observations.size() != expected.size())
            throw new IllegalStateException("incomplete random outcome matrix");
        for (int index = 0; index < expected.size(); index++) {
            RandomOutcomeObservation row = observations.get(index);
            if (row == null || !row.canonical().equals(expected.get(index).canonical()))
                throw new IllegalStateException("random outcome differs at " + expected.get(index).key()
                        + ": " + (row == null ? "null" : row.canonical()));
        }
        return new RandomOutcomeEvidence(observations);
    }
    static List<RandomOutcomeObservation> expectations() {
        List<RandomOutcomeObservation> rows = new ArrayList<RandomOutcomeObservation>();
        for (int draw = 0; draw < 10; draw++)
            rows.add(row("gravel/" + draw, "10:" + draw + ";", 0, draw == 0 ? 318 : 13, 0, 0, false));
        for (int depth : new int[] {1, 3, 5}) for (int bound : new int[] {500, 300}) {
            for (int draw = 0; draw < bound; draw++) {
                boolean bite = draw == 0;
                rows.add(row("bite/" + depth + "/" + bound + "/" + draw,
                        bound + ":" + draw + ";" + (bite ? "30:0;" : ""), bite ? 10 : 0,
                        bite ? 1 : 0, bite ? 349 : 0, bite ? 1 : 0, true));
            }
        }
        for (int draw = 0; draw < 30; draw++) {
            String key = "window/" + draw + "/";
            rows.add(row(key + 0, "500:0;30:" + draw + ";", draw + 10, 0, 0, 0, false));
            for (int tick = 1; tick <= draw + 10; tick++)
                rows.add(row(key + tick, "", draw + 10 - tick, 0, 0, 0, false));
            rows.add(row(key + "expired", "", 0, 0, 0, 0, true));
        }
        for (int first : new int[] {1, 5, 17}) for (int tick = 1; tick <= first; tick++) {
            boolean bite = tick == first;
            rows.add(row("first/" + first + "/" + tick, bite ? "500:0;30:0;" : "500:1;",
                    bite ? 10 : 0, 0, 0, 0, false));
        }
        return rows;
    }
    private static RandomOutcomeObservation row(String key, String draws, int counter, int result,
            int item, int count, boolean dead) {
        return new RandomOutcomeObservation(key, draws, counter, result, item, count, dead);
    }
}
