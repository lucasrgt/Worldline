package worldline.smoke.b173entityrender;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import worldline.b173.B173EntityRender;
import worldline.b173.B173EntityRenderFrame;
import worldline.b173.B173RgbaDigest;
import worldline.testapi.NativeEntityRenderSubject;
import worldline.testapi.NativeEntityRenderObservation;
import worldline.testkit.NativeEntityRenderFixture;

/** Captures a complete concrete native EntityList render family in one fresh JVM. */
public final class B173NativeEntityRenderSmoke {
    private B173NativeEntityRenderSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 4, "expected role mappings subjects output");
        String role = arguments[0];
        require(role.equals("mapped") || role.equals("official"), "invalid native role");
        List<NativeEntityRenderSubject> subjects = subjects(Paths.get(arguments[2]));
        int[] ids = subjects.stream().mapToInt(NativeEntityRenderSubject::legacyId).toArray();
        List<B173EntityRenderFrame> frames = B173EntityRender.render(Paths.get(arguments[1]),
                role.equals("official"), ids);
        require(frames.size() == subjects.size(), "native frame census drift");
        List<NativeEntityRenderObservation> observations = new ArrayList<>();
        String origin = role.equals("official") ? "jars/minecraft.jar" : "minecraft/bin/";
        for (int index = 0; index < frames.size(); index++) {
            B173EntityRenderFrame frame = frames.get(index);
            NativeEntityRenderSubject subject = subjects.get(index);
            require(frame.legacyId() == subject.legacyId()
                    && frame.entityClass().equals(subject.entityClass()), "native registry identity drift");
            require(frame.entityOrigin().replace('\\', '/').contains(origin)
                    && frame.rendererOrigin().replace('\\', '/').contains(origin), "native class provenance drift");
            observations.add(new NativeEntityRenderObservation(subject, frame.rendererClass(),
                    frame.pixels(), frame.sha256()));
        }
        String canonical = NativeEntityRenderFixture.verify(subjects, observations).canonical();
        Path output = Paths.get(arguments[3]);
        Files.createDirectories(output.getParent());
        byte[] bytes = canonical.getBytes(StandardCharsets.UTF_8);
        Files.write(output, bytes);
        System.out.println("WORLDLINE_ENTITY_RENDER_ROLE=" + role);
        System.out.println("WORLDLINE_ENTITY_RENDER_SUBJECTS=" + subjects.size());
        System.out.println("WORLDLINE_ENTITY_RENDER_SIGNATURE=" + B173RgbaDigest.hash(ByteBuffer.wrap(bytes)));
        System.out.println("WORLDLINE_ENTITY_RENDER_PROVENANCE=all-entities-and-renderers:" + origin);
    }

    private static List<NativeEntityRenderSubject> subjects(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals("subject_id\tlegacy_id\tregistry_class\tlayer"),
                "native entity subjects header drift");
        List<NativeEntityRenderSubject> subjects = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String[] row = lines.get(index).split("\t", -1);
            require(row.length == 4, "malformed native entity subject");
            subjects.add(new NativeEntityRenderSubject(row[0], Integer.parseInt(row[1]), row[2], row[3]));
        }
        require(subjects.size() == 23, "concrete registry census drift");
        return subjects;
    }
    private static void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
