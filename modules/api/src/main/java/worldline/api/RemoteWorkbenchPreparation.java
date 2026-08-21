package worldline.api;

import java.util.Objects;

/** Immutable accepted preparation of the exact three-plank workbench row. */
public final class RemoteWorkbenchPreparation {
    private final int personalSlot, takeAction, firstAction, secondAction, thirdAction;
    private final RemoteItemStack ingredient, intermediateResult, modeledResult;
    private final RemoteItemStack cursorAfterTake, cursorAfterFirst, cursorAfterSecond;
    private final RemoteInventoryView before, oneWide, twoWide, prepared, personalBefore, personalAfter;

    public RemoteWorkbenchPreparation(int personalSlot, int takeAction, int firstAction,
            int secondAction, int thirdAction, RemoteItemStack ingredient,
            RemoteItemStack intermediateResult, RemoteItemStack modeledResult,
            RemoteItemStack cursorAfterTake, RemoteItemStack cursorAfterFirst,
            RemoteItemStack cursorAfterSecond, boolean cursorEmptyAfterThird,
            RemoteInventoryView before, RemoteInventoryView oneWide, RemoteInventoryView twoWide,
            RemoteInventoryView prepared, RemoteInventoryView personalBefore, RemoteInventoryView personalAfter) {
        if (personalSlot < 9 || personalSlot > 44 || takeAction < 1 || takeAction > 32764
                || firstAction != takeAction + 1 || secondAction != firstAction + 1
                || thirdAction != secondAction + 1 || ingredient == null || intermediateResult == null
                || modeledResult == null || !container(before) || !container(oneWide) || !container(twoWide)
                || !container(prepared) || !personal(personalBefore) || !personal(personalAfter)
                || before.windowId() != oneWide.windowId() || before.windowId() != twoWide.windowId()
                || before.windowId() != prepared.windowId() || !cursorEmptyAfterThird)
            throw new IllegalArgumentException("invalid workbench preparation identity");
        RemoteItemStack planks = new RemoteItemStack(5, 3, 0), one = new RemoteItemStack(5, 1, 0);
        if (!ingredient.equals(planks) || !intermediateResult.equals(new RemoteItemStack(72, 1, 0))
                || !modeledResult.equals(new RemoteItemStack(44, 3, 2))
                || !planks.equals(cursorAfterTake) || !new RemoteItemStack(5, 2, 0).equals(cursorAfterFirst)
                || !one.equals(cursorAfterSecond))
            throw new IllegalArgumentException("invalid workbench preparation recipe");
        int source = personalSlot + 1;
        require(item(before, source, planks) && emptyOwned(before), "invalid initial workbench state");
        require(oneWide.slot(source).empty() && oneWide.slot(0).empty() && item(oneWide, 1, one)
                && oneWide.slot(2).empty() && oneWide.slot(3).empty(), "invalid one-wide workbench state");
        require(twoWide.slot(source).empty() && item(twoWide, 0, intermediateResult)
                && item(twoWide, 1, one) && item(twoWide, 2, one)
                && twoWide.slot(3).empty(), "invalid two-wide workbench state");
        require(prepared.slot(source).empty() && item(prepared, 0, modeledResult)
                && item(prepared, 1, one) && item(prepared, 2, one)
                && item(prepared, 3, one), "invalid prepared workbench state");
        require(item(personalBefore, personalSlot, planks)
                && personalAfter.slot(personalSlot).empty(), "invalid personal preparation state");
        require(tail(before, personalBefore) && tail(oneWide, personalAfter)
                && tail(twoWide, personalAfter) && tail(prepared, personalAfter), "workbench tail drifted");
        for (int slot = 0; slot < 46; slot++) if (slot != 0 && slot != 1 && slot != 2
                && slot != 3 && slot != source) require(before.slot(slot).equals(oneWide.slot(slot))
                && oneWide.slot(slot).equals(twoWide.slot(slot)) && twoWide.slot(slot).equals(prepared.slot(slot)),
                "unrelated workbench slot changed");
        for (int slot = 0; slot < 45; slot++) if (slot != personalSlot)
            require(personalBefore.slot(slot).equals(personalAfter.slot(slot)), "unrelated personal slot changed");
        this.personalSlot = personalSlot; this.takeAction = takeAction; this.firstAction = firstAction;
        this.secondAction = secondAction; this.thirdAction = thirdAction; this.ingredient = ingredient;
        this.intermediateResult = intermediateResult; this.modeledResult = modeledResult;
        this.cursorAfterTake = cursorAfterTake; this.cursorAfterFirst = cursorAfterFirst;
        this.cursorAfterSecond = cursorAfterSecond; this.before = before; this.oneWide = oneWide;
        this.twoWide = twoWide; this.prepared = prepared;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }
    public int personalSlot() { return personalSlot; } public int takeAction() { return takeAction; }
    public int firstAction() { return firstAction; } public int secondAction() { return secondAction; }
    public int thirdAction() { return thirdAction; } public RemoteItemStack ingredient() { return ingredient; }
    public RemoteItemStack intermediateResult() { return intermediateResult; }
    public RemoteItemStack modeledResult() { return modeledResult; }
    public RemoteItemStack cursorAfterTake() { return cursorAfterTake; }
    public RemoteItemStack cursorAfterFirst() { return cursorAfterFirst; }
    public RemoteItemStack cursorAfterSecond() { return cursorAfterSecond; }
    public boolean cursorEmptyAfterThird() { return true; }
    public RemoteInventoryView before() { return before; } public RemoteInventoryView oneWide() { return oneWide; }
    public RemoteInventoryView twoWide() { return twoWide; }
    public RemoteInventoryView prepared() { return prepared; }
    public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }
    private static boolean container(RemoteInventoryView view) { return view != null && view.windowId() >= 1
            && view.windowId() <= 100 && view.size() == 46; }
    private static boolean personal(RemoteInventoryView view) { return view != null
            && view.windowId() == 0 && view.size() == 45; }
    private static boolean emptyOwned(RemoteInventoryView view) { for (int slot = 0; slot < 10; slot++)
        if (!view.slot(slot).empty()) return false; return true; }
    private static boolean item(RemoteInventoryView view, int slot, RemoteItemStack expected) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(expected); }
    private static boolean tail(RemoteInventoryView combined, RemoteInventoryView personal) {
        for (int slot = 9; slot <= 44; slot++) if (!combined.slot(slot + 1).equals(new RemoteInventorySlot(
                slot + 1, personal.slot(slot).empty() ? null : personal.slot(slot).item()))) return false; return true; }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteWorkbenchPreparation)) return false;
        RemoteWorkbenchPreparation value = (RemoteWorkbenchPreparation) other; return personalSlot == value.personalSlot
                && takeAction == value.takeAction && firstAction == value.firstAction
                && secondAction == value.secondAction && thirdAction == value.thirdAction
                && ingredient.equals(value.ingredient) && intermediateResult.equals(value.intermediateResult)
                && modeledResult.equals(value.modeledResult) && before.equals(value.before)
                && cursorAfterTake.equals(value.cursorAfterTake) && cursorAfterFirst.equals(value.cursorAfterFirst)
                && cursorAfterSecond.equals(value.cursorAfterSecond) && oneWide.equals(value.oneWide)
                && twoWide.equals(value.twoWide) && prepared.equals(value.prepared)
                && personalBefore.equals(value.personalBefore) && personalAfter.equals(value.personalAfter); }
    @Override public int hashCode() { return Objects.hash(personalSlot, takeAction, firstAction, secondAction,
            thirdAction, ingredient, intermediateResult, modeledResult, cursorAfterTake, cursorAfterFirst,
            cursorAfterSecond, before, oneWide, twoWide, prepared,
            personalBefore, personalAfter); }
}
