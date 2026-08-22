package worldline.symbolgraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Strict reader for the class, field, and method portion of Tiny v2. */
public final class TinyV2Reader {
    public TinyMapping read(Reader input) throws IOException {
        if (input == null) throw new NullPointerException("input");
        BufferedReader reader = new BufferedReader(input);
        String header = reader.readLine();
        if (header == null) throw new IllegalArgumentException("empty Tiny document");
        String[] columns = header.split("\t", -1);
        if (columns.length < 5 || !"tiny".equals(columns[0]) || !"2".equals(columns[1])) {
            throw new IllegalArgumentException("expected Tiny v2 header");
        }
        List<String> namespaces = Arrays.asList(Arrays.copyOfRange(columns, 3, columns.length));
        uniqueNonempty(namespaces);
        List<TinySymbol> symbols = new ArrayList<TinySymbol>();
        String owner = null;
        String line;
        int number = 1;
        while ((line = reader.readLine()) != null) {
            number++;
            if (line.isEmpty()) continue;
            String[] parts = line.split("\t", -1);
            if (!line.startsWith("\t")) {
                if (!"c".equals(parts[0])) continue;
                requireColumns(parts, namespaces.size() + 1, number);
                List<String> names = names(parts, 1, namespaces.size());
                owner = names.get(0);
                symbols.add(new TinySymbol(
                        new SymbolKey(SymbolKind.CLASS, "", owner, ""), names));
            } else if (!line.startsWith("\t\t")
                    && parts.length > 1 && ("f".equals(parts[1]) || "m".equals(parts[1]))) {
                if (owner == null) throw new IllegalArgumentException("member before class at line " + number);
                requireColumns(parts, namespaces.size() + 3, number);
                SymbolKind kind = SymbolKind.fromTiny(parts[1]);
                List<String> names = names(parts, 3, namespaces.size());
                symbols.add(new TinySymbol(
                        new SymbolKey(kind, owner, names.get(0), parts[2]), names));
            }
        }
        return new TinyMapping(namespaces, symbols);
    }

    private static List<String> names(String[] parts, int start, int count) {
        List<String> names = new ArrayList<String>();
        for (int index = 0; index < count; index++) names.add(parts[start + index]);
        if (names.get(0).isEmpty()) throw new IllegalArgumentException("empty first-namespace name");
        return names;
    }

    private static void uniqueNonempty(List<String> values) {
        List<String> seen = new ArrayList<String>();
        for (String value : values) {
            if (value.isEmpty() || seen.contains(value)) throw new IllegalArgumentException("invalid namespaces");
            seen.add(value);
        }
    }

    private static void requireColumns(String[] parts, int expected, int line) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("wrong column count at line " + line);
        }
    }
}
