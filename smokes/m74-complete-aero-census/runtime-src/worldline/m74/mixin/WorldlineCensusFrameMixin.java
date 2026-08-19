package worldline.m74.mixin;

import aero.modellib.Aero_ChunkVisibility;
import aero.modellib.Aero_MeshRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.*;

/** Arms, captures, seals, writes, and cleanly exits the complete census. */
@Mixin(GameRenderer.class)
public abstract class WorldlineCensusFrameMixin {
    @Shadow private Minecraft client; @Unique private boolean sent, ready, armed, complete;
    @Inject(method = "onFrameUpdate(F)V", at = @At("HEAD")) private void head(float delta, CallbackInfo callback) {
        WorldlineFrameCensus.head(); WorldlineMeshCountersAccess.worldline$reset();
    }
    @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL")) private void tail(float delta, CallbackInfo callback) {
        if (complete) return; WorldlineCensusSync.apply(client.world); WorldlineFrameCensus.tail(Aero_MeshRenderer.atRestRendersThisFrame(),
                Aero_MeshRenderer.atRestListCallsThisFrame(), Aero_ChunkVisibility.visibleChunkCount(), WorldlineCensusSync.packed(), WorldlineCensusProbe.mask());
        if (!sent && !WorldlineCensusProbe.warm()) return; client.currentScreen = null; client.paused = false; client.skipGameRender = false;
        client.options.hideHud = true; client.options.bobView = false;
        if (!sent) { MessagePacket packet = new MessagePacket(WorldlineCensusMod.ACTIVATE); packet.ints = new int[]{WorldlineCensusProbe.nonce()};
            client.getNetworkHandler().sendPacket(packet); sent = true; WorldlineCensusProbe.trigger(); return; }
        if (!ready) { client.player.yaw = -90F; client.player.pitch = 0F;
            if (!WorldlineCensusSync.tracked(client.world, client.player.x, client.player.y, client.player.z, client.player.yaw, client.player.pitch)) return;
            MessagePacket packet = new MessagePacket(WorldlineCensusMod.READY); packet.ints = WorldlineCensusSync.plan(); client.getNetworkHandler().sendPacket(packet);
            ready = true; System.out.println("[WorldlineCensus] plan-ready"); return; }
        if (!armed) { if (!WorldlineCensusSync.ready(WorldlineCensusProbe.mask())) return; armed = true;
            System.out.println("[WorldlineCensus] census-start mode=" + WorldlineCensusProbe.mode() + " nonce=" + WorldlineCensusProbe.nonce()
                    + " plan=" + WorldlineCensusSync.x() + "/" + WorldlineCensusSync.y() + "/" + WorldlineCensusSync.z()); WorldlineFrameCensus.arm(); return; }
        if (!WorldlineFrameCensus.sealed()) return; WorldlineCensusFile.write(); complete = true;
        System.out.println("[WorldlineCensus] complete mode=" + WorldlineCensusProbe.mode() + " samples=" + WorldlineFrameCensus.count()
                + " elapsedNs=" + WorldlineFrameCensus.elapsed() + " mask=" + WorldlineCensusProbe.mask());
        client.getNetworkHandler().disconnect(); client.scheduleStop();
    }
}
