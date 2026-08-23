package worldline.m72.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m72.probe.WorldlineContentProbe;
import worldline.m72.WorldlineContentSync;

/** Keeps rendering after the first Aero invocation, then disconnects cleanly. */
@Mixin(GameRenderer.class)
public abstract class WorldlineContentFrameMixin {
  @Shadow private Minecraft client;
  @Unique private boolean complete;
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void frame(float delta, CallbackInfo callback) {
    if (complete)
      return;
    WorldlineContentSync.apply(client.world);
    if (!WorldlineContentProbe.ready())
      return;
    client.currentScreen = null;
    client.paused = false;
    client.skipGameRender = false;
    client.options.hideHud = true;
    client.options.bobView = false;
    if (WorldlineContentProbe.frame() < Integer.getInteger("worldline.content.frames", 20))
      return;
    complete = true;
    System.out.println("[WorldlineContent] complete frames=" + WorldlineContentProbe.frames());
    client.getNetworkHandler().disconnect();
    client.scheduleStop();
  }
}
