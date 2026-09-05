package worldline.m794;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import net.minecraft.client.render.chunk.ChunkBuilder;

/** Native chunk diagnostics read and written only after the measured render boundary. */
public final class ProductionTerrainSnapshot {
  private static ChunkBuilder[] chunks;
  private ProductionTerrainSnapshot() {}
  public static void observe(ChunkBuilder[] value) {
    chunks = value;
  }
  public static void sample() {
    if (!ProductionState.captureFrame() || ProductionState.scene() != 11)
      return;
    File root = new File(System.getProperty("worldline.m794.metrics")).getParentFile();
    String arm = ProductionState.candidateEnabled() ? "on" : "off";
    File target = new File(root, "terrain-" + arm + "-" + ProductionState.checkpoint() + ".csv");
    try (PrintWriter out = new PrintWriter(new FileWriter(target))) {
      out.println("index,x,y,z,frustum,dirty,empty0,empty1,list0,list1");
      for (int i = 0; i < chunks.length; i++) {
        ChunkBuilder chunk = chunks[i];
        out.println(i + "," + chunk.x + "," + chunk.y + "," + chunk.z + "," + chunk.inFrustum + "," + chunk.dirty + ","
            + chunk.renderLayerEmpty[0] + "," + chunk.renderLayerEmpty[1] + "," + chunk.getRenderListId(0) + ","
            + chunk.getRenderListId(1));
      }
    } catch (Exception error) {
      throw new IllegalStateException("M794 terrain snapshot failed", error);
    }
  }
}
