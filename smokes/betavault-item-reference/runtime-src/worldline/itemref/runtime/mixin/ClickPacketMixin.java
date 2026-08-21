package worldline.itemref.runtime.mixin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.itemref.runtime.ItemRefs;

/** Controlled Packet102 suffix for transaction prediction. */
@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickPacketMixin {
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
