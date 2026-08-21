package worldline.m72.client;

import aero.modellib.Aero_BECellRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import worldline.m72.WorldlineContentBlockEntity;
import worldline.m72.WorldlineContentMod;
import worldline.m72.probe.WorldlineContentProbe;

/** Client-only renderer that validates synchronized state before using Aero. */
public final class WorldlineContentRenderer extends BlockEntityRenderer {
    private static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/worldline-m72-probe.obj");
    @Override public void render(BlockEntity raw, double x, double y, double z, float partialTick) {
        WorldlineContentBlockEntity entity = (WorldlineContentBlockEntity) raw;
        if (entity.world.getBlockId(entity.x, entity.y, entity.z) != WorldlineContentMod.block.id)
            throw new IllegalStateException("remote custom block identity drifted");
        if (entity.nonce() <= 0) return;
        float brightness = entity.world.method_1782(entity.x, entity.y + 1, entity.z);
        Aero_BECellRenderer.queueAtRest(MODEL, "/terrain.png", entity,
                x, y, z, 0F, brightness, Aero_RenderOptions.DEFAULT);
        WorldlineContentProbe.rendered(entity.x, entity.y, entity.z,
                WorldlineContentMod.block.id, entity.nonce());
    }
}
