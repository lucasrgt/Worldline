import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Generates and validates documentation topology, semantic totals, and milestone status. */
final class DocumentationCatalog {
    private static final Pattern ROLE_GROUP = Pattern.compile(
            "roles[.]put\\([^,]+,\\s*list\\((.*?)\\)\\);", Pattern.DOTALL);
    private static final Pattern QUOTED = Pattern.compile("\"[^\"]+\"");
    private final Path root;
    DocumentationCatalog(Path root) { this.root = root.toAbsolutePath().normalize(); }
    public static void main(String[] arguments) {
        try {
            if (!List.of(arguments).equals(List.of("--write")))
                throw new IllegalArgumentException("usage: DocumentationCatalog --write");
            new DocumentationCatalog(Path.of("")).write();
        } catch (Exception error) {
            System.err.println("documentation generation failed: " + error.getMessage());
            System.exit(1);
        }
    }
    void execute() throws Exception {
        require(index().equals(read("docs/generated/INDEX.md")), "documentation index drift");
        require(status().equals(read("docs/generated/STATUS.md")), "generated status drift");
        require(milestones().equals(read("docs/generated/MILESTONES.md")),
                "generated milestone catalog drift");
        System.out.println("  documentation catalog: " + rootDocuments().size()
                + " root documents, " + SmokeDiscovery.discover(root).size() + " milestones");
    }
    private void write() throws Exception {
        Path generated = root.resolve("docs/generated"); Files.createDirectories(generated);
        Files.writeString(generated.resolve("INDEX.md"), index(), StandardCharsets.UTF_8);
        Files.writeString(generated.resolve("STATUS.md"), status(), StandardCharsets.UTF_8);
        Files.writeString(generated.resolve("MILESTONES.md"), milestones(), StandardCharsets.UTF_8);
        System.out.println("documentation catalog updated");
    }
    private String index() throws Exception {
        Map<String, List<Path>> groups = new LinkedHashMap<>();
        for (String name : List.of("Project", "Features", "Milestones", "Performance"))
            groups.put(name, new ArrayList<>());
        for (Path path : rootDocuments()) groups.get(group(path.getFileName().toString())).add(path);
        StringBuilder text = new StringBuilder("# Documentation Index\n\n");
        text.append("Generated from the canonical Markdown files in `docs/`. Files remain at stable root paths ")
                .append("so milestone receipts and external links do not churn.\n\n");
        for (Map.Entry<String, List<Path>> entry : groups.entrySet()) {
            text.append("## ").append(entry.getKey()).append("\n\n");
            for (Path path : entry.getValue()) text.append("- [")
                    .append(path.getFileName()).append("](../").append(path.getFileName()).append(")\n");
            text.append('\n');
        }
        return text.toString();
    }
    private String status() throws Exception {
        int categories = 0, roles = 0; String semantic = read(
                "modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java");
        Matcher groups = ROLE_GROUP.matcher(semantic);
        while (groups.find()) { categories++; Matcher values = QUOTED.matcher(groups.group(1));
            while (values.find()) roles++; }
        Map<String, Integer> fable = new java.util.TreeMap<>();
        for (String line : read("docs/FABLE2_PROGRAM.md").split("\\R")) {
            Matcher row = Pattern.compile("^\\| [A-Z]+-[0-9]+ \\| (DONE|ACTIVE|QUEUED|EXTERNAL) \\|")
                    .matcher(line);
            if (row.find()) fable.merge(row.group(1), 1, Integer::sum);
        }
        int smokes = SmokeDiscovery.discover(root).size(); long maps;
        try (var paths = Files.walk(root.resolve("smokes"))) {
            maps = paths.filter(path -> path.getFileName().toString().equals("MAP.md")).count(); }
        return "# Generated Repository Status\n\n"
                + "This file is derived from `SemanticRoles`, smoke descriptors, behavior maps, and the "
                + "Fable 2 ledger. Edit those sources, then run `Gate --refresh-documentation`.\n\n"
                + "| Measure | Value |\n| --- | ---: |\n"
                + "| Semantic categories | " + categories + " |\n"
                + "| Required semantic roles | " + roles + " |\n"
                + "| Smoke milestones | " + smokes + " |\n"
                + "| Behavior maps including aggregates | " + maps + " |\n"
                + "| Fable 2 DONE | " + fable.getOrDefault("DONE", 0) + " |\n"
                + "| Fable 2 ACTIVE | " + fable.getOrDefault("ACTIVE", 0) + " |\n"
                + "| Fable 2 QUEUED | " + fable.getOrDefault("QUEUED", 0) + " |\n"
                + "| Fable 2 EXTERNAL | " + fable.getOrDefault("EXTERNAL", 0) + " |\n";
    }
    private String milestones() throws Exception {
        SmokePins pins = new SmokePins(root); SmokeInputFingerprint fingerprints =
                new SmokeInputFingerprint(root); StringBuilder text = new StringBuilder(
                "# Generated Milestone Catalog\n\n"
                + "Status is calculated from the current behavioral fingerprint and qualification lock.\n\n"
                + "| Milestone | Qualification | Contract |\n| --- | --- | --- |\n");
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            Properties descriptor = load(root.resolve("smokes").resolve(smoke.id)
                    .resolve("smoke.properties"));
            SmokePins.Entry pin = pins.match(smoke.id, fingerprints.compute(smoke));
            String status = pin == null ? "PENDING" : pin.source().equals("executed")
                    ? "EXECUTED" : "CARRIED";
            String contract = descriptor.getProperty("narrative.title",
                    descriptor.getProperty("testkit.contract", smoke.id)).replace('|', '/');
            text.append("| `").append(smoke.id).append("` | ").append(status).append(" | ")
                    .append(contract).append(" |\n");
        }
        return text.toString();
    }
    private List<Path> rootDocuments() throws Exception {
        try (var paths = Files.list(root.resolve("docs"))) {
            return paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString())).toList(); }
    }
    private static String group(String name) {
        if (name.matches("(?:M[0-9]+|AERO_M[0-9]+).*\\.md")) return "Milestones";
        if (name.matches(".*(?:PERF|PERFORMANCE|OPTIMIZATION|FRAME|LATENCY|POOL|CACHE).*"))
            return "Performance";
        if (name.matches("(?:ARCHITECTURE|VERIFICATION_ARCHITECTURE|ENGINEERING_WORKFLOW|FABLE.*|"
                + "ROADMAP|VISION|CENSUS|FIRST_CYCLE|RELEASE.*)[.]md")) return "Project";
        return "Features";
    }
    private Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private String read(String relative) throws Exception {
        return Files.readString(root.resolve(relative), StandardCharsets.UTF_8).replace("\r\n", "\n"); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
