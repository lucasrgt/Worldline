package worldline.m72;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

/** Common block class: deliberately has no Aero or client references. */
public final class WorldlineContentBlock extends TemplateBlockWithEntity {
    public WorldlineContentBlock(Identifier identifier) {
        super(identifier, Material.STONE);
        setHardness(1.0F); setTranslationKey(identifier);
    }
    @Override protected BlockEntity createBlockEntity() { return new WorldlineContentBlockEntity(); }
    @Override public int getRenderType() { return -1; }
    @Override public boolean isOpaque() { return false; }
}
