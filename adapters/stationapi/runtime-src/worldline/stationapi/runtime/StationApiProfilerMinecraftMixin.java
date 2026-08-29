package worldline.stationapi.runtime;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Measures tick and display-present work surrounding the renderer frame. */
@Mixin(value = Minecraft.class, priority = 900)
public abstract class StationApiProfilerMinecraftMixin {
    @Unique private long worldlineTickStart;
    @Unique private long worldlineDisplayStart;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineTickBegin(CallbackInfo callback) {
        worldlineTickStart = StationApiProfilerRuntime.timer();
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void worldlineTickEnd(CallbackInfo callback) {
        if (worldlineTickStart != 0L)
            StationApiProfilerRuntime.tick(System.nanoTime() - worldlineTickStart);
    }

    @Inject(method = "run()V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;update()V"))
    private void worldlineDisplayBegin(CallbackInfo callback) {
        worldlineDisplayStart = StationApiProfilerRuntime.timer();
    }

    @Inject(method = "run()V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;update()V", shift = At.Shift.AFTER))
    private void worldlineDisplayEnd(CallbackInfo callback) {
        if (worldlineDisplayStart != 0L)
            StationApiProfilerRuntime.display(System.nanoTime() - worldlineDisplayStart);
    }
}
