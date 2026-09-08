package worldline.smoke.b173random;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import worldline.api.RuntimeSnapshot;
import worldline.b173server.B173NativeRandomOutcomes;
import worldline.testapi.RandomOutcomeObservation;
import worldline.testkit.RandomOutcomeFixture;

/** A fresh native server process executing the full finite controlled-draw matrix. */
public final class NativeRandomOutcomeSmoke {
    private NativeRandomOutcomeSmoke() { }
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4)
            throw new IllegalArgumentException("expected role mappings subjects output");
        String role = arguments[0];
        if (!role.equals("mapped") && !role.equals("official"))
            throw new IllegalArgumentException("role");
        String family = new String(Files.readAllBytes(Paths.get(arguments[2])), StandardCharsets.UTF_8);
        if (!family.equals("subject_id\nnative-gravel\nnative-fishing\n"))
            throw new IllegalArgumentException("native random family differs");
        List<RandomOutcomeObservation> rows = B173NativeRandomOutcomes.observe(Paths.get(arguments[1]),
                role.equals("official"));
        StringBuilder observed = new StringBuilder();
        for (RandomOutcomeObservation row : rows)
            observed.append(row.canonical()).append('\n');
        Path output = Paths.get(arguments[3]);
        Files.write(output.resolveSibling(output.getFileName() + ".observed"),
                observed.toString().getBytes(StandardCharsets.UTF_8));
        byte[] bytes = RandomOutcomeFixture.verify(rows).canonical().getBytes(StandardCharsets.UTF_8);
        Files.write(output, bytes);
        System.out.println("WORLDLINE_RANDOM_ROLE=" + role);
        System.out.println("WORLDLINE_RANDOM_SUBJECTS=2");
        System.out.println("WORLDLINE_RANDOM_SIGNATURE=" + RuntimeSnapshot.of(bytes).sha256());
        System.out.println("WORLDLINE_RANDOM_PROVENANCE=all-native-random-classes:"
                + (role.equals("official") ? "jars/minecraft_server.jar" : "minecraft_server/bin/"));
    }
}
