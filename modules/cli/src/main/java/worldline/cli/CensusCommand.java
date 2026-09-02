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
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.testkit.B173EntityPhysicalEnvelopeScenario;
import worldline.analysis.CensusRunner;
import worldline.testkit.B173EntityPersistenceScenario;
import worldline.testkit.EntityPersistenceEvidence;
import worldline.testkit.EntityPersistenceFixture;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testkit.BlockRegistryCensusScenario;
import worldline.testapi.BlockRegistryEvidence;
import worldline.testapi.BlockRegistryFixture;
import worldline.testapi.ConformanceLayer;
import worldline.testapi.EntityConformancePlan;
import worldline.testapi.EntityConformanceProfile;
import worldline.testapi.EntityConformanceTemplate;
import worldline.testkit.EntityRegistryCensusScenario;
import worldline.testapi.EntityRegistryEvidence;
import worldline.testapi.EntityRegistryFixture;
import worldline.testapi.EntityPhysicalEnvelopeEvidence;
import worldline.testapi.EntityPhysicalEnvelopeFixture;

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
        EntityPersistenceEvidence persistence = EntityPersistenceFixture.execute(
                new B173EntityPersistenceScenario("b1.7.3"));
        Path persistenceFile = outDir.resolve("entity-persistence.wlevidence");
        byte[] persistenceBytes = persistence.canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(persistenceFile, persistenceBytes, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("entity-persistence.sha256=" + Checks.sha256(persistenceBytes));
        output.println("entity-persistence.file=" + persistenceFile);
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
        EntityPhysicalEnvelopeEvidence entityEnvelope = EntityPhysicalEnvelopeFixture.execute(
                entityPhysicalEnvelopePlan(), new B173EntityPhysicalEnvelopeScenario("b1.7.3"));
        Path entityEnvelopeFile = outDir.resolve("entity-physical-envelope.wlevidence");
        byte[] entityEnvelopeBytes = entityEnvelope.canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(entityEnvelopeFile, entityEnvelopeBytes, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("entity-physical-envelope.sha256=" + Checks.sha256(entityEnvelopeBytes));
        output.println("entity-physical-envelope.file=" + entityEnvelopeFile);
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

    private static EntityConformancePlan entityPhysicalEnvelopePlan() throws IOException {
        Path base = Paths.get("behavior", "functional-census", "b1.7.3", "entities");
        List<String> subjects = Files.readAllLines(base.resolve("subjects.tsv"),
                StandardCharsets.UTF_8);
        List<String> profileRows = Files.readAllLines(base.resolve("profiles.tsv"),
                StandardCharsets.UTF_8);
        Checks.require(!subjects.isEmpty() && subjects.get(0).startsWith("subject_id\t")
                && !profileRows.isEmpty()
                && profileRows.get(0).equals("subject_id\tsingular\tarchetypes"),
                "invalid Entity Functional Census physical profiles");
        Map<String, String[]> profiles = new LinkedHashMap<String, String[]>();
        for (int line = 1; line < profileRows.size(); line++) {
            if (profileRows.get(line).trim().isEmpty() || profileRows.get(line).startsWith("#")) {
                continue;
            }
            String[] columns = profileRows.get(line).split("\\t", -1);
            Checks.require(columns.length == 3 && profiles.put(columns[0], columns) == null,
                    "invalid entity physical profile");
        }
        List<EntityConformanceProfile> expanded = new ArrayList<EntityConformanceProfile>();
        for (int line = 1; line < subjects.size(); line++) {
            if (subjects.get(line).trim().isEmpty() || subjects.get(line).startsWith("#")) continue;
            String subject = subjects.get(line).split("\\t", -1)[0];
            if ("b1.7.3:entity/048".equals(subject)) continue;
            String[] profile = profiles.get(subject);
            Checks.require(profile != null, "entity physical profile is absent");
            expanded.add(new EntityConformanceProfile(subject,
                    java.util.Arrays.asList(profile[2].split(",")),
                    Boolean.parseBoolean(profile[1]),
                    Collections.<String, ConformanceLayer>emptyMap()));
        }
        Checks.require(expanded.size() == 23, "concrete entity physical plan drifted");
        return new EntityConformancePlan(expanded, Collections.singletonList(
                new EntityConformanceTemplate("collision-shape", ConformanceLayer.ARCHETYPE)));
    }
}
