package worldline.b173;

import worldline.api.RuntimeSnapshot;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;

/** Exact b1.7.3 runtime provider for portable reproduction bundles. */
public final class B173ReplayProvider implements ReplayProvider {
    public static final String RUNTIME_ID = "minecraft-b1.7.3-client";
    public static final String WORLDLINE_VERSION = "0.3.0";
    public static final String CLIENT_SHA256 =
            "af1fa04b8006d3ef78c7e24f8de4aa56f439a74d7f314827529062d5bab6db4c";
    public static final String TOOLCHAIN_REVISION = "9ece383d9bfe993d763d75b503e913f0dfbd8852";

    public B173ReplayProvider() {}

    public static ReproductionBundle bundle(RuntimeSnapshot snapshot) {
        return ReproductionBundle.create(RUNTIME_ID, WORLDLINE_VERSION, CLIENT_SHA256,
                TOOLCHAIN_REVISION, snapshot);
    }

    @Override public String runtimeId() { return RUNTIME_ID; }

    @Override public ReplayReport replay(ReproductionBundle bundle) {
        if (bundle == null) throw new NullPointerException("bundle");
        require(bundle.runtimeId().equals(RUNTIME_ID), "bundle runtime mismatch");
        require(bundle.worldlineVersion().equals(WORLDLINE_VERSION), "Worldline version mismatch");
        require(bundle.clientSha256().equals(CLIENT_SHA256), "client SHA-256 mismatch");
        require(bundle.toolchainRevision().equals(TOOLCHAIN_REVISION), "toolchain revision mismatch");
        B173Runtime runtime = B173Runtimes.restore(bundle.snapshot());
        try {
            require(runtime.minecraftClassSource().replace('\\', '/').contains("instrumented-client/"),
                    "replay did not use the controlled client");
            require(runtime.snapshot().equals(bundle.snapshot()), "replayed snapshot did not round-trip");
            B173Observation value = runtime.observe();
            return new ReplayReport(RUNTIME_ID, value.clientTick(), state(value));
        } finally { runtime.close(); }
    }

    private static String state(B173Observation value) {
        return "tick" + value.clientTick() + "=" + value.clientTick() + "," + value.worldTime()
                + "," + value.rngSeed() + "," + value.entityCount() + "," + value.cloudTick()
                + "," + value.guiTick() + "," + value.rendererTick() + "," + value.playerXBits()
                + "," + value.playerYBits() + "," + value.playerZBits() + "," + value.health()
                + "," + value.selectedSlot() + "," + value.blockColumn()[0] + ","
                + value.blockColumn()[1];
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
