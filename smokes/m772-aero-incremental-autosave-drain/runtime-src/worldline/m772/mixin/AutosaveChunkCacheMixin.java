package worldline.m772.mixin;

import java.util.List;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldline.m772.AutosaveProbe;

/** Observes exact dirty-set drainage without changing the save algorithm. */
@Mixin(value = ChunkCache.class, priority = 900)
public abstract class AutosaveChunkCacheMixin {
    @Shadow private List<Chunk> chunks;

    @Inject(method = "save(ZLnet/minecraft/client/gui/screen/LoadingDisplay;)Z", at = @At("HEAD"))
    private void worldlineSaveBegin(boolean force, LoadingDisplay display,
                                    CallbackInfoReturnable<Boolean> callback) {
        AutosaveProbe.beginSave(force, chunks);
    }

    @Inject(method = "save(ZLnet/minecraft/client/gui/screen/LoadingDisplay;)Z", at = @At("RETURN"))
    private void worldlineSaveEnd(boolean force, LoadingDisplay display,
                                  CallbackInfoReturnable<Boolean> callback) {
        AutosaveProbe.endSave(force, chunks);
    }
}
