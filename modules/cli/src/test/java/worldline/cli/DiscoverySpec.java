package worldline.cli;

/** Top-level fixture used to verify bounded automatic spec discovery. */
public final class DiscoverySpec extends worldline.test.WorldlineSpec {
    @Override protected void define() {
        worldline.test.Worldline.test("discovered", context ->
                worldline.test.Expect.expect(context.seed()).toEqual(173L));
    }
}
