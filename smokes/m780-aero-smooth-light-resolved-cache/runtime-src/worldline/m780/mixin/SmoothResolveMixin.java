package worldline.m780.mixin;

import net.minecraft.world.World;
import net.minecraft.client.render.Tessellator;
import aero.modellib.render.Aero_RenderOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m780.SmoothLightProbe;
import worldline.m780.SyntheticLight;

/** Replaces only the smooth resolver's world sample with the controlled grid. */
@Mixin(targets = "aero.modellib.Aero_MeshSmoothLightRenderer", priority = 1200)
public abstract class SmoothResolveMixin {
    @Redirect(method = "resolve",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_1782(III)F"))
    private static float worldlineBrightness(World world, int x, int y, int z) {
        return SyntheticLight.brightness(x, y, z);
    }

    @Inject(method = "emit", at = @At("HEAD"))
    private static void worldlineResolved(Tessellator tessellator, float[][][] groups,
            float inverseScale, Aero_RenderOptions options, float[] resolved,
            CallbackInfo callback) {
        SmoothLightProbe.resolved(resolved);
    }
}
