package worldline.api;

/** Immutable selector state kept separate from query actions and assertions. */
final class GameUiSelector {
    private final String role, name, id, label, text;
    private final Integer slot;

    private GameUiSelector(String role, String name, String id, String label, String text, Integer slot) {
        this.role = token(role, "role"); this.name = token(name, "name");
        this.id = token(id, "id"); this.label = token(label, "label");
        this.text = token(text, "text"); this.slot = slot;
    }

    static GameUiSelector all() { return new GameUiSelector(null, null, null, null, null, null); }

    GameUiSelector role(String value) { return copy(required(value, "role"), name, id, label, text, slot); }
    GameUiSelector name(String value) { return copy(role, required(value, "name"), id, label, text, slot); }
    GameUiSelector id(String value) { return copy(role, name, required(value, "id"), label, text, slot); }
    GameUiSelector label(String value) { return copy(role, name, id, required(value, "label"), text, slot); }
    GameUiSelector text(String value) { return copy(role, name, id, label, required(value, "text"), slot); }
    GameUiSelector slot(int value) {
        if (value < 0) throw new IllegalArgumentException("slot index must not be negative");
        return copy(GameUiNode.SLOT, name, id, label, text, Integer.valueOf(value));
    }

    boolean matches(GameUiNode node) {
        return (role == null || role.equals(node.role()))
                && (name == null || name.equals(node.name()))
                && (id == null || id.equals(node.id()))
                && (label == null || label.equals(node.label()))
                && (text == null || text.equals(node.text()))
                && (slot == null || slot.intValue() == node.index());
    }

    String description() {
        StringBuilder value = new StringBuilder("[");
        append(value, "role", role); append(value, "name", name); append(value, "id", id);
        append(value, "label", label); append(value, "text", text);
        if (slot != null) append(value, "slot", slot.toString());
        return value.append(']').toString();
    }

    private static GameUiSelector copy(String role, String name, String id, String label,
            String text, Integer slot) {
        return new GameUiSelector(role, name, id, label, text, slot);
    }
    private static void append(StringBuilder target, String key, String value) {
        if (value == null) return;
        if (target.length() > 1) target.append(',');
        target.append(key).append('=').append(value);
    }
    private static String required(String value, String label) {
        String clean = token(value, label);
        if (clean == null) throw new IllegalArgumentException(label + " must not be blank");
        return clean;
    }
    private static String token(String value, String label) {
        if (value == null) return null;
        String clean = value.trim();
        if (clean.isEmpty()) throw new IllegalArgumentException(label + " must not be blank");
        return clean;
    }
}
