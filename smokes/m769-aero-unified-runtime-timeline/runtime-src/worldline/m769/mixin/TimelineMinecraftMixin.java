package worldline.m769.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m769.TimelineState;
import worldline.profiling.ClientProfilerRuntime;

/** Drives M769 and measures tick plus display-present work. */
@Mixin(value = Minecraft.class, priority = 900)
public abstract class TimelineMinecraftMixin {
    @Unique private long worldlineTickStart;
    @Unique private long worldlineDisplayStart;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineTickBegin(CallbackInfo callback) {
        TimelineState.drive((Minecraft) (Object) this);
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
