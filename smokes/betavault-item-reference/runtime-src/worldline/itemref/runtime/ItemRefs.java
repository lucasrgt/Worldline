package worldline.itemref.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.item.ItemStack;
import worldline.itemref.ItemReferenceCarrier;
import worldline.itemref.ItemReferenceWire;
import worldline.itemref.LogicalItemReference;

/** Runtime-only conversion between a physical stack and the neutral carrier. */
public final class ItemRefs {
    private ItemRefs() {}

    public static LogicalItemReference get(ItemStack stack) {
        return stack == null ? null
                : ((ItemReferenceCarrier) (Object) stack).worldline$getLogicalItemReference();
    }

    public static void set(ItemStack stack, LogicalItemReference reference) {
        if (stack == null) throw new IllegalArgumentException("stack");
        ((ItemReferenceCarrier) (Object) stack).worldline$setLogicalItemReference(reference);
    }

    public static void write(DataOutputStream output, ItemStack stack) throws IOException {
        ItemReferenceWire.write(output, get(stack));
    }

    public static void read(DataInputStream input, ItemStack stack) throws IOException {
        LogicalItemReference reference = ItemReferenceWire.read(input);
        if (reference != null && stack == null) throw new IOException("reference attached to empty stack");
        if (stack != null) set(stack, reference);
    }
}
