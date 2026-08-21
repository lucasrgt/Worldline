package worldline.m74;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

/** Common server-safe block for the complete-census fixture. */
public final class WorldlineCensusBlock extends TemplateBlockWithEntity {
    public WorldlineCensusBlock(Identifier id) { super(id, Material.STONE); setHardness(1F); setTranslationKey(id); }
    @Override protected BlockEntity createBlockEntity() { return new WorldlineCensusBlockEntity(); }
    @Override public int getRenderType() { return -1; }
    @Override public boolean isOpaque() { return false; }
}
