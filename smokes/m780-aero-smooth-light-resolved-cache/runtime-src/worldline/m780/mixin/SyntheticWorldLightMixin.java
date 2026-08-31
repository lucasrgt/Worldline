package worldline.m780.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m780.SyntheticLight;

@Mixin(value = World.class, priority = 1200)
public abstract class SyntheticWorldLightMixin {
    @Inject(method = "method_1782(III)F", at = @At("HEAD"), cancellable = true)
    private void worldlineBrightness(int x, int y, int z,
            CallbackInfoReturnable<Float> callback) {
        if (SyntheticLight.active()) callback.setReturnValue(SyntheticLight.brightness(x, y, z));
    }
}
