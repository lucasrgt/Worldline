package worldline.testkit;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkUnload;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;

final class ChunkReloadFixtureTest {
    private ChunkReloadFixtureTest() { }
    static void execute() {
        RemoteDroppedItem firstItem = new RemoteDroppedItem(1, new RemoteItemStack(3, 1, 0),
                328.5D, 72D, 328.5D, 0D, 0D, 0D);
        RemoteDroppedItem secondItem = new RemoteDroppedItem(8, new RemoteItemStack(3, 1, 0),
                328.5D, 71D, 328.5D, 0D, 0D, 0D);
        RemoteObjectSpawn firstCart = new RemoteObjectSpawn(2, 10, 329 * 32 + 16,
                72 * 32 + 27, 328 * 32 + 16, 0, 0, 0, 0);
        RemoteObjectSpawn secondCart = new RemoteObjectSpawn(9, 10, 329 * 32 + 16,
                72 * 32 + 27, 328 * 32 + 16, 0, 0, 0, 0);
        ChunkReloadFixture.Evidence first = ChunkReloadFixture.observe(20, 20,
                new BlockPosition(329, 72, 328), new BlockState(62, 2), new BlockState(62, 2),
                new RemoteItemStack(20, 1, 0), firstItem, secondItem, firstCart, secondCart,
                new RemoteChunkUnload(20, 20, 48));
        ChunkReloadFixture.Evidence second = ChunkReloadFixture.observe(20, 20,
                new BlockPosition(329, 72, 328), new BlockState(62, 2), new BlockState(62, 2),
                new RemoteItemStack(20, 1, 0), firstItem, secondItem, firstCart, secondCart,
                new RemoteChunkUnload(20, 20, 41));
        require(first.equals(second) && first.cartType() == 10
                && first.item().equals(new RemoteItemStack(3, 1, 0)),
                "chunk reload fixture is not equatable");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
