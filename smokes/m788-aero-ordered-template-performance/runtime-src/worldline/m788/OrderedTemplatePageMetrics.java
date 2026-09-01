package worldline.m788;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.util.Aero_Profiler;
import java.io.PrintWriter;

/** Owns Cell Page counters kept outside the frame/allocation probe. */
final class OrderedTemplatePageMetrics {
    private static int calls, rebuilds, direct, cachedMax;
    private static int lastCalls, lastRebuilds, lastDirect;
    private static int compiledStart, expiredStart, evictedStart;

    private OrderedTemplatePageMetrics() {}

    static void beginArm() {
        calls = rebuilds = direct = cachedMax = 0;
        lastCalls = lastRebuilds = lastDirect = 0;
        Aero_Profiler.reset();
        compiledStart = Aero_BECellRenderer.compiledCachedPages();
        expiredStart = Aero_BECellRenderer.expiredCachedPages();
        evictedStart = Aero_BECellRenderer.evictedCachedPages();
    }

    static void recordFlush(int compiledBefore) {
        lastCalls = Aero_BECellRenderer.pageCallsThisFrame();
        lastRebuilds = Aero_BECellRenderer.compiledCachedPages() - compiledBefore;
        lastDirect = Aero_BECellRenderer.directFallbacksThisFrame();
        calls += lastCalls;
        rebuilds += lastRebuilds;
        direct += lastDirect;
        cachedMax = Math.max(cachedMax, Aero_BECellRenderer.cachedPageCount());
    }

    static void write(PrintWriter out) {
        out.println("page.calls=" + calls);
        out.println("page.rebuilds=" + rebuilds);
        out.println("page.direct=" + direct);
        out.println("page.cached.max=" + cachedMax);
        out.println("page.last.calls=" + lastCalls);
        out.println("page.last.rebuilds=" + lastRebuilds);
        out.println("page.last.direct=" + lastDirect);
        out.println("page.compiled=" + (Aero_BECellRenderer.compiledCachedPages() - compiledStart));
        out.println("page.expired=" + (Aero_BECellRenderer.expiredCachedPages() - expiredStart));
        out.println("page.evicted=" + (Aero_BECellRenderer.evictedCachedPages() - evictedStart));
        out.println("page.flattened=" + Aero_BECellRenderer.flattenedPagesEnabled());
        out.println("page.ttl.frames=" + Aero_BECellRenderer.pageTtlFrames());
        out.println("page.rebuild.budget=" + Aero_BECellRenderer.rebuildsPerFrame());
        out.println("page.cache.max=" + Aero_BECellRenderer.maxCachedPages());
        out.println("profiler.flush.calls=" + Aero_Profiler.callCount("aero.becell.flush"));
        out.println("profiler.flush.nanos=" + Aero_Profiler.totalNanos("aero.becell.flush"));
    }
}
