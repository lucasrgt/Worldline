package worldline.stationapi.control;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Native operations for the local-world control profile. Every command leaves game time unchanged. */
final class StationApiLocalActions {
    private StationApiLocalActions() { }
    static String execute(Minecraft client, String command) {
        if (client.world.isRemote) throw new IllegalStateException("local control cannot mutate a remote world");
        String[] words = command.split(" ", -1);
        switch (words[0]) {
        case "BLOCK": case "SET_BLOCK":
            boolean write = words[0].equals("SET_BLOCK"); width(words, write ? 6 : 4);
            int x = integer(words, 1), y = integer(words, 2), z = integer(words, 3);
            require(y >= 0 && y < 128 && Math.abs((long) x) < 30000000 && Math.abs((long) z) < 30000000,
                    "block position outside Beta world");
            if (!write) return client.world.getBlockId(x, y, z) + "," + client.world.getBlockMeta(x, y, z);
            int id = integer(words, 4), meta = integer(words, 5);
            require(id >= 0 && id <= 255 && (id == 0 || Block.BLOCKS[id] != null) && meta >= 0 && meta < 16,
                    "invalid native block state");
            return Boolean.toString(client.world.setBlock(x, y, z, id, meta));
        case "TELEPORT":
            width(words, 4); double px = decimal(words, 1), py = decimal(words, 2), pz = decimal(words, 3);
            require(Math.abs(px) < 30000000 && py >= 0 && py < 128 && Math.abs(pz) < 30000000, "invalid pose");
            client.player.setPosition(px, py, pz); return "ok";
        case "HOTBAR":
            width(words, 2); int slot = integer(words, 1); require(slot >= 0 && slot < 9, "invalid hotbar slot");
            client.player.inventory.selectedSlot = slot; return "ok";
        case "GIVE":
            width(words, 3); give(client, integer(words, 1), integer(words, 2)); return "ok";
        case "ITEMS": width(words, 1); return StationApiLocalInventory.items(client);
        case "UI": width(words, 1); return StationApiLocalInventory.tree(client);
        case "OPEN_INVENTORY":
            width(words, 1); client.setScreen(new InventoryScreen(client.player));
            EntityRenderDispatcher.INSTANCE.init(client.world, client.textureManager, client.textRenderer,
                    client.player, client.options, 0F);
            client.gameRenderer.onFrameUpdate(0F); return "ok";
        case "CLOSE_UI": width(words, 1); client.setScreen(null); return "ok";
        case "CLICK":
            width(words, 4); StationApiLocalInventory.click(client, integer(words, 1), integer(words, 2), integer(words, 3));
            return "ok";
        default: throw new IllegalArgumentException("unknown local control command");
        }
    }
    private static void give(Minecraft client, int id, int count) {
        require(id >= 0 && id < Item.ITEMS.length && Item.ITEMS[id] != null && count > 0 && count <= 2304,
                "invalid give request");
        for (int remaining = count; remaining > 0;) {
            int chunk = Math.min(remaining, client.player.inventory.getMaxCountPerStack());
            ItemStack stack = new ItemStack(id, chunk, 0); client.player.inventory.addStack(stack);
            int added = chunk - stack.count; require(added > 0, "inventory is full"); remaining -= added;
        }
        client.player.inventory.markDirty();
    }
    private static int integer(String[] words, int index) { return Integer.parseInt(words[index]); }
    private static double decimal(String[] words, int index) {
        double value = Double.parseDouble(words[index]); require(Double.isFinite(value), "non-finite pose"); return value;
    }
    private static void width(String[] words, int expected) { require(words.length == expected, "command width"); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
}
