package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;
import worldline.m92.*;

/** Binds three removals and reverse restorations in one natural page. */
public final class WorldlineRecoveryEvent {
  private static final int AFTER = Integer.getInteger("worldline.recovery.after", 300),
                           GAP = Integer.getInteger("worldline.recovery.restoreAfter", 30);
  static final int[] requests = {-1, -1, -1, -1, -1, -1}, events = {-1, -1, -1, -1, -1, -1},
                     indices = {1, 2, 3, 3, 2, 1}, operations = {1, 1, 1, 2, 2, 2},
                     members = {15, 14, 13, 14, 15, 16};
  private static final boolean[] air = new boolean[4], restored = new boolean[4];
  private static int step;
  private WorldlineRecoveryEvent() {
  }
  public static void head(Minecraft client) {
    if (!WorldlinePagedBridge.running() || WorldlinePagedBridge.sealed())
      return;
    int x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y(), z = WorldlineCensusSync.z(),
        root = WorldlineCensusProbe.nonce();
    WorldlineRecoveryState.apply(client.world);
    if (step < 6 && readyToSend()) {
      if (AFTER != 300 || GAP != 30
          || !"8".equals(System.getProperty("aero.becell.rebuildsPerFrame")))
        throw new IllegalStateException("M92 runtime drift");
      int ordinal = step + 1, index = indices[step], operation = operations[step];
      MessagePacket packet = new MessagePacket(WorldlineRecoveryMod.CHANGE);
      packet.ints = new int[] {x, y + index, z, root, ordinal, operation, index};
      client.getNetworkHandler().sendPacket(packet);
      requests[step] = WorldlinePagedBridge.count();
      step++;
    }
    if (step > 0) {
      int at = step - 1, index = indices[at], operation = operations[at], ty = y + index;
      if (operation == 1)
        air[index] |= client.world.getBlockId(x, ty, z) == 0;
      else {
        WorldlineRecoveryState.apply(client.world);
        restored[index] = client.world.getBlockId(x, ty, z) == WorldlineCensusMod.block.id
            && client.world.getBlockEntity(x, ty, z) instanceof WorldlineCensusBlockEntity be
            && be.nonce() == root * 100 + index + 1;
      }
    }
  }
  private static boolean readyToSend() {
    return step == 0
        ? WorldlinePagedBridge.count() >= AFTER
        : events[step - 1] >= 0 && WorldlinePagedBridge.count() >= events[step - 1] + GAP;
  }
  public static void tail() {
    if (step == 0)
      return;
    int at = step - 1;
    if (events[at] >= 0)
      return;
    int ordinal = at + 1, index = indices[at], operation = operations[at],
        x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y() + index,
        z = WorldlineCensusSync.z(), root = WorldlineCensusProbe.nonce(),
        queued = Aero_BECellRenderer.queuedLastFrame();
    boolean accepted = operation == 1 ? WorldlineRecoveryState.removed(ordinal, x, y, z, root)
            && air[index] && queued == members[at]
                                      : WorldlineRecoveryState.restored(ordinal, x, y, z, root)
            && restored[index] && queued == members[at];
    if (accepted)
      events[at] = WorldlinePagedBridge.count();
  }
  static boolean valid() {
    for (int i = 0; i < 6; i++)
      if (requests[i] < 0 || events[i] < requests[i] || i > 0 && requests[i] < events[i - 1] + GAP)
        return false;
    return air[1] && air[2] && air[3] && restored[1] && restored[2] && restored[3];
  }
  static String diagnostic() {
    return "requests=" + join(requests) + " events=" + join(events) + " air=" + air[1] + "/"
        + air[2] + "/" + air[3] + " restored=" + restored[1] + "/" + restored[2] + "/"
        + restored[3];
  }
  private static String join(int[] v) {
    return v[0] + "/" + v[1] + "/" + v[2] + "/" + v[3] + "/" + v[4] + "/" + v[5];
  }
}
