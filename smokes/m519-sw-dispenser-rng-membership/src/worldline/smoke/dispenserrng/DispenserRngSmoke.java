package worldline.smoke.dispenserrng;
import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;
/** Freezes reservoir sampling membership, depletion, single, and empty controls. */
public final class DispenserRngSmoke {
  private static final long SEED = 51920240821L;
  private DispenserRngSmoke() {
  }
  public static void main(String[] a) {
    CanonicalTrace t = new CanonicalTrace(SEED);
    multi(t);
    single(t);
    empty(t);
    t.emitTo(System.out);
  }
  private static MinecraftRuntime open(DispenserRngBackend b, String n) {
    MinecraftRuntime r = new ControlledMinecraftRuntime(b);
    r.bootHeadless();
    r.loadWorld(WorldSource.at(Paths.get("memory", n)));
    return r;
  }
  private static void multi(CanonicalTrace t) {
    DispenserRngBackend b = new DispenserRngBackend(SEED);
    MinecraftRuntime r = open(b, "multi");
    try {
      b.multi();
      for (int n = 1; n <= 9; n++) {
        r.tick();
        b.requireMember();
        b.snapshot(t, "multi-" + n);
      }
      r.tick();
      b.requireEmpty();
      b.snapshot(t, "multi-empty");
    } finally {
      r.close();
    }
  }
  private static void single(CanonicalTrace t) {
    DispenserRngBackend b = new DispenserRngBackend(SEED);
    MinecraftRuntime r = open(b, "single");
    try {
      b.single();
      r.tick();
      b.requireSingle();
      b.snapshot(t, "single-1");
      r.tick();
      b.requireSingle();
      b.snapshot(t, "single-2");
      r.tick();
      b.requireEmpty();
      b.snapshot(t, "single-empty");
    } finally {
      r.close();
    }
  }
  private static void empty(CanonicalTrace t) {
    DispenserRngBackend b = new DispenserRngBackend(SEED);
    MinecraftRuntime r = open(b, "empty");
    try {
      r.tick();
      b.requireEmpty();
      b.snapshot(t, "empty");
    } finally {
      r.close();
    }
  }
}
