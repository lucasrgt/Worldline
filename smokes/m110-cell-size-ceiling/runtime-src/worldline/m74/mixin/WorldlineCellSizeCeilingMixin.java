package worldline.m74.mixin;

import net.minecraft.client.render.GameRenderer;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import worldline.m74.client.WorldlineCellSizeCeilingGate;

/** Publishes the immutable cell-size runtime before remote readiness. */
@Mixin(value=GameRenderer.class,priority=1600)
public abstract class WorldlineCellSizeCeilingMixin {
    @Inject(method="onFrameUpdate(F)V",at=@At("HEAD"))private void head(float delta,CallbackInfo ci){WorldlineCellSizeCeilingGate.check();}
}
