package worldline.m74.mixin;

import net.minecraft.client.render.GameRenderer;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import worldline.m74.*;import worldline.m74.client.*;

/** Fires one cold transition at HEAD and publishes evidence only after seal. */
@Mixin(value=GameRenderer.class,priority=1600)
public abstract class WorldlineColdFrameMixin {
    @Inject(method="onFrameUpdate(F)V",at=@At("HEAD"))private void head(float delta,CallbackInfo ci){WorldlineColdEvent.head();}
    @Inject(method="onFrameUpdate(F)V",at=@At("TAIL"))private void tail(float delta,CallbackInfo ci){WorldlineColdEvent.tail();if(WorldlineFrameCensus.sealed()&&!WorldlineColdFile.written())WorldlineColdFile.write();}
}
