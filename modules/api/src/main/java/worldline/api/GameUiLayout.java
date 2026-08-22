package worldline.api;

/** Optional geometry boundary for semantic UI nodes. */
public interface GameUiLayout extends GameUi {
    GameUiBounds viewport();

    GameUiBounds bounds(GameUiNode node);
}
