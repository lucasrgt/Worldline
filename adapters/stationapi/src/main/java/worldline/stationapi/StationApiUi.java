package worldline.stationapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import worldline.api.GameUi;
import worldline.api.GameUiCapability;
import worldline.api.GameUiNode;

/** Semantic inventory lifecycle and native primary clicks over acknowledged local-world commands. */
final class StationApiUi implements GameUi {
    private final StationApiRuntime runtime;
    StationApiUi(StationApiRuntime runtime) { this.runtime = runtime; }
    @Override public Set<GameUiCapability> capabilities() {
        return Collections.unmodifiableSet(EnumSet.of(GameUiCapability.SEMANTIC_TREE,
                GameUiCapability.INVENTORY_LIFECYCLE, GameUiCapability.NODE_CLICK));
    }
    @Override public String screen() { return runtime.control("UI").split("[|]", -1)[0].replace("none", ""); }
    @Override public List<GameUiNode> nodes() {
        String[] parts = runtime.control("UI").split("[|]", -1);
        if (parts[0].equals("none") && parts.length == 1) return Collections.emptyList();
        if (!parts[0].equals("inventory") || parts.length != 46)
            throw new IllegalStateException("StationAPI native inventory topology differs");
        List<GameUiNode> result = new ArrayList<GameUiNode>();
        Map<String, String> identity = Collections.singletonMap("session", runtime.session());
        result.add(new GameUiNode(GameUiNode.SCREEN, "inventory", -1, -1, 0, identity));
        for (int index = 1; index < parts.length; index++) {
            String[] slot = parts[index].split(",", -1);
            if (slot.length != 3 || Integer.parseInt(slot[0]) != index - 1)
                throw new IllegalStateException("StationAPI slot sequence differs");
            result.add(new GameUiNode(GameUiNode.SLOT, "slot-" + (index - 1), index - 1,
                    Integer.parseInt(slot[1]), Integer.parseInt(slot[2]), identity));
        }
        return Collections.unmodifiableList(result);
    }
    @Override public GameUiNode node(String role, String name) {
        for (GameUiNode node : nodes()) if (node.role().equals(role) && node.name().equals(name)) return node;
        throw new IllegalArgumentException("StationAPI UI node is absent");
    }
    @Override public GameUiNode slot(int index) {
        if (index < 0 || index > 44) throw new IllegalArgumentException("inventory slot must be 0..44");
        return node(GameUiNode.SLOT, "slot-" + index);
    }
    @Override public void openInventory() { runtime.mutate("OPEN_INVENTORY"); }
    @Override public void close() { runtime.mutate("CLOSE_UI"); }
    @Override public void click(GameUiNode node) {
        if (node == null) throw new NullPointerException("node");
        if (!GameUiNode.SLOT.equals(node.role()) || !runtime.session().equals(node.attribute("session"))
                || !slot(node.index()).equals(node)) throw new IllegalArgumentException("stale or foreign UI node");
        runtime.mutate("CLICK " + node.index() + " " + node.itemId() + " " + node.count());
    }
}
