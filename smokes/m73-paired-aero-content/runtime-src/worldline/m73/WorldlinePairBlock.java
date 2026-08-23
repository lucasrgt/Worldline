package worldline.m73;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

/** Common server-safe block for the paired M73 content fixture. */
public final class WorldlinePairBlock extends TemplateBlockWithEntity {
  public WorldlinePairBlock(Identifier identifier) {
    super(identifier, Material.STONE);
    setHardness(1F);
    setTranslationKey(identifier);
  }
  @Override
  protected BlockEntity createBlockEntity() {
    return new WorldlinePairBlockEntity();
  }
  @Override
  public int getRenderType() {
    return -1;
  }
  @Override
  public boolean isOpaque() {
    return false;
  }
}
