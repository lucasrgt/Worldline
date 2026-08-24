package worldline.symbolgraph;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class MappingBatchTest {
    private MappingBatchTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-mapping-batch-");
        try {
            String officialName = "worldline/symbolgraph/MappingBatchTest";
            Path official = root.resolve("official.jar"), inventory = root.resolve("inventory.jar");
            Path nostalgia = root.resolve("nostalgia.jar"), retro = root.resolve("retro.tiny");
            Path descriptor = root.resolve("retro.properties"), policy = root.resolve("batch.properties");
            official(official, officialName);
            archive(inventory, "tiny\t2\t0\tintermediary\tclientOfficial\tserverOfficial\n"
                    + "c\tfixture/Class\t" + officialName + "\t" + officialName + "\n");
            archive(nostalgia, "tiny\t2\t0\tintermediary\tnamed\n"
                    + "c\tfixture/Class\tFixtureClass\n"
                    + "c\tfixture/Orphan\tRetractedClass\n");
            TinyMapping feather = read("tiny\t2\t0\tintermediary\tnamed\n"
                    + "c\tfixture/Class\tCurrentClass\n");
            String retroText = "tiny\t2\t0\tnamed\tclient\tserver\n"
                    + "c\tfixture/Retro\t" + officialName + "\t" + officialName + "\n";
            Files.write(retro, retroText.getBytes(StandardCharsets.UTF_8));
            Files.write(descriptor, ("id=test\nexpected.bytes=" + Files.size(retro)
                    + "\nexpected.sha256=" + sha(Files.readAllBytes(retro)) + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            TinyMapping intermediary = MappingArchive.read(inventory, "mappings/mappings.tiny");
            TinyMapping named = MappingArchive.read(nostalgia, "mappings/mappings.tiny");
            TinyMapping retroMapping = read(retroText);
            SymbolGraph graph = new RetroMcpImport().apply(
                    new SymbolGraphBuilder().build(intermediary, named), intermediary, retroMapping).graph();
            MappingCoverageReport coverage = MappingCoverageReport.create(
                    official, official, inventory, nostalgia, descriptor, retro);
            MappingBatchReport report = MappingBatchReport.create(coverage, intermediary, named,
                    feather, graph, 100);
            require("1".equals(report.metric("qualified.total")), "qualified identity count");
            require("1".equals(report.metric("excluded.total")), "orphan count");
            require(report.excludedIds().size() == 1
                    && report.render().contains(report.excludedIds().get(0)), "orphan identity attestation");
            require("true".equals(report.metric("complete")), "complete batch status");
            String exact = MappingBatchGate.policy(report);
            require(exact.contains("expected.report.sha256=" + report.sha256()), "exact policy render");
            Files.write(policy, exact.getBytes(StandardCharsets.UTF_8));
            MappingBatchGate.verify(report, policy);
            Files.write(policy, "schema=1\n".getBytes(StandardCharsets.UTF_8));
            failure(() -> MappingBatchGate.verify(report, policy));
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList())) Files.delete(path);
            }
        }
        System.out.println("MappingBatchTest passed");
    }

    private static TinyMapping read(String text) throws Exception {
        return new TinyV2Reader().read(new StringReader(text));
    }
    private static void archive(Path path, String tiny) throws Exception {
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry("mappings/mappings.tiny"));
            output.write(tiny.getBytes(StandardCharsets.UTF_8)); output.closeEntry();
        }
    }
    private static void official(Path path, String name) throws Exception {
        byte[] bytes;
        try (InputStream input = MappingBatchTest.class.getResourceAsStream("MappingBatchTest.class")) {
            require(input != null, "test class resource"); bytes = input.readAllBytes();
        }
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry(name + ".class")); output.write(bytes); output.closeEntry();
        }
    }
    private static String sha(byte[] bytes) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes))
            value.append(String.format("%02x", Integer.valueOf(item & 255)));
        return value.toString();
    }
    private static void failure(Checked action) throws Exception {
        try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalStateException expected) { }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private interface Checked { void run() throws Exception; }
}
