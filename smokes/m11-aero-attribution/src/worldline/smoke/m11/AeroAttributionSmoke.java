package worldline.smoke.m11;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.MessageDigest;
import worldline.aero.AeroFrameLog;
import worldline.analysis.FrameAttribution;

/** Exercises Aero log adaptation and loads the built library without compile-time coupling. */
public final class AeroAttributionSmoke {
    private AeroAttributionSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) throw new IllegalArgumentException("expected mode");
        if ("diagnose".equals(arguments[0])) diagnose();
        else if ("artifact".equals(arguments[0])) artifact();
        else throw new IllegalArgumentException("unknown mode");
    }

    private static void diagnose() throws Exception {
        String baseline = line("16.0", "0", 12, 12, 3);
        FrameAttribution.Result logical = AeroFrameLog.compare(baseline,
                line("80.0", "0", 720, 720, 90));
        FrameAttribution.Result runtime = AeroFrameLog.compare(baseline,
                line("80.0", "36", 12, 12, 3));
        require(logical.cause() == FrameAttribution.Cause.LOGICAL_WORK,
                "logical spike was not attributed to work");
        require(runtime.cause() == FrameAttribution.Cause.RUNTIME_STALL,
                "stable work was not attributed to runtime");
        String report = logical.canonical() + "\n" + runtime.canonical() + "\n";
        System.out.println("WORLDLINE_LOGICAL_CAUSE=" + logical.cause());
        System.out.println("WORLDLINE_RUNTIME_CAUSE=" + runtime.cause());
        System.out.println("WORLDLINE_LOGICAL_TOP_COUNTER=" + logical.topCounter());
        System.out.println("WORLDLINE_ATTRIBUTION_SIGNATURE=" + sha256(report));
    }

    private static void artifact() throws Exception {
        Class<?> type = Class.forName("aero.modellib.util.Aero_Profiler");
        Method enabled = type.getMethod("setEnabled", boolean.class);
        enabled.invoke(null, true);
        require((Boolean) type.getMethod("isEnabled").invoke(null), "Aero profiler did not execute");
        enabled.invoke(null, false);
        URL origin = type.getProtectionDomain().getCodeSource().getLocation();
        String version = type.getPackage().getImplementationVersion();
        require("3.0.0".equals(version), "unexpected Aero implementation version " + version);
        System.out.println("WORLDLINE_AERO_LOAD=PASS");
        System.out.println("WORLDLINE_AERO_VERSION=" + version);
        System.out.println("WORLDLINE_AERO_PROVENANCE=" + origin);
    }

    private static String line(String frameMs, String gcMs, long accepted, long flushed, long batches) {
        return "[Aero_FrameSpike] frameMs=" + frameMs + " gcTimeDeltaMs=" + gcMs
                + " animAccepted=" + accepted + " animRejected=0 batchQueued=" + flushed
                + " batchFlushed=" + flushed + " batchBatches=" + batches + " batchImmediate=0"
                + " atRestRenders=4 atRestListCalls=4 cellCalls=2 cellRebuilds=0"
                + " compileChunksCalls=0 renderChunksCalls=4 dlLive=16 prewarmDrained=0 visibleChunks=128";
    }

    private static String sha256(String value) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"));
        StringBuilder result = new StringBuilder();
        for (byte item : hash) result.append(String.format("%02x", item & 255));
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
