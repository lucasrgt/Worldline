package worldline.itemref.runtime;

import betaenergistics.storage.BE_ItemKey;
import betaenergistics.vault.BE_CellMutation;
import betaenergistics.vault.BE_CellVault;
import betavault.minecraft.VaultReference;
import java.lang.invoke.MethodHandles;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Paths;
import java.util.Collections;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.server.event.network.PlayerPacketHandlerSetEvent;
import worldline.itemref.LogicalItemReference;

/** Real BetaEnergistics cell creation and restart resolution through an ItemStack. */
public final class ItemReferenceServer {
    private static final BE_ItemKey IRON = new BE_ItemKey(265, 0);
    private static ServerPlayerEntity player;
    private static int ticks;
    static { EntrypointManager.registerLookup(MethodHandles.lookup()); }

    @EventListener
    private static void connected(PlayerPacketHandlerSetEvent event) {
        if (player != null) throw new IllegalStateException("item-reference smoke supports one client");
        player = event.player; ticks = 0;
    }

    @EventListener
    private static void tick(GameTickEvent.End event) {
        if (player == null || ++ticks < 80) return;
        ServerPlayerEntity current = player; player = null;
        String phase = required("worldline.itemref.phase");
        BE_CellVault vault = BE_CellVault.open(Paths.get(required("worldline.itemref.world")));
        current.skipPacketSlotUpdates = true;
        try {
            if (phase.equals("create")) create(current, vault);
            else if (phase.equals("reload")) reload(current, vault);
            else throw new IllegalStateException("invalid item-reference phase");
            current.playerScreenHandler.sendContentUpdates();
        } finally { current.skipPacketSlotUpdates = false; }
        current.networkHandler.sendPacket(new InventoryS2CPacket(
                current.playerScreenHandler.syncId, current.playerScreenHandler.getStacks()));
    }

    private static void create(ServerPlayerEntity player, BE_CellVault vault) {
        require(player.inventory.main[0] == null, "create inventory was not empty");
        VaultReference reference = vault.create(1, 4096);
        try (BE_CellMutation mutation = vault.begin(reference)) {
            require(mutation.insert(IRON, 100) == 100, "cell insert drift");
            require(mutation.commit().writes() == 1, "cell transaction drift");
        }
        LogicalItemReference logical = LogicalItemReference.parse(reference.canonical());
        ItemStack stack = new ItemStack(1, 1, 0); ItemRefs.set(stack, logical);
        require(logical.equals(ItemRefs.get(stack.copy())), "copy lost reference");
        require(logical.equals(ItemRefs.get(stack.copy().split(1))), "split lost reference");
        require(logical.equals(packetRoundTrip(stack)), "Packet104 codec lost reference");
        VaultReference other = vault.create(1, 4096);
        ItemStack distinct = new ItemStack(1, 1, 0);
        ItemRefs.set(distinct, LogicalItemReference.parse(other.canonical()));
        require(!stack.isItemEqual(distinct) && !stack.equals(distinct)
                && !ItemStack.areEqual(stack, distinct), "distinct cells compared equal");
        player.inventory.setStack(0, stack);
        require(logical.equals(ItemRefs.get(player.playerScreenHandler.getSlot(36).getStack())),
                "screen-handler slot lost reference");
        System.out.println("WORLDLINE_ITEMREF_CREATE=PASS ref=" + reference.canonical()
                + " amount=100 copy=preserved split=preserved equality=isolated");
    }

    private static void reload(ServerPlayerEntity player, BE_CellVault vault) {
        ItemStack stack = player.inventory.main[0];
        LogicalItemReference logical = ItemRefs.get(stack);
        require(logical != null, "player NBT lost reference");
        VaultReference reference = VaultReference.parse(logical.canonical());
        require(vault.read(reference).amount(IRON) == 100, "logical contents drifted");
        System.out.println("WORLDLINE_ITEMREF_RELOAD=PASS ref=" + reference.canonical()
                + " amount=100 nbt=preserved");
    }

    private static String required(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isEmpty()) throw new IllegalStateException("missing " + name);
        return value;
    }

    private static LogicalItemReference packetRoundTrip(ItemStack stack) {
        InventoryS2CPacket outgoing = new InventoryS2CPacket(0, Collections.singletonList(stack));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        outgoing.write(new DataOutputStream(bytes));
        InventoryS2CPacket incoming = new InventoryS2CPacket();
        incoming.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
        return ItemRefs.get(incoming.contents[0]);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
