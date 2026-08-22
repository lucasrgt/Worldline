package worldline.itemref.runtime.mixin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.itemref.runtime.ItemRefs;

/** Appends one nullable logical reference per decoded Packet104 slot. */
@Mixin(InventoryS2CPacket.class)
public abstract class InventoryPacketMixin {
    @Shadow private ItemStack[] contents;

    @Inject(method = "write(Ljava/io/DataOutputStream;)V", at = @At("TAIL"))
    private void writeReferences(DataOutputStream output, CallbackInfo callback) throws IOException {
        for (ItemStack stack : contents) ItemRefs.write(output, stack);
    }

    @Inject(method = "read(Ljava/io/DataInputStream;)V", at = @At("TAIL"))
    private void readReferences(DataInputStream input, CallbackInfo callback) throws IOException {
        for (ItemStack stack : contents) ItemRefs.read(input, stack);
    }
}
