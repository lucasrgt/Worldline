package worldline.m74.mixin;

import net.minecraft.block.entity.BlockEntity;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.client.*;

/** Times all sixteen renderer invocations surrounding real page enqueue. */
@Mixin(WorldlineCensusRenderer.class)
public abstract class WorldlinePagedRendererMixin {
    @Inject(method="render",at=@At("HEAD"))private void begin(BlockEntity be,double x,double y,double z,float tick,CallbackInfo ci){WorldlinePagedTimer.rendererBegin();}
    @Inject(method="render",at=@At("RETURN"))private void end(BlockEntity be,double x,double y,double z,float tick,CallbackInfo ci){WorldlinePagedTimer.rendererEnd();}
}
