package worldline.itemref.runtime.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.itemref.LogicalItemReference;
import worldline.itemref.runtime.ItemRefs;

/** Preserves logical identity across StationAPI's legacy physical slot application. */
@Mixin(ClientNetworkHandler.class)
public abstract class ClientInventoryReferenceMixin {
  @Shadow private Minecraft minecraft;
  @Unique private LogicalItemReference worldline$prior;
  @Unique private int worldline$priorId;
  @Unique private int worldline$priorDamage;

  @Inject(method = "onInventory(Lnet/minecraft/network/packet/s2c/play/InventoryS2CPacket;)V",
      at = @At("TAIL"))
  private void
  applyInventoryReferences(InventoryS2CPacket packet, CallbackInfo callback) {
    ScreenHandler handler = handler(packet.syncId);
    if (handler == null)
      return;
    for (int index = 0; index < packet.contents.length; index++) {
      LogicalItemReference reference = ItemRefs.get(packet.contents[index]);
      if (reference == null)
        continue;
      ItemStack target = handler.getSlot(index).getStack();
      if (target == null)
        throw new IllegalStateException("logical reference applied to empty slot");
      ItemRefs.set(target, reference);
    }
  }

  @Inject(
      method =
          "onScreenHandlerSlotUpdate(Lnet/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket;)V",
      at = @At("HEAD"))
  private void
  capturePriorReference(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callback) {
    worldline$prior = null;
    ScreenHandler handler = handler(packet.syncId);
    if (handler == null || packet.slot < 0 || packet.slot >= handler.slots.size())
      return;
    ItemStack prior = handler.getSlot(packet.slot).getStack();
    worldline$prior = ItemRefs.get(prior);
    if (prior != null) {
      worldline$priorId = prior.itemId;
      worldline$priorDamage = prior.getDamage();
    }
  }

  @Inject(
      method =
          "onScreenHandlerSlotUpdate(Lnet/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket;)V",
      at = @At("TAIL"))
  private void
  applySlotReference(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callback) {
    ScreenHandler handler = handler(packet.syncId);
    if (handler == null || packet.slot < 0 || packet.slot >= handler.slots.size())
      return;
    ItemStack target = handler.getSlot(packet.slot).getStack();
    LogicalItemReference incoming = ItemRefs.get(packet.stack);
    if (target != null && incoming != null)
      ItemRefs.set(target, incoming);
    else if (target != null && worldline$prior != null && target.itemId == worldline$priorId
        && target.getDamage() == worldline$priorDamage)
      ItemRefs.set(target, worldline$prior);
    worldline$prior = null;
  }

  @Unique
  private ScreenHandler handler(int syncId) {
    if (minecraft.player == null)
      return null;
    if (syncId == 0)
      return minecraft.player.playerScreenHandler;
    return minecraft.player.currentScreenHandler.syncId == syncId
        ? minecraft.player.currentScreenHandler
        : null;
  }
}
