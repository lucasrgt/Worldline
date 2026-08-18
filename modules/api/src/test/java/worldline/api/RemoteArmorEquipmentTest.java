package worldline.api;

import java.util.ArrayList;
import java.util.List;

final class RemoteArmorEquipmentTest {
    private RemoteArmorEquipmentTest() {}
    static void run() {
        RemoteItemStack helmet = new RemoteItemStack(298, 1, 0);
        RemoteInventoryView before = view(36, helmet, -1, null);
        RemoteInventoryView taken = view(-1, null, -1, null);
        RemoteInventoryView after = view(-1, null, 5, helmet);
        RemoteArmorEquip equip = new RemoteArmorEquip(36, RemoteArmorSlot.HELMET,
                1, 2, helmet, before, taken, after);
        if (equip.personalSlot() != 36 || equip.slot() != RemoteArmorSlot.HELMET
                || equip.takeAction() != 1 || equip.placeAction() != 2 || equip.after() != after)
            throw new AssertionError("armor equip accessors drifted");
        RemoteArmorPiece piece = new RemoteArmorPiece("Worldline", RemoteArmorSlot.HELMET, 298, 0);
        if (piece.legacyId() != 298 || piece.damage() != 0 || piece.slot().equipmentSlot() != 4)
            throw new AssertionError("remote armor accessors drifted");
        if (!RemoteArmorPiece.empty("Worldline", RemoteArmorSlot.HELMET).empty()
                || RemoteArmorSlot.fromEquipmentSlot(1) != RemoteArmorSlot.BOOTS)
            throw new AssertionError("remote empty armor mapping drifted");
        failure(() -> new RemoteArmorEquip(36, RemoteArmorSlot.HELMET, 1, 2,
                helmet, before, taken, view(-1, null, 6, helmet)));
        failure(() -> new RemoteArmorPiece("bad-name", RemoteArmorSlot.HELMET, 298, 0));
    }
    private static RemoteInventoryView view(int first, RemoteItemStack firstItem,
            int second, RemoteItemStack secondItem) {
        List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int index = 0; index < 45; index++) slots.add(new RemoteInventorySlot(index,
                index == first ? firstItem : index == second ? secondItem : null));
        return new RemoteInventoryView(0, slots);
    }
    private static void failure(Runnable action) { try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalArgumentException expected) { } }
}
