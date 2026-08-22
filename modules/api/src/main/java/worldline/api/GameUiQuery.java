package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lazy semantic locator over the current UI tree. */
public final class GameUiQuery {
    private final GameUi ui;
    private final GameUiSelector selector;

    private GameUiQuery(GameUi ui, GameUiSelector selector) {
        if (ui == null || selector == null) throw new NullPointerException("UI locator");
        this.ui = ui; this.selector = selector;
    }

    static GameUiQuery all(GameUi ui) { return new GameUiQuery(ui, GameUiSelector.all()); }

    public GameUiQuery role(String value) { return new GameUiQuery(ui, selector.role(value)); }

    public GameUiQuery name(String value) { return new GameUiQuery(ui, selector.name(value)); }

    public GameUiQuery id(String value) { return new GameUiQuery(ui, selector.id(value)); }

    public GameUiQuery label(String value) { return new GameUiQuery(ui, selector.label(value)); }

    public GameUiQuery text(String value) { return new GameUiQuery(ui, selector.text(value)); }

    public GameUiQuery slot(int index) { return new GameUiQuery(ui, selector.slot(index)); }

    public List<GameUiNode> all() {
        ui.require(GameUiCapability.SEMANTIC_TREE);
        List<GameUiNode> matches = new ArrayList<GameUiNode>();
        for (GameUiNode node : ui.nodes()) if (selector.matches(node)) matches.add(node);
        return Collections.unmodifiableList(matches);
    }

    public int count() { return all().size(); }

    public boolean exists() { return !all().isEmpty(); }

    public GameUiNode first() {
        List<GameUiNode> matches = all();
        if (matches.isEmpty()) throw failure("matched no nodes");
        return matches.get(0);
    }

    public GameUiNode at(int index) {
        List<GameUiNode> matches = all();
        if (index < 0 || index >= matches.size()) throw failure("has no result at index " + index);
        return matches.get(index);
    }

    public GameUiNode single() {
        List<GameUiNode> matches = all();
        if (matches.size() != 1) throw failure("expected one node but matched " + matches.size());
        return matches.get(0);
    }

    public GameUiQuery shouldExist() {
        if (!exists()) throw failure("expected at least one node");
        return this;
    }

    public GameUiQuery shouldNotExist() {
        if (exists()) throw failure("expected no matching nodes but found " + count());
        return this;
    }

    public GameUiQuery shouldHaveCount(int expected) {
        if (expected < 0) throw new IllegalArgumentException("expected count must not be negative");
        int actual = count();
        if (actual != expected) throw failure("expected count " + expected + " but was " + actual);
        return this;
    }

    public GameUiQuery click() {
        ui.require(GameUiCapability.NODE_CLICK);
        ui.click(actionable("click"));
        return this;
    }

    public GameUiQuery shouldBeVisible() {
        if (!single().visible()) throw failure("expected a visible node");
        return this;
    }

    public GameUiQuery shouldBeEnabled() {
        if (!single().enabled()) throw failure("expected an enabled node");
        return this;
    }

    public GameUiQuery shouldBeDisabled() {
        if (single().enabled()) throw failure("expected a disabled node");
        return this;
    }

    public GameUiQuery shouldBeFocused() {
        ui.require(GameUiCapability.FOCUS);
        if (!single().focused()) throw failure("expected a focused node");
        return this;
    }

    public GameUiQuery shouldHaveTabIndex(int expected) {
        if (expected < 0) throw new IllegalArgumentException("expected tab index must not be negative");
        int actual = single().tabIndex();
        if (actual != expected) throw failure("expected tab index " + expected + " but was " + actual);
        return this;
    }

    public GameUiQuery shouldHaveLabel(String expected) {
        if (expected == null) throw new NullPointerException("expected label");
        String actual = single().label();
        if (!expected.equals(actual)) throw failure("expected label " + expected + " but was " + actual);
        return this;
    }

    public GameUiQuery shouldHaveText(String expected) {
        if (expected == null) throw new NullPointerException("expected text");
        String actual = single().text();
        if (!expected.equals(actual)) throw failure("expected text " + expected + " but was " + actual);
        return this;
    }

    public GameUiQuery shouldHaveValue(String expected) {
        if (expected == null) throw new NullPointerException("expected value");
        String actual = single().value();
        if (!expected.equals(actual)) throw failure("expected value " + expected + " but was " + actual);
        return this;
    }

    public GameUiQuery shouldBeChecked() {
        if (!single().checked()) throw failure("expected a checked node");
        return this;
    }

    public GameUiQuery shouldBeSelected() {
        if (!single().selected()) throw failure("expected a selected node");
        return this;
    }

    public GameUiQuery shouldBeExpanded() {
        if (!single().expanded()) throw failure("expected an expanded node");
        return this;
    }

    public GameUiQuery shouldBeReadOnly() {
        if (!single().readOnly()) throw failure("expected a read-only node");
        return this;
    }

    public GameUiQuery shouldBeEmpty() {
        if (!single().empty()) throw failure("expected an empty node");
        return this;
    }

    public GameUiQuery shouldHaveItem(int itemId, int count) {
        GameUiNode node = single();
        if (node.itemId() != itemId || node.count() != count) {
            throw failure("expected item " + itemId + "x" + count + " but was "
                    + node.itemId() + "x" + node.count());
        }
        return this;
    }

    public GameUiQuery shouldContainItem(int itemId) {
        GameUiNode node = single();
        if (node.itemId() != itemId || node.count() <= 0) {
            throw failure("expected item " + itemId + " but was " + node.itemId() + "x" + node.count());
        }
        return this;
    }

    public GameUiQuery focus() {
        input(GameUiCapability.FOCUS).focus(actionable("focus"));
        return this;
    }

    public GameUiQuery type(String value) {
        if (value == null) throw new NullPointerException("text");
        input(GameUiCapability.TEXT_INPUT).type(actionable("type"), value);
        return this;
    }

    public GameUiQuery fill(String value) {
        if (value == null) throw new NullPointerException("text");
        input(GameUiCapability.TEXT_REPLACE).fill(actionable("fill"), value);
        return this;
    }

    public GameUiQuery rightClick() {
        input(GameUiCapability.SECONDARY_CLICK).rightClick(actionable("right click"));
        return this;
    }

    public GameUiQuery setValue(int value) {
        input(GameUiCapability.VALUE_INPUT).setValue(actionable("set value"), value);
        return this;
    }

    public GameUiQuery press(GameUiKey key) {
        focus(); ui.press(key); return this;
    }

    public GameUiQuery hover() {
        input(GameUiCapability.POINTER).hover(actionable("hover"));
        return this;
    }

    public GameUiQuery dragTo(GameUiQuery target) {
        if (target == null) throw new NullPointerException("target");
        input(GameUiCapability.DRAG_DROP).drag(actionable("drag"), target.actionable("drop"), 0);
        return this;
    }

    public GameUiBounds bounds() {
        ui.require(GameUiCapability.GEOMETRY);
        if (!(ui instanceof GameUiLayout)) throw ui.capabilityContract(GameUiCapability.GEOMETRY);
        return ((GameUiLayout) ui).bounds(single());
    }

    public GameUiQuery shouldBeWithinViewport() {
        GameUiBounds actual = bounds(), viewport = ui.viewport();
        if (!viewport.contains(actual)) throw failure("is outside viewport " + viewport + ": " + actual);
        return this;
    }

    public GameUiQuery shouldNotOverlap(GameUiQuery other) {
        if (other == null) throw new NullPointerException("other");
        GameUiBounds actual = bounds(), target = other.bounds();
        if (actual.overlaps(target)) throw failure("overlaps " + other.description());
        return this;
    }

    private GameUiInput input(GameUiCapability capability) {
        ui.require(capability);
        if (!(ui instanceof GameUiInput)) throw ui.capabilityContract(capability);
        return (GameUiInput) ui;
    }

    private GameUiNode actionable(String action) {
        GameUiNode node = single();
        if (!node.visible()) throw failure("cannot " + action + " a hidden node");
        if (!node.enabled()) throw failure("cannot " + action + " a disabled node");
        return node;
    }

    private AssertionError failure(String message) {
        return new AssertionError("UI locator " + description() + " " + message);
    }

    private String description() { return selector.description(); }
}
