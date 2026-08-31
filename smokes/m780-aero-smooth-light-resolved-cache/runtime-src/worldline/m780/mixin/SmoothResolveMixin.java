package worldline.m780.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import worldline.m780.SyntheticLight;

/** Replaces only the smooth resolver's world sample with the controlled grid. */
@Mixin(targets = "aero.modellib.Aero_MeshSmoothLightRenderer", priority = 1200)
public abstract class SmoothResolveMixin {
    @Redirect(method = "resolve",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_1782(III)F"))
    private static float worldlineBrightness(World world, int x, int y, int z) {
        return SyntheticLight.brightness(x, y, z);
    }
}
