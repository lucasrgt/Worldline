package butter.testing;

/** Test-only immutable node matching Butter's reflective getter surface. */
public final class HostUiNode {
    private final String role, name, label;
    private final int index, itemId, count;
    private final boolean enabled, focused;

    public HostUiNode(String role, String name, String label, int index, int itemId,
            int count, boolean enabled, boolean focused) {
        this.role = role; this.name = name; this.label = label; this.index = index;
        this.itemId = itemId; this.count = count; this.enabled = enabled; this.focused = focused;
    }

    public String role() { return role; }
    public String name() { return name; }
    public String label() { return label; }
    public int index() { return index; }
    public int itemId() { return itemId; }
    public int count() { return count; }
    public boolean enabled() { return enabled; }
    public boolean focused() { return focused; }
}
