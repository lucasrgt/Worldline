package example;

import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;

public final class ArtifactsSpec extends WorldlineSpec {
    @Override protected void define() {
        test("attaches text", context -> {
            context.attach("note.txt", "author-owned diagnostic"); expect(true).toBeTrue();
        });
        test("exposes artifact directory", context -> expect(context.artifactDirectory())
                .notToBeNull());
    }
}
