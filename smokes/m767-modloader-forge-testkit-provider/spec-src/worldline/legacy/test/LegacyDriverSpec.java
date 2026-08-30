package worldline.legacy.test;

import java.util.LinkedHashSet;
import java.util.Set;
import worldline.api.GamePosition;
import worldline.api.RuntimeState;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.afterAll;
import static worldline.test.Worldline.test;

/** Two fresh-session checks shared by the ModLoader and Forge providers. */
public final class LegacyDriverSpec extends WorldlineSpec {
    private static final Set<String> USERS = new LinkedHashSet<String>();
    @Override protected void define() {
        test("first isolated legacy session", context -> exercise(context));
        test("second isolated legacy session", context -> exercise(context));
        afterAll(() -> expect(USERS.size()).toEqual(2));
    }

    private static void exercise(worldline.test.TestContext context) {
        expect(context.runtime().state()).toEqual(RuntimeState.WORLD_LOADED);
        String username = context.runtime().player().username();
        if (!USERS.add(username)) throw new AssertionError("legacy session identity was reused");
        expect(username).toContain("Wl");
        expect(context.runtime().player().health()).toBeGreaterThan(0);
        GamePosition position = context.runtime().player().position();
        if (!Double.isFinite(position.x()) || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) throw new AssertionError("non-finite legacy pose");
        long before = context.runtime().world().time();
        context.runtime().tick();
        long after = context.runtime().world().time();
        if (after <= before) throw new AssertionError("legacy controlled tick did not advance world time");
    }
}
