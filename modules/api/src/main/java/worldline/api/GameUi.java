package worldline.api;

import java.util.List;

/** Neutral semantic tree for the current game screen. */
public interface GameUi {
    String screen();

    List<GameUiNode> nodes();

    GameUiNode node(String role, String name);

    GameUiNode slot(int index);

    void openInventory();

    void close();

    void click(GameUiNode node);
}
