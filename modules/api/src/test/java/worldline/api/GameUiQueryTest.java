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
        input.put("id", "machine.input"); input.put("label", "Input");
        input.put("text", "Iron"); input.put("value", "ore");
        input.put("tabIndex", "0"); input.put("selected", "true");
        Map<String, String> output = new LinkedHashMap<String, String>(); output.put("tabIndex", "1");
        MutableUi ui = new MutableUi(Arrays.asList(
                new GameUiNode(GameUiNode.SCREEN, "crusher", -1, -1, 0),
                new GameUiNode(GameUiNode.SLOT, "input", 0, 265, 4, input),
                new GameUiNode(GameUiNode.SLOT, "output", 1, -1, 0, output)));
        require(ui.getByRole(GameUiNode.SLOT).shouldHaveCount(2).count() == 2, "role locator");
        require(ui.getByRole(GameUiNode.SLOT, "input").single().itemId() == 265, "role/name locator");
        require(ui.getById("machine.input").single().itemId() == 265, "explicit id locator");
        require(ui.getById("output").single().empty(), "legacy id fallback");
        require(ui.getByLabel("Input").single().count() == 4, "label locator");
        require("Iron".equals(ui.getByText("Iron").single().text()), "text locator");
        require(ui.getSlot(1).single().empty(), "slot locator");
        ui.getByLabel("Input").click();
        require(ui.clicks == 1, "locator click");
        ui.getByLabel("Input").focus().type("ore").fill("iron").press(GameUiKey.ENTER).hover()
                .shouldBeVisible().shouldBeEnabled().shouldHaveItem(265, 4)
                .shouldBeWithinViewport().shouldNotOverlap(ui.getSlot(1));
        ui.getSlot(0).dragTo(ui.getSlot(1));
        ui.getSlot(0).rightClick().setValue(7);
        require(ui.focused.index() == 0 && "ore".equals(ui.typed) && "iron".equals(ui.filled)
                && ui.pressed == GameUiKey.ENTER && ui.hovered.index() == 0
                && ui.dragged.index() == 1 && ui.secondary.index() == 0 && ui.assigned == 7,
                "semantic input actions");
        require(ui.getSlot(0).bounds().equals(new GameUiBounds(10, 20, 16, 16)), "node bounds");
        require(ui.getSlot(1).shouldBeEmpty().single().empty(), "empty assertion");
        ui.getByLabel("Input").shouldHaveLabel("Input").shouldHaveText("Iron")
                .shouldHaveValue("ore").shouldBeSelected().shouldContainItem(265);
        ui.getByLabel("Input").shouldHaveTabIndex(0);
        ui.getByName("missing").shouldNotExist();
        GameUiContract.validate(ui);
        failure(() -> ui.getByRole(GameUiNode.SLOT).single(), "expected one node");
        failure(() -> ui.getByName("missing").first(), "matched no nodes");
        GameUi readOnly = new MutableUi(ui.nodes()) {
            @Override public Set<GameUiCapability> capabilities() {
                return Collections.singleton(GameUiCapability.SEMANTIC_TREE);
            }
        };
        failure(() -> readOnly.getSlot(0).click(), "E2302");
        Map<String, String> disabled = new LinkedHashMap<String, String>();
        disabled.put("enabled", "false");
        MutableUi disabledUi = new MutableUi(Arrays.asList(
                new GameUiNode(GameUiNode.SCREEN, "crusher", -1, -1, 0),
                new GameUiNode(GameUiNode.BUTTON, "start", -1, -1, 0, disabled)));
        disabledUi.getByRole(GameUiNode.BUTTON).shouldBeDisabled();
        failure(() -> disabledUi.getByRole(GameUiNode.BUTTON).click(), "disabled node");
        GameUi invalid = new MutableUi(ui.nodes()) {
            @Override public String screen() { return "missing-root"; }
        };
        failure(() -> GameUiContract.validate(invalid), "E2317");
        Map<String, String> duplicateTab = new LinkedHashMap<String, String>();
        duplicateTab.put("tabIndex", "0");
        GameUi invalidTabOrder = new MutableUi(Arrays.asList(
                new GameUiNode(GameUiNode.SCREEN, "crusher", -1, -1, 0),
                new GameUiNode(GameUiNode.BUTTON, "one", -1, -1, 0, duplicateTab),
                new GameUiNode(GameUiNode.BUTTON, "two", -1, -1, 0, duplicateTab)));
        failure(() -> GameUiContract.validate(invalidTabOrder), "E2322");
        Map<String, String> duplicateId = new LinkedHashMap<String, String>();
        duplicateId.put("id", "action");
        GameUi invalidIds = new MutableUi(Arrays.asList(
                new GameUiNode(GameUiNode.SCREEN, "crusher", -1, -1, 0),
                new GameUiNode(GameUiNode.BUTTON, "one", -1, -1, 0, duplicateId),
                new GameUiNode(GameUiNode.BUTTON, "two", -1, -1, 0, duplicateId)));
        failure(() -> GameUiContract.validate(invalidIds), "E2324");
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
                    GameUiCapability.KEYBOARD, GameUiCapability.TEXT_INPUT,
                    GameUiCapability.TEXT_REPLACE,
                    GameUiCapability.VALUE_INPUT, GameUiCapability.SECONDARY_CLICK,
                    GameUiCapability.POINTER,
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
        @Override public void fill(GameUiNode node, String text) { focused = node; filled = text; }
        @Override public void press(GameUiKey key) { pressed = key; }
        @Override public void hover(GameUiNode node) { hovered = node; }
        @Override public void rightClick(GameUiNode node) { secondary = node; }
        @Override public void setValue(GameUiNode node, int value) { assigned = value; }
        @Override public void click(int x, int y, int button) { clicks++; }
        @Override public void drag(GameUiNode source, GameUiNode target, int button) { dragged = target; }
        @Override public GameUiBounds viewport() { return new GameUiBounds(0, 0, 320, 240); }
        @Override public GameUiBounds bounds(GameUiNode node) {
            return node.index() < 0 ? viewport() : new GameUiBounds(10 + node.index() * 20, 20, 16, 16);
        }
        GameUiNode focused, hovered, dragged, secondary;
        String typed, filled; int assigned;
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
