package example;

import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;

public final class PlayerSpec extends WorldlineSpec {
    @Override protected void define() {
        test("reports health", context -> expect(context.health()).toEqual(20));
        test("reports position", context -> expect(context.position().y()).toBeGreaterThan(0));
    }
}
