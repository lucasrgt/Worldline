import java.util.regex.Pattern;

/** Regex grammar used by the reviewed data-driven cycle migration. */
final class DataDrivenCycleSyntax {
    static final Pattern ID = Pattern.compile(
            "private\\s+static\\s+final\\s+String\\s+ID\\s*=\\s*\\\"([^\\\"]+)\\\"");
    static final Pattern MAIN = Pattern.compile("\\\"(worldline\\.smoke\\.[^\\\"]+)\\\"");
    static final Pattern VALUE = Pattern.compile("value\\(config,\\s*\\\"([^\\\"]+)\\\"\\)");
    static final Pattern PRODUCT = Pattern.compile("product\\(\\s*\\\"([a-z0-9-]+)\\\"\\)");
    static final Pattern INPUT = Pattern.compile(
            "javaFiles\\(root\\.resolve\\(\\s*\\\"([^\\\"]+)\\\"\\)\\)");
    static final Pattern PREFIX = Pattern.compile("line\\(output,\\s*\\\"([^\\\"]+)\\\"\\)");
    static final Pattern OUTPUT = Pattern.compile(
            "output\\.contains\\(\\s*\\\"([^\\\"]+)\\\"\\)");
    static final Pattern CONTAINS = Pattern.compile(
            "(!?)(?:first\\.)?(signal|trace)\\.contains\\(\\s*\\\"([^\\\"]+)\\\"\\)");

    private DataDrivenCycleSyntax() { }
}
