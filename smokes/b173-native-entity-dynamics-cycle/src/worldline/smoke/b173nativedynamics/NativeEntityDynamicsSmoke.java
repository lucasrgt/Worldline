package worldline.smoke.b173nativedynamics;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RuntimeSnapshot;
import worldline.b173server.B173NativeEntityDynamics;
import worldline.testapi.NativeEntityDynamicsCase;
import worldline.testapi.NativeEntityDynamicsCase.Rule;
import worldline.testapi.NativeEntityDynamicsObservation;
import worldline.testkit.NativeEntityDynamicsFixture;

/** One fresh native server process producing all caller-declared paired dynamics observations. */
public final class NativeEntityDynamicsSmoke {
    private NativeEntityDynamicsSmoke() {
    }
    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 4, "expected role mappings subjects output");
        String role = arguments[0];
        require(role.equals("mapped") || role.equals("official"), "native role differs");
        List<NativeEntityDynamicsCase> cases = cases(Paths.get(arguments[2]));
        List<NativeEntityDynamicsObservation> observations = B173NativeEntityDynamics.observe(
                Paths.get(arguments[1]), role.equals("official"), cases);
        StringBuilder diagnostic = new StringBuilder();
        for (NativeEntityDynamicsObservation observation : observations) {
            diagnostic.append(observation.canonical()).append('\n');
        }
        Path output = Paths.get(arguments[3]);
        Files.createDirectories(output.getParent());
        Files.write(output.resolveSibling(output.getFileName() + ".observed"),
                diagnostic.toString().getBytes(StandardCharsets.UTF_8));
        String canonical = NativeEntityDynamicsFixture.verify(cases, observations).canonical();
        byte[] bytes = canonical.getBytes(StandardCharsets.UTF_8);
        Files.write(output, bytes);
        System.out.println("WORLDLINE_ENTITY_DYNAMICS_ROLE=" + role);
        System.out.println("WORLDLINE_ENTITY_DYNAMICS_SUBJECTS=23");
        System.out.println("WORLDLINE_ENTITY_DYNAMICS_SIGNATURE=" + RuntimeSnapshot.of(bytes).sha256());
        System.out.println("WORLDLINE_ENTITY_DYNAMICS_PROVENANCE=all-native-entities:"
                + (role.equals("official") ? "jars/minecraft_server.jar" : "minecraft_server/bin/"));
    }
    private static List<NativeEntityDynamicsCase> cases(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals(
                "subject_id\tlegacy_id\tregistry_class\tlayer\tmovement_rule\tenvironment_rule\ttick_rule"),
                "native case header");
        Set<Integer> subjects = new HashSet<>();
        List<NativeEntityDynamicsCase> cases = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String[] row = lines.get(index).split("\t", -1);
            require(row.length == 7, "native case width");
            int id = Integer.parseInt(row[1]);
            require(id != 48 && subjects.add(id), "native subject duplicated or abstract");
            NativeEntityDynamicsCase motion = new NativeEntityDynamicsCase(id, row[2], row[3], Rule.valueOf(row[4]));
            require(motion.subject().equals(row[0]), "native subject identity differs");
            cases.add(motion);
            for (int column = 5; column <= 6; column++) {
                if (!row[column].equals("NONE")) {
                    cases.add(new NativeEntityDynamicsCase(id, row[2], row[3], Rule.valueOf(row[column])));
                }
            }
        }
        require(subjects.size() == 23 && cases.size() == 66, "native dynamics family is incomplete");
        return cases;
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
