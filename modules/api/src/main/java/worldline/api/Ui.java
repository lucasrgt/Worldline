package worldline.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Flutter-inspired declaration language. Layout widgets flatten; leaves become
 * {@link GameUiSpec} nodes. No constraint solver and no runtime rebuild.
 */
public final class Ui {
    private static final Widget[] NONE = new Widget[0];

    private Ui() {}

    public static GameUiSpec screen(String name, Widget... children) {
        if (children == null) throw new NullPointerException("children");
        List<GameUiSpec.Part> parts = new ArrayList<GameUiSpec.Part>();
        return GameUiSpec.fromBuilder(name, parts, flatten(children, parts));
    }

    public static Widget row(String name, Widget... children) { return group("row", name, children); }

    public static Widget column(String name, Widget... children) { return group("column", name, children); }

    public static Widget slot(String name) { return new Widget("slot", name, name, NONE); }

    public static Widget progress(String name) { return new Widget("progress_arrow", name, null, NONE); }

    public static Widget energy(String name) { return new Widget("energy_bar", name, null, NONE); }

    public static Widget tank(String name) {
        return new Widget(name != null && name.contains("gas") ? "gas_tank" : "fluid_tank", name, null, NONE);
    }

    public static Widget search(String name) { return new Widget("search_box", name, null, NONE); }

    public static Widget playerInventory() { return new Widget("player", "player", null, NONE); }

    private static Widget group(String kind, String name, Widget... children) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("layout name");
        if (children == null) throw new NullPointerException("children");
        return new Widget(kind, name, null, children);
    }

    private static boolean flatten(Widget[] widgets, List<GameUiSpec.Part> parts) {
        boolean player = false;
        for (Widget widget : widgets) {
            if (widget == null) throw new NullPointerException("widget");
            if ("row".equals(widget.kind) || "column".equals(widget.kind))
                player |= flatten(widget.children, parts);
            else if ("player".equals(widget.kind)) player = true;
            else parts.add(new GameUiSpec.Part(widget.kind, widget.name, widget.slotType));
        }
        return player;
    }

    /** Immutable declaration node. Layout kinds do not appear in the spec. */
    public static final class Widget {
        private final String kind, name, slotType;
        private final Widget[] children;

        private Widget(String kind, String name, String slotType, Widget[] children) {
            if (kind == null || kind.isEmpty() || name == null || name.isEmpty())
                throw new IllegalArgumentException("widget identity");
            this.kind = kind;
            this.name = name;
            this.slotType = slotType;
            this.children = children;
        }
    }
}
