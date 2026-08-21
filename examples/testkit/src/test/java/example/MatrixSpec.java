package example;

import java.util.Arrays;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.each;

public final class MatrixSpec extends WorldlineSpec {
    @Override protected void define() {
        each(Arrays.asList(1, 2)).test("ticks row %# value %s", (context, ticks) -> {
            context.tick(ticks); expect(context.runtime().world().time()).toBeGreaterThan(0);
        });
    }
}
