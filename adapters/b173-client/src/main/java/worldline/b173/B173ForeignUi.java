package worldline.b173;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import worldline.api.GameUi;
import worldline.api.GameUiCapability;
import worldline.api.GameUiInput;
import worldline.api.GameUiKey;
import worldline.api.GameUiNode;

/** Reflective GameUi over Butter HostUi. Worldline product modules do not import Butter. */
final class B173ForeignUi implements GameUiInput {
    private static final Class<?> HOST = load();
    private final Object host;

    private B173ForeignUi(Object host) { this.host = host; }

    private static Class<?> load() {
        try { return Class.forName("butter.testing.HostUi"); }
        catch (ClassNotFoundException ignored) { return null; }
    }

    static GameUi bind(Object screen) {
        if (screen instanceof GameUi) return (GameUi) screen;
        if (screen == null || HOST == null || !HOST.isInstance(screen)) return null;
        return new B173ForeignUi(screen);
    }

    @Override public String screen() { return (String) invoke("screen"); }

    @Override public Set<GameUiCapability> capabilities() {
        EnumSet<GameUiCapability> value = EnumSet.of(
                GameUiCapability.SEMANTIC_TREE, GameUiCapability.NODE_CLICK);
        if (has("type", Character.TYPE) && has("backspace")) value.add(GameUiCapability.TEXT_INPUT);
        if (has("rightClick", String.class)) value.add(GameUiCapability.SECONDARY_CLICK);
        if (has("setValue", String.class, Integer.TYPE)) value.add(GameUiCapability.VALUE_INPUT);
        if (hasFocusedNode()) value.add(GameUiCapability.FOCUS);
        return Collections.unmodifiableSet(value);
    }

    @Override public List<GameUiNode> nodes() {
        List<?> raw = (List<?>) invoke("nodes");
        List<GameUiNode> nodes = new ArrayList<GameUiNode>();
        for (int index = 0; index < raw.size(); index++) nodes.add(node(raw.get(index)));
        return Collections.unmodifiableList(nodes);
    }

    @Override public GameUiNode node(String role, String name) {
        for (GameUiNode node : nodes()) if (node.role().equals(role) && node.name().equals(name)) return node;
        throw new IllegalStateException("no UI node " + role + "/" + name);
    }

    @Override public GameUiNode slot(int index) {
        for (GameUiNode node : nodes()) if (GameUiNode.SLOT.equals(node.role())
                && node.index() == index) return node;
        throw new IllegalArgumentException("slot index out of range: " + index);
    }

    @Override public void openInventory() { throw new IllegalStateException(
            "Butter screens do not open vanilla inventory"); }

    @Override public void close() { throw new IllegalStateException(
            "close Butter screens through the client"); }

    @Override public void click(GameUiNode node) {
        if (node == null) throw new NullPointerException("node");
        invoke("click", node.name());
    }

    @Override public void focus(GameUiNode node) {
        requireForeign(GameUiCapability.FOCUS); invoke("click", node.name());
        if (!node.name().equals(focused().name())) throw new IllegalStateException("Butter node did not focus");
    }

    @Override public GameUiNode focused() {
        requireForeign(GameUiCapability.FOCUS);
        for (GameUiNode node : nodes()) if (node.focused()) return node;
        throw new IllegalStateException("Butter UI has no focused node");
    }

    @Override public void type(GameUiNode node, String text) {
        requireForeign(GameUiCapability.TEXT_INPUT); focus(node);
        for (int index = 0; index < text.length(); index++) invokeChar("type", text.charAt(index));
    }

    @Override public void fill(GameUiNode node, String text) { throw unavailable(GameUiCapability.TEXT_REPLACE); }

    @Override public void press(GameUiKey key) {
        if (key != GameUiKey.BACKSPACE) throw unavailable(GameUiCapability.KEYBOARD);
        requireForeign(GameUiCapability.TEXT_INPUT);
        invoke("backspace");
    }

    @Override public void hover(GameUiNode node) { throw unavailable(GameUiCapability.POINTER); }
    @Override public void click(int x, int y, int button) { throw unavailable(GameUiCapability.POINTER); }
    @Override public void drag(GameUiNode source, GameUiNode target, int button) {
        throw unavailable(GameUiCapability.DRAG_DROP); }
    @Override public void rightClick(GameUiNode node) {
        requireForeign(GameUiCapability.SECONDARY_CLICK); invoke("rightClick", node.name());
    }
    @Override public void setValue(GameUiNode node, int value) {
        requireForeign(GameUiCapability.VALUE_INPUT); invokeInt("setValue", node.name(), value);
    }

    private GameUiNode node(Object value) {
        try {
            Class<?> type = value.getClass();
            Map<String, String> attributes = new LinkedHashMap<String, String>();
            optional(type, value, "id", attributes); optional(type, value, "label", attributes);
            optional(type, value, "enabled", attributes);
            optional(type, value, "focused", attributes);
            return new GameUiNode(
                    (String) type.getMethod("role").invoke(value),
                    (String) type.getMethod("name").invoke(value),
                    ((Integer) type.getMethod("index").invoke(value)).intValue(),
                    ((Integer) type.getMethod("itemId").invoke(value)).intValue(),
                    ((Integer) type.getMethod("count").invoke(value)).intValue(), attributes);
        } catch (Exception error) {
            throw new IllegalStateException("Butter HostUiNode getters failed", error);
        }
    }

    private Object invoke(String name) {
        try { return host.getClass().getMethod(name).invoke(host); }
        catch (Exception error) { throw new IllegalStateException("Butter HostUi." + name + " failed", error); }
    }

    private Object invoke(String name, String argument) {
        try { Method method = host.getClass().getMethod(name, String.class); return method.invoke(host, argument); }
        catch (Exception error) { throw new IllegalStateException("Butter HostUi." + name + " failed", error); }
    }

    private void invokeChar(String name, char argument) {
        try { host.getClass().getMethod(name, Character.TYPE).invoke(host, Character.valueOf(argument)); }
        catch (Exception error) { throw new IllegalStateException("Butter HostUi." + name + " failed", error); }
    }

    private void invokeInt(String name, String node, int value) {
        try { host.getClass().getMethod(name, String.class, Integer.TYPE)
                .invoke(host, node, Integer.valueOf(value)); }
        catch (Exception error) { throw new IllegalStateException("Butter HostUi." + name + " failed", error); }
    }

    private boolean has(String name, Class<?>... parameters) {
        try { host.getClass().getMethod(name, parameters); return true; }
        catch (NoSuchMethodException ignored) { return false; }
    }

    private boolean hasFocusedNode() {
        List<?> raw = (List<?>) invoke("nodes");
        return !raw.isEmpty() && has(raw.get(0).getClass(), "focused");
    }

    private static boolean has(Class<?> type, String name) {
        try { type.getMethod(name); return true; }
        catch (NoSuchMethodException ignored) { return false; }
    }

    private static void optional(Class<?> type, Object value, String name, Map<String, String> target)
            throws ReflectiveOperationException {
        if (!has(type, name)) return;
        String result = String.valueOf(type.getMethod(name).invoke(value));
        if (!"label".equals(name) || !result.isEmpty()) target.put(name, result);
    }

    private void requireForeign(GameUiCapability capability) {
        if (!capabilities().contains(capability)) throw unavailable(capability);
    }

    private static IllegalStateException unavailable(GameUiCapability capability) {
        return new IllegalStateException("E2302 UI capability unavailable: " + capability);
    }
}
