package worldline.api;

import java.util.List;
import java.util.Collections;
import java.util.Set;

/** Neutral semantic tree for the current game screen. */
public interface GameUi {
    default Set<GameUiCapability> capabilities() {
        return Collections.singleton(GameUiCapability.SEMANTIC_TREE);
    }

    default boolean supports(GameUiCapability capability) {
        if (capability == null) throw new NullPointerException("capability");
        return capabilities().contains(capability);
    }

    default void require(GameUiCapability capability) {
        if (!supports(capability)) {
            throw new IllegalStateException("E2302 UI capability unavailable: " + capability);
        }
    }

    default GameUiQuery query() { return GameUiQuery.all(this); }

    default GameUiQuery getByRole(String role) { return query().role(role); }

    default GameUiQuery getByName(String name) { return query().name(name); }

    default GameUiQuery getByLabel(String label) { return query().label(label); }

    default GameUiQuery getByText(String text) { return query().text(text); }

    default GameUiQuery getSlot(int index) { return query().slot(index); }

    default void press(GameUiKey key) {
        require(GameUiCapability.KEYBOARD);
        if (!(this instanceof GameUiInput)) throw capabilityContract(GameUiCapability.KEYBOARD);
        ((GameUiInput) this).press(key);
    }

    default GameUiBounds viewport() {
        require(GameUiCapability.GEOMETRY);
        if (!(this instanceof GameUiLayout)) throw capabilityContract(GameUiCapability.GEOMETRY);
        return ((GameUiLayout) this).viewport();
    }

    default IllegalStateException capabilityContract(GameUiCapability capability) {
        return new IllegalStateException("E2303 UI adapter declares " + capability
                + " without its capability interface");
    }

    String screen();

    List<GameUiNode> nodes();

    GameUiNode node(String role, String name);

    GameUiNode slot(int index);

    void openInventory();

    void close();

    void click(GameUiNode node);
}
