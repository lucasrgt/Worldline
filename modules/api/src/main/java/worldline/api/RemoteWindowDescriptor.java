package worldline.api;

import java.util.Objects;

/** Immutable server-authored identity and shape of one remote window. */
public final class RemoteWindowDescriptor {
    private final int windowId;
    private final RemoteWindowKind kind;
    private final String title;
    private final int containerSlots;

    public RemoteWindowDescriptor(int windowId, RemoteWindowKind kind, String title, int containerSlots) {
        if (windowId < 1 || windowId > 100) throw new IllegalArgumentException("invalid container window ID");
        if (kind == null || title == null || title.isEmpty() || title.length() > 64)
            throw new IllegalArgumentException("invalid remote window identity");
        if (containerSlots < 1 || containerSlots > RemoteInventoryView.MAX_SLOTS)
            throw new IllegalArgumentException("invalid container slot count");
        if (kind == RemoteWindowKind.CHEST && !(("Chest".equals(title) || "Minecart".equals(title)) && containerSlots == 27
                || "Large chest".equals(title) && containerSlots == 54))
            throw new IllegalArgumentException("invalid single-chest descriptor");
        if (kind == RemoteWindowKind.FURNACE && (!"Furnace".equals(title) || containerSlots != 3))
            throw new IllegalArgumentException("invalid furnace descriptor");
        if (kind == RemoteWindowKind.WORKBENCH && (!"Crafting".equals(title) || containerSlots != 9))
            throw new IllegalArgumentException("invalid workbench descriptor");
        if (kind == RemoteWindowKind.DISPENSER && (!"Trap".equals(title) || containerSlots != 9))
            throw new IllegalArgumentException("invalid dispenser descriptor");
        this.windowId = windowId; this.kind = kind; this.title = title; this.containerSlots = containerSlots;
    }

    public int windowId() { return windowId; }
    public RemoteWindowKind kind() { return kind; }
    public String title() { return title; }
    public int containerSlots() { return containerSlots; }
    public int playerTailOffset() { return kind == RemoteWindowKind.WORKBENCH ? 10 : containerSlots; }
    public int totalSlots() { return playerTailOffset() + 36; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteWindowDescriptor)) return false;
        RemoteWindowDescriptor value = (RemoteWindowDescriptor) other;
        return windowId == value.windowId && containerSlots == value.containerSlots
                && kind == value.kind && title.equals(value.title);
    }
    @Override public int hashCode() { return Objects.hash(windowId, kind, title, containerSlots); }
}
