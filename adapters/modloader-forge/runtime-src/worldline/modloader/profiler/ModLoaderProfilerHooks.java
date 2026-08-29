package worldline.modloader.profiler;

import worldline.profiling.ClientProfilerRuntime;
import worldline.profiling.WorldlineProfilerMetrics;

/** Java 8 hook boundary for source-injected ModLoader and Forge clients. */
public final class ModLoaderProfilerHooks {
    private static boolean configured;
    private ModLoaderProfilerHooks() {}

    public static void frameBegin() {
        configure(); ClientProfilerRuntime.beginFrame();
    }
    public static void frameEnd() { ClientProfilerRuntime.endFrame(); }

    public static long tickBegin() { configure(); return ClientProfilerRuntime.timer(); }
    public static void tickEnd(long started) {
        if (started != 0L) ClientProfilerRuntime.tick(System.nanoTime() - started);
    }

    public static long displayBegin() { configure(); return ClientProfilerRuntime.timer(); }
    public static void displayEnd(long started) {
        if (started != 0L) ClientProfilerRuntime.display(System.nanoTime() - started);
    }

    public static long worldBegin() { configure(); return ClientProfilerRuntime.timer(); }
    public static void worldEnd(long started) {
        ClientProfilerRuntime.elapsed(WorldlineProfilerMetrics.RENDER_WORLD, started);
    }

    public static long compileBegin(int backlog) {
        configure();
        if (ClientProfilerRuntime.frameOpen())
            ClientProfilerRuntime.gauge("chunk.backlog.count", Math.max(0, backlog));
        return ClientProfilerRuntime.timer();
    }
    public static void compileEnd(long started) {
        ClientProfilerRuntime.elapsed(WorldlineProfilerMetrics.CHUNK_COMPILE, started);
    }

    public static long rebuildBegin() {
        configure(); ClientProfilerRuntime.count("chunk.rebuild.calls");
        return ClientProfilerRuntime.timer();
    }
    public static void rebuildEnd(long started) {
        ClientProfilerRuntime.elapsed(WorldlineProfilerMetrics.CHUNK_REBUILD, started);
    }

    public static void startCapture() { configure(); ClientProfilerRuntime.startCapture(); }
    public static void finish(String reason) { ClientProfilerRuntime.finish(reason); }

    private static synchronized void configure() {
        if (configured) return;
        String loader = System.getProperty("worldline.profiler.loader", "modloader");
        if (!"modloader".equals(loader) && !"forge".equals(loader))
            throw new IllegalArgumentException("legacy profiler loader must be modloader or forge");
        ClientProfilerRuntime.configure("modloader-forge", loader); configured = true;
    }
}
