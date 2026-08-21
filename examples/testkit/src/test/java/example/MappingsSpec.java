package example;

import worldline.test.SemanticSelector;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.*;

public final class MappingsSpec extends WorldlineSpec {
    @Override protected void define() {
        test("exposes evidence", context -> expect(entity("b1.7.3:pig").evidence()).toEqual("M141"));
        test("exposes access", context -> expect(item("b1.7.3:diamond_sword").access())
                .toEqual(SemanticSelector.Access.READ_ONLY));
    }
}
