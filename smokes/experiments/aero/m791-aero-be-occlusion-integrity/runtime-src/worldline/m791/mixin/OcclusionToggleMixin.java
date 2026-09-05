package worldline.m791.mixin;

import aero.modellib.Aero_BECellOcclusion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/** Makes the production candidate switch mutable only inside M791. */
@Mixin(value = Aero_BECellOcclusion.class, remap = false)
abstract class OcclusionToggleMixin {
    @Shadow @Final @Mutable public static boolean ENABLED;
}
