package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import worldline.analysis.CensusRunner;
import worldline.testkit.BlockConformancePlan;
import worldline.testkit.BlockConformanceProfile;
import worldline.testkit.BlockConformanceTemplate;
import worldline.testkit.BlockRegistryCensusScenario;
import worldline.testkit.BlockRegistryEvidence;
import worldline.testkit.BlockRegistryFixture;
import worldline.testkit.ConformanceLayer;
import worldline.testkit.EntityConformancePlan;
import worldline.testkit.EntityConformanceProfile;
import worldline.testkit.EntityConformanceTemplate;
import worldline.testkit.EntityRegistryCensusScenario;
import worldline.testkit.EntityRegistryEvidence;
import worldline.testkit.EntityRegistryFixture;

/** Captures the controlled b1.7.3 registry census into canonical files. */
final class CensusCommand {
    private CensusCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length != 2 || !"census".equals(arguments[0])) {
            return WorldlineCli.usage(error);
        }
        Path outDir = Paths.get(arguments[1]);
        CensusRunner runner = Checks.provider("worldline.census.provider",
                "worldline.b173.B173CensusRunner", CensusRunner.class);
        List<String> sections = runner.sections();
        Files.createDirectories(outDir);
        output.println("WORLDLINE_CENSUS=PASS");
        for (String section : sections) {
            Path file = outDir.resolve(section + ".wlcensus");
            byte[] bytes = runner.section(section).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            Files.write(file, bytes, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
            output.println(section + ".sha256=" + Checks.sha256(bytes));
            output.println(section + ".file=" + file);
        }
        BlockRegistryEvidence registry = BlockRegistryFixture.execute(registryPlan(),
                new BlockRegistryCensusScenario(runner, "b1.7.3"));
        Path registryFile = outDir.resolve("registry.wlevidence");
        byte[] registryBytes = registry.canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(registryFile, registryBytes, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("registry.sha256=" + Checks.sha256(registryBytes));
        output.println("registry.file=" + registryFile);
        EntityRegistryEvidence entityRegistry = EntityRegistryFixture.execute(
                entityRegistryPlan(), new EntityRegistryCensusScenario(runner, "b1.7.3"));
        Path entityRegistryFile = outDir.resolve("entity-registry.wlevidence");
        byte[] entityRegistryBytes = entityRegistry.canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(entityRegistryFile, entityRegistryBytes, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("entity-registry.sha256=" + Checks.sha256(entityRegistryBytes));
        output.println("entity-registry.file=" + entityRegistryFile);
        return 0;
    }

    private static BlockConformancePlan registryPlan() throws IOException {
        Path subjects = Paths.get("behavior", "functional-census", "b1.7.3", "subjects.tsv");
        List<String> lines = Files.readAllLines(subjects, StandardCharsets.UTF_8);
        Checks.require(!lines.isEmpty() && lines.get(0).startsWith("subject_id\t"),
                "invalid Functional Census subjects");
        List<BlockConformanceProfile> profiles = new ArrayList<BlockConformanceProfile>();
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).trim().isEmpty() || lines.get(line).startsWith("#")) continue;
            String subject = lines.get(line).split("\\t", -1)[0];
            profiles.add(new BlockConformanceProfile(subject,
                    Collections.singletonList("registry"), false,
                    Collections.<String, ConformanceLayer>emptyMap()));
        }
        return new BlockConformancePlan(profiles, Collections.singletonList(
                new BlockConformanceTemplate("registry-presence", ConformanceLayer.UNIVERSAL)));
    }

    private static EntityConformancePlan entityRegistryPlan() throws IOException {
        Path subjects = Paths.get("behavior", "functional-census", "b1.7.3", "entities",
                "subjects.tsv");
        List<String> lines = Files.readAllLines(subjects, StandardCharsets.UTF_8);
        Checks.require(!lines.isEmpty() && lines.get(0).startsWith("subject_id\t"),
                "invalid Entity Functional Census subjects");
        List<EntityConformanceProfile> profiles = new ArrayList<EntityConformanceProfile>();
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).trim().isEmpty() || lines.get(line).startsWith("#")) continue;
            String subject = lines.get(line).split("\\t", -1)[0];
            profiles.add(new EntityConformanceProfile(subject,
                    Collections.singletonList("registry"), false,
                    Collections.<String, ConformanceLayer>emptyMap()));
        }
        return new EntityConformancePlan(profiles, Collections.singletonList(
                new EntityConformanceTemplate("registry-presence", ConformanceLayer.UNIVERSAL)));
    }
}
