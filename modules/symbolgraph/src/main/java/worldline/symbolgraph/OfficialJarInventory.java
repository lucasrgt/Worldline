package worldline.symbolgraph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class OfficialJarInventory {
    private final Set<OfficialSymbolKey> symbols;

    private OfficialJarInventory(Set<OfficialSymbolKey> symbols) {
        this.symbols = Collections.unmodifiableSet(new TreeSet<OfficialSymbolKey>(symbols));
    }

    public static OfficialJarInventory read(Path jarPath) throws IOException {
        Set<OfficialSymbolKey> symbols = new TreeSet<OfficialSymbolKey>();
        ClassFileReader reader = new ClassFileReader();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
                try (java.io.InputStream input = jar.getInputStream(entry)) {
                    for (OfficialSymbolKey symbol : reader.read(input)) {
                        if (!symbols.add(symbol)) {
                            throw new IllegalArgumentException("duplicate official symbol: " + symbol);
                        }
                    }
                }
            }
        }
        return new OfficialJarInventory(symbols);
    }

    Set<OfficialSymbolKey> symbols() { return symbols; }

    public Map<SymbolKind, Integer> counts() {
        Map<SymbolKind, Integer> counts = new EnumMap<SymbolKind, Integer>(SymbolKind.class);
        for (SymbolKind kind : SymbolKind.values()) counts.put(kind, Integer.valueOf(0));
        for (OfficialSymbolKey symbol : symbols) {
            SymbolKind kind = symbol.kind();
            counts.put(kind, Integer.valueOf(counts.get(kind).intValue() + 1));
        }
        return Collections.unmodifiableMap(counts);
    }
}
