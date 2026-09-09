package worldline.smoke.b173nativestimulus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RuntimeSnapshot;
import worldline.b173server.B173NativeEntityStimulus;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsCase.Rule;
import worldline.testapi.NativeEntityDynamicsObservation;
import worldline.testkit.NativeEntityStimulusFixture;

/** One fresh native server process producing all caller-declared paired dynamics observations. */
public final class NativeEntityStimulusSmoke {
    private NativeEntityStimulusSmoke() {
    }
    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 4, "expected role mappings subjects output");
        String role = arguments[0];
        require(role.equals("mapped") || role.equals("official"), "native role differs");
        List<NativeEntityDynamicsCase> cases = cases(Paths.get(arguments[2]));
        List<NativeEntityDynamicsObservation> observations = B173NativeEntityStimulus.observe(
                Paths.get(arguments[1]), role.equals("official"), cases);
        StringBuilder diagnostic = new StringBuilder();
        for (NativeEntityDynamicsObservation observation : observations) {
            diagnostic.append(observation.canonical()).append('\n');
        }
        Path output = Paths.get(arguments[3]);
        Files.createDirectories(output.getParent());
        Files.write(output.resolveSibling(output.getFileName() + ".observed"),
                diagnostic.toString().getBytes(StandardCharsets.UTF_8));
        String canonical = NativeEntityStimulusFixture.verify(cases, observations).canonical();
        byte[] bytes = canonical.getBytes(StandardCharsets.UTF_8);
        Files.write(output, bytes);
        System.out.println("WORLDLINE_ENTITY_STIMULUS_ROLE=" + role);
        System.out.println("WORLDLINE_ENTITY_STIMULUS_SUBJECTS=20");
        System.out.println("WORLDLINE_ENTITY_STIMULUS_SIGNATURE=" + RuntimeSnapshot.of(bytes).sha256());
        System.out.println("WORLDLINE_ENTITY_STIMULUS_PROVENANCE=all-native-entities:"
                + (role.equals("official") ? "jars/minecraft_server.jar" : "minecraft_server/bin/"));
    }
    private static List<NativeEntityDynamicsCase> cases(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals(
                "subject_id\tlegacy_id\tregistry_class\tlayer\tspawn_rule\tdamage_rule\tdrops_rule\tinteraction_rule"),
                "native case header");
        Set<Integer> subjects = new HashSet<>();
        List<NativeEntityDynamicsCase> cases = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String[] row = lines.get(index).split("\t", -1);
            require(row.length == 8, "native case width");
            int id = Integer.parseInt(row[1]);
            require(id != 48 && subjects.add(id), "native subject duplicated or abstract");
            for (int column = 4; column <= 7; column++) {
                if (row[column].equals("NONE")) {
                    continue;
                }
                Rule rule = Rule.valueOf(row[column]);
                NativeEntityDynamicsCase scene = new NativeEntityDynamicsCase(id, row[2],
                        rule == Rule.SPAWN ? "UNIVERSAL" : row[3], rule);
                require(scene.subject().equals(row[0]), "native subject identity differs");
                cases.add(scene);
            }
        }
        require(subjects.size() == 20 && cases.size() == 53, "native stimulus family is incomplete");
        return cases;
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
