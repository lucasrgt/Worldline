package worldline.aero.mixin;

import net.minecraft.world.chunk.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Opt-in cap on vanilla's non-forced 24-chunk autosave batch.
 * {@code -Dworldline.saveBudget.chunks=1} spreads dirty writes across
 * later 40-tick saves. Forced saves and a missing/zero property keep 24.
 */
@Mixin(ChunkCache.class)
public abstract class WorldlineSaveBudgetMixin {
    @ModifyConstant(
        method = "save(ZLnet/minecraft/client/gui/screen/LoadingDisplay;)Z",
        constant = @Constant(intValue = 24)
    )
    private int worldlineSaveBatch(int vanilla, boolean force) {
        if (force) return vanilla;
        int cap = Integer.getInteger("worldline.saveBudget.chunks", 0).intValue();
        return cap > 0 ? cap : vanilla;
    }
}
