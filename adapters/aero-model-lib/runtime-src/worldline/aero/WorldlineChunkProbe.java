package worldline.aero;

/** Frame-scoped counters for the M14 chunk caller experiment. */
public final class WorldlineChunkProbe {
    private static final boolean ENABLED = Boolean.getBoolean("worldline.chunkProbe.enabled");
    private static int calls, falseReturns, trueReturns, forced, rebuilds;
    private static int queueStart, queueEnd, queueMax, invalidates, marks, sorts;
    private static int ambient, reloads, policyBatches, policyRebuilds, policyRemaining;
    private static int accepted, deferred, completed, stalled, contractRemaining;
    private static int proposed, visibleDebt, visibleAccepted, budgetStops;
    private static int dirty, visible, visibleDirty, visibleReady, oldest, oldestVisible;
    private static long compileNs, compileStart;
    private static boolean compiling;

    private WorldlineChunkProbe() {}

    public static void beginFrame() {
        if (!ENABLED) return;
        calls = falseReturns = trueReturns = forced = rebuilds = 0;
        queueStart = queueEnd = queueMax = -1;
        invalidates = marks = sorts = ambient = reloads = 0;
        policyBatches = policyRebuilds = policyRemaining = 0;
        accepted = deferred = completed = stalled = contractRemaining = 0;
        proposed = visibleDebt = visibleAccepted = budgetStops = 0;
        dirty = visible = visibleDirty = visibleReady = oldest = oldestVisible = -1;
        compileNs = compileStart = 0L; compiling = false;
    }

    public static void beginCompile(int queue, boolean isForced) {
        if (!ENABLED) return;
        if (calls == 0) queueStart = queue;
        calls++; if (isForced) forced++;
        queueMax = Math.max(queueMax, queue); compiling = true;
        compileStart = System.nanoTime();
    }

    public static void endCompile(int queue, boolean complete) {
        if (!ENABLED || !compiling) return;
        compileNs += System.nanoTime() - compileStart; compiling = false;
        queueEnd = queue; queueMax = Math.max(queueMax, queue);
        if (complete) trueReturns++; else falseReturns++;
    }

    public static void rebuilt() { if (ENABLED && compiling) rebuilds++; }
    public static void invalidated() { if (ENABLED) invalidates++; }
    public static void marked() { if (ENABLED) marks++; }
    public static void sorted() { if (ENABLED) sorts++; }
    public static void ambient() { if (ENABLED) ambient++; }
    public static void reloaded() { if (ENABLED) reloads++; }
    public static void policy(int built, int remaining) {
        if (!ENABLED) return;
        policyBatches++; policyRebuilds += built; policyRemaining = remaining;
    }

    public static void contract(String status, int built, int remaining, int offered,
            int debt, int builtVisible, boolean budgetStopped) {
        if (!ENABLED) return;
        accepted += built; contractRemaining = remaining;
        proposed += offered; visibleDebt = debt; visibleAccepted += builtVisible;
        if (budgetStopped) budgetStops++;
        if ("COMPLETE".equals(status)) completed++;
        else if ("ACCEPTED_DEFERRED".equals(status)) deferred++;
        else stalled++;
    }

    public static void readiness(int queue, int dirtyCount, int visibleCount,
            int visibleDirtyCount, int visibleReadyCount, int maxAge, int maxVisibleAge) {
        if (!ENABLED) return;
        queueEnd = queue; dirty = dirtyCount; visible = visibleCount;
        visibleDirty = visibleDirtyCount; visibleReady = visibleReadyCount;
        oldest = maxAge; oldestVisible = maxVisibleAge;
    }

    public static void endFrame() {
        if (!ENABLED) return;
        System.out.println("[WorldlineChunkProbe] calls=" + calls + " false=" + falseReturns
                + " true=" + trueReturns + " forced=" + forced + " rebuilds=" + rebuilds
                + " queueStart=" + queueStart + " queueEnd=" + queueEnd + " queueMax=" + queueMax
                + " invalidates=" + invalidates + " marks=" + marks + " sorts=" + sorts
                + " ambient=" + ambient + " reloads=" + reloads
                + " policyBatches=" + policyBatches + " policyRebuilds=" + policyRebuilds
                + " policyRemaining=" + policyRemaining + " accepted=" + accepted
                + " deferred=" + deferred + " completed=" + completed + " stalled=" + stalled
                + " contractRemaining=" + contractRemaining + " proposed=" + proposed
                + " visibleDebt=" + visibleDebt + " visibleAccepted=" + visibleAccepted
                + " budgetStops=" + budgetStops + " dirty=" + dirty
                + " visible=" + visible + " visibleDirty=" + visibleDirty
                + " visibleReady=" + visibleReady + " oldest=" + oldest
                + " oldestVisible=" + oldestVisible + " compileUs=" + compileNs / 1000L);
    }
}
