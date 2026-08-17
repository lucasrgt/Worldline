package worldline.api;

import java.util.Objects;

/** Immutable four-click personal 2x2 crafting transition. */
public final class RemotePersonalCraft {
    private final int ingredientSlot, takeAction, placeAction, resultAction, storeAction;
    private final RemoteItemStack ingredient, output;
    private final RemoteInventoryView before, matrix, crafted, after;

    public RemotePersonalCraft(int ingredientSlot, int takeAction, int placeAction,
            int resultAction, int storeAction, RemoteItemStack ingredient, RemoteItemStack output,
            RemoteInventoryView before, RemoteInventoryView matrix,
            RemoteInventoryView crafted, RemoteInventoryView after) {
        if (ingredientSlot < 9 || ingredientSlot > 44 || takeAction < 1 || takeAction > 32764
                || placeAction != takeAction + 1 || resultAction != placeAction + 1
                || storeAction != resultAction + 1 || storeAction > 32767)
            throw new IllegalArgumentException("invalid personal crafting identity");
        if (ingredient == null || output == null || !window(before) || !window(matrix)
                || !window(crafted) || !window(after))
            throw new IllegalArgumentException("invalid personal crafting state");
        require(before.slot(ingredientSlot).item().equals(ingredient), "ingredient source mismatch");
        require(matrix.slot(ingredientSlot).empty() && matrix.slot(0).item().equals(output)
                && matrix.slot(1).item().equals(ingredient), "crafting matrix mismatch");
        require(crafted.slot(ingredientSlot).empty() && emptyCraft(crafted), "crafted state mismatch");
        require(after.slot(ingredientSlot).item().equals(output) && emptyCraft(after), "stored output mismatch");
        require(emptyCraft(before), "initial crafting matrix was occupied");
        for (int slot = 0; slot < 45; slot++) if (slot != 0 && slot != 1 && slot != ingredientSlot)
            require(before.slot(slot).equals(matrix.slot(slot)) && matrix.slot(slot).equals(crafted.slot(slot))
                    && crafted.slot(slot).equals(after.slot(slot)), "unrelated crafting slot changed");
        this.ingredientSlot = ingredientSlot; this.takeAction = takeAction; this.placeAction = placeAction;
        this.resultAction = resultAction; this.storeAction = storeAction;
        this.ingredient = ingredient; this.output = output; this.before = before;
        this.matrix = matrix; this.crafted = crafted; this.after = after;
    }

    public int ingredientSlot() { return ingredientSlot; }
    public int takeAction() { return takeAction; }
    public int placeAction() { return placeAction; }
    public int resultAction() { return resultAction; }
    public int storeAction() { return storeAction; }
    public RemoteItemStack ingredient() { return ingredient; }
    public RemoteItemStack output() { return output; }
    public RemoteInventoryView before() { return before; }
    public RemoteInventoryView matrix() { return matrix; }
    public RemoteInventoryView crafted() { return crafted; }
    public RemoteInventoryView after() { return after; }

    private static boolean window(RemoteInventoryView view) {
        return view != null && view.windowId() == 0 && view.size() == 45; }
    private static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false; return true; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message); }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemotePersonalCraft)) return false; RemotePersonalCraft value = (RemotePersonalCraft) other;
        return ingredientSlot == value.ingredientSlot && takeAction == value.takeAction
                && placeAction == value.placeAction && resultAction == value.resultAction
                && storeAction == value.storeAction && ingredient.equals(value.ingredient)
                && output.equals(value.output) && before.equals(value.before) && matrix.equals(value.matrix)
                && crafted.equals(value.crafted) && after.equals(value.after); }
    @Override public int hashCode() { return Objects.hash(ingredientSlot, takeAction, placeAction,
            resultAction, storeAction, ingredient, output, before, matrix, crafted, after); }
}
