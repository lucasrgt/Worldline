package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteItemStack;

/** Builds a fixed four-cell state-domain arena through official gameplay actions. */
final class B173StateDomainArena {
    static final long SEED = 17_320_110_707L;
    static final String USERNAME = "WlStateDomain";
    static final BlockState SUPPORT_STATE = new BlockState(1, 0);
    static final List<BlockPosition> SUPPORTS = Collections.unmodifiableList(Arrays.asList(
            new BlockPosition(4, 71, 4), new BlockPosition(6, 71, 4),
            new BlockPosition(4, 71, 6), new BlockPosition(6, 71, 6)));

    private B173StateDomainArena() { }

    static B173WireClient open(B173DedicatedServer server, Path workspace, int port,
            Duration timeout, B173StateDomainLoadout loadout) throws Exception {
        seed(workspace, loadout);
        B173WireClient client = new B173WireClient("127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect();
            client.synchronizePose();
            int occupied = loadout.support.hotbar == 0 ? 2 : 3;
            require(client.awaitInventory().occupiedSlots() == occupied,
                    "state-domain inventory drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = foundation(initial);
            int column = 0;
            client.selectHeldSlot(0);
            while (B173FixtureSupport.water(
                    initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                client.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "state-domain water column exceeded fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                client.moveAndObserve(0D, 1D, 0D, 1);
                column++;
            }
            BlockPosition east = B173FixtureSupport.place(client, top, BlockFace.EAST, 1);
            BlockPosition northEast = B173FixtureSupport.place(client, east, BlockFace.EAST, 1);
            BlockPosition south = B173FixtureSupport.place(client, top, BlockFace.SOUTH, 1);
            BlockPosition southWest = B173FixtureSupport.place(client, south, BlockFace.SOUTH, 1);
            BlockPosition southMiddle = B173FixtureSupport.place(
                    client, southWest, BlockFace.EAST, 1);
            BlockPosition southEast = B173FixtureSupport.place(
                    client, southMiddle, BlockFace.EAST, 1);
            require(column == 17 && Arrays.asList(top, northEast, southWest, southEast)
                    .equals(SUPPORTS), "state-domain support grid drift");
            overlay(client, loadout);
            for (BlockPosition support : SUPPORTS) {
                client.awaitBlock(support, SUPPORT_STATE);
                BlockState above = loadout.support.hotbar == 0
                        ? new BlockState(0, 0) : loadout.support.state;
                client.awaitBlock(BlockFace.UP.adjacent(support), above);
            }
            return client;
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static void seed(Path workspace, B173StateDomainLoadout loadout) {
        RemoteItemStack item = loadout.item;
        if (loadout.support.hotbar == 0) {
            B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                    new int[] {0, loadout.hotbar}, new int[] {1, item.legacyId()},
                    new int[] {48, item.count()}, new int[] {0, item.damage()});
            return;
        }
        RemoteItemStack support = loadout.support.item;
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, loadout.hotbar, loadout.support.hotbar},
                new int[] {1, item.legacyId(), support.legacyId()},
                new int[] {48, item.count(), support.count()},
                new int[] {0, item.damage(), support.damage()});
    }

    private static void overlay(B173WireClient client, B173StateDomainLoadout loadout)
            throws Exception {
        if (loadout.support.hotbar == 0) {
            return;
        }
        client.selectHeldSlot(loadout.support.hotbar);
        for (BlockPosition pad : SUPPORTS) {
            B173FixtureSupport.place(client, pad, BlockFace.UP, loadout.support.state);
        }
        client.selectHeldSlot(0);
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) {
            for (int y = 126; y >= 1; y--) {
                if (chunk.blockAt(x, y, z).legacyId() == 3
                        && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                    return new BlockPosition(x, y, z);
                }
            }
        }
        throw new IllegalStateException("deterministic state-domain foundation is absent");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
