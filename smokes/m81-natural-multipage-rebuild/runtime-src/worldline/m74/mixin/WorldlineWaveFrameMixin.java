package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;
import worldline.m74.client.*;

/** Coordinates the natural server-authored multipage change with the census. */
@Mixin(value = GameRenderer.class, priority = 1600)
public abstract class WorldlineWaveFrameMixin {
  @Shadow @Final private Minecraft client;
  @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
  private void head(float d, CallbackInfo c) {
    WorldlineWaveEvent.head(client);
  }
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void tail(float d, CallbackInfo c) {
    WorldlineWaveEvent.tail();
    if (WorldlineFrameCensus.sealed() && !WorldlineWaveFile.written())
      WorldlineWaveFile.write();
  }
}
