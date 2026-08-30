package worldline.m768.mixin;

import aero.modellib.WorldlineHistoricalCensus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Snapshots the prior complete frame before Aero resets its frame counters. */
@Mixin(value = GameRenderer.class, priority = 900)
public abstract class HistoricalFrameMixin {
    @Shadow @Final private Minecraft client;
    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineHistoricalFrame(float tickDelta, CallbackInfo callback) {
        WorldlineHistoricalCensus.beforeFrame(client);
    }
}
