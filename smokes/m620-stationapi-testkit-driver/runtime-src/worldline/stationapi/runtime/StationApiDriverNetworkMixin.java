package worldline.stationapi.runtime;

import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.packet.login.LoginHelloPacket;
import net.minecraft.network.packet.play.PlayerMovePacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freezes driver readiness only after official login, pose, and chunk packets were applied. */
@Mixin(ClientNetworkHandler.class)
public abstract class StationApiDriverNetworkMixin {
    @Inject(method = "onHello", at = @At("TAIL"))
    private void worldlineHello(LoginHelloPacket packet, CallbackInfo callback) {
        StationApiDriverProbe.hello();
    }
    @Inject(method = "onPlayerMove", at = @At("TAIL"))
    private void worldlinePlay(PlayerMovePacket packet, CallbackInfo callback) {
        StationApiDriverProbe.play();
    }
    @Inject(method = "handleChunkData", at = @At("TAIL"))
    private void worldlineChunk(ChunkDataS2CPacket packet, CallbackInfo callback) {
        StationApiDriverProbe.chunk();
    }
}
