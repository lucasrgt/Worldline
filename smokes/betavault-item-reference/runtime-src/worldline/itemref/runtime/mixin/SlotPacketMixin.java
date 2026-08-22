package worldline.itemref.runtime.mixin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.itemref.runtime.ItemRefs;

/** Controlled Packet103 suffix. */
@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public abstract class SlotPacketMixin {
    @Shadow private ItemStack stack;
    @Inject(method = "write(Ljava/io/DataOutputStream;)V", at = @At("TAIL"))
    private void writeReference(DataOutputStream output, CallbackInfo callback) throws IOException {
        ItemRefs.write(output, stack);
    }
    @Inject(method = "read(Ljava/io/DataInputStream;)V", at = @At("TAIL"))
    private void readReference(DataInputStream input, CallbackInfo callback) throws IOException {
        ItemRefs.read(input, stack);
    }
}
