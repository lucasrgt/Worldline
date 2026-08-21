package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.client.WorldlineDecompositionClient;

/** Installs the treatment at HEAD without depending on M74 injector order. */
@Mixin(GameRenderer.class)
public abstract class WorldlineDecompositionFrameMixin {
    @Shadow @Final private Minecraft client;
    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD")) private void setup(float delta, CallbackInfo callback) { WorldlineDecompositionClient.setup(client); }
}
