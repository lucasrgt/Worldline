package worldline.smoke.b173flowingfluid;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.testkit.FlowingFluidLifecycleEvidence;
import worldline.testkit.FlowingFluidLifecycleFixture;
import worldline.trace.CanonicalTrace;

/** Qualifies the public moving-fluid lifecycle against mapped official classes. */
public final class FlowingFluidLifecycleSmoke {
    private FlowingFluidLifecycleSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        FlowingFluidLifecycleEvidence evidence = FlowingFluidLifecycleFixture.execute(
                new FlowingFluidLifecycleBackend());
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(evidence.canonical().getBytes(StandardCharsets.UTF_8));
        CanonicalTrace trace = new CanonicalTrace(FlowingFluidLifecycleBackend.SEED);
        trace.record("flowing-fluid-lifecycle", 0L, 0, integers(digest));
        trace.emitTo(System.out);
    }

    private static int[] integers(byte[] digest) {
        int[] values = new int[digest.length / 4];
        for (int index = 0; index < values.length; index++) {
            int offset = index * 4;
            values[index] = (digest[offset] & 255) << 24
                    | (digest[offset + 1] & 255) << 16
                    | (digest[offset + 2] & 255) << 8
                    | digest[offset + 3] & 255;
        }
        return values;
    }
}
