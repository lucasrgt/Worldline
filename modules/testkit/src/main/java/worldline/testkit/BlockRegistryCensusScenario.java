package worldline.testkit;
import worldline.testapi.BlockRegistryObservation;
import worldline.testapi.BlockRegistryScenario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.analysis.CensusRunner;

/** Adapts a canonical runtime census to the public block-registry scenario. */
public final class BlockRegistryCensusScenario implements BlockRegistryScenario {
    private final CensusRunner runner;
    private final String version;

    public BlockRegistryCensusScenario(CensusRunner runner, String version) {
        if (runner == null) throw new NullPointerException("census runner");
        if (version == null || !version.matches("b[0-9]+\\.[0-9]+\\.[0-9]+")) {
            throw new IllegalArgumentException("version");
        }
        this.runner = runner;
        this.version = version;
    }

    @Override public List<BlockRegistryObservation> observe() {
        require(runner.sections().contains("blocks"), "block census section is absent");
        String first = runner.section("blocks");
        String second = runner.section("blocks");
        require(first.equals(second), "block census changed between captures");
        return parse(first);
    }

    private List<BlockRegistryObservation> parse(String document) {
        if (document == null) throw new IllegalStateException("block census is absent");
        String[] lines = document.split("\\n", -1);
        require(lines.length >= 5 && "WORLDLINE-CENSUS/1".equals(lines[0])
                && "section=blocks".equals(lines[1]), "invalid block census framing");
        int expected = number(lines[2], "rows=");
        int digestLine = lines.length - 2;
        require(lines[lines.length - 1].isEmpty()
                && lines[digestLine].startsWith("sha256="), "invalid block census ending");
        String body = document.substring(0, document.lastIndexOf("sha256="));
        require(lines[digestLine].equals("sha256=" + sha256(body)),
                "block census digest drifted");
        List<BlockRegistryObservation> result = new ArrayList<BlockRegistryObservation>();
        for (int line = 3; line < digestLine; line++) {
            require(lines[line].matches("b[0-9]{3}=.+"), "invalid block census row");
            String legacy = lines[line].substring(1, 4);
            result.add(new BlockRegistryObservation(version + ":block/" + legacy, lines[line]));
        }
        require(result.size() == expected, "block census row count drifted");
        return Collections.unmodifiableList(result);
    }

    private static int number(String value, String prefix) {
        require(value.startsWith(prefix), "block census row count is absent");
        try { return Integer.parseInt(value.substring(prefix.length())); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid row count"); }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte item : digest) result.append(String.format("%02x", item & 255));
            return result.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
