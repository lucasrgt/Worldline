package worldline.profiling;

import java.lang.management.CompilationMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;

/** Best-effort Java 8 sampler; its capability list contains only available MXBean signals. */
public final class JvmProfilerSampler implements ProfilerSession.Sampler {
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
    private final ClassLoadingMXBean classes = ManagementFactory.getClassLoadingMXBean();
    private final List<GarbageCollectorMXBean> collectors =
            ManagementFactory.getGarbageCollectorMXBeans();
    private final ProfilerRegistry.Handle cpu, gcPause, gcCount, heap, nonHeap, liveThreads,
            loadedClasses, jit, blocked, waited;
    private long cpuBefore, gcTimeBefore, gcCountBefore, jitBefore, blockedBefore, waitedBefore;
    private long threadId;

    public static void registerCapabilities(ProfilerRegistry.Builder builder) {
        if (builder == null) throw new NullPointerException("profiler registry builder");
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        if (enableCpu(threads)) builder.support(WorldlineProfilerMetrics.FRAME_CPU);
        builder.support(WorldlineProfilerMetrics.GC_PAUSE, "jvm.gc.collections",
                "jvm.heap.used.bytes", "jvm.nonheap.used.bytes", "jvm.threads.live.count",
                "jvm.classes.loaded.count");
        if (enableContention(threads))
            builder.support("thread.blocked.nanos", "thread.waited.nanos");
        CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
        if (compilation != null && compilation.isCompilationTimeMonitoringSupported())
            builder.support(WorldlineProfilerMetrics.JIT_COMPILATION);
    }

    public JvmProfilerSampler(ProfilerRegistry registry) {
        if (registry == null) throw new NullPointerException("profiler registry");
        cpu = registry.optional(WorldlineProfilerMetrics.FRAME_CPU);
        gcPause = registry.optional(WorldlineProfilerMetrics.GC_PAUSE);
        gcCount = registry.optional("jvm.gc.collections");
        heap = registry.optional("jvm.heap.used.bytes");
        nonHeap = registry.optional("jvm.nonheap.used.bytes");
        liveThreads = registry.optional("jvm.threads.live.count");
        loadedClasses = registry.optional("jvm.classes.loaded.count");
        jit = registry.optional(WorldlineProfilerMetrics.JIT_COMPILATION);
        blocked = registry.optional("thread.blocked.nanos");
        waited = registry.optional("thread.waited.nanos");
    }

    @Override public void beginFrame() {
        threadId = Thread.currentThread().getId();
        cpuBefore = cpu == null ? -1L : threads.getCurrentThreadCpuTime();
        gcTimeBefore = gcPause == null ? -1L : gcTimeMillis();
        gcCountBefore = gcCount == null ? -1L : gcCollections();
        jitBefore = jit == null ? -1L : compilation.getTotalCompilationTime();
        ThreadInfo info = blocked == null && waited == null ? null : threads.getThreadInfo(threadId);
        blockedBefore = info == null ? -1L : info.getBlockedTime();
        waitedBefore = info == null ? -1L : info.getWaitedTime();
    }

    @Override public void endFrame(ProfilerSession session) {
        if (cpu != null) session.set(cpu, delta(cpuBefore, threads.getCurrentThreadCpuTime()));
        if (gcPause != null) session.set(gcPause, millis(delta(gcTimeBefore, gcTimeMillis())));
        if (gcCount != null) session.set(gcCount, delta(gcCountBefore, gcCollections()));
        if (heap != null) session.set(heap, memory.getHeapMemoryUsage().getUsed());
        if (nonHeap != null) session.set(nonHeap, memory.getNonHeapMemoryUsage().getUsed());
        if (liveThreads != null) session.set(liveThreads, threads.getThreadCount());
        if (loadedClasses != null) session.set(loadedClasses, classes.getLoadedClassCount());
        if (jit != null) session.set(jit, millis(delta(jitBefore,
                compilation.getTotalCompilationTime())));
        ThreadInfo info = blocked == null && waited == null ? null : threads.getThreadInfo(threadId);
        if (blocked != null) session.set(blocked, millis(delta(blockedBefore,
                info == null ? -1L : info.getBlockedTime())));
        if (waited != null) session.set(waited, millis(delta(waitedBefore,
                info == null ? -1L : info.getWaitedTime())));
    }

    private long gcTimeMillis() {
        long total = 0L;
        for (GarbageCollectorMXBean collector : collectors)
            total += Math.max(0L, collector.getCollectionTime());
        return total;
    }
    private long gcCollections() {
        long total = 0L;
        for (GarbageCollectorMXBean collector : collectors)
            total += Math.max(0L, collector.getCollectionCount());
        return total;
    }
    private static long delta(long before, long after) {
        return before < 0L || after < before ? 0L : after - before;
    }
    private static long millis(long value) { return Math.multiplyExact(value, 1_000_000L); }
    private static boolean enableCpu(ThreadMXBean threads) {
        if (!threads.isCurrentThreadCpuTimeSupported()) return false;
        try {
            if (!threads.isThreadCpuTimeEnabled()) threads.setThreadCpuTimeEnabled(true);
            return threads.isThreadCpuTimeEnabled();
        } catch (SecurityException denied) { return false; }
    }
    private static boolean enableContention(ThreadMXBean threads) {
        if (!threads.isThreadContentionMonitoringSupported()) return false;
        try {
            if (!threads.isThreadContentionMonitoringEnabled())
                threads.setThreadContentionMonitoringEnabled(true);
            return threads.isThreadContentionMonitoringEnabled();
        } catch (SecurityException denied) { return false; }
    }
}
