package worldline.m74.mixin;

import aero.modellib.model.Aero_MeshModel;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.gen.Accessor;import worldline.m74.client.WorldlineCensusRenderer;

/** Test-only access to the exact model identity cached by the M74 renderer. */
@Mixin(WorldlineCensusRenderer.class)
public interface WorldlineColdModelAccess {
    @Accessor("MODEL") static Aero_MeshModel worldline$model(){throw new AssertionError();}
}
