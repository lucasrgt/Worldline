package worldline.smoke.redstoneore;
import java.nio.file.Paths;
import worldline.api.*;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;
/** Activates one ore and waits for seeded vanilla random-tick reversion. */
public final class RedstoneOreSmoke {
  private static final long SEED = 51120240820L;
  private RedstoneOreSmoke() {
  }
  public static void main(String[] a) {
    RedstoneOreBackend b = new RedstoneOreBackend(SEED);
    MinecraftRuntime r = new ControlledMinecraftRuntime(b);
    r.bootHeadless();
    try {
      r.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      CanonicalTrace t = new CanonicalTrace(SEED);
      b.seed();
      b.snapshot(t, "seeded", 0);
      b.trigger();
      b.snapshot(t, "triggered", 0);
      int tick = 0;
      while (tick < 2000 && !b.reverted()) {
        r.tick();
        tick++;
      }
      if (!b.reverted())
        throw new IllegalStateException("ore did not revert within 2000 random ticks");
      b.snapshot(t, "random-reverted", tick);
      b.assertFinal();
      t.emitTo(System.out);
    } finally {
      r.close();
    }
  }
}
