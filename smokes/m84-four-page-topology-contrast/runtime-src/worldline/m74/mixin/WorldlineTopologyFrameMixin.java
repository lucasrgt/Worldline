package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;
import worldline.m74.client.*;

/** Coordinates one server-authored topology arm with the census. */
@Mixin(value = GameRenderer.class, priority = 1600)
public abstract class WorldlineTopologyFrameMixin {
  @Shadow @Final private Minecraft client;
  @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
  private void head(float d, CallbackInfo c) {
    normalize();
    WorldlineTopologyEvent.head(client);
  }
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void tail(float d, CallbackInfo c) {
    normalize();
    WorldlineTopologyEvent.tail();
    if (WorldlineFrameCensus.sealed() && !WorldlineTopologyFile.written())
      WorldlineTopologyFile.write();
  }
  private void normalize() {
    if (client.player != null) {
      client.player.yaw = -90F;
      client.player.pitch = 0F;
    }
  }
}
