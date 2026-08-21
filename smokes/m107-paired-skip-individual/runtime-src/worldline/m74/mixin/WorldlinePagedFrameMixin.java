package worldline.m74.mixin;

import net.minecraft.client.Minecraft;import net.minecraft.client.render.GameRenderer;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;import worldline.m74.client.*;

/** Aligns paged timing/state to every retained M74 record. */
@Mixin(value=GameRenderer.class,priority=1500)
public abstract class WorldlinePagedFrameMixin {
    @Shadow @Final private Minecraft client;
    @Inject(method="onFrameUpdate(F)V",at=@At("HEAD"))private void head(float delta,CallbackInfo ci){if(!WorldlinePagedGate.prepare(client))WorldlinePagedTimer.head();}
    @Inject(method="onFrameUpdate(F)V",at=@At("TAIL"))private void tail(float delta,CallbackInfo ci){WorldlinePagedTimer.tail();if(WorldlineFrameCensus.sealed()&&!WorldlinePagedFile.written())WorldlinePagedFile.write();}
}
