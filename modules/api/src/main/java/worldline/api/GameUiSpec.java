package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Declared UI tree that a builder can emit and a live {@link GameUi} can match.
 * Item contents are ignored; only role, name, and container index are structural.
 */
public final class GameUiSpec {
    private final String screen;
    private final List<GameUiNode> nodes;

    private GameUiSpec(String screen, List<GameUiNode> nodes) {
        if (screen == null || screen.isEmpty()) throw new IllegalArgumentException("ui spec screen");
        if (nodes == null || nodes.isEmpty()) throw new IllegalArgumentException("ui spec nodes");
        this.screen = screen;
        this.nodes = Collections.unmodifiableList(new ArrayList<GameUiNode>(nodes));
    }

    public static GameUiSpec of(String screen, List<GameUiNode> nodes) {
        return new GameUiSpec(screen, nodes);
    }

    /** Vanilla player inventory tree already proven by the GUI cycle. */
    public static GameUiSpec inventory() {
        return vanillaContainer(GameUiNode.INVENTORY, 45);
    }

    /** Vanilla workbench result, 3x3 matrix, and player inventory tree. */
    public static GameUiSpec workbench() {
        return vanillaContainer(GameUiNode.WORKBENCH, 46);
    }

    private static GameUiSpec vanillaContainer(String screen, int slots) {
        List<GameUiNode> nodes = new ArrayList<GameUiNode>();
        nodes.add(new GameUiNode(GameUiNode.SCREEN, screen, -1, -1, 0));
        for (int index = 0; index < slots; index++) {
            nodes.add(new GameUiNode(GameUiNode.SLOT, Integer.toString(index), index, -1, 0));
        }
        return new GameUiSpec(screen, nodes);
    }

    /**
     * Maps Aero Machine Maker {@code guiComponents} plus implicit player inventory.
     * Unknown types fail closed. Separators are omitted.
     */
    public static GameUiSpec fromBuilder(String screen, List<Part> parts) {
        return fromBuilder(screen, parts, true);
    }

    public static GameUiSpec fromBuilder(String screen, List<Part> parts, boolean playerInventory) {
        if (parts == null) throw new NullPointerException("parts");
        List<GameUiNode> nodes = new ArrayList<GameUiNode>();
        nodes.add(new GameUiNode(GameUiNode.SCREEN, screen, -1, -1, 0));
        Map<String, Integer> used = new LinkedHashMap<String, Integer>();
        int slotIndex = 0;
        for (Part part : parts) {
            if (part == null) throw new NullPointerException("part");
            if ("separator".equals(part.type)) continue;
            String role = roleOf(part.type);
            String name = unique(used, part.name != null && !part.name.isEmpty()
                    ? part.name : defaultName(part));
            int index = GameUiNode.SLOT.equals(role) ? slotIndex++ : -1;
            nodes.add(new GameUiNode(role, name, index, -1, 0));
        }
        if (playerInventory) for (int player = 0; player < 36; player++) {
            nodes.add(new GameUiNode(GameUiNode.SLOT, "player." + player, slotIndex++, -1, 0));
        }
        return new GameUiSpec(screen, nodes);
    }

    public String screen() { return screen; }

    public List<GameUiNode> nodes() { return nodes; }

    public GameUiNode node(String role, String name) {
        for (GameUiNode node : nodes) {
            if (node.role().equals(role) && node.name().equals(name)) return node;
        }
        throw new IllegalStateException("no UI spec node " + role + "/" + name);
    }

    public boolean matchesStructure(List<GameUiNode> live) {
        if (live == null || live.size() != nodes.size()) return false;
        for (int index = 0; index < nodes.size(); index++) {
            GameUiNode expected = nodes.get(index), actual = live.get(index);
            if (!expected.role().equals(actual.role()) || !expected.name().equals(actual.name())
                    || expected.index() != actual.index()) return false;
        }
        return true;
    }

    @Override public boolean equals(Object other) {
        return other instanceof GameUiSpec && screen.equals(((GameUiSpec) other).screen)
                && nodes.equals(((GameUiSpec) other).nodes);
    }

    @Override public int hashCode() { return 31 * screen.hashCode() + nodes.hashCode(); }

    public static String roleOf(String type) {
        if ("slot".equals(type) || "big_slot".equals(type)) return GameUiNode.SLOT;
        if ("progress_arrow".equals(type) || "flame".equals(type)) return GameUiNode.PROGRESS;
        if ("energy_bar".equals(type)) return GameUiNode.ENERGY;
        if (type != null && type.contains("tank")) return GameUiNode.TANK;
        if (type != null && type.startsWith("search_box")) return GameUiNode.SEARCH;
        if (type != null && type.startsWith("scrollbar")) return GameUiNode.SCROLL;
        throw new IllegalArgumentException("unsupported builder component: " + type);
    }

    private static String defaultName(Part part) {
        if (part.slotType != null && !part.slotType.isEmpty()) return part.slotType;
        if ("progress_arrow".equals(part.type)) return "craft";
        if ("energy_bar".equals(part.type)) return "energy";
        if (part.type.contains("fluid")) return "fluid";
        if (part.type.contains("gas")) return "gas";
        if (part.type.startsWith("search_box")) return "search";
        if (part.type.startsWith("scrollbar")) return "scroll";
        return part.type;
    }

    private static String unique(Map<String, Integer> used, String name) {
        Integer count = used.get(name);
        used.put(name, count == null ? 1 : count + 1);
        return count == null ? name : name + "." + count;
    }

    /** One Aero {@code guiComponents} entry. {@code name} may be null to auto-name. */
    public static final class Part {
        public final String type, name, slotType;

        public Part(String type, String name, String slotType) {
            if (type == null || type.isEmpty()) throw new IllegalArgumentException("builder part type");
            this.type = type;
            this.name = name;
            this.slotType = slotType;
        }
    }
}
