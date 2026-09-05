package worldline.m794.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m794.ProductionState;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class ProductionMinecraftMixin {
  @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
  private void worldlinePreventCaptureMenu(CallbackInfo callback) {
    if (ProductionState.retaining())
      callback.cancel();
  }

  @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
  private void worldlineDrive(CallbackInfo callback) {
    try {
      ProductionState.drive((Minecraft) (Object) this);
      if (ProductionState.freezeTicks())
        callback.cancel();
    } catch (RuntimeException error) {
      error.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
