package worldline.m74.client;

import aero.modellib.Aero_BECellIndex;
import aero.modellib.Aero_BECellRenderer;

/** Freezes the literal cell-size treatment independently of page counters. */
public final class WorldlineCellSizeFloorGate {
  private static boolean done;
  private WorldlineCellSizeFloorGate() {
  }
  public static void check() {
    if (done)
      return;
    String raw = System.getProperty("aero.becell.size"),
           skip = System.getProperty("aero.becell.skipIndividual");
    if (!("0".equals(raw) || "1".equals(raw)) || Aero_BECellIndex.CELL_SIZE != 1
        || !"false".equals(skip) || Aero_BECellRenderer.SKIP_INDIVIDUAL_RENDERERS)
      throw new IllegalStateException("M109 cell-size floor drift");
    done = true;
    System.out.println("[WorldlineCellSizeFloor] armed raw=" + raw
        + " effective=1 minimum=2 skipIndividual=false");
  }
}
