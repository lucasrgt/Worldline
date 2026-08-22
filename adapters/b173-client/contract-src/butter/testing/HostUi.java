package butter.testing;

import java.util.List;

/** Test-only shape of the external Butter contract bound by method name. */
public interface HostUi {
    String screen();
    List<HostUiNode> nodes();
    void click(String name);
    void type(char value);
    void backspace();
    void setValue(String name, int value);
    void rightClick(String name);
}
