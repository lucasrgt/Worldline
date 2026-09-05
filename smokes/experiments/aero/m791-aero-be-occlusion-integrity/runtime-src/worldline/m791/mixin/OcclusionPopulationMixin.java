package worldline.m791.mixin;

import aero.modellib.test.AeroTestMod;
import net.modificationstation.stationapi.api.event.world.gen.WorldGenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m791.OcclusionState;

/** The M791 fixture owns machine placement, including subsequently loaded chunks. */
@Mixin(value = AeroTestMod.class, remap = false)
public abstract class OcclusionPopulationMixin {
    @Inject(method = "populateChunk", at = @At("HEAD"), cancellable = true)
    private static void worldlineOwnPopulation(WorldGenEvent.ChunkDecoration event,
            CallbackInfo callback) {
        if (OcclusionState.ENABLED) callback.cancel();
    }
}
