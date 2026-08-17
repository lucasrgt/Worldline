package worldline.api;

/** Immutable semantic UI node. Item ID -1 means empty or not applicable. */
public final class GameUiNode {
    public static final String SCREEN = "screen", SLOT = "slot", INVENTORY = "inventory";
    public static final String PROGRESS = "progress", ENERGY = "energy", TANK = "tank";
    public static final String SEARCH = "search", SCROLL = "scroll";
    private final String role, name;
    private final int index, itemId, count;

    public GameUiNode(String role, String name, int index, int itemId, int count) {
        if (role == null || name == null) throw new NullPointerException("ui node");
        if (role.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("ui node identity");
        if (itemId < -1 || count < 0) throw new IllegalArgumentException("ui node item");
        this.role = role;
        this.name = name;
        this.index = index;
        this.itemId = itemId;
        this.count = count;
    }

    public String role() { return role; }

    public String name() { return name; }

    public int index() { return index; }

    public int itemId() { return itemId; }

    public int count() { return count; }

    public boolean empty() { return itemId < 0; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GameUiNode)) return false;
        GameUiNode value = (GameUiNode) other;
        return role.equals(value.role) && name.equals(value.name) && index == value.index
                && itemId == value.itemId && count == value.count;
    }

    @Override public int hashCode() {
        return 31 * (31 * (31 * (31 * role.hashCode() + name.hashCode()) + index) + itemId) + count;
    }

    @Override public String toString() {
        return "GameUiNode[" + role + "/" + name + "#" + index + ":" + itemId + "x" + count + "]";
    }
}
