import java.util.List;

/** Produces a host-bound, lower-only memory floor from candidate telemetry. */
final class ExecutionTelemetryAdmission {
    private ExecutionTelemetryAdmission() { }

    static byte[] recommendation(List<ExecutionTelemetryHistory.Profile> profiles,
            String os, String architecture) {
        long peak = 0L, samples = 0L;
        for (ExecutionTelemetryHistory.Profile profile : profiles) {
            if (!profile.sample().runtimeCapability().equals("candidate-worker")
                    || !profile.sample().os().equals(os)
                    || !profile.sample().architecture().equals(architecture)
                    || profile.peakMemoryMax() == null) continue;
            peak = Math.max(peak, profile.peakMemoryMax());
            samples = Math.addExact(samples, profile.count());
        }
        if (samples == 0L) return null;
        long margin = Math.max(1L, Math.floorDiv(peak, 4L));
        long minimum = Math.addExact(peak, margin);
        String text = "schema=worldline.telemetry-admission.v1\n"
                + "os=" + os + "\narchitecture=" + architecture
                + "\nruntime.capability=candidate-worker\nsample.count=" + samples
                + "\nobserved.peak.memory.bytes=" + peak
                + "\nminimum.worker.memory.bytes=" + minimum
                + "\ndirection=lower-only\n";
        return text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
