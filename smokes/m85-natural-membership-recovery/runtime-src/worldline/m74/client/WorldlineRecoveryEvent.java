package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;
import worldline.m74.WorldlineCensusProbe;
import worldline.m74.WorldlineCensusSync;
import worldline.m74.WorldlinePagedBridge;
import worldline.m85.WorldlineRecoveryMod;
import worldline.m85.WorldlineRecoveryState;

/** Binds exact remove and restore requests to the first 15- and 16-member rebuild records. */
public final class WorldlineRecoveryEvent {
    private static final int AFTER = Integer.getInteger("worldline.recovery.after", 300);
    private static final int RESTORE_AFTER = Integer.getInteger("worldline.recovery.restoreAfter", 30);
    static boolean removeRequested, restoreRequested, blockAir, blockRestored, beRestored;
    static int removeRequest = -1, removeEvent = -1, restoreRequest = -1, restoreEvent = -1;
    static int removePages = -1, removeRebuilds = -1, removeDirect = -1, removeCache = -1;
    static int restorePages = -1, restoreRebuilds = -1, restoreDirect = -1, restoreCache = -1;
    private WorldlineRecoveryEvent() {}
    public static void head(Minecraft client) {
        if (!WorldlinePagedBridge.running() || WorldlinePagedBridge.sealed()) return; int x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y(),
                z = WorldlineCensusSync.z(), root = WorldlineCensusProbe.nonce(); WorldlineRecoveryState.apply(client.world);
        if (!removeRequested && WorldlinePagedBridge.count() >= AFTER) {
            if (AFTER != 300 || RESTORE_AFTER != 30 || !"8".equals(System.getProperty("aero.becell.rebuildsPerFrame"))) throw new IllegalStateException("M85 runtime drift");
            send(client, x, y, z, root, 1); removeRequest = WorldlinePagedBridge.count(); removeRequested = true;
        }
        if (removeRequested && !restoreRequested) blockAir |= client.world.getBlockId(x, y, z) == 0;
        if (removeEvent >= 0 && !restoreRequested && WorldlinePagedBridge.count() >= removeEvent + RESTORE_AFTER) {
            send(client, x, y, z, root, 2); restoreRequest = WorldlinePagedBridge.count(); restoreRequested = true;
        }
        if (restoreRequested) { WorldlineRecoveryState.apply(client.world); blockRestored = client.world.getBlockId(x, y, z) == WorldlineCensusMod.block.id;
            beRestored = client.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be && be.nonce() == root * 100 + 1; }
    }
    public static void tail() {
        int x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y(), z = WorldlineCensusSync.z(), root = WorldlineCensusProbe.nonce(), queued = Aero_BECellRenderer.queuedLastFrame();
        if (removeRequested && removeEvent < 0 && WorldlineRecoveryState.removed(x, y, z, root) && blockAir && queued == 15) {
            removeEvent = WorldlinePagedBridge.count(); removePages = Aero_BECellRenderer.pageCallsThisFrame(); removeRebuilds = Aero_BECellRenderer.pageRebuildsThisFrame();
            removeDirect = Aero_BECellRenderer.directFallbacksThisFrame(); removeCache = Aero_BECellRenderer.cachedPageCount();
        }
        if (restoreRequested && restoreEvent < 0 && WorldlineRecoveryState.restored(x, y, z, root) && blockRestored && beRestored && queued == 16) {
            restoreEvent = WorldlinePagedBridge.count(); restorePages = Aero_BECellRenderer.pageCallsThisFrame(); restoreRebuilds = Aero_BECellRenderer.pageRebuildsThisFrame();
            restoreDirect = Aero_BECellRenderer.directFallbacksThisFrame(); restoreCache = Aero_BECellRenderer.cachedPageCount();
        }
    }
    private static void send(Minecraft client, int x, int y, int z, int root, int phase) { MessagePacket packet = new MessagePacket(WorldlineRecoveryMod.CHANGE);
        packet.ints = new int[]{x, y, z, root, phase}; client.getNetworkHandler().sendPacket(packet); }
    static boolean valid() { return removeEvent >= removeRequest && restoreRequest >= removeEvent + RESTORE_AFTER && restoreEvent >= restoreRequest && blockAir && blockRestored && beRestored
            && removePages == 3 && removeRebuilds == 0 && removeDirect == 1 && removeCache == 4
            && restorePages == 4 && restoreRebuilds == 1 && restoreDirect == 0 && restoreCache == 4; }
    static String diagnostic() { return "requests=" + removeRequest + "/" + restoreRequest + ",events=" + removeEvent + "/" + restoreEvent
            + ",air=" + blockAir + ",restored=" + blockRestored + "/" + beRestored + ",pages=" + removePages + "/" + restorePages
            + ",rebuilds=" + removeRebuilds + "/" + restoreRebuilds + ",direct=" + removeDirect + "/" + restoreDirect + ",cache=" + removeCache + "/" + restoreCache; }
}
