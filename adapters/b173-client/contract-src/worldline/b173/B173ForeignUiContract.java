package worldline.b173;

import java.util.Arrays;
import java.util.List;
import butter.testing.HostUi;
import butter.testing.HostUiNode;
import worldline.api.GameUi;
import worldline.api.GameUiCapability;
import worldline.api.GameUiContract;
import worldline.api.GameUiNode;

/** Host-only acceptance of the optional Butter reflection bridge. */
public final class B173ForeignUiContract {
    private B173ForeignUiContract() {}

    public static void main(String[] arguments) {
        Screen screen = new Screen(); GameUi ui = B173ForeignUi.bind(screen);
        GameUiContract.validate(ui);
        require(ui.supports(GameUiCapability.TEXT_INPUT)
                && ui.supports(GameUiCapability.FOCUS)
                && ui.supports(GameUiCapability.SECONDARY_CLICK)
                && ui.supports(GameUiCapability.VALUE_INPUT), "extended capabilities missing");
        ui.getByLabel("Search").focus().type("ore").rightClick().setValue(7);
        require("search".equals(screen.focused) && "ore".equals(screen.typed)
                && "search".equals(screen.secondary) && screen.value == 7, "extended actions drifted");
        require(ui.getByRole(GameUiNode.TEXT_FIELD).shouldBeFocused().single().enabled(),
                "node attributes drifted");
        System.out.println("B173ForeignUiContract passed");
    }

    private static final class Screen implements HostUi {
        String focused, typed = "", secondary; int value;
        @Override public String screen() { return "butter"; }
        @Override public List<HostUiNode> nodes() {
            return Arrays.asList(new HostUiNode("screen", "butter", "", -1, -1, 0, true, false),
                    new HostUiNode("textbox", "search", "Search", -1, -1, 0, true,
                            "search".equals(focused)));
        }
        @Override public void click(String name) { focused = name; }
        @Override public void type(char item) { typed += item; }
        @Override public void backspace() {
            if (!typed.isEmpty()) typed = typed.substring(0, typed.length() - 1);
        }
        @Override public void setValue(String name, int item) { focused = name; value = item; }
        @Override public void rightClick(String name) { secondary = name; }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
