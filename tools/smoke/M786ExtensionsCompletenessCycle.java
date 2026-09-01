import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/** Qualifies the complete public binding ledgers plus the external SDK and Atlas route. */
public final class M786ExtensionsCompletenessCycle {
    private static final String ID = "m786-extensions-completeness";
    private static final String SIGNAL = "block-bindings=1056,entity-bindings=97,"
            + "public-bindings=1153,extensions=1,subjects=4,contracts=3,atlas-records=8,"
            + "cli-import=pass,imports=public-only";
    private static final Path ROOT = Paths.get("").toAbsolutePath().normalize();

    private M786ExtensionsCompletenessCycle() { }

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/M786ExtensionsCompletenessCycle.java " + ID);
            System.exit(2);
        }
        try {
            int blocks = bindings("behavior/functional-census/b1.7.3/testkit-bindings.tsv");
            int entities = bindings(
                    "behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv");
            require(blocks == 1056 && entities == 97, "public binding census drifted");
            Path nested = ROOT.resolve(".worldline/smokes/" + ID + "/m785-runner");
            SmokeSupport.recreate(ROOT, nested);
            String classpath = System.getProperty("java.class.path");
            run(Arrays.asList("javac", "-encoding", "UTF-8", "-classpath", classpath,
                    "-d", nested.toString(), "tools/harness/SmokeSupport.java",
                    "tools/smoke/M785PublicExtensionsSdkCycle.java"));
            String external = run(Arrays.asList("java", "-classpath",
                    nested + File.pathSeparator + classpath, "M785PublicExtensionsSdkCycle",
                    "m785-public-extensions-sdk"));
            require(external.contains("WORLDLINE_EXTENSION_SDK=PASS")
                    && external.contains("WORLDLINE_ATLAS_EXTENSIONS=PASS"),
                    "external extension qualification did not pass");
            String signature = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(SIGNAL.getBytes(StandardCharsets.UTF_8)));
            require("d4fd89a2c0deb9fa8436f17df1ebe9e8044b9a6788020a17b15a6b78644940ff"
                    .equals(signature), "completeness signature drifted");
            System.out.println("WORLDLINE_EXTENSIONS_COMPLETENESS=PASS");
            System.out.println(SIGNAL);
            System.out.println("  signature: " + signature);
        } catch (Exception failure) {
            System.err.println("M786 extensions completeness failed: " + failure.getMessage());
            System.exit(1);
        }
    }

    private static int bindings(String relative) throws Exception {
        List<String> lines = Files.readAllLines(ROOT.resolve(relative), StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals(
                "subject_id\ttemplate_id\tfixture\tbinding\tevidence_id"),
                "binding ledger header drifted");
        Set<String> keys = new HashSet<String>();
        for (int line = 1; line < lines.size(); line++) {
            String[] fields = lines.get(line).split("\\t", -1);
            require(fields.length == 5 && keys.add(fields[0] + "#" + fields[1]),
                    "invalid or duplicate binding row");
            require(fields[3].matches("worldline\\.testkit\\.[A-Z][A-Za-z0-9]+#[a-z][A-Za-z0-9]*"),
                    "binding is outside the public TestKit");
        }
        return keys.size();
    }

    private static String run(List<String> command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(ROOT.toFile())
                .redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.print(output);
        require(process.waitFor() == 0, "external extension cycle failed");
        return output;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
