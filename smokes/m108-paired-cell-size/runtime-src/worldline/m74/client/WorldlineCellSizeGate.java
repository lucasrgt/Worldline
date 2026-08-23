package worldline.m74.client;

import aero.modellib.Aero_BECellIndex;
import aero.modellib.Aero_BECellRenderer;

/** Freezes the literal cell-size treatment independently of page counters. */
public final class WorldlineCellSizeGate {
  private static boolean done;
  private WorldlineCellSizeGate() {
  }
  public static void check() {
    if (done)
      return;
    String raw = System.getProperty("aero.becell.size"),
           skip = System.getProperty("aero.becell.skipIndividual");
    if (!("2".equals(raw) || "8".equals(raw)) || Aero_BECellIndex.CELL_SIZE != Integer.parseInt(raw)
        || !"false".equals(skip) || Aero_BECellRenderer.SKIP_INDIVIDUAL_RENDERERS)
      throw new IllegalStateException("M108 cell-size drift");
    done = true;
    System.out.println("[WorldlineCellSize] armed size=" + raw + " minimum=2 skipIndividual=false");
  }
}
