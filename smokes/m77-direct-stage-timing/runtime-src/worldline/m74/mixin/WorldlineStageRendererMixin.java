package worldline.m74.mixin;

import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.WorldlineStageTimer;
import worldline.m74.client.WorldlineCensusRenderer;

/** Times the complete Worldline renderer invocation. */
@Mixin(WorldlineCensusRenderer.class)
public abstract class WorldlineStageRendererMixin {
    @Inject(method = "render", at = @At("HEAD")) private void begin(BlockEntity be, double x, double y, double z, float tick, CallbackInfo ci) { WorldlineStageTimer.rendererBegin(); }
    @Inject(method = "render", at = @At("RETURN")) private void end(BlockEntity be, double x, double y, double z, float tick, CallbackInfo ci) { WorldlineStageTimer.rendererEnd(); }
}
