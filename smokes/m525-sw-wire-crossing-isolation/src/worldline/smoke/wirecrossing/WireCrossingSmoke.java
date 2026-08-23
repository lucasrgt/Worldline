package worldline.smoke.wirecrossing;
import java.nio.file.Paths;
import worldline.api.*;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;
public final class WireCrossingSmoke {
  private static final long SEED = 52520240820L;
  private WireCrossingSmoke() {
  }
  public static void main(String[] a) {
    WireCrossingBackend b = new WireCrossingBackend(SEED);
    MinecraftRuntime r = new ControlledMinecraftRuntime(b);
    r.bootHeadless();
    try {
      r.loadWorld(WorldSource.at(Paths.get("memory", "worldline-smoke")));
      CanonicalTrace t = new CanonicalTrace(SEED);
      b.fixture();
      r.tick();
      b.snapshot(t, "lower-powered");
      b.disconnectLower();
      r.tick();
      b.snapshot(t, "connector-removed");
      b.powerUpper();
      r.tick();
      b.snapshot(t, "upper-powered");
      b.assertFinal();
      t.emitTo(System.out);
    } finally {
      r.close();
    }
  }
}
