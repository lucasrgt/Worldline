package worldline.m770.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m770.GpuQueryCapture;
import worldline.m770.PresentState;
import worldline.profiling.ClientProfilerRuntime;

/** Drives M770 and separates forced GPU drain from Display.update. */
@Mixin(value = Minecraft.class, priority = 900)
public abstract class PresentMinecraftMixin {
    @Unique private long worldlineTickStart;
    @Unique private long worldlineDisplayStart;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineTickBegin(CallbackInfo callback) {
        PresentState.drive((Minecraft) (Object) this);
        worldlineTickStart = ClientProfilerRuntime.timer();
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void worldlineTickEnd(CallbackInfo callback) {
        if (worldlineTickStart != 0L) {
            ClientProfilerRuntime.tick(System.nanoTime() - worldlineTickStart);
        }
    }

    @Inject(method = "run()V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;update()V"))
    private void worldlineDisplayBegin(CallbackInfo callback) {
        GpuQueryCapture.beforeDisplayUpdate();
        worldlineDisplayStart = ClientProfilerRuntime.timer();
    }

    @Inject(method = "run()V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;update()V", shift = At.Shift.AFTER))
    private void worldlineDisplayEnd(CallbackInfo callback) {
        if (worldlineDisplayStart != 0L) {
            ClientProfilerRuntime.display(System.nanoTime() - worldlineDisplayStart);
        }
    }
}
