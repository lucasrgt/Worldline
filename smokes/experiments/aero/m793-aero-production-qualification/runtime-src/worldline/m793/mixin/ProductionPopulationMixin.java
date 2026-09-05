package worldline.m793.mixin;

import aero.modellib.test.AeroTestMod;
import net.modificationstation.stationapi.api.event.world.gen.WorldGenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m793.ProductionState;

/** The M793 fixture owns machine placement, including subsequently loaded chunks. */
@Mixin(value = AeroTestMod.class, remap = false)
public abstract class ProductionPopulationMixin {
    @Inject(method = "populateChunk", at = @At("HEAD"), cancellable = true)
    private static void worldlineOwnPopulation(WorldGenEvent.ChunkDecoration event,
            CallbackInfo callback) {
        if (ProductionState.ENABLED) callback.cancel();
    }
}
