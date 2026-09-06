package worldline.m794;

import org.lwjgl.opengl.GL11;

/** Diagnostic entry condition; all GPU completion wait is reported separately. */
final class ProductionGpuDrain {
  static final boolean ENABLED = Boolean.getBoolean("worldline.m794.gpuQuiescent");

  private ProductionGpuDrain() {}

  static long await() {
    if (!ENABLED || !ProductionState.acquiring()) return 0L;
    long before = System.nanoTime();
    GL11.glFinish();
    return System.nanoTime() - before;
  }
}
