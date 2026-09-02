package worldline.smoke.b173specialworld;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import worldline.b173.B173SpecialWorldRender;
import worldline.b173.B173WorldBlockFrame;
import worldline.testkit.NativeWorldBlockRenderEvidence;
import worldline.testkit.NativeWorldBlockRenderFixture;
import worldline.testapi.NativeWorldBlockRenderObservation;
import worldline.testapi.NativeWorldBlockRenderPlan;
import worldline.testapi.NativeWorldBlockRenderSubject;

/** Renders every special RenderBlocks route through mapped or official classes. */
public final class B173NativeSpecialWorldRenderSmoke {
    private B173NativeSpecialWorldRenderSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 19) throw new IllegalArgumentException("expected render mapping");
        String role = arguments[0];
        Path clientJar = Paths.get(arguments[16]);
        Path catalog = Paths.get(arguments[17]);
        Path evidencePath = Paths.get(arguments[18]);
        List<NativeWorldBlockRenderSubject> subjects = subjects(catalog);
        int[] ids = subjects.stream().mapToInt(NativeWorldBlockRenderSubject::legacyId).toArray();
        int[] metadata = subjects.stream().mapToInt(
                NativeWorldBlockRenderSubject::metadata).toArray();
        List<B173WorldBlockFrame> frames = B173SpecialWorldRender.render(clientJar,
                arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                arguments[6], arguments[7], arguments[8], arguments[9], arguments[10],
                arguments[11], arguments[12], arguments[13], arguments[14], arguments[15],
                ids, metadata);
        List<NativeWorldBlockRenderObservation> observations = new ArrayList<>();
        for (int index = 0; index < subjects.size(); index++) {
            NativeWorldBlockRenderSubject subject = subjects.get(index);
            B173WorldBlockFrame frame = frames.get(index);
            require(subject.renderType() == frame.renderType(),
                    "catalog render type drift: " + subject.subject());
            observations.add(new NativeWorldBlockRenderObservation(subject.subject(),
                    frame.legacyId(), frame.metadata(), frame.renderType(), frame.geometryPixels(),
                    frame.frameSha256(), "draw-calls=1,pixels=" + frame.geometryPixels()));
        }
        NativeWorldBlockRenderEvidence evidence = NativeWorldBlockRenderFixture.verify(
                new NativeWorldBlockRenderPlan("native-special-world-render", subjects),
                observations);
        String canonical = evidence.canonical();
        Files.createDirectories(evidencePath.getParent());
        Files.write(evidencePath, canonical.getBytes(StandardCharsets.UTF_8));
        String signature = sha256(canonical.getBytes(StandardCharsets.UTF_8));
        String provenance = frames.get(0).provenance().replace('\\', '/');
        require(frames.stream().allMatch(frame -> frame.provenance().replace('\\', '/')
                .equals(provenance)), "mixed native renderer provenance");
        System.out.println("WORLDLINE_SPECIAL_WORLD_ROLE=" + role);
        System.out.println("WORLDLINE_SPECIAL_WORLD_SUBJECTS=" + subjects.size());
        System.out.println("WORLDLINE_SPECIAL_WORLD_SIGNATURE=" + signature);
        System.out.println("WORLDLINE_SPECIAL_WORLD_PROVENANCE=" + provenance);
    }

    private static List<NativeWorldBlockRenderSubject> subjects(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals(
                "subject_id\tlegacy_id\tname\tmetadata\trender_type"), "catalog header drift");
        List<NativeWorldBlockRenderSubject> rows = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String[] fields = lines.get(index).split("\\t", -1);
            require(fields.length == 5 && fields[2].matches("[a-z0-9-]+"),
                    "invalid catalog row " + index);
            rows.add(new NativeWorldBlockRenderSubject(fields[0], Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[3]), Integer.parseInt(fields[4])));
        }
        require(rows.size() == 30, "native special world-render census drift: " + rows.size());
        return rows;
    }

    private static String sha256(byte[] bytes) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes))
            value.append(String.format("%02x", item & 255));
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
