package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import worldline.m74.*;

/** Real at-rest Aero path with allocation-free primitive census accounting. */
public final class WorldlineCensusRenderer extends BlockEntityRenderer {
    private static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/worldline-m74-probe.obj");
    @Override public void render(BlockEntity raw, double x, double y, double z, float partialTick) {
        WorldlineCensusBlockEntity be = (WorldlineCensusBlockEntity) raw; if (be.world.getBlockId(be.x, be.y, be.z) != WorldlineCensusMod.block.id)
            throw new IllegalStateException("M74 rendered block drift"); if (be.nonce() <= 0) return;
        Aero_BECellRenderer.queueAtRest(MODEL, "/terrain.png", be, x, y, z, 0F, be.world.method_1782(be.x, be.y + 1, be.z), Aero_RenderOptions.DEFAULT);
        WorldlineCensusProbe.rendered(be.x, be.y, be.z, be.nonce());
    }
}
