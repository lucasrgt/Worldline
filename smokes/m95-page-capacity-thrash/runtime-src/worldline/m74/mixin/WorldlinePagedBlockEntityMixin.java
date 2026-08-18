package worldline.m74.mixin;

import aero.modellib.render.Aero_CellRenderableBE;
import org.spongepowered.asm.mixin.Mixin;
import worldline.m74.WorldlineCensusBlockEntity;

/** Client-only marker; the common/server block-entity class remains Aero-free. */
@Mixin(WorldlineCensusBlockEntity.class)
public abstract class WorldlinePagedBlockEntityMixin implements Aero_CellRenderableBE {
    @Override public int aeroRenderStateHash(){return 0;}@Override public int aeroOrientationHash(){return 0;}
    @Override public boolean aeroCanCellPage(){return true;}@Override public boolean aeroWantsAnimation(){return false;}
}
