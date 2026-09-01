package worldline.m787.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m787.ColdEntryLoadTrace;
import worldline.m787.ColdEntryState;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class ColdEntryFrameMixin {
    @Shadow private Minecraft client;

    @ModifyVariable(method = "onFrameUpdate(F)V", at = @At("HEAD"), argsOnly = true)
    private float worldlineFixedDelta(float tickDelta) {
        return ColdEntryState.fixtureActive() ? 1.0F : tickDelta;
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
    private void worldlineBeginFrame(float tickDelta, CallbackInfo callback) {
        ColdEntryState.beginFrame(client);
    }

    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
    private void worldlineFinishFrame(float tickDelta, CallbackInfo callback) {
        ColdEntryState.finishFrame(client);
    }

    @Inject(method = "renderWorld(FI)V", at = @At("HEAD"))
    private void worldlineObserveRenderWorld(float tickDelta, int eye, CallbackInfo callback) {
        ColdEntryLoadTrace.renderWorld();
    }
}
