import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Inventories and emits reviewed patches for exact repeated b1.7.3 smoke helpers. */
public final class SharedFixturePatch {
    private static final Pattern METHOD = Pattern.compile(
            "(?m)^  private static (?:void awaitPlayers|int local|boolean water|String sha|"
            + "BlockPosition place)\\(");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) throws Exception {
        SharedFixturePatch patch = new SharedFixturePatch();
        if (arguments.length == 0) patch.inventory();
        else if (arguments.length == 2) patch.emit(Integer.parseInt(arguments[0]),
                Integer.parseInt(arguments[1]));
        else throw new IllegalArgumentException("usage: SharedFixturePatch [OFFSET COUNT]");
    }

    private void inventory() throws Exception {
        int files = 0; int[] counts = new int[Kind.values().length];
        for (Change change : changes()) {
            files++; for (Kind kind : change.kinds) counts[kind.ordinal()]++;
        }
        System.out.println("files=" + files);
        for (Kind kind : Kind.values())
            System.out.println(kind.name().toLowerCase() + "=" + counts[kind.ordinal()]);
    }

    private void emit(int offset, int count) throws Exception {
        List<Change> changes = changes(); int end = Math.min(changes.size(), offset + count);
        require(offset >= 0 && count > 0 && offset < end, "empty shared-fixture patch range");
        StringBuilder patch = new StringBuilder("*** Begin Patch\n");
        for (Change change : changes.subList(offset, end)) {
            patch.append("*** Update File: ").append(change.path).append('\n');
            patch.append("@@\n ").append(change.packageLine).append('\n')
                    .append("+import static worldline.b173server.B173FixtureSupport.*;\n");
            for (Method method : change.methods) {
                patch.append("@@\n");
                for (String line : method.source.split("\\R"))
                    patch.append('-').append(line).append('\n');
            }
        }
        patch.append("*** End Patch\n"); System.out.print(patch);
    }

    private List<Change> changes() throws Exception {
        List<Change> result = new ArrayList<>();
        try (var paths = Files.walk(root.resolve("smokes"))) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".java")).sorted().toList()) {
                String prior = Files.readString(path, StandardCharsets.UTF_8), current = prior;
                Matcher matcher = METHOD.matcher(prior); List<Method> methods = new ArrayList<>();
                while (matcher.find()) {
                    int open = prior.indexOf('{', matcher.start()), close = closing(prior, open);
                    String source = prior.substring(matcher.start(), close + 1);
                    Kind kind = classify(source); if (kind != null) methods.add(new Method(
                            matcher.start(), close + 1 + newline(prior, close + 1), kind, source));
                }
                if (methods.isEmpty()) continue;
                Set<Kind> kinds = EnumSet.noneOf(Kind.class);
                for (int index = methods.size() - 1; index >= 0; index--) {
                    Method method = methods.get(index); kinds.add(method.kind);
                    current = current.substring(0, method.start) + current.substring(method.end);
                }
                StringBuilder imports = new StringBuilder();
                imports.append("import static worldline.b173server.B173FixtureSupport.*;\n");
                int packageEnd = current.indexOf('\n') + 1;
                current = current.substring(0, packageEnd) + "\n" + imports + current.substring(packageEnd);
                result.add(new Change(path, prior, current, kinds,
                        prior.substring(0, prior.indexOf('\n')).stripTrailing(), methods));
            }
        }
        return result.stream().sorted(Comparator.comparing(change -> change.path.toString())).toList();
    }

    static String rewrite(String prior) {
        Matcher matcher = METHOD.matcher(prior); List<Method> methods = new ArrayList<>();
        while (matcher.find()) {
            int open = prior.indexOf('{', matcher.start()), close = closing(prior, open);
            String source = prior.substring(matcher.start(), close + 1);
            Kind kind = classify(source);
            if (kind != null) methods.add(new Method(matcher.start(),
                    close + 1 + newline(prior, close + 1), kind, source));
        }
        if (methods.isEmpty()) return prior;
        String current = prior;
        for (int index = methods.size() - 1; index >= 0; index--) {
            Method method = methods.get(index);
            current = current.substring(0, method.start) + current.substring(method.end);
        }
        int packageEnd = current.indexOf('\n') + 1;
        return current.substring(0, packageEnd)
                + "import static worldline.b173server.B173FixtureSupport.*;\n"
                + current.substring(packageEnd);
    }

    private static Kind classify(String source) {
        String value = compact(source);
        if (value.startsWith("privatestaticintlocal(")
                && value.matches(".*\\{return[a-zA-Z]+-[a-zA-Z]+\\*16;}") ) return Kind.LOCAL;
        if (value.startsWith("privatestaticbooleanwater(")
                && value.matches(".*\\{return([a-zA-Z]+==8\\|\\|[a-zA-Z]+==9|"
                + "[a-zA-Z]+==9\\|\\|[a-zA-Z]+==8);}")) return Kind.WATER;
        if (value.startsWith("privatestaticvoidawaitPlayers(")
                && value.contains("System.currentTimeMillis()+5000")
                && value.contains(".players().size()==") && value.contains("Thread.sleep(100)")
                && value.endsWith("thrownewIllegalStateException(\"playercountdrift\");}"))
            return Kind.AWAIT_PLAYERS;
        if (value.startsWith("privatestaticBlockPositionplace(")
                && value.contains(".adjacent(") && value.contains(".placeHeldBlock(")
                && value.contains(".awaitBlock(") && value.contains("newBlockState(")
                && value.contains(",0));") && value.matches(".*return[a-zA-Z]+;}") )
            return Kind.PLACE;
        if (value.startsWith("privatestaticStringsha(") && value.contains("SHA-256")
                && (value.contains("StandardCharsets.UTF_8") || value.contains("getBytes(\"UTF-8\")"))
                && value.contains("String.format(\"%02x\",") && value.contains("&255")
                && value.matches(".*return[a-zA-Z]+[.]toString\\(\\);}")) return Kind.SHA;
        return null;
    }

    private static int closing(String source, int open) {
        int depth = 0; boolean string = false, character = false, escaped = false;
        for (int index = open; index < source.length(); index++) {
            char value = source.charAt(index);
            if (string || character) {
                if (escaped) escaped = false; else if (value == '\\') escaped = true;
                else if (string && value == '"') string = false;
                else if (character && value == '\'') character = false; continue;
            }
            if (value == '"') { string = true; continue; }
            if (value == '\'') { character = true; continue; }
            if (value == '{') depth++; else if (value == '}' && --depth == 0) return index;
        }
        throw new IllegalStateException("unclosed helper method");
    }
    private static int newline(String source, int index) {
        return index < source.length() && source.charAt(index) == '\r' ? 2
                : index < source.length() && source.charAt(index) == '\n' ? 1 : 0;
    }
    private static String compact(String value) { return value.replaceAll("\\s+", ""); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private enum Kind { AWAIT_PLAYERS, PLACE, LOCAL, WATER, SHA }
    private record Method(int start, int end, Kind kind, String source) { }
    private record Change(Path path, String prior, String current, Set<Kind> kinds,
            String packageLine, List<Method> methods) { }
}
