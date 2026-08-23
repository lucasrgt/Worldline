package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlinePairedProbe;
import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.packet.login.LoginHelloPacket;
import net.minecraft.network.packet.play.ChatMessagePacket;
import net.minecraft.network.packet.play.EntityAnimationPacket;
import net.minecraft.network.packet.play.PlayerMovePacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records the common chat anchor and bounded event-arm packet order. */
@Mixin(ClientNetworkHandler.class)
public abstract class WorldlinePairedNetworkMixin {
  @Inject(method = "onHello", at = @At("TAIL"))
  private void hello(LoginHelloPacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.hello();
  }
  @Inject(method = "onPlayerMove", at = @At("TAIL"))
  private void play(PlayerMovePacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.play();
  }
  @Inject(method = "handleChunkData", at = @At("TAIL"))
  private void chunk(ChunkDataS2CPacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.chunk();
  }
  @Inject(method = "onPlayerSpawn", at = @At("TAIL"))
  private void player(PlayerSpawnS2CPacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.identity(packet.name, packet.id);
  }
  @Inject(method = "onChatMessage", at = @At("TAIL"))
  private void chat(ChatMessagePacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.chat(packet.chatMessage);
  }
  @Inject(method = "onEntityAnimation", at = @At("TAIL"))
  private void animation(EntityAnimationPacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.animation(packet.id, packet.animationId);
  }
  @Inject(method = "onEntityStatus", at = @At("TAIL"))
  private void status(EntityStatusS2CPacket packet, CallbackInfo ci) {
    WorldlinePairedProbe.status(packet.id, packet.status);
  }
}
