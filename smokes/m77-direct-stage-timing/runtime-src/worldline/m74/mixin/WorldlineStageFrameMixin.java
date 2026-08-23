package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;
import worldline.m74.client.WorldlineStageGate;

/** Aligns M77's primitive stage records to the retained M74 frame records. */
@Mixin(value = GameRenderer.class, priority = 1500)
public abstract class WorldlineStageFrameMixin {
  @Shadow @Final private Minecraft client;
  @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
  private void head(float delta, CallbackInfo callback) {
    if (!WorldlineStageGate.prepare(client))
      WorldlineStageTimer.head();
  }
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void tail(float delta, CallbackInfo callback) {
    WorldlineStageTimer.tail();
    if (WorldlineFrameCensus.sealed() && !WorldlineStageFile.written())
      WorldlineStageFile.write();
  }
}
