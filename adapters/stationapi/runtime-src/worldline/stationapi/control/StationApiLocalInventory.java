package worldline.stationapi.control;

import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** Reads native stacks and routes clicks through the singleplayer interaction manager. */
final class StationApiLocalInventory {
    private StationApiLocalInventory() { }
    static String items(Minecraft client) {
        Map<Integer, Integer> items = new TreeMap<Integer, Integer>();
        for (ItemStack stack : client.player.inventory.main) add(items, stack);
        for (ItemStack stack : client.player.inventory.armor) add(items, stack);
        add(items, client.player.inventory.getCursorStack());
        StringBuilder result = new StringBuilder("items");
        for (Map.Entry<Integer, Integer> entry : items.entrySet())
            result.append('|').append(entry.getKey()).append(',').append(entry.getValue());
        return result.toString();
    }
    static String tree(Minecraft client) {
        if (client.currentScreen == null) return "none";
        requireInventory(client); StringBuilder result = new StringBuilder("inventory");
        for (Object value : client.player.currentScreenHandler.slots) {
            Slot slot = (Slot) value; ItemStack stack = slot.getStack();
            result.append('|').append(slot.id).append(',').append(stack == null ? -1 : stack.itemId)
                    .append(',').append(stack == null ? 0 : stack.count);
        }
        return result.toString();
    }
    static void click(Minecraft client, int slot, int id, int count) {
        requireInventory(client);
        if (slot < 0 || slot >= client.player.currentScreenHandler.slots.size())
            throw new IllegalArgumentException("invalid native slot");
        ItemStack stack = client.player.currentScreenHandler.getSlot(slot).getStack();
        if ((stack == null ? -1 : stack.itemId) != id || (stack == null ? 0 : stack.count) != count)
            throw new IllegalArgumentException("stale native slot");
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot, 0, false, client.player);
    }
    private static void add(Map<Integer, Integer> items, ItemStack stack) {
        if (stack != null && stack.count > 0)
            items.put(stack.itemId, items.getOrDefault(stack.itemId, 0) + stack.count);
    }
    private static void requireInventory(Minecraft client) {
        if (!(client.currentScreen instanceof InventoryScreen))
            throw new IllegalStateException("inventory is not open");
    }
}
