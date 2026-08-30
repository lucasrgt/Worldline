package worldline.m772.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m772.AutosaveProbe;

/** Captures every complete rendered frame in the retained autosave window. */
@Mixin(value = GameRenderer.class, priority = 900)
public abstract class AutosaveFrameMixin {
    @Unique private long worldlinePriorFrameStart;

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineFrameBegin(float tickDelta, CallbackInfo callback) {
        long current = System.nanoTime();
        if (worldlinePriorFrameStart != 0L) {
            AutosaveProbe.frame(current - worldlinePriorFrameStart);
        }
        worldlinePriorFrameStart = current;
    }
}
