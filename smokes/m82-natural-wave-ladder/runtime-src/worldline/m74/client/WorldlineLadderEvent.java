package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;
import worldline.m82.*;

/** Requests one bounded wave and binds its first reduced-membership record. */
public final class WorldlineLadderEvent {
  private static final int AFTER = Integer.getInteger("worldline.ladder.after", 300),
                           TARGETS = Integer.getInteger("worldline.ladder.targets", 0);
  static boolean requested, acked, allAir;
  static int requestIndex = -1, eventIndex = -1, eventPages = -1, eventRebuilds = -1,
             eventDirect = -1, eventCache = -1;
  private WorldlineLadderEvent() {
  }
  private static int[] indices() {
    if (TARGETS == 1)
      return new int[] {0};
    if (TARGETS == 2)
      return new int[] {0, 4};
    if (TARGETS == 4)
      return new int[] {0, 1, 4, 5};
    throw new IllegalStateException("invalid M82 target count");
  }
  public static void head(Minecraft client) {
    if (!WorldlinePagedBridge.running() || WorldlinePagedBridge.sealed())
      return;
    int x = WorldlineCensusSync.x(), y = WorldlineCensusSync.y(), z = WorldlineCensusSync.z(),
        root = WorldlineCensusProbe.nonce();
    if (!requested && WorldlinePagedBridge.count() >= AFTER) {
      if (AFTER != 300 || !"8".equals(System.getProperty("aero.becell.rebuildsPerFrame")))
        throw new IllegalStateException("M82 runtime drift");
      MessagePacket p = new MessagePacket(WorldlineLadderMod.CHANGE);
      p.ints = new int[] {x, y, z, root, TARGETS};
      client.getNetworkHandler().sendPacket(p);
      requestIndex = WorldlinePagedBridge.count();
      requested = true;
    }
    if (requested) {
      acked = WorldlineLadderState.matches(x, y, z, root, TARGETS);
      allAir = true;
      for (int i : indices())
        allAir &= client.world.getBlockId(x, y + i % 4, z + i / 4) == 0;
    }
  }
  public static void tail() {
    if (!requested || !acked || !allAir || eventIndex >= 0
        || Aero_BECellRenderer.queuedLastFrame() != 16 - TARGETS)
      return;
    eventIndex = WorldlinePagedBridge.count();
    eventPages = Aero_BECellRenderer.pageCallsThisFrame();
    eventRebuilds = Aero_BECellRenderer.pageRebuildsThisFrame();
    eventDirect = Aero_BECellRenderer.directFallbacksThisFrame();
    eventCache = Aero_BECellRenderer.cachedPageCount();
  }
  static int expectedRebuilds() {
    return TARGETS == 1 ? 1 : 2;
  }
  static boolean valid() {
    return eventIndex >= requestIndex && allAir && eventPages == 2
        && eventRebuilds == expectedRebuilds() && eventDirect == 0 && eventCache == 2;
  }
  static String diagnostic() {
    return "targets=" + TARGETS + ",requested=" + requested + ",acked=" + acked + ",air=" + allAir
        + ",request=" + requestIndex + ",event=" + eventIndex + ",pages=" + eventPages
        + ",rebuilds=" + eventRebuilds + ",direct=" + eventDirect + ",cache=" + eventCache;
  }
}
