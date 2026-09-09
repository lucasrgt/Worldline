package worldline.stationapi.runtime;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Qualification-only assertion that the configured world is authoritative and local. */
@Mixin(Minecraft.class)
public abstract class WorldControlProbeMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void verifyLocalWorld(CallbackInfo callback) {
        Minecraft client = (Minecraft) (Object) this;
        if (Boolean.getBoolean("worldline.stationapi.localWorld") && client.world != null && client.world.isRemote)
            throw new AssertionError("local control qualification entered a remote world");
    }
}
