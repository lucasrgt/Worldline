package worldline.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable semantic UI node. Item ID -1 means empty or not applicable. */
public final class GameUiNode {
    public static final String SCREEN = "screen", SLOT = "slot", INVENTORY = "inventory";
    public static final String PROGRESS = "progress", ENERGY = "energy", TANK = "tank";
    public static final String SEARCH = "search", SCROLL = "scroll";
    public static final String BUTTON = "button", TEXT_FIELD = "textbox", LABEL = "label";
    public static final String PANEL = "panel", TEXT = "text", SLIDER = "slider";
    public static final String TAB = "tab", TAB_LIST = "tablist", SCROLL_BAR = "scrollbar";
    public static final String SEPARATOR = "separator", CHECKBOX = "checkbox", RADIO = "radio";
    public static final String TOGGLE = "toggle";
    private final String role, name;
    private final int index, itemId, count;
    private final Map<String, String> attributes;

    public GameUiNode(String role, String name, int index, int itemId, int count) {
        this(role, name, index, itemId, count, Collections.<String, String>emptyMap());
    }

    public GameUiNode(String role, String name, int index, int itemId, int count,
            Map<String, String> attributes) {
        if (role == null || name == null) throw new NullPointerException("ui node");
        if (attributes == null) throw new NullPointerException("ui node attributes");
        if (role.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("ui node identity");
        if (itemId < -1 || count < 0) throw new IllegalArgumentException("ui node item");
        this.role = role;
        this.name = name;
        this.index = index;
        this.itemId = itemId;
        this.count = count;
        Map<String, String> copy = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null
                    || entry.getKey().trim().isEmpty()) throw new IllegalArgumentException("ui node attribute");
            copy.put(entry.getKey(), entry.getValue());
        }
        this.attributes = Collections.unmodifiableMap(copy);
    }

    public String role() { return role; }

    public String name() { return name; }

    public int index() { return index; }

    public int itemId() { return itemId; }

    public int count() { return count; }

    public Map<String, String> attributes() { return attributes; }

    public String attribute(String key) { return attributes.get(key); }

    public String label() { return attributes.containsKey("label") ? attributes.get("label") : name; }

    public String text() { return attributes.containsKey("text") ? attributes.get("text") : ""; }

    public String value() { return attributes.containsKey("value") ? attributes.get("value") : ""; }

    public boolean visible() { return flag("visible", true); }

    public boolean enabled() { return flag("enabled", true); }

    public boolean focused() { return flag("focused", false); }

    public boolean checked() { return flag("checked", false); }

    public boolean selected() { return flag("selected", false); }

    public boolean expanded() { return flag("expanded", false); }

    public boolean readOnly() { return flag("readOnly", false); }

    /** Zero-based keyboard focus order, or -1 when the node is not tabbable. */
    public int tabIndex() {
        String value = attributes.get("tabIndex");
        if (value == null) return -1;
        try {
            int index = Integer.parseInt(value);
            if (index < 0) throw new NumberFormatException();
            return index;
        } catch (NumberFormatException invalid) {
            throw new IllegalStateException("invalid UI node tabIndex=" + value);
        }
    }

    public boolean empty() { return itemId < 0; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GameUiNode)) return false;
        GameUiNode value = (GameUiNode) other;
        return role.equals(value.role) && name.equals(value.name) && index == value.index
                && itemId == value.itemId && count == value.count && attributes.equals(value.attributes);
    }

    @Override public int hashCode() {
        return 31 * (31 * (31 * (31 * (31 * role.hashCode() + name.hashCode()) + index)
                + itemId) + count) + attributes.hashCode();
    }

    @Override public String toString() {
        return "GameUiNode[" + role + "/" + name + "#" + index + ":" + itemId + "x" + count
                + (attributes.isEmpty() ? "" : "," + attributes) + "]";
    }

    private boolean flag(String key, boolean fallback) {
        String value = attributes.get(key);
        if (value == null) return fallback;
        if ("true".equals(value)) return true;
        if ("false".equals(value)) return false;
        throw new IllegalStateException("invalid UI node flag " + key + "=" + value);
    }
}
