package worldline.stationapi.test;

import java.util.LinkedHashSet;
import java.util.Set;
import worldline.api.GamePosition;
import worldline.api.RuntimeState;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.afterAll;
import static worldline.test.Worldline.test;

/** Two external TestKit cases proving fresh StationAPI processes and controlled ticks. */
public final class StationApiDriverSpec extends WorldlineSpec {
    private static final Set<String> USERS = new LinkedHashSet<String>();
    @Override protected void define() {
        test("first isolated StationAPI session", context -> exercise(context));
        test("second isolated StationAPI session", context -> exercise(context));
        afterAll(() -> expect(USERS.size()).toEqual(2));
    }

    private static void exercise(worldline.test.TestContext context) {
        expect(context.runtime().state()).toEqual(RuntimeState.WORLD_LOADED);
        String username = context.runtime().player().username();
        if (!USERS.add(username)) throw new AssertionError("StationAPI session identity was reused");
        expect(username).toContain("WlSta");
        expect(context.runtime().player().health()).toBeGreaterThan(0);
        GamePosition position = context.runtime().player().position();
        if (!Double.isFinite(position.x()) || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) throw new AssertionError("non-finite remote pose");
        long before = context.runtime().world().time();
        context.runtime().tick();
        long after = context.runtime().world().time();
        if (after < before) throw new AssertionError("StationAPI world time moved backwards");
    }
}
