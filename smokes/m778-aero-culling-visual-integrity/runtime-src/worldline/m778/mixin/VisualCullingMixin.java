package worldline.m778.mixin;

import aero.modellib.render.Aero_FrustumCull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/** Makes the production culling switch mutable only inside the M778 oracle. */
@Mixin(value = Aero_FrustumCull.class, remap = false)
abstract class VisualCullingMixin {
    @Shadow @Final @Mutable public static boolean ENABLED;
}
