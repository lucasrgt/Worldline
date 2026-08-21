package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;

/** Bounded modern-Java thread/process inventory captured on a timeout. */
final class TimeoutInventory {
    private TimeoutInventory() {}
    static byte[] capture() {
        StringBuilder value = new StringBuilder("WORLDLINE-TIMEOUT-INVENTORY/1\n")
                .append("captured=").append(Instant.now()).append('\n');
        Thread.getAllStackTraces().entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getKey().threadId()))
                .limit(256).forEach(entry -> thread(value, entry));
        ProcessHandle.allProcesses().sorted(Comparator.comparingLong(ProcessHandle::pid)).limit(256)
                .forEach(process -> value.append("process=").append(process.pid()).append('|')
                        .append(process.info().command().orElse("unknown")).append('\n'));
        if (value.length() > 1_048_576) value.setLength(1_048_576);
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static void thread(StringBuilder value, Map.Entry<Thread, StackTraceElement[]> entry) {
        Thread thread = entry.getKey(); value.append("thread=").append(thread.threadId()).append('|')
                .append(thread.getState()).append('|').append(thread.getName()).append('\n');
        StackTraceElement[] frames = entry.getValue();
        for (int index = 0; index < Math.min(frames.length, 32); index++)
            value.append("  at ").append(frames[index]).append('\n');
    }
}
