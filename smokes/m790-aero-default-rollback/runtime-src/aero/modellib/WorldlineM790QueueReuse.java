package aero.modellib;

/** Test-only access to bounded Cell Page queue-reuse state. */
public final class WorldlineM790QueueReuse {
    private WorldlineM790QueueReuse() {}

    public static void discardQueued() {
        for (int index = 0; index < Aero_BECellRenderState.ACTIVE_PAGES.size(); index++) {
            Aero_BECellQueuePool.release(Aero_BECellRenderState.ACTIVE_PAGES.get(index));
        }
        Aero_BECellRenderState.ACTIVE.clear();
        Aero_BECellRenderState.ACTIVE_PAGES.clear();
        Aero_BECellReplay.clear();
        Aero_BECellRenderState.queuedThisFrame = 0;
    }

    public static boolean enabled() { return Aero_BECellQueuePool.ENABLED; }
    public static int pooledPages() { return Aero_BECellQueuePool.pooledPages(); }
    public static int allocatedPages() { return Aero_BECellQueuePool.allocatedPages(); }
    public static int reusedPages() { return Aero_BECellQueuePool.reusedPages(); }
    public static int discardedPages() { return Aero_BECellQueuePool.discardedPages(); }
}
