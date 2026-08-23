package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;
import worldline.m74.client.*;

/** Coordinates the natural server-authored recovery change with the census. */
@Mixin(value = GameRenderer.class, priority = 1600)
public abstract class WorldlineRecoveryFrameMixin {
  @Shadow @Final private Minecraft client;
  @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
  private void head(float d, CallbackInfo c) {
    WorldlineRecoveryEvent.head(client);
  }
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void tail(float d, CallbackInfo c) {
    WorldlineRecoveryEvent.tail();
    if (WorldlineFrameCensus.sealed() && !WorldlineRecoveryFile.written())
      WorldlineRecoveryFile.write();
  }
}
