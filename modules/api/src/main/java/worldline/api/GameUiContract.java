package worldline.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fail-closed structural and capability validation for external UI adapters. */
public final class GameUiContract {
    private GameUiContract() {}

    public static void validate(GameUi ui) {
        if (ui == null) throw new NullPointerException("ui");
        Set<GameUiCapability> capabilities = ui.capabilities();
        require(capabilities != null && capabilities.contains(GameUiCapability.SEMANTIC_TREE),
                "E2310 semantic tree capability is required");
        requireInput(capabilities, ui, GameUiCapability.KEYBOARD);
        requireInput(capabilities, ui, GameUiCapability.POINTER);
        requireInput(capabilities, ui, GameUiCapability.FOCUS);
        requireInput(capabilities, ui, GameUiCapability.DRAG_DROP);
        require(!capabilities.contains(GameUiCapability.GEOMETRY) || ui instanceof GameUiLayout,
                "E2311 geometry capability has no GameUiLayout");
        require(!capabilities.contains(GameUiCapability.SCREENSHOT) || ui instanceof GameUiVisual,
                "E2312 screenshot capability has no GameUiVisual");
        validateTree(ui);
        if (capabilities.contains(GameUiCapability.GEOMETRY)) validateLayout((GameUiLayout) ui);
        if (capabilities.contains(GameUiCapability.SCREENSHOT)) ui.screenshot();
    }

    private static void validateTree(GameUi ui) {
        String screen = ui.screen(); List<GameUiNode> nodes = ui.nodes();
        require(screen != null && nodes != null, "E2313 UI tree returned null");
        if (screen.isEmpty()) {
            require(nodes.isEmpty(), "E2314 closed screen exposed nodes"); return;
        }
        Set<String> identities = new HashSet<String>(); int roots = 0;
        for (GameUiNode node : nodes) {
            require(node != null, "E2315 UI tree contains null");
            require(identities.add(node.role() + "\u0000" + node.name()),
                    "E2316 duplicate UI identity " + node.role() + "/" + node.name());
            if (GameUiNode.SCREEN.equals(node.role()) && screen.equals(node.name())) roots++;
        }
        require(roots == 1, "E2317 current screen needs exactly one matching root");
    }

    private static void validateLayout(GameUiLayout ui) {
        GameUiBounds viewport = ui.viewport();
        require(viewport != null && !viewport.empty(), "E2318 viewport is empty");
        for (GameUiNode node : ui.nodes()) {
            GameUiBounds bounds = ui.bounds(node);
            require(bounds != null, "E2319 node has no bounds " + node.role() + "/" + node.name());
            if (node.visible()) require(viewport.contains(bounds),
                    "E2320 visible node is clipped " + node.role() + "/" + node.name());
        }
    }

    private static void requireInput(Set<GameUiCapability> capabilities, GameUi ui,
            GameUiCapability capability) {
        require(!capabilities.contains(capability) || ui instanceof GameUiInput,
                "E2321 input capability has no GameUiInput: " + capability);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
