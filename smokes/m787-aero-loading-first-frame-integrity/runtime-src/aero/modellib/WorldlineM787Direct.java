package aero.modellib;

import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;

/** Test-only access to the exact Cell Page immediate fallback. */
public final class WorldlineM787Direct {
    private WorldlineM787Direct() {}

    public static void draw(Aero_MeshModel model, String texture,
            double x, double y, double z, float rotation, float brightness,
            Aero_RenderOptions options) {
        Aero_BECellFlush.drawDirect(model, texture,
            x, y, z, rotation, brightness, options);
    }

    public static void discardQueued() {
        for (int index = 0; index < Aero_BECellRenderState.ACTIVE_PAGES.size(); index++) {
            Aero_BECellRenderState.ACTIVE_PAGES.get(index).clear();
        }
        Aero_BECellRenderState.ACTIVE.clear();
        Aero_BECellRenderState.ACTIVE_PAGES.clear();
        Aero_BECellReplay.clear();
        Aero_BECellRenderState.queuedThisFrame = 0;
    }
}
