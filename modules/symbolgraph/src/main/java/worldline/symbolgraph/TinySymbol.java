package worldline.symbolgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TinySymbol {
    private final SymbolKey key;
    private final List<String> names;

    TinySymbol(SymbolKey key, List<String> names) {
        this.key = key;
        this.names = Collections.unmodifiableList(new ArrayList<String>(names));
    }

    public SymbolKey key() { return key; }
    public List<String> names() { return names; }
    public String name(int namespaceIndex) { return names.get(namespaceIndex); }
}
