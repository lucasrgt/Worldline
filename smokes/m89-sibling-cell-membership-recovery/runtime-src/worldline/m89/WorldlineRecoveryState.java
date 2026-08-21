package worldline.m89;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Primitive remove/restore acknowledgement and buffered restore state. */
public final class WorldlineRecoveryState {
    private static boolean removeAck, restoreAck, restoreReceived, restoreApplied;
    private static int x, y, z, root, restoreNonce;
    private WorldlineRecoveryState() {}
    public static synchronized void ack(int[] values) {
        if (values == null || values.length != 5 || values[4] < 1 || values[4] > 2) throw new IllegalStateException("invalid M89 ack");
        if (values[4] == 1) { if (removeAck) throw new IllegalStateException("duplicate M89 remove ack");
            x = values[0]; y = values[1]; z = values[2]; root = values[3]; removeAck = true; return; }
        if (!removeAck || restoreAck || values[0] != x || values[1] != y || values[2] != z || values[3] != root)
            throw new IllegalStateException("invalid M89 restore ack"); restoreAck = true;
    }
    public static synchronized void restore(int[] values) {
        if (values == null || values.length != 4 || values[3] <= 0) throw new IllegalStateException("invalid M89 restore state");
        if (restoreReceived) { if (values[0] != x || values[1] != y || values[2] != z || values[3] != restoreNonce)
            throw new IllegalStateException("conflicting M89 restore state"); return; }
        if (!removeAck || values[0] != x || values[1] != y || values[2] != z || values[3] != root * 100 + 5)
            throw new IllegalStateException("unbound M89 restore state"); restoreNonce = values[3]; restoreReceived = true;
    }
    public static synchronized void apply(World world) {
        if (!restoreReceived || restoreApplied || world == null || world.getBlockId(x, y, z) != WorldlineCensusMod.block.id) return;
        BlockEntity raw = world.getBlockEntity(x, y, z); if (raw != null && !(raw instanceof WorldlineCensusBlockEntity))
            throw new IllegalStateException("M89 restored BE type drift");
        WorldlineCensusBlockEntity be = raw == null ? new WorldlineCensusBlockEntity() : (WorldlineCensusBlockEntity) raw;
        if (be.nonce() != 0 && be.nonce() != restoreNonce) throw new IllegalStateException("M89 restored nonce drift");
        be.setNonce(restoreNonce); if (raw == null) world.setBlockEntity(x, y, z, be); restoreApplied = true;
    }
    public static synchronized boolean removed(int ex, int ey, int ez, int nonce) { return removeAck && x == ex && y == ey && z == ez && root == nonce; }
    public static synchronized boolean restored(int ex, int ey, int ez, int nonce) { return restoreAck && restoreReceived && restoreApplied && x == ex && y == ey && z == ez && root == nonce; }
}
