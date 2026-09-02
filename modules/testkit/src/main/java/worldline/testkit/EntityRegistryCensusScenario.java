package worldline.testkit;
import worldline.testapi.EntityRegistryObservation;
import worldline.testapi.EntityRegistryScenario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.analysis.CensusRunner;

/** Adapts a canonical runtime census to the public EntityList registry scenario. */
public final class EntityRegistryCensusScenario implements EntityRegistryScenario {
    private final CensusRunner runner;
    private final String version;

    public EntityRegistryCensusScenario(CensusRunner runner, String version) {
        if (runner == null) throw new NullPointerException("census runner");
        if (version == null || !version.matches("b[0-9]+\\.[0-9]+\\.[0-9]+")) {
            throw new IllegalArgumentException("version");
        }
        this.runner = runner;
        this.version = version;
    }

    @Override public List<EntityRegistryObservation> observe() {
        require(runner.sections().contains("entities"), "entity census section is absent");
        String first = runner.section("entities");
        String second = runner.section("entities");
        require(first.equals(second), "entity census changed between captures");
        return parse(first);
    }

    private List<EntityRegistryObservation> parse(String document) {
        if (document == null) throw new IllegalStateException("entity census is absent");
        String[] lines = document.split("\\n", -1);
        require(lines.length >= 5 && "WORLDLINE-CENSUS/1".equals(lines[0])
                && "section=entities".equals(lines[1]), "invalid entity census framing");
        int expected = number(lines[2], "rows=");
        int digestLine = lines.length - 2;
        require(lines[lines.length - 1].isEmpty()
                && lines[digestLine].startsWith("sha256="), "invalid entity census ending");
        String body = document.substring(0, document.lastIndexOf("sha256="));
        require(lines[digestLine].equals("sha256=" + sha256(body)),
                "entity census digest drifted");
        List<EntityRegistryObservation> result = new ArrayList<EntityRegistryObservation>();
        for (int line = 3; line < digestLine; line++) {
            require(lines[line].matches("e[0-9]{3}=name=[A-Za-z0-9]+\\|class=[A-Za-z0-9_$]+"),
                    "invalid entity census row");
            String legacy = lines[line].substring(1, 4);
            result.add(new EntityRegistryObservation(
                    version + ":entity/" + legacy, lines[line]));
        }
        require(result.size() == expected, "entity census row count drifted");
        return Collections.unmodifiableList(result);
    }

    private static int number(String value, String prefix) {
        require(value.startsWith(prefix), "entity census row count is absent");
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
