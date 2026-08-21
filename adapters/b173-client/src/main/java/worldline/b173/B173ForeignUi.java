package worldline.b173;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.GameUi;
import worldline.api.GameUiNode;

/** Reflective GameUi over Butter HostUi. Worldline product modules do not import Butter. */
final class B173ForeignUi implements GameUi {
    private static final Class<?> HOST = load();
    private final Object host;

    private B173ForeignUi(Object host) { this.host = host; }

    private static Class<?> load() {
        try {
            return Class.forName("butter.testing.HostUi");
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    static GameUi bind(Object screen) {
        if (screen == null || HOST == null || !HOST.isInstance(screen)) return null;
        return new B173ForeignUi(screen);
    }

    @Override public String screen() { return (String) invoke("screen"); }

    @Override public List<GameUiNode> nodes() {
        List<?> raw = (List<?>) invoke("nodes");
        List<GameUiNode> nodes = new ArrayList<GameUiNode>();
        for (int index = 0; index < raw.size(); index++) nodes.add(node(raw.get(index)));
        return Collections.unmodifiableList(nodes);
    }

    @Override public GameUiNode node(String role, String name) {
        for (GameUiNode node : nodes()) {
            if (node.role().equals(role) && node.name().equals(name)) return node;
        }
        throw new IllegalStateException("no UI node " + role + "/" + name);
    }

    @Override public GameUiNode slot(int index) {
        for (GameUiNode node : nodes()) {
            if (GameUiNode.SLOT.equals(node.role()) && node.index() == index) return node;
        }
        throw new IllegalArgumentException("slot index out of range: " + index);
    }

    @Override public void openInventory() {
        throw new IllegalStateException("Butter screens do not open vanilla inventory");
    }

    @Override public void close() {
        throw new IllegalStateException("close Butter screens through the client");
    }

    @Override public void click(GameUiNode node) {
        if (node == null) throw new NullPointerException("node");
        invoke("click", node.name());
    }

    private GameUiNode node(Object value) {
        try {
            Class<?> type = value.getClass();
            return new GameUiNode(
                    (String) type.getMethod("role").invoke(value),
                    (String) type.getMethod("name").invoke(value),
                    ((Integer) type.getMethod("index").invoke(value)).intValue(),
                    ((Integer) type.getMethod("itemId").invoke(value)).intValue(),
                    ((Integer) type.getMethod("count").invoke(value)).intValue());
        } catch (Exception error) {
            throw new IllegalStateException("Butter HostUiNode getters failed", error);
        }
    }

    private Object invoke(String name) {
        try {
            return host.getClass().getMethod(name).invoke(host);
        } catch (Exception error) {
            throw new IllegalStateException("Butter HostUi." + name + " failed", error);
        }
    }

    private Object invoke(String name, String argument) {
        try {
            Method method = host.getClass().getMethod(name, String.class);
            return method.invoke(host, argument);
        } catch (Exception error) {
            throw new IllegalStateException("Butter HostUi." + name + " failed", error);
        }
    }
}
