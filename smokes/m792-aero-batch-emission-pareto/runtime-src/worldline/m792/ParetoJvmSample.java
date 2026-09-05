package worldline.m792;

import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import worldline.profiling.ProfilerRecorder;

/** Attribution only: these observations never remove frames or replace wall-time gates. */
final class ParetoJvmSample {
  private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
  private static final CompilationMXBean COMPILER = ManagementFactory.getCompilationMXBean();
  private static final List<GarbageCollectorMXBean> COLLECTORS = ManagementFactory.getGarbageCollectorMXBeans();
  private static long cpu, gc, jit;
  static {
    if (!THREADS.isCurrentThreadCpuTimeSupported() || COMPILER == null
        || !COMPILER.isCompilationTimeMonitoringSupported())
      throw new IllegalStateException("M792 JVM attribution unavailable");
    THREADS.setThreadCpuTimeEnabled(true);
  }
  private ParetoJvmSample() {}

  static void begin() {
    gc = gcMillis();
    jit = COMPILER.getTotalCompilationTime();
    cpu = THREADS.getCurrentThreadCpuTime();
  }
  static void record(ProfilerRecorder recorder) {
    long used = THREADS.getCurrentThreadCpuTime() - cpu;
    long pause = gcMillis() - gc, compilation = COMPILER.getTotalCompilationTime() - jit;
    if (used < 0 || pause < 0 || compilation < 0)
      throw new IllegalStateException("M792 JVM attribution counter reversed");
    recorder.set(8, used);
    recorder.set(9, pause * 1000000L);
    recorder.set(10, compilation * 1000000L);
  }
  private static long gcMillis() {
    long total = 0;
    for (GarbageCollectorMXBean collector : COLLECTORS) {
      long value = collector.getCollectionTime();
      if (value < 0)
        throw new IllegalStateException("M792 GC timing unavailable");
      total += value;
    }
    return total;
  }
}
