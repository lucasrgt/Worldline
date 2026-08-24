package worldline.testkit;

import worldline.api.BlockPosition;
import worldline.api.RemoteMapContent;

final class MapDataContentFixtureTest {
    private MapDataContentFixtureTest() { }

    static void execute() {
        byte[] colors = new byte[RemoteMapContent.WIDTH * RemoteMapContent.WIDTH];
        colors[0] = 4; colors[129] = 9;
        RemoteMapContent content = new RemoteMapContent(358, 0, 128, 130, 2, colors);
        BlockPosition position = new BlockPosition(4, 60, 4);
        MapDataContentFixture.Evidence first = MapDataContentFixture.observe(
                17320110707L, position, content);
        MapDataContentFixture.Evidence second = MapDataContentFixture.observe(
                17320110707L, position, content);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.seed() == 17320110707L && first.position().equals(position)
                && first.columns() == 128 && first.nonZero() == 2 && first.palette() == 3
                && first.colorsSha256().matches("[0-9a-f]{64}"),
                "map content evidence drifted");
        fail(() -> MapDataContentFixture.observe(1L, position,
                new RemoteMapContent(358, 0, 127, 130, 2, colors)));
    }

    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid map content accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
