package worldline.b173;

import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import worldline.api.ItemCensus;
import worldline.api.WearCensus;

/** Shared stack-to-census folding for player, drops, and containers. */
final class B173Items {
    private B173Items() {}

    static ItemCensus add(ItemCensus census, ItemStack stack) {
        return stack == null ? census : census.plus(stack.itemID, stack.stackSize);
    }

    static ItemCensus add(ItemCensus census, ItemStack[] stacks) {
        if (stacks == null) return census;
        for (int index = 0; index < stacks.length; index++) census = add(census, stacks[index]);
        return census;
    }

    static ItemCensus add(ItemCensus census, IInventory inventory) {
        if (inventory == null) return census;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            census = add(census, inventory.getStackInSlot(slot));
        }
        return census;
    }

    static ItemCensus addContainer(ItemCensus census, ItemStack stack) {
        if (stack == null) return census;
        Item item = stack.getItem();
        if (item == null || !item.hasContainerItem()) return census;
        Item leftover = item.getContainerItem();
        if (leftover == null) return census;
        int count = stack.stackSize > 0 ? stack.stackSize : 1;
        return census.plus(leftover.shiftedIndex, count);
    }

    static WearCensus wear(ItemStack stack) {
        if (stack == null) return WearCensus.empty();
        Item item = stack.getItem();
        if (item == null || item.getMaxDamage() <= 0) return WearCensus.empty();
        return WearCensus.empty().plus(stack.itemID, stack.getItemDamage(), 1);
    }

    static WearCensus wear(ItemStack[] stacks) {
        if (stacks == null) return WearCensus.empty();
        WearCensus census = WearCensus.empty();
        for (int index = 0; index < stacks.length; index++) census = census.plus(wear(stacks[index]));
        return census;
    }

    static WearCensus wear(World world) {
        if (world == null) throw new NullPointerException("world");
        WearCensus census = WearCensus.empty();
        for (Object value : world.loadedEntityList) {
            if (value instanceof EntityItem) census = census.plus(wear(((EntityItem) value).item));
            else if (value instanceof IInventory && !(value instanceof EntityPlayer)) {
                census = census.plus(wear((IInventory) value));
            }
        }
        for (Object value : world.loadedTileEntityList) {
            if (value instanceof IInventory) census = census.plus(wear((IInventory) value));
        }
        return census;
    }

    private static WearCensus wear(IInventory inventory) {
        WearCensus census = WearCensus.empty();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            census = census.plus(wear(inventory.getStackInSlot(slot)));
        }
        return census;
    }

    static ItemCensus addContainer(ItemCensus census, ItemStack[] stacks) {
        if (stacks == null) return census;
        for (int index = 0; index < stacks.length; index++) {
            census = addContainer(census, stacks[index]);
        }
        return census;
    }
}
