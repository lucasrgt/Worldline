package worldline.api;

import java.util.Objects;

/** Immutable explicit-close evidence confirmed by an accepted personal-window no-op. */
public final class RemoteWindowClosure {
    private final RemoteContainerWindow closedWindow;
    private final int proofAction, proofSlot;
    private final RemoteInventoryView personalBefore, personalAfter;

    public RemoteWindowClosure(RemoteContainerWindow closedWindow, int proofAction, int proofSlot,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter) {
        if (closedWindow == null || proofAction < 1 || proofAction > 32767
                || proofSlot < 9 || proofSlot > 44 || personalBefore == null || personalAfter == null
                || personalBefore.windowId() != 0 || personalAfter.windowId() != 0
                || personalBefore.size() != 45 || !personalBefore.equals(personalAfter)
                || !personalBefore.slot(proofSlot).empty())
            throw new IllegalArgumentException("invalid remote window closure evidence");
        this.closedWindow = closedWindow; this.proofAction = proofAction; this.proofSlot = proofSlot;
        this.personalBefore = personalBefore; this.personalAfter = personalAfter;
    }

    public RemoteContainerWindow closedWindow() { return closedWindow; }
    public int proofAction() { return proofAction; }
    public int proofSlot() { return proofSlot; }
    public RemoteInventoryView personalBefore() { return personalBefore; }
    public RemoteInventoryView personalAfter() { return personalAfter; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteWindowClosure)) return false; RemoteWindowClosure value = (RemoteWindowClosure) other;
        return proofAction == value.proofAction && proofSlot == value.proofSlot
                && closedWindow.equals(value.closedWindow) && personalBefore.equals(value.personalBefore)
                && personalAfter.equals(value.personalAfter); }
    @Override public int hashCode() {
        return Objects.hash(closedWindow, proofAction, proofSlot, personalBefore, personalAfter); }
}
