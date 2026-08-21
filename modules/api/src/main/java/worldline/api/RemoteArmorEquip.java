package worldline.api;

import java.util.Objects;

/** Immutable accepted two-click move from personal storage into one leather armor slot. */
public final class RemoteArmorEquip {
    private final int personalSlot, takeAction, placeAction; private final RemoteArmorSlot slot;
    private final RemoteItemStack stack; private final RemoteInventoryView before, taken, after;
    public RemoteArmorEquip(int personalSlot, RemoteArmorSlot slot, int takeAction, int placeAction,
            RemoteItemStack stack, RemoteInventoryView before, RemoteInventoryView taken,
            RemoteInventoryView after) {
        RemoteItemStack expected = slot == null ? null : new RemoteItemStack(slot.leatherItemId(), 1, 0);
        if (personalSlot < 9 || personalSlot > 44 || slot == null || takeAction < 1
                || takeAction > 32766 || placeAction != takeAction + 1 || !expected.equals(stack)
                || !window(before) || !window(taken) || !window(after))
            throw new IllegalArgumentException("invalid leather armor equip identity");
        int armor = slot.containerSlot();
        if (!item(before, personalSlot, stack) || !before.slot(armor).empty()
                || !taken.slot(personalSlot).empty() || !taken.slot(armor).empty()
                || !after.slot(personalSlot).empty() || !item(after, armor, stack))
            throw new IllegalArgumentException("invalid leather armor equip state");
        for (int index = 0; index < 45; index++) if (index != personalSlot && index != armor
                && (!before.slot(index).equals(taken.slot(index)) || !taken.slot(index).equals(after.slot(index))))
            throw new IllegalArgumentException("leather armor equip changed unrelated slot");
        this.personalSlot = personalSlot; this.slot = slot; this.takeAction = takeAction;
        this.placeAction = placeAction; this.stack = stack; this.before = before; this.taken = taken; this.after = after;
    }
    public int personalSlot() { return personalSlot; } public RemoteArmorSlot slot() { return slot; }
    public int takeAction() { return takeAction; } public int placeAction() { return placeAction; }
    public RemoteItemStack stack() { return stack; } public RemoteInventoryView before() { return before; }
    public RemoteInventoryView taken() { return taken; } public RemoteInventoryView after() { return after; }
    private static boolean window(RemoteInventoryView view) { return view != null
            && view.windowId() == 0 && view.size() == 45; }
    private static boolean item(RemoteInventoryView view, int index, RemoteItemStack expected) {
        return !view.slot(index).empty() && view.slot(index).item().equals(expected); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteArmorEquip)) return false;
        RemoteArmorEquip value = (RemoteArmorEquip) other; return personalSlot == value.personalSlot
                && slot == value.slot && takeAction == value.takeAction && placeAction == value.placeAction
                && stack.equals(value.stack) && before.equals(value.before) && taken.equals(value.taken)
                && after.equals(value.after); }
    @Override public int hashCode() { return Objects.hash(personalSlot, slot, takeAction, placeAction,
            stack, before, taken, after); }
}
