package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;
import worldline.m87.*;

/** Binds exact recovery of cell indices zero and one to four transition records. */
public final class WorldlineRecoveryEvent {
  private static final int AFTER = Integer.getInteger("worldline.recovery.after", 300),
                           GAP = Integer.getInteger("worldline.recovery.restoreAfter", 30);
  static final int[] requests = {-1, -1, -1, -1}, events = {-1, -1, -1, -1}, indices = {0, 0, 1, 1};
  private static final boolean[] air = new boolean[2], restored = new boolean[2];
  private static int step;
  private WorldlineRecoveryEvent() {
  }
  public static void head(Minecraft client) {
    if (!WorldlinePagedBridge.running() || WorldlinePagedBridge.sealed())
      return;
    int x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y(), z = WorldlineCensusSync.z(),
        root = WorldlineCensusProbe.nonce();
    WorldlineRecoveryState.apply(client.world);
    if (step < 4 && readyToSend()) {
      if (AFTER != 300 || GAP != 30
          || !"8".equals(System.getProperty("aero.becell.rebuildsPerFrame")))
        throw new IllegalStateException("M87 runtime drift");
      int generation = step / 2 + 1, operation = step % 2 + 1, index = indices[step];
      MessagePacket packet = new MessagePacket(WorldlineRecoveryMod.CHANGE);
      packet.ints = new int[] {x, y + index, z, root, generation, operation, index};
      client.getNetworkHandler().sendPacket(packet);
      requests[step] = WorldlinePagedBridge.count();
      step++;
    }
    if (step > 0) {
      int index = indices[step - 1], generation = index + 1, ty = y + index;
      if (step % 2 == 1)
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
    int index = indices[at], generation = index + 1, operation = at % 2 + 1,
        x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y() + index,
        z = WorldlineCensusSync.z(), root = WorldlineCensusProbe.nonce(),
        queued = Aero_BECellRenderer.queuedLastFrame();
    boolean accepted = operation == 1
        ? WorldlineRecoveryState.removed(generation, x, y, z, root) && air[index] && queued == 15
        : WorldlineRecoveryState.restored(generation, x, y, z, root) && restored[index]
            && queued == 16;
    if (!accepted)
      return;
    events[at] = WorldlinePagedBridge.count();
  }
  static boolean valid() {
    for (int i = 0; i < 4; i++)
      if (requests[i] < 0 || events[i] < requests[i] || i > 0 && requests[i] < events[i - 1] + GAP)
        return false;
    return air[0] && air[1] && restored[0] && restored[1];
  }
  static String diagnostic() {
    return "requests=" + join(requests) + " events=" + join(events) + " air=" + air[0] + "/"
        + air[1] + " restored=" + restored[0] + "/" + restored[1];
  }
  private static String join(int[] v) {
    return v[0] + "/" + v[1] + "/" + v[2] + "/" + v[3];
  }
}
