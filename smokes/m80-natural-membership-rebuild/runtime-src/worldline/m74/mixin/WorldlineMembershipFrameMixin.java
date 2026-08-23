package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;
import worldline.m74.client.*;

/** Coordinates the natural server-authored membership change with the census. */
@Mixin(value = GameRenderer.class, priority = 1600)
public abstract class WorldlineMembershipFrameMixin {
  @Shadow @Final private Minecraft client;
  @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD"))
  private void head(float d, CallbackInfo c) {
    WorldlineMembershipEvent.head(client);
  }
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void tail(float d, CallbackInfo c) {
    WorldlineMembershipEvent.tail();
    if (WorldlineFrameCensus.sealed() && !WorldlineMembershipFile.written())
      WorldlineMembershipFile.write();
  }
}
