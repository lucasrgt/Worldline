package aero.modellib;

/** Narrow package bridge used only by the M791 rollback oracle. */
public final class WorldlineM791OcclusionBridge {
    private WorldlineM791OcclusionBridge() {}

    public static void clearHistory() {
        Aero_BECellOcclusion.clearCamera();
    }
}
