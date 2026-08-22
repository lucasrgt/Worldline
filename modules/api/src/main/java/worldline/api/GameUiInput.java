package worldline.api;

/** Optional semantic keyboard and pointer boundary for a game UI. */
public interface GameUiInput extends GameUi {
    void focus(GameUiNode node);

    GameUiNode focused();

    void type(GameUiNode node, String text);

    void press(GameUiKey key);

    void hover(GameUiNode node);

    void rightClick(GameUiNode node);

    void setValue(GameUiNode node, int value);

    void click(int x, int y, int button);

    void drag(GameUiNode source, GameUiNode target, int button);
}
