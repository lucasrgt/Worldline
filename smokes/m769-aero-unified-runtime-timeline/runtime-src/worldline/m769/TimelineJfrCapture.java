package worldline.m769;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import jdk.jfr.Category;
import jdk.jfr.Configuration;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Recording;
import jdk.jfr.StackTrace;

/** Owns the low-overhead JFR side of the M769 unified timeline. */
public final class TimelineJfrCapture {
    private static Recording recording;
    private static Path output;
    private static boolean active;
    private static long lastSequence = -1L;

    private TimelineJfrCapture() {}

    public static void start() {
        require(!active && recording == null, "duplicate M769 JFR start");
        try {
            output = Path.of(System.getProperty("worldline.m769.jfr")).toAbsolutePath();
            Files.createDirectories(output.getParent());
            Files.deleteIfExists(output);
            recording = new Recording(Configuration.getConfiguration("profile"));
            recording.setName("worldline-m769-aero-runtime-timeline");
            recording.setToDisk(true);
            recording.enable(FrameAnchorEvent.class).withoutStackTrace();
            Set<String> available = FlightRecorder.getFlightRecorder().getEventTypes().stream()
                    .map(type -> type.getName()).collect(Collectors.toSet());
            for (String name : new String[] {"jdk.GarbageCollection", "jdk.GCPhasePause",
                    "jdk.SafepointBegin", "jdk.SafepointStateSynchronization",
                    "jdk.SafepointCleanup", "jdk.SafepointEnd", "jdk.FileRead",
                    "jdk.FileWrite"}) {
                if (available.contains(name)) {
                    recording.enable(name).withoutStackTrace().withThreshold(Duration.ZERO);
                }
            }
            if (available.contains("jdk.ObjectAllocationSample")) {
                recording.enable("jdk.ObjectAllocationSample").withoutStackTrace()
                        .with("throttle", "150/s");
            }
            recording.start();
            active = true;
            anchor(-1L, System.nanoTime(), System.currentTimeMillis(), 0);
            System.out.println("[WorldlineM769] jfr-start path=" + output);
        } catch (Exception error) {
            throw new IllegalStateException("M769 JFR start failed", error);
        }
    }

    public static void anchor(long sequence, long monotonicNanos, long epochMillis, int phase) {
        if (!active) return;
        FrameAnchorEvent event = new FrameAnchorEvent();
        event.sequence = sequence;
        event.monotonicNanos = monotonicNanos;
        event.epochMillis = epochMillis;
        event.phase = phase;
        event.commit();
    }

    public static void frame(long sequence, long monotonicNanos, long epochMillis, int phase) {
        if (!active) return;
        lastSequence = sequence;
        int interval = Integer.getInteger("worldline.m769.anchorInterval", 128);
        if (sequence % interval == 0L) anchor(sequence, monotonicNanos, epochMillis, phase);
    }

    public static void finish() {
        require(active && recording != null, "M769 JFR is not active");
        try {
            anchor(lastSequence, System.nanoTime(), System.currentTimeMillis(), TimelineState.phase());
            recording.stop();
            recording.dump(output);
            recording.close();
            recording = null;
            active = false;
            require(Files.isRegularFile(output) && Files.size(output) > 0L,
                    "M769 JFR artifact absent");
            System.out.println("[WorldlineM769] jfr-sealed bytes=" + Files.size(output));
        } catch (Exception error) {
            throw new IllegalStateException("M769 JFR seal failed", error);
        }
    }

    public static boolean active() {
        return active;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    @Name("worldline.FrameAnchor")
    @Label("Worldline frame anchor")
    @Category({"Worldline", "Profiler"})
    @StackTrace(false)
    @Enabled(true)
    static final class FrameAnchorEvent extends Event {
        @Label("Frame sequence") public long sequence;
        @Label("Monotonic nanoseconds") public long monotonicNanos;
        @Label("Epoch milliseconds") public long epochMillis;
        @Label("Route phase") public int phase;
    }
}
