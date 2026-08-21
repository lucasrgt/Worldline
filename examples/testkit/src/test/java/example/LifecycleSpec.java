package example;

import java.util.concurrent.atomic.AtomicInteger;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.*;

public final class LifecycleSpec extends WorldlineSpec {
    private final AtomicInteger attempts = new AtomicInteger();
    @Override protected void define() {
        beforeEach(context -> attempts.incrementAndGet());
        afterEach(context -> context.attach("lifecycle.txt", "closed attempt " + context.attempt()));
        test("runs beforeEach", context -> expect(attempts.get()).toBeGreaterThan(0));
        test("exposes attempt", context -> expect(context.attempt()).toEqual(1));
    }
}
