package worldline.m794.mixin;

import java.util.Random;
import net.minecraft.client.render.texture.LavaSideSprite;
import net.minecraft.client.render.texture.LavaSprite;
import net.minecraft.client.render.texture.WaterSideSprite;
import net.minecraft.client.render.texture.WaterSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Controlled oracle input: identical native procedural texture evolution in every JVM. */
@Mixin({WaterSprite.class, WaterSideSprite.class, LavaSprite.class, LavaSideSprite.class})
abstract class ProductionTextureMixin {
  @Unique private final Random worldlineTextureRandom = new Random(173L);
  @Unique private int worldlineTextureTicks;

  @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
  private double worldlineTextureRandom() {
    return worldlineTextureRandom.nextDouble();
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void worldlineTextureClock(CallbackInfo callback) {
    if (worldlineTextureTicks == 32)
      callback.cancel();
    else
      worldlineTextureTicks++;
  }
}
