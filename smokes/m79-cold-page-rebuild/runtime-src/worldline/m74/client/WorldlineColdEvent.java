package worldline.m74.client;

import aero.modellib.*;import worldline.m74.*;import worldline.m74.mixin.WorldlineColdModelAccess;

/** One allocation-bounded, explicitly armed cache disposal and rebuild event. */
public final class WorldlineColdEvent {
    private static final int AFTER=Integer.getInteger("worldline.cold.after",300);static boolean fired,pending;static int index=-1,cachedBefore,cachedAfterDispose,cachedAfterRebuild,compiledBefore,compiledAfter,deletedBefore,deletedAfter;static long disposeNs;
    private WorldlineColdEvent(){}
    public static void head(){if(fired||WorldlinePagedBridge.sealed()||!WorldlinePagedBridge.running()||WorldlinePagedBridge.count()<AFTER)return;
        if(AFTER!=300||!Aero_BECellRenderer.ENABLED||!Aero_BECellRenderer.SKIP_INDIVIDUAL_RENDERERS||!"8".equals(System.getProperty("aero.becell.rebuildsPerFrame")))throw new IllegalStateException("M79 runtime drift");
        cachedBefore=Aero_BECellRenderer.cachedPageCount();compiledBefore=Aero_BECellRenderer.compiledCachedPages();deletedBefore=Aero_BECellRenderer.deletedPages();if(cachedBefore!=4||compiledBefore<4||deletedBefore<0)throw new IllegalStateException("M79 warm cache drift "+cachedBefore+"/"+compiledBefore+"/"+deletedBefore);
        long start=System.nanoTime();Aero_MeshRenderer.disposeModel(WorldlineColdModelAccess.worldline$model());disposeNs=System.nanoTime()-start;if(disposeNs<=0)throw new IllegalStateException("M79 nonpositive dispose");
        cachedAfterDispose=Aero_BECellRenderer.cachedPageCount();deletedAfter=Aero_BECellRenderer.deletedPages();if(cachedAfterDispose!=0||deletedAfter-deletedBefore!=4)throw new IllegalStateException("M79 disposal drift");fired=pending=true;}
    public static void tail(){if(!pending)return;index=WorldlinePagedBridge.count();cachedAfterRebuild=Aero_BECellRenderer.cachedPageCount();compiledAfter=Aero_BECellRenderer.compiledCachedPages();
        if(index<AFTER||Aero_BECellRenderer.queuedLastFrame()!=16||Aero_BECellRenderer.pageCallsThisFrame()!=4||Aero_BECellRenderer.directFallbacksThisFrame()!=0||Aero_BECellRenderer.pageRebuildsThisFrame()!=4||cachedAfterRebuild!=4||compiledAfter-compiledBefore!=4)throw new IllegalStateException("M79 rebuild drift");pending=false;}
}
