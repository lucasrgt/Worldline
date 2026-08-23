package worldline.m74.client;

import aero.modellib.Aero_FramePacer;
import net.minecraft.client.Minecraft;
import worldline.m74.*;

/** Qualifies the paged runtime before the first retained M74 interval. */
public final class WorldlinePagedGate {
  private static boolean prepared;
  private WorldlinePagedGate() {
  }
  public static boolean prepare(Minecraft minecraft) {
    if (prepared || !WorldlinePagedBridge.armed())
      return false;
    if (!WorldlineCensusProbe.mode().equals("present") || minecraft.options.fpsLimit != 0
        || Aero_FramePacer.ENABLED || Aero_FramePacer.targetFps() != 0)
      throw new IllegalStateException("M99 runtime configuration drift");
    WorldlinePagedBridge.align();
    prepared = true;
    WorldlinePagedTimer.arm();
    System.out.println("[WorldlinePaged] armed marker=true fpsLimit=0 aeroFramePacing=false");
    return true;
  }
}
