package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineMultiplayerProbe;
import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.packet.login.LoginHelloPacket;
import net.minecraft.network.packet.play.PlayerMovePacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records official login, play-ready and remote chunk handler boundaries. */
@Mixin(ClientNetworkHandler.class)
public abstract class WorldlineMultiplayerNetworkMixin {
    @Inject(method = "onHello", at = @At("TAIL"))
    private void worldlineHello(LoginHelloPacket packet, CallbackInfo callback) {
        WorldlineMultiplayerProbe.hello();
    }
    @Inject(method = "onPlayerMove", at = @At("TAIL"))
    private void worldlinePlayReady(PlayerMovePacket packet, CallbackInfo callback) {
        WorldlineMultiplayerProbe.playReady();
    }
    @Inject(method = "handleChunkData", at = @At("TAIL"))
    private void worldlineChunk(ChunkDataS2CPacket packet, CallbackInfo callback) {
        WorldlineMultiplayerProbe.chunk();
    }
}
