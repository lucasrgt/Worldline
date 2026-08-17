package worldline.api;

import java.util.Objects;

/** Immutable four-action personal-tail to furnace input/fuel load. */
public final class RemoteFurnaceLoad {
    private final int inputPersonalSlot, fuelPersonalSlot;
    private final int inputTakeAction, inputStoreAction, fuelTakeAction, fuelStoreAction;
    private final RemoteItemStack input, fuel;
    private final RemoteInventoryView before, after, personalBefore, personalAfter;

    public RemoteFurnaceLoad(int inputPersonalSlot, int fuelPersonalSlot, int inputTakeAction,
            int inputStoreAction, int fuelTakeAction, int fuelStoreAction,
            RemoteItemStack input, RemoteItemStack fuel,
            RemoteInventoryView before, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter) {
        if (inputPersonalSlot < 9 || inputPersonalSlot > 44 || fuelPersonalSlot < 9
                || fuelPersonalSlot > 44 || inputPersonalSlot == fuelPersonalSlot
                || inputTakeAction < 1 || inputTakeAction > 32764
                || inputStoreAction != inputTakeAction + 1 || fuelTakeAction != inputStoreAction + 1
                || fuelStoreAction != fuelTakeAction + 1 || input == null || fuel == null
                || before == null || after == null || personalBefore == null || personalAfter == null
                || before.windowId() < 1 || before.windowId() > 100
                || before.windowId() != after.windowId() || before.size() != 39 || after.size() != 39
                || personalBefore.windowId() != 0 || personalAfter.windowId() != 0
                || personalBefore.size() != 45 || personalAfter.size() != 45
                || !input.equals(new RemoteItemStack(12, 1, 0))
                || !fuel.equals(new RemoteItemStack(263, 1, 0)))
            throw new IllegalArgumentException("invalid furnace load identity");
        int inputCombined = inputPersonalSlot - 6, fuelCombined = fuelPersonalSlot - 6;
        if (before.slot(inputCombined).empty() || before.slot(fuelCombined).empty()
                || !before.slot(inputCombined).item().equals(input)
                || !before.slot(fuelCombined).item().equals(fuel)
                || !before.slot(0).empty() || !before.slot(1).empty() || !before.slot(2).empty()
                || !after.slot(inputCombined).empty() || !after.slot(fuelCombined).empty()
                || after.slot(0).empty() || after.slot(1).empty() || !after.slot(2).empty()
                || !after.slot(0).item().equals(input) || !after.slot(1).item().equals(fuel)
                || personalBefore.slot(inputPersonalSlot).empty()
                || personalBefore.slot(fuelPersonalSlot).empty()
                || !personalBefore.slot(inputPersonalSlot).item().equals(input)
                || !personalBefore.slot(fuelPersonalSlot).item().equals(fuel)
                || !personalAfter.slot(inputPersonalSlot).empty()
                || !personalAfter.slot(fuelPersonalSlot).empty())
            throw new IllegalArgumentException("invalid furnace load state");
        for (int slot = 0; slot < 39; slot++) if (slot != 0 && slot != 1
                && slot != inputCombined && slot != fuelCombined
                && !before.slot(slot).equals(after.slot(slot)))
            throw new IllegalArgumentException("furnace load changed unrelated combined slot");
        for (int slot = 0; slot < 45; slot++) if (slot != inputPersonalSlot
                && slot != fuelPersonalSlot && !personalBefore.slot(slot).equals(personalAfter.slot(slot)))
            throw new IllegalArgumentException("furnace load changed unrelated personal slot");
        for (int slot = 9; slot < 45; slot++) {
            int combined = slot - 6;
            if (!same(before.slot(combined), personalBefore.slot(slot))
                    || !same(after.slot(combined), personalAfter.slot(slot)))
                throw new IllegalArgumentException("furnace tail drift");
        }
        this.inputPersonalSlot = inputPersonalSlot; this.fuelPersonalSlot = fuelPersonalSlot;
        this.inputTakeAction = inputTakeAction; this.inputStoreAction = inputStoreAction;
        this.fuelTakeAction = fuelTakeAction; this.fuelStoreAction = fuelStoreAction;
        this.input = input; this.fuel = fuel; this.before = before; this.after = after;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }
    public int inputPersonalSlot() { return inputPersonalSlot; }
    public int fuelPersonalSlot() { return fuelPersonalSlot; }
    public int inputTakeAction() { return inputTakeAction; }
    public int inputStoreAction() { return inputStoreAction; }
    public int fuelTakeAction() { return fuelTakeAction; }
    public int fuelStoreAction() { return fuelStoreAction; }
    public RemoteItemStack input() { return input; }
    public RemoteItemStack fuel() { return fuel; }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView after() { return after; }
    public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteFurnaceLoad)) return false; RemoteFurnaceLoad value = (RemoteFurnaceLoad) other;
        return inputPersonalSlot == value.inputPersonalSlot && fuelPersonalSlot == value.fuelPersonalSlot
                && inputTakeAction == value.inputTakeAction && inputStoreAction == value.inputStoreAction
                && fuelTakeAction == value.fuelTakeAction && fuelStoreAction == value.fuelStoreAction
                && input.equals(value.input) && fuel.equals(value.fuel)
                && before.equals(value.before) && after.equals(value.after)
                && personalBefore.equals(value.personalBefore) && personalAfter.equals(value.personalAfter); }
    @Override public int hashCode() { return Objects.hash(inputPersonalSlot, fuelPersonalSlot,
            inputTakeAction, inputStoreAction, fuelTakeAction, fuelStoreAction, input, fuel,
            before, after, personalBefore, personalAfter); }
    private static boolean same(RemoteInventorySlot left, RemoteInventorySlot right) {
        return left.empty() ? right.empty() : !right.empty() && left.item().equals(right.item()); }
}
