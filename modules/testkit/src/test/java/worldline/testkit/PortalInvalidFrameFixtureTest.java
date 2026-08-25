package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

final class PortalInvalidFrameFixtureTest {
    private PortalInvalidFrameFixtureTest() { }

    static void execute() {
        Geometry geometry = geometry();
        PortalInvalidFrameFixture.Evidence first = verify(world(geometry, false),
                world(geometry, false), geometry, true);
        PortalInvalidFrameFixture.Evidence second = verify(world(geometry, false),
                world(geometry, false), geometry, true);
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.obsidianCells() == 13 && first.missingAir()
                && first.fireObserved() && first.livePortalCells() == 0
                && first.persistedPortalCells() == 0,
                "portal rejection evidence was not equatable");
        reject(() -> verify(world(geometry, false), world(geometry, false), geometry, false));
        reject(() -> verify(world(geometry, true), world(geometry, false), geometry, true));
        reject(() -> verify(world(geometry, false), world(geometry, true), geometry, true));
    }

    private static PortalInvalidFrameFixture.Evidence verify(RemoteWorldView live,
            RemoteWorldView persisted, Geometry geometry, boolean fireObserved) {
        return PortalInvalidFrameFixture.reject(live, persisted, geometry.frame,
                geometry.missing, geometry.interior, fireObserved);
    }
    private static RemoteWorldView world(Geometry geometry, boolean portal) {
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 81920);
        RemoteChunkSnapshot chunk = new RemoteChunkSnapshot(region, new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        for (BlockPosition cell : geometry.frame)
            chunk = chunk.withBlock(cell.x(), cell.y(), cell.z(), new BlockState(49, 0));
        if (portal) chunk = chunk.withBlock(5, 66, 4, new BlockState(90, 1));
        return new RemoteWorldView(List.of(chunk));
    }
    private static Geometry geometry() {
        BlockPosition bottom = new BlockPosition(4, 65, 4);
        List<BlockPosition> frame = new ArrayList<>(), interior = new ArrayList<>();
        for (int x = 0; x < 4; x++) frame.add(new BlockPosition(bottom.x() + x, 65, 4));
        for (int y = 1; y <= 4; y++) {
            frame.add(new BlockPosition(4, 65 + y, 4));
            frame.add(new BlockPosition(7, 65 + y, 4));
        }
        frame.add(new BlockPosition(5, 69, 4));
        for (int y = 1; y <= 3; y++) for (int x = 1; x <= 2; x++)
            interior.add(new BlockPosition(4 + x, 65 + y, 4));
        return new Geometry(frame, new BlockPosition(6, 69, 4), interior);
    }
    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid portal rejection accepted"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static final class Geometry {
        final List<BlockPosition> frame, interior;
        final BlockPosition missing;
        Geometry(List<BlockPosition> frame, BlockPosition missing, List<BlockPosition> interior) {
            this.frame = frame; this.missing = missing; this.interior = interior;
        }
    }
}
