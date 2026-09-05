package worldline.m794.mixin;

import java.util.Random;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.binder.ArsenicFlowingLava;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.binder.ArsenicFlowingWater;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.binder.ArsenicStillLava;
import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.binder.ArsenicStillWater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Controlled oracle input: identical native procedural texture evolution in every JVM. */
@Mixin(value = {ArsenicStillWater.class, ArsenicFlowingWater.class, ArsenicStillLava.class, ArsenicFlowingLava.class},
    remap = false)
abstract class ProductionTextureMixin {
  @Unique private final Random worldlineTextureRandom = new Random(173L);
  @Unique private int worldlineTextureTicks;

  @Inject(method = "reloadFromTexturePack", at = @At("TAIL"))
  private void worldlineTextureReload(CallbackInfo callback) {
    worldlineTextureRandom.setSeed(173L);
    worldlineTextureTicks = 0;
  }

  @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
  private double worldlineTextureRandom() {
    return worldlineTextureRandom.nextDouble();
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void worldlineTextureClock(CallbackInfo callback) {
    if (worldlineTextureTicks == 32)
      callback.cancel();
    else if (++worldlineTextureTicks == 32)
      System.out.println("[M794 texture ready] " + getClass().getSimpleName() + " ticks=32");
  }
}
