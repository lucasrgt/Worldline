package worldline.m776.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m776.MatrixState;

@Mixin(value = Minecraft.class, priority = 900)
public abstract class MatrixMinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineDrive(CallbackInfo callback) {
        try {
            MatrixState.drive((Minecraft) (Object) this);
        } catch (RuntimeException error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
