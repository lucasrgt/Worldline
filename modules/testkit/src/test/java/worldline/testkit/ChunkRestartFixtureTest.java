package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkUnload;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteObjectSpawn;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWindowKind;

final class ChunkRestartFixtureTest {
    private ChunkRestartFixtureTest() { }
    static void execute() {
        RemoteItemStack glass = new RemoteItemStack(20, 1, 0);
        RemoteContainerWindow reopened = new RemoteContainerWindow(
                new RemoteWindowDescriptor(1, RemoteWindowKind.CHEST, "Chest", 27),
                chestWindow(glass));
        RemoteDroppedItem firstItem = new RemoteDroppedItem(1, new RemoteItemStack(3, 1, 0),
                328.5D, 72D, 328.5D, 0D, 0D, 0D);
        RemoteDroppedItem secondItem = new RemoteDroppedItem(8, new RemoteItemStack(3, 1, 0),
                328.5D, 71D, 328.5D, 0D, 0D, 0D);
        RemoteObjectSpawn firstCart = new RemoteObjectSpawn(2, 10, 329 * 32 + 16,
                72 * 32 + 27, 328 * 32 + 16, 0, 0, 0, 0);
        RemoteObjectSpawn secondCart = new RemoteObjectSpawn(9, 10, 329 * 32 + 16,
                72 * 32 + 27, 328 * 32 + 16, 0, 0, 0, 0);
        ChunkRestartFixture.Evidence first = ChunkRestartFixture.await(20, 20,
                new BlockPosition(329, 73, 328), glass, reopened, firstItem, secondItem,
                firstCart, secondCart, new RemoteChunkUnload(20, 20, 48));
        ChunkRestartFixture.Evidence second = ChunkRestartFixture.await(20, 20,
                new BlockPosition(329, 73, 328), glass, reopened, secondItem, firstItem,
                secondCart, firstCart, new RemoteChunkUnload(20, 20, 41));
        require(first.equals(second) && first.cartType() == 10
                && first.item().equals(new RemoteItemStack(3, 1, 0))
                && first.stored().equals(glass),
                "chunk restart fixture is not equatable");
    }
    private static RemoteInventoryView chestWindow(RemoteItemStack glass) {
        List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int slot = 0; slot < 63; slot++)
            slots.add(new RemoteInventorySlot(slot, slot == 0 ? glass : null));
        return new RemoteInventoryView(1, slots);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
