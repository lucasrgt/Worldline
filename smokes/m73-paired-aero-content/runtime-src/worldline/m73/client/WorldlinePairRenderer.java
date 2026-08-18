package worldline.m73.client;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import worldline.m73.WorldlinePairBlockEntity;
import worldline.m73.WorldlinePairMod;
import worldline.m73.probe.WorldlinePairProbe;

/** Qualifies each unique synchronized BE only after a real Aero queue call returns. */
public final class WorldlinePairRenderer extends BlockEntityRenderer {
    private static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/worldline-m73-probe.obj");
    @Override public void render(BlockEntity raw, double x, double y, double z, float partialTick) {
        WorldlinePairBlockEntity be = (WorldlinePairBlockEntity) raw;
        if (be.world.getBlockId(be.x, be.y, be.z) != WorldlinePairMod.block.id)
            throw new IllegalStateException("M73 rendered block drift");
        if (be.nonce() <= 0) return;
        float brightness = be.world.method_1782(be.x, be.y + 1, be.z);
        Aero_BECellRenderer.queueAtRest(MODEL, "/terrain.png", be, x, y, z, 0F, brightness, Aero_RenderOptions.DEFAULT);
        WorldlinePairProbe.rendered(be.x, be.y, be.z, be.nonce());
    }
}
