package example;

import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.*;

public final class BlocksSpec extends WorldlineSpec {
    @Override protected void define() {
        test("reads promoted air", context -> expect(context.block(pos(8, 70, 8)))
                .toEqual(block("b1.7.3:air"))).tag("block");
        test("writes promoted glass", context -> {
            context.setBlock(pos(9, 65, 9), block("b1.7.3:glass"));
            expect(context.block(pos(9, 65, 9))).toEqual(block("b1.7.3:glass"));
        }).tag("block");
    }
}
