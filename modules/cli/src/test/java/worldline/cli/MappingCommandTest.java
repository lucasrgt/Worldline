package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import worldline.symbolgraph.MappingCoverageReport;
import worldline.symbolgraph.MappingEvidenceReport;
import worldline.symbolgraph.MappingQualificationQueue;

public final class MappingCommandTest {
    private MappingCommandTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-mapping-command-");
        String officialName = "worldline/cli/MappingCommandTest";
        Path official = root.resolve("official.jar");
        Path intermediary = root.resolve("intermediary.jar");
        Path nostalgia = root.resolve("nostalgia.jar");
        Path retro = root.resolve("retro.tiny");
        Path descriptor = root.resolve("retro.properties");
        Path policy = root.resolve("coverage.properties");
        Path evidence = root.resolve("evidence.tsv");
        try {
            official(official, officialName);
            archive(intermediary, "tiny\t2\t0\tintermediary\tclientOfficial\tserverOfficial\n"
                    + "c\tfixture/Class\t" + officialName + "\t" + officialName + "\n");
            archive(nostalgia, "tiny\t2\t0\tintermediary\tnamed\n"
                    + "c\tfixture/Class\tFixtureClass\n");
            String retroText = "tiny\t2\t0\tnamed\tclient\tserver\n"
                    + "c\tfixture/Retro\t" + officialName + "\t" + officialName + "\n";
            Files.write(retro, retroText.getBytes(StandardCharsets.UTF_8));
            Files.write(descriptor, ("id=test\nexpected.bytes=" + Files.size(retro)
                    + "\nexpected.sha256=" + sha(Files.readAllBytes(retro)) + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            MappingCoverageReport report = MappingCoverageReport.create(
                    official, official, intermediary, nostalgia, descriptor, retro);
            MappingQualificationQueue queue = MappingQualificationQueue.create(
                    official, official, intermediary, nostalgia, descriptor, retro);
            require(!queue.items().isEmpty() && queue.render().contains("UNMAPPED_METHOD")
                    && queue.sha256().equals(MappingQualificationQueue.create(official, official,
                            intermediary, nostalgia, descriptor, retro).sha256()),
                    "mapping qualification queue is incomplete or unstable");
            String item = queue.items().get(0).id();
            Files.write(evidence, ("schema=1\nqueue.sha256=" + queue.sha256()
                    + "\nitem\tsource\tevidence\talias\treference\n"
                    + item + "\tnostalgia-b173\tcross-namespace\tstableName\tfixture:named\n"
                    + item + "\tornithe-b174\tcross-version\tstableName\tfixture:next\n")
                    .getBytes(StandardCharsets.UTF_8));
            MappingEvidenceReport evidenceReport = MappingEvidenceReport.create(queue, evidence);
            require("CORROBORATED".equals(evidenceReport.status(item))
                    && evidenceReport.render().contains("summary.conflict=0"),
                    "mapping evidence report failed to corroborate independent aliases");
            ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
            int status = WorldlineCli.run(new String[] {"mappings", "report", official.toString(),
                    official.toString(), intermediary.toString(), nostalgia.toString(), descriptor.toString(),
                    retro.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && error.size() == 0
                    && output.toString("UTF-8").contains("WORLDLINE_MAPPINGS_REPORT=PASS"),
                    "mapping report CLI failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mappings", "queue", official.toString(),
                    official.toString(), intermediary.toString(), nostalgia.toString(), descriptor.toString(),
                    retro.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && error.size() == 0
                    && output.toString("UTF-8").contains("WORLDLINE_MAPPINGS_QUEUE=PASS")
                    && output.toString("UTF-8").contains("queue.sha256="),
                    "mapping qualification queue CLI failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mappings", "evidence", official.toString(),
                    official.toString(), intermediary.toString(), nostalgia.toString(), descriptor.toString(),
                    retro.toString(), evidence.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && error.size() == 0
                    && output.toString("UTF-8").contains("WORLDLINE_MAPPINGS_EVIDENCE=PASS")
                    && output.toString("UTF-8").contains("CORROBORATED"),
                    "mapping evidence CLI failed");
            StringBuilder expected = new StringBuilder("schema=1\n");
            for (Map.Entry<String, String> metric : report.metrics().entrySet())
                expected.append("expected.").append(metric.getKey()).append('=')
                        .append(metric.getValue()).append('\n');
            expected.append("expected.report.sha256=").append(report.sha256()).append('\n');
            Files.write(policy, expected.toString().getBytes(StandardCharsets.UTF_8));
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mappings", "audit", official.toString(),
                    official.toString(), intermediary.toString(), nostalgia.toString(), descriptor.toString(),
                    retro.toString(), policy.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && error.size() == 0
                    && output.toString("UTF-8").contains("WORLDLINE_MAPPINGS_AUDIT=PASS")
                    && output.toString("UTF-8").contains("report.sha256="), "mapping audit CLI failed");
            Files.write(policy, "schema=1\n".getBytes(StandardCharsets.UTF_8));
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mappings", "audit", official.toString(),
                    official.toString(), intermediary.toString(), nostalgia.toString(), descriptor.toString(),
                    retro.toString(), policy.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 1 && error.toString("UTF-8").contains("does not enumerate every metric"),
                    "mapping audit accepted an incomplete policy");
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList())) Files.delete(path);
            }
        }
        System.out.println("MappingCommandTest passed");
    }

    private static void archive(Path path, String tiny) throws Exception {
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry("mappings/mappings.tiny"));
            output.write(tiny.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
    }

    private static void official(Path path, String name) throws Exception {
        byte[] bytes;
        try (InputStream input = MappingCommandTest.class.getResourceAsStream("MappingCommandTest.class")) {
            if (input == null) throw new IllegalStateException("test class resource absent");
            bytes = read(input);
        }
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry(name + ".class"));
            output.write(bytes);
            output.closeEntry();
        }
    }

    private static byte[] read(InputStream input) throws Exception {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096]; int count;
        while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static String sha(byte[] bytes) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes))
            value.append(String.format("%02x", Integer.valueOf(item & 255)));
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
