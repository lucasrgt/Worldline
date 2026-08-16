package worldline.smoke.m4;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import worldline.api.RuntimeSnapshot;
import worldline.api.SnapshotMinecraftRuntime;
import worldline.api.WorldSource;
import worldline.b173.B173Keys;
import worldline.b173.B173Observation;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;

/** Captures or restores the canonical M4 durable snapshot in a fresh JVM. */
public final class M4SnapshotSmoke {
    private static final long SEED = 17320110707L;
    private static final long RNG_SEED = 2026071501L;
    private static final WorldSource WORLD = WorldSource.at(
            Paths.get("memory", "worldline-client-cycle"));

    private M4SnapshotSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 2, "expected capture|restore and snapshot path");
        Path path = Paths.get(arguments[1]);
        if (arguments[0].equals("capture")) capture(path);
        else if (arguments[0].equals("restore")) restore(path);
        else throw new IllegalArgumentException("unknown M4 mode " + arguments[0]);
    }

    private static void capture(Path path) throws Exception {
        B173Runtime concrete = B173Runtimes.create(SEED);
        SnapshotMinecraftRuntime runtime = concrete;
        expectSnapshotFailure(runtime, "loaded world");
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WORLD);
            concrete.reseed(RNG_SEED);
            concrete.scheduler().afterTicks(2, () -> concrete.tap(B173Keys.SLOT_1 + 2));
            expectSnapshotFailure(runtime, "drained scheduler");
            runtime.tick(4);
            RuntimeSnapshot snapshot = runtime.snapshot();
            Files.write(path, snapshot.bytes());
            emit(snapshot, concrete.observe());
        } finally { runtime.close(); }
        expectSnapshotFailure(runtime, "loaded world");
    }

    private static void restore(Path path) throws Exception {
        RuntimeSnapshot snapshot = RuntimeSnapshot.of(Files.readAllBytes(path));
        B173Runtime concrete = B173Runtimes.restore(snapshot);
        SnapshotMinecraftRuntime runtime = concrete;
        try {
            require(snapshot.equals(runtime.snapshot()), "restored snapshot did not round-trip");
            emit(snapshot, concrete.observe());
        } finally { runtime.close(); }
    }

    private static void emit(RuntimeSnapshot snapshot, B173Observation value) {
        System.out.println("WORLDLINE_M4_SOURCE=" + source());
        System.out.println("WORLDLINE_M4_SNAPSHOT_SHA=" + snapshot.sha256());
        System.out.println("WORLDLINE_M4_STATE=tick4=" + value.clientTick() + ","
                + value.worldTime() + "," + value.rngSeed() + "," + value.entityCount() + ","
                + value.cloudTick() + "," + value.guiTick() + "," + value.rendererTick() + ","
                + value.playerXBits() + "," + value.playerYBits() + "," + value.playerZBits() + ","
                + value.health() + "," + value.selectedSlot() + "," + value.blockColumn()[0] + ","
                + value.blockColumn()[1]);
        System.out.println("WORLDLINE_M4_FINGERPRINT=" + value.fingerprint());
        System.out.println("WORLDLINE_M4_ROUNDTRIP=true");
    }

    private static String source() {
        try { return Class.forName("net.minecraft.client.Minecraft").getProtectionDomain()
                .getCodeSource().getLocation().toString(); }
        catch (ClassNotFoundException error) { throw new IllegalStateException(error); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void expectSnapshotFailure(SnapshotMinecraftRuntime runtime, String message) {
        try { runtime.snapshot(); throw new IllegalStateException("snapshot unexpectedly succeeded"); }
        catch (IllegalStateException expected) {
            require(expected.getMessage().contains(message), "unexpected snapshot lifecycle failure");
        }
    }
}
