package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
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
    String minimum = System.getProperty("aero.becell.minInstances"),
           cache = System.getProperty("aero.becell.maxCachedPages");
    if (!WorldlineCensusProbe.mode().equals("present") || !Aero_BECellRenderer.ENABLED
        || !("2".equals(minimum) || "5".equals(minimum)) || !"-1".equals(cache)
        || minecraft.options.fpsLimit != 0 || Aero_FramePacer.ENABLED
        || Aero_FramePacer.targetFps() != 0)
      throw new IllegalStateException("M106 runtime configuration drift");
    WorldlinePagedBridge.align();
    prepared = true;
    WorldlinePagedTimer.arm();
    System.out.println("[WorldlinePaged] armed marker=true pages=true minimum=" + minimum
        + " cache=-1 fpsLimit=0 aeroFramePacing=false");
    return true;
  }
}
