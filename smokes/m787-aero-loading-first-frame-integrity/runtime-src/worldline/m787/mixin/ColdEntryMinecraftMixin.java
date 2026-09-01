package worldline.m787.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m787.ColdEntryState;

/** Drives and closes the synchronous restored-world loading boundary. */
@Mixin(value = Minecraft.class, priority = 900)
public abstract class ColdEntryMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void worldlineDrive(CallbackInfo callback) {
        try {
            ColdEntryState.drive((Minecraft) (Object) this);
            if (ColdEntryState.freezeTicks()) callback.cancel();
        } catch (RuntimeException error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Inject(method = "startGame(Ljava/lang/String;Ljava/lang/String;J)V", at = @At("RETURN"))
    private void worldlineWorldLoaded(String directory, String name, long seed,
            CallbackInfo callback) {
        ColdEntryState.worldLoaded((Minecraft) (Object) this);
    }
}
