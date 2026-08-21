package worldline.m74.mixin;

import aero.modellib.Aero_MeshRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Test-only bridge to the pinned Aero per-frame counter reset. */
@Mixin(Aero_MeshRenderer.class)
public interface WorldlineMeshCountersAccess {
    @Invoker("beginFrameCounters") static void worldline$reset() { throw new AssertionError(); }
}
