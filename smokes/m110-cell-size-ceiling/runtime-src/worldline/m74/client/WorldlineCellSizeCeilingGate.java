package worldline.m74.client;

import aero.modellib.Aero_BECellIndex;
import aero.modellib.Aero_BECellRenderer;

/** Freezes the literal cell-size treatment independently of page counters. */
public final class WorldlineCellSizeCeilingGate {
  private static boolean done;
  private WorldlineCellSizeCeilingGate() {
  }
  public static void check() {
    if (done)
      return;
    String raw = System.getProperty("aero.becell.size"),
           skip = System.getProperty("aero.becell.skipIndividual");
    if (!("33".equals(raw) || "32".equals(raw)) || Aero_BECellIndex.CELL_SIZE != 32
        || !"false".equals(skip) || Aero_BECellRenderer.SKIP_INDIVIDUAL_RENDERERS)
      throw new IllegalStateException("M110 cell-size ceiling drift");
    done = true;
    System.out.println("[WorldlineCellSizeCeiling] armed raw=" + raw
        + " effective=32 minimum=2 skipIndividual=false");
  }
}
