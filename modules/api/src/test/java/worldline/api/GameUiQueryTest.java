package worldline.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GameUiQueryTest {
    private GameUiQueryTest() {}

    public static void main(String[] arguments) {
        Map<String, String> input = new LinkedHashMap<String, String>();
        input.put("label", "Input"); input.put("text", "Iron");
        MutableUi ui = new MutableUi(Arrays.asList(
                new GameUiNode(GameUiNode.SCREEN, "crusher", -1, -1, 0),
                new GameUiNode(GameUiNode.SLOT, "input", 0, 265, 4, input),
                new GameUiNode(GameUiNode.SLOT, "output", 1, -1, 0)));
        require(ui.getByRole(GameUiNode.SLOT).shouldHaveCount(2).count() == 2, "role locator");
        require(ui.getByName("input").single().itemId() == 265, "name locator");
        require(ui.getByLabel("Input").single().count() == 4, "label locator");
        require("Iron".equals(ui.getByText("Iron").single().text()), "text locator");
        require(ui.getSlot(1).single().empty(), "slot locator");
        ui.getByLabel("Input").click();
        require(ui.clicks == 1, "locator click");
        ui.getByLabel("Input").focus().type("ore").press(GameUiKey.ENTER).hover()
                .shouldBeVisible().shouldBeEnabled().shouldHaveItem(265, 4)
                .shouldBeWithinViewport().shouldNotOverlap(ui.getSlot(1));
        ui.getSlot(0).dragTo(ui.getSlot(1));
        require(ui.focused.index() == 0 && "ore".equals(ui.typed)
                && ui.pressed == GameUiKey.ENTER && ui.hovered.index() == 0
                && ui.dragged.index() == 1, "semantic input actions");
        require(ui.getSlot(0).bounds().equals(new GameUiBounds(10, 20, 16, 16)), "node bounds");
        require(ui.getSlot(1).shouldBeEmpty().single().empty(), "empty assertion");
        failure(() -> ui.getByRole(GameUiNode.SLOT).single(), "expected one node");
        failure(() -> ui.getByName("missing").first(), "matched no nodes");
        GameUi readOnly = new MutableUi(ui.nodes()) {
            @Override public Set<GameUiCapability> capabilities() {
                return Collections.singleton(GameUiCapability.SEMANTIC_TREE);
            }
        };
        failure(() -> readOnly.getSlot(0).click(), "E2302");
        input.put("label", "Changed");
        require("Input".equals(ui.getSlot(0).single().label()), "node attributes were mutable");
        System.out.println("GameUiQueryTest passed");
    }

    private static class MutableUi implements GameUiInput, GameUiLayout {
        private final List<GameUiNode> nodes;
        int clicks;
        MutableUi(List<GameUiNode> nodes) { this.nodes = Collections.unmodifiableList(nodes); }
        @Override public Set<GameUiCapability> capabilities() {
            return Collections.unmodifiableSet(EnumSet.of(
                    GameUiCapability.SEMANTIC_TREE, GameUiCapability.NODE_CLICK,
                    GameUiCapability.KEYBOARD, GameUiCapability.POINTER,
                    GameUiCapability.FOCUS, GameUiCapability.DRAG_DROP,
                    GameUiCapability.GEOMETRY));
        }
        @Override public String screen() { return "crusher"; }
        @Override public List<GameUiNode> nodes() { return nodes; }
        @Override public GameUiNode node(String role, String name) {
            return getByRole(role).name(name).single();
        }
        @Override public GameUiNode slot(int index) { return getSlot(index).single(); }
        @Override public void openInventory() { throw new UnsupportedOperationException(); }
        @Override public void close() { throw new UnsupportedOperationException(); }
        @Override public void click(GameUiNode node) { clicks++; }
        @Override public void focus(GameUiNode node) { focused = node; }
        @Override public GameUiNode focused() { return focused; }
        @Override public void type(GameUiNode node, String text) { focused = node; typed = text; }
        @Override public void press(GameUiKey key) { pressed = key; }
        @Override public void hover(GameUiNode node) { hovered = node; }
        @Override public void click(int x, int y, int button) { clicks++; }
        @Override public void drag(GameUiNode source, GameUiNode target, int button) { dragged = target; }
        @Override public GameUiBounds viewport() { return new GameUiBounds(0, 0, 320, 240); }
        @Override public GameUiBounds bounds(GameUiNode node) {
            return node.index() < 0 ? viewport() : new GameUiBounds(10 + node.index() * 20, 20, 16, 16);
        }
        GameUiNode focused, hovered, dragged;
        String typed;
        GameUiKey pressed;
    }

    private static void failure(Runnable action, String message) {
        try { action.run(); throw new AssertionError("failure was not reported: " + message); }
        catch (AssertionError | IllegalStateException expected) {
            if (expected.getMessage() == null || !expected.getMessage().contains(message)) throw expected;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
