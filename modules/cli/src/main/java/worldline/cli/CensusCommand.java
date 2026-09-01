package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.List;
import worldline.analysis.CensusRunner;
import worldline.testkit.B173EntityPersistenceScenario;
import worldline.testkit.EntityPersistenceEvidence;
import worldline.testkit.EntityPersistenceFixture;

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
        return 0;
    }
}
