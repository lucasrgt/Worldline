package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Public acceptance rejects fake acknowledgements, missing actions and elapsed command ticks. */
public final class WorldControlFixtureTest {
    private WorldControlFixtureTest() { }
    static void execute() {
        List<String> rows = Arrays.asList("write=true", "block=35:7", "repeat=false", "remove=true", "air=0",
                "pose=8.5,122.0,8.5", "selected=8", "selected=0", "given=3", "screen=inventory", "slots=45",
                "source=1:3", "picked=0", "cursor-inclusive=3", "destination=1:3", "closed=true", "retained=3",
                "command-ticks=0", "native-ticks=1");
        WorldControlEvidence evidence = WorldControlFixture.verify(new WorldControlEvidence(rows));
        if (!evidence.equals(new WorldControlEvidence(new ArrayList<>(rows)))) throw new AssertionError("equality");
        for (int index = 0; index < rows.size(); index++) {
            List<String> changed = new ArrayList<>(rows); changed.set(index, "fabricated"); reject(changed);
        }
        reject(rows.subList(1, rows.size()));
        List<String> reordered = new ArrayList<>(rows); Collections.swap(reordered, 0, 1); reject(reordered);
    }
    private static void reject(List<String> rows) {
        try { WorldControlFixture.verify(new WorldControlEvidence(rows)); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("invalid control evidence accepted");
    }
}
