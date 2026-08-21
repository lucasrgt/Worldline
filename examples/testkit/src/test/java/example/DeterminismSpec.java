package example;

import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;
import static worldline.test.Worldline.worldline;

public final class DeterminismSpec extends WorldlineSpec {
    @Override protected void define() {
        test("uses an explicit seed", worldline().runtime("b1.7.3").seed(173L)
                .run(context -> expect(context.seed()).toEqual(173L)));
        test("advances deterministic time", context -> expect(() -> context.runtime().world().time())
                .toChange(() -> context.tick()));
    }
}
