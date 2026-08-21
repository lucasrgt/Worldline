package example;

import java.util.Arrays;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;

public final class SnapshotSpec extends WorldlineSpec {
    @Override protected void define() {
        test("snapshots player state", context -> expect(Arrays.asList(context.health(),
                context.selectedHotbarSlot())).toMatchSnapshot("player"));
        test("snapshots position", context -> expect(context.position().toString())
                .toMatchSnapshot("position"));
    }
}
